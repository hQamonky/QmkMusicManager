package com.qmk.musicmanager.manager

import com.qmk.musicmanager.extension.applyNamingRules
import com.qmk.musicmanager.model.Metadata
import com.qmk.musicmanager.model.MusicInfo
import com.qmk.musicmanager.model.NamingFormat
import com.qmk.musicmanager.model.NamingRule
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag
import java.io.File

class Id3Manager {
    fun getMetadata(videoInfo: MusicInfo, namingFormat: NamingFormat, namingRules: List<NamingRule>): Metadata {
        val name = videoInfo.title.applyNamingRules(namingRules)
        val splitTitle = name.split(namingFormat.separator)
        val title = if (splitTitle.size >= 2) {
            if (namingFormat.artist_before_title) splitTitle[1] else splitTitle[0]
        } else {
            name
        }
        val artist = if (splitTitle.size >= 2) {
            if (namingFormat.artist_before_title) splitTitle[0] else splitTitle[1]
        } else {
            videoInfo.channel
        }
        val album = videoInfo.channel
        val year = videoInfo.upload_date.take(4)
        val comment = "{\"platform\": \"youtube\", \"id\": \"${videoInfo.id}\"}"
        return Metadata(name, title, artist, album, year, comment)
    }

    fun getMetadata(file: File): Metadata {
        val f = AudioFileIO.read(file)
        val tag: Tag = f.tag
        return Metadata(
            name = "",
            title = tag.getFirst(FieldKey.TITLE),
            artist = tag.getFirst(FieldKey.ARTIST),
            album = tag.getFirst(FieldKey.ALBUM),
            year = tag.getFirst(FieldKey.YEAR),
            comment = tag.getFirst(FieldKey.COMMENT)
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
        tag.setField(FieldKey.YEAR, metadata.year)
        tag.setField(FieldKey.COMMENT, metadata.comment)
        f.commit()
    }

    fun updateMetadata(
        file: File,
        title: String? = null,
        artist: String? = null
    ) {
        val f = AudioFileIO.read(file)
        val tag: Tag = f.tag

        val currentMetadata = Metadata(
            name = "",
            title = tag.getFirst(FieldKey.TITLE),
            artist = tag.getFirst(FieldKey.ARTIST),
            album = tag.getFirst(FieldKey.ALBUM),
            year = tag.getFirst(FieldKey.YEAR),
            comment = tag.getFirst(FieldKey.COMMENT)
        )

        if (title != null && currentMetadata.title != title) {
            tag.setField(FieldKey.TITLE, title)
        }
        if (artist != null && currentMetadata.artist != artist) {
            tag.setField(FieldKey.ARTIST, artist)
        }
        f.commit()
    }
}