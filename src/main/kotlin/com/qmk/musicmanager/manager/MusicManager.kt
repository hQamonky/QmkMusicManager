package com.qmk.musicmanager.manager

import com.qmk.musicmanager.model.Music
import com.qmk.musicmanager.service.MusicService
import java.io.File


class MusicManager(private val service: MusicService,
                   private val configurationManager: ConfigurationManager = ConfigurationManager(),
                   private val id3Manager: Id3Manager = Id3Manager()
) {
    fun editMusic(music: Music) {
        val musicFolder = configurationManager.getConfiguration().musicFolder
        val file = File("${musicFolder}/${music.name}.mp3")
        id3Manager.updateMetadata(
            file,
            music.title,
            music.artist
        )
        service.save(music)
    }
}