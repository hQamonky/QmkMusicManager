package com.qmk.musicmanager.domain.manager

import com.google.gson.Gson
import com.qmk.musicmanager.domain.model.CommentsTag
import com.qmk.musicmanager.domain.model.Music
import com.qmk.musicmanager.domain.model.SourceTag
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class TagsMigrationManager(private val configurationManager: ConfigurationManager = ConfigurationManager()) {
    data class OldMetadata(
        val name: String,
        val title: String,
        val artist: String,
        val album: String,
        val year: String,
        val comment: OldCommentTag?
    )

    data class OldCommentTag(
        val platform: String, val id: String
    )

    private fun getOldMetadata(file: File): OldMetadata {
        val f = AudioFileIO.read(file)
        val tag: Tag = f.tag
        return OldMetadata(
            name = "",
            title = tag.getFirst(FieldKey.TITLE),
            artist = tag.getFirst(FieldKey.ARTIST),
            album = tag.getFirst(FieldKey.ALBUM),
            year = tag.getFirst(FieldKey.YEAR),
            comment = getOldCommentTag(tag)
        )
    }

    private fun getOldCommentTag(tag: Tag): OldCommentTag? {
        return try {
            Gson().fromJson(tag.getFirst(FieldKey.COMMENT), OldCommentTag::class.java)
        } catch (e: Exception) {
            println("Error parsing comment to json.")
            null
        } catch (e: NullPointerException) {
            println("Comment is null.")
            null
        }
    }

    fun isMetadataIsOld(tag: Tag): Boolean {
        return try {
            val gson = Gson()
            val tagKey = tag.getFirst(FieldKey.COMMENT)
            val comment = gson.fromJson(tagKey, CommentsTag::class.java)
            val isNull = comment == null
            if (isNull) {
                true
            } else {
                val isNewTag = comment == CommentsTag()
                isNewTag
            }
        } catch (e: Exception) {
            println("Error parsing comments to json. Metadata is old format.")
            true
        } catch (e: NullPointerException) {
            println("Comments tag is null. Metadata is missing.")
            true
        }
    }

    fun getNewCommentTagFromOldFile(file: File): CommentsTag {
        val oldMetadata = getOldMetadata(file)
        return if (oldMetadata.comment == null) {
            CommentsTag(
                source = null,
                playlists = getPlaylistsOfMusic(file),
                customTags = listOf(),
                downloadDate = ""
            )
        } else {
            CommentsTag(
                source = SourceTag(
                    id = oldMetadata.comment.id,
                    platform = oldMetadata.comment.platform,
                    uploaderId = "",
                    uploader = oldMetadata.album,
                    uploadDate = oldMetadata.year
                ), playlists = getPlaylistsOfMusic(file), customTags = listOf(), downloadDate = ""
            )
        }
    }

    private fun getPlaylistsOfMusic(file: File): List<String> {
        val music = Music(
            fileName = file.nameWithoutExtension,
            fileExtension = file.extension,
            title = "", artist = "", platformId = "", uploaderId = "", uploadDate = "", isNew = false
        )
        val mopidyManager = MopidyManager(configurationManager)
        val playlistDir = "${configurationManager.getConfiguration().playlistsFolder}/Mopidy"

        val list = mutableListOf<String>()

        File(playlistDir).walk().forEach lit1@{ playlist ->
            if (playlist.isDirectory || playlist.extension != "m3u8") return@lit1
            val playlistName = playlist.nameWithoutExtension
            if (mopidyManager.isMusicInPlaylist(music, playlistName)) {
                list.add(playlistName)
            }
        }

        return list
    }

    private fun convertFileMetadata(file: File) {
        val f = AudioFileIO.read(file)
        val tag: Tag = f.tag
        if (!isMetadataIsOld(tag)) return
        val comments = getNewCommentTagFromOldFile(file)
        tag.setField(FieldKey.COMMENT, comments.toJson())
        f.commit()
    }

    fun getAudioFiles(): List<File>? {
        val audioFolder = File(configurationManager.getConfiguration().audioFolder)
        if (!audioFolder.isDirectory) return null
        val files = mutableListOf<File>()
        audioFolder.walk().forEach lit1@{ musicFile ->
            val fileType = getFileType(musicFile.toString())
            if (!fileType.contains("audio")) return@lit1
            files.add(musicFile)
        }
        return files
    }

    private fun getFileType(filePath: String): String {
        val path: Path = Paths.get(filePath)
        return try {
            val contentType = Files.probeContentType(path)
            contentType ?: "Unknown"
        } catch (e: Exception) {
            // Handle exceptions as needed
            e.printStackTrace()
            "Unknown"
        }
    }

    fun convertAllFilesMetadata(): String? {
        val files = getAudioFiles() ?: return "Failed to get music folder."
        files.forEach { musicFile ->
            convertFileMetadata(musicFile)
        }
        return null
    }

    suspend fun addInfoFromDeezer(file: File) {
        val deezerManager = DeezerManager()
        val id3Manager = Id3Manager()
        val currentMetadata = id3Manager.getMetadata(file)
        if (currentMetadata.comments?.source == null) return
        val deezerMetadata = deezerManager.findFullMetadata(currentMetadata.title, currentMetadata.artist, file)
        val finalMetadata = deezerMetadata?.let {
            currentMetadata.copy(
                title = it.title,
                artist = it.artist,
                genre = it.genre,
                album = it.album,
                year = it.releaseDate
            )
        } ?: return
        id3Manager.setMetadata(file, finalMetadata)
    }

    suspend fun addAllFilesDeezerMetadata(): String? {
        val files = getAudioFiles() ?: return "Failed to get music folder."
        files.forEach { musicFile ->
            addInfoFromDeezer(musicFile)
        }
        return null
    }

    private suspend fun addInfoFromDeezerForMissingArtist(file: File) {
        val deezerManager = DeezerManager()
        val id3Manager = Id3Manager()
        val currentMetadata = id3Manager.getMetadata(file)
        if (currentMetadata.comments?.source == null) return
        if (currentMetadata.genre != "") return
        if (currentMetadata.artist != currentMetadata.album) return
        val deezerMetadata = deezerManager.findFullMetadata(currentMetadata.title)
        val finalMetadata = deezerMetadata?.let {
            currentMetadata.copy(
                title = it.title,
                artist = it.artist,
                genre = it.genre,
                album = it.album,
                year = it.releaseDate
            )
        } ?: return
        println("Set tag of ${file.nameWithoutExtension} : title = ${finalMetadata.title}, artist = ${finalMetadata.artist}")
        id3Manager.setMetadata(file, finalMetadata)
    }

    suspend fun addAllFilesDeezerMetadataForMissingArtist(): String? {
        val files = getAudioFiles() ?: return "Failed to get music folder."
        files.forEach { musicFile ->
            addInfoFromDeezerForMissingArtist(musicFile)
        }
        return null
    }
}