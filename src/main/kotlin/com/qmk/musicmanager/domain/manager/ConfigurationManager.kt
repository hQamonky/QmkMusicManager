package com.qmk.musicmanager.domain.manager

import com.google.gson.Gson
import com.qmk.musicmanager.domain.model.Settings
import java.io.File

class ConfigurationManager(
    private val configurationFile: File = File("./data/configuration.json"),
    private val accoustidApiKey: File = File("./data/accoustidapikey"),
    configuration: Settings = Settings()
) {

    init {
        if (configurationFile.createNewFile()) {
            configurationFile.writeText(configuration.toJson())
        }
    }

    fun getConfiguration(): Settings {
        return Gson().fromJson(configurationFile.readText(Charsets.UTF_8), Settings::class.java)
    }

    fun setConfiguration(settings: Settings) {
        configurationFile.writeText(settings.toJson())
    }

    fun setAudioFolder(path: String) {
        setConfiguration(getConfiguration().copy(audioFolder = path))
    }

    fun setPlaylistsFolder(path: String) {
        setConfiguration(getConfiguration().copy(playlistsFolder = path))
    }

    fun setArchiveFolder(path: String) {
        setConfiguration(getConfiguration().copy(archiveFolder = path))
    }

    fun setAudioFormat(format: String) {
        setConfiguration(getConfiguration().copy(audioFormat = format))
    }

    fun setDownloadOccurrence(occurrence: Int) {
        setConfiguration(getConfiguration().copy(downloadOccurrence = occurrence))
    }

    fun setAutoDownload(autoDownload: Boolean) {
        setConfiguration(getConfiguration().copy(autoDownload = autoDownload))
    }

    fun setRapidapiKey(key: String) {
        setConfiguration(getConfiguration().copy(rapidapiKey = key))
    }

    fun setAccoustidApiKey(key: String) {
        if (!accoustidApiKey.exists()) accoustidApiKey.createNewFile()
        accoustidApiKey.writeText(text = key)
    }

    fun getAccoustidApiKey(): String? {
        if (!accoustidApiKey.exists()) return null
        return accoustidApiKey.readText(Charsets.UTF_8)
    }
}