package com.qmk.musicmanager.domain.manager

import com.qmk.musicmanager.database.dao.MusicDAOImpl
import com.qmk.musicmanager.domain.model.Music
import java.io.File


class MusicManager(
    private val musicDAO: MusicDAOImpl,
    private val configurationManager: ConfigurationManager = ConfigurationManager(),
    private val id3Manager: Id3Manager = Id3Manager()
) {
    suspend fun editMusic(music: Music) : Boolean {
        val musicFolder = configurationManager.getConfiguration().musicFolder
        val file = File("${musicFolder}/${music.fileName}.${music.fileExtension}")
        id3Manager.updateMetadata(
            file = file,
            title = music.title,
            artist = music.artist,
            playlists = music.playlists,
            customTags = music.tags
        )
        return musicDAO.editMusic(
            music.id,
            music.fileName,
            music.fileExtension,
            music.title,
            music.artist,
            music.uploaderId,
            music.uploadDate,
            music.isNew
        )
    }

    suspend fun getNewMusic() : List<Music> {
        return musicDAO.newMusic()
    }
}