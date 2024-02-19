package com.qmk.musicmanager.domain.manager

import com.google.gson.Gson
import com.qmk.musicmanager.domain.extension.applyNamingRules
import com.qmk.musicmanager.domain.extension.toAuthorizedFileName
import com.qmk.musicmanager.domain.model.*
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Id3Manager {
    val gson = Gson()

    private fun getCommentsTag(comment: String?): CommentsTag? {
        return try {
            gson.fromJson(comment, CommentsTag::class.java)
        } catch (e: Exception) {
            println("Error parsing comment to json.")
            null
        } catch (e: NullPointerException) {
            println("Comment is null.")
            null
        }
    }

    fun getMetadataFromYoutube(
        videoInfo: MusicInfo,
        namingFormat: NamingFormat,
        namingRules: List<NamingRule>
    ): Metadata {
        val name = videoInfo.title.toAuthorizedFileName()
        val formattedTitle = videoInfo.title.applyNamingRules(namingRules)
        val splitTitle = formattedTitle.split(namingFormat.separator)
        val title = if (splitTitle.size >= 2) {
            if (namingFormat.artistBeforeTitle) splitTitle[1] else splitTitle[0]
        } else {
            formattedTitle
        }
        val artist = if (splitTitle.size >= 2) {
            if (namingFormat.artistBeforeTitle) splitTitle[0] else splitTitle[1]
        } else {
            videoInfo.channel
        }
        val album = videoInfo.channel
        return Metadata(
            name = name,
            title = title,
            artist = artist,
            genre = "",
            album = album,
            year = "",
            comments = CommentsTag(
                source = SourceTag(
                    id = videoInfo.id,
                    platform = "youtube",
                    uploaderId = videoInfo.channel_id,
                    uploader = videoInfo.channel,
                    uploadDate = videoInfo.upload_date
                ),
                playlists = listOf(),
                customTags = listOf(),
                downloadDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            )
        )
    }

    fun getMetadata(file: File): Metadata {
        val f = AudioFileIO.read(file)
        val tag: Tag = f.tag
        return Metadata(
            name = file.nameWithoutExtension,
            title = tag.getFirst(FieldKey.TITLE),
            artist = tag.getFirst(FieldKey.ARTIST),
            genre = tag.getFirst(FieldKey.GENRE),
            album = tag.getFirst(FieldKey.ALBUM),
            year = tag.getFirst(FieldKey.YEAR),
            comments = getCommentsTag(tag.getFirst(FieldKey.COMMENT))
        )
    }

    fun setMetadata(
        file: File,
        metadata: Metadata
    ) {
        // ID3 tags example here : http://www.jthink.net/jaudiotagger/examples_write.jsp
        val f = AudioFileIO.read(file)
        val tag: Tag = f.tag
        tag.setField(FieldKey.TITLE, metadata.title)
        tag.setField(FieldKey.ARTIST, metadata.artist)
        tag.setField(FieldKey.ALBUM, metadata.album)
        tag.setField(FieldKey.GENRE, metadata.genre)
//        tag.setField(FieldKey.YEAR, metadata.year) // Not working anymore for some reason. Maybe it's an Int and not a String ?
        tag.setField(FieldKey.COMMENT, metadata.comments?.toJson(gson))
        f.commit()
    }

    fun updateMetadata(
        file: File,
        title: String? = null,
        artist: String? = null,
        genre: String? = null,
        album: String? = null,
        year: String? = null,
        playlists: List<String>? = null,
        customTags: List<String>? = null
    ) {
        val currentMetadata = getMetadata(file)

        val f = AudioFileIO.read(file)
        val tag: Tag = f.tag

        if (title != null && currentMetadata.title != title) {
            tag.setField(FieldKey.TITLE, title)
        }
        if (artist != null && currentMetadata.artist != artist) {
            tag.setField(FieldKey.ARTIST, artist)
        }
        if (genre != null && currentMetadata.genre != genre) {
            tag.setField(FieldKey.GENRE, genre)
        }
        if (album != null && currentMetadata.album != album) {
            tag.setField(FieldKey.ALBUM, album)
        }
        if (year != null && currentMetadata.year != year) {
            tag.setField(FieldKey.YEAR, year)
        }
        if (playlists != null || customTags != null) {
            var comments = currentMetadata.comments
            if (playlists != null) {
                comments = comments?.copy(playlists = playlists) ?: CommentsTag(playlists = playlists)
            }
            if (customTags != null) {
                comments = comments?.copy(customTags = customTags) ?: CommentsTag(customTags = customTags)
            }
            tag.setField(FieldKey.COMMENT, comments?.toJson(gson))
        }
        f.commit()
    }

    fun addMusicToPlaylist(music: File, playlistName: String): Boolean {
        try {
            val playlists = getMetadata(file = music).comments?.playlists?.toMutableList() ?: mutableListOf()
            if (playlists.contains(playlistName)) return true
            playlists.add(playlistName)
            updateMetadata(file = music, playlists = playlists)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun removeMusicFromPlaylist(music: File, playlistName: String): Boolean {
        try {
            val playlists = getMetadata(file = music).comments?.playlists?.toMutableList() ?: mutableListOf()
            if (!playlists.contains(playlistName)) return true
            playlists.remove(playlistName)
            updateMetadata(file = music, playlists = playlists)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun renamePlaylistForMusic(oldName: String, newName: String, music: File): Boolean {
        try {
            val playlists = getMetadata(file = music).comments?.playlists?.toMutableList() ?: mutableListOf()
            if (!playlists.contains(oldName)) return false
            playlists.remove(oldName)
            playlists.add(newName)
            updateMetadata(file = music, playlists = playlists)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun addCustomTagToMusic(tag: String, music: File): Boolean {
        try {
            val customTags = getMetadata(file = music).comments?.customTags?.toMutableList() ?: mutableListOf()
            if (customTags.contains(tag)) return true
            customTags.add(tag)
            updateMetadata(file = music, customTags = customTags)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun removeCustomTagFromMusic(tag: String, music: File): Boolean {
        try {
            val customTags = getMetadata(file = music).comments?.customTags?.toMutableList() ?: mutableListOf()
            if (!customTags.contains(tag)) return true
            customTags.remove(tag)
            updateMetadata(file = music, customTags = customTags)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun addMetadataToExternalFiles(audioFolder: File): String? {
        val files = getAudioFiles(audioFolder) ?: return "Failed to get music folder."
        files.forEach { musicFile ->
            val metadata = getMetadata(musicFile)
            if (metadata.comments == null) {
                val f = AudioFileIO.read(musicFile)
                val tag: Tag = f.tag
                tag.setField(FieldKey.COMMENT,CommentsTag(
                    downloadDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                ).toJson(gson))
                f.commit()
            }
        }
        return null
    }

    fun addUntaggedFilesToPlaylists(audioFolder: File, playlists: List<String>): List<File>? {
        val files = getAudioFiles(audioFolder) ?: return null
        val externalFiles = mutableListOf<File>()
        files.forEach { musicFile ->
            val metadata = getMetadata(musicFile)
            if (metadata.comments == null) {
                val f = AudioFileIO.read(musicFile)
                val tag: Tag = f.tag
                tag.setField(FieldKey.COMMENT,CommentsTag(
                    playlists = playlists,
                    downloadDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                ).toJson(gson))
                f.commit()
                externalFiles.add(musicFile)
            }
        }
        return externalFiles
    }

    private fun getAudioFiles(audioFolder: File): List<File>? {
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
}