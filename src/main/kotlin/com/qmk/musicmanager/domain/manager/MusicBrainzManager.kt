package com.qmk.musicmanager.domain.manager

import com.qmk.musicmanager.api.MusicBrainzAPI

class MusicBrainzManager(
    val configurationManager: ConfigurationManager = ConfigurationManager(),
    val musicBrainzAPI: MusicBrainzAPI = MusicBrainzAPI()
) {
    fun search() {

    }
}