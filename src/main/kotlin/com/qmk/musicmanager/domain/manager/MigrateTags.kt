package com.qmk.musicmanager.domain.manager

import com.google.gson.Gson
import com.qmk.musicmanager.domain.model.CommentsTag
import com.qmk.musicmanager.domain.model.SourceTag
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag
import java.io.File

class MigrateTags {
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
            println("Error parsing comments to json.")
            true
        } catch (e: NullPointerException) {
            println("Comments is null.")
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

    private fun getPlaylistsOfMusic(file: File) : List<String> {
        return listOf() // TODO
    }

    fun convertFileMetadata(file: File) {
        val f = AudioFileIO.read(file)
        val tag: Tag = f.tag
        if (!isMetadataIsOld(tag)) return
        val comments = getNewCommentTagFromOldFile(file)
        tag.setField(FieldKey.COMMENT, comments.toJson())
        f.commit()
    }
}