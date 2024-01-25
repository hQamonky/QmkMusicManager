package com.qmk.musicmanager.domain.manager

import com.qmk.musicmanager.domain.model.Metadata
import com.qmk.musicmanager.api.ShazamAPI
import java.io.File

class RapidapiManager(
    configurationManager: ConfigurationManager = ConfigurationManager(),
    private val id3Manager: Id3Manager = Id3Manager()
) {
    private lateinit var shazamAPI: ShazamAPI

    init {
        val rapidapiKey = configurationManager.getConfiguration().rapidapiKey
        if (rapidapiKey.isNotEmpty() && rapidapiKey.isNotBlank()) {
            shazamAPI = ShazamAPI(rapidapiKey)
        }
    }

    fun getFullMetadata(file: File): Metadata? {
        val currentMetadata = id3Manager.getMetadata(file)
        if (!this::shazamAPI.isInitialized) {
            return null
        }
        shazamAPI.search("${currentMetadata.title} ${currentMetadata.artist}")
        return null // TODO : handle shazam api result and return Metadata
    }
}