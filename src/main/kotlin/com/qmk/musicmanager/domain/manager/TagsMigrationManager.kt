package com.qmk.musicmanager.domain.manager

import com.google.gson.Gson
import com.qmk.musicmanager.domain.model.CommentsTag
import com.qmk.musicmanager.domain.model.Music
import com.qmk.musicmanager.domain.model.SourceTag
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag
import java.io.File
import javax.activation.MimetypesFileTypeMap

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

    private fun isMetadataIsOld(tag: Tag): Boolean {
        return try {
            Gson().fromJson(tag.getFirst(FieldKey.COMMENT), CommentsTag::class.java)
            false
        } catch (e: Exception) {
            println("Error parsing comments to json. Metadata is old format.")
            true
        } catch (e: NullPointerException) {
            println("Comments tag is null. Metadata is missing.")
            true
        }
    }

    private fun getNewCommentTagFromOldFile(file: File): CommentsTag {
        val oldMetadata = getOldMetadata(file)
        return if (oldMetadata.comment == null) {
            CommentsTag(
                source = null,
                playlists = listOf(),
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
            title = "", artist = "", id = "", uploaderId = "", uploadDate = "", isNew = false
        )
        val mopidyManager = MopidyManager(configurationManager)
        val playlistDir = "${configurationManager.getConfiguration().musicFolder}/Playlists/Mopidy"

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

    fun convertAllFilesMetadata(): String? {
        val musicFolder = File(configurationManager.getConfiguration().musicFolder)
        if (!musicFolder.isDirectory) return "Failed to get music folder."
        musicFolder.walk().forEach lit1@{ musicFile ->
            val fileType = MimetypesFileTypeMap().getContentType(musicFile)
            if (!fileType.contains("audio")) return@lit1
            convertFileMetadata(musicFile)
        }
        return null
    }
}