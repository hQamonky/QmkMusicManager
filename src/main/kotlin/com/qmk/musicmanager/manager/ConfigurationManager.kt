package com.qmk.musicmanager.manager

import com.google.gson.Gson
import com.qmk.musicmanager.model.Settings
import java.io.File

class ConfigurationManager(
    private val configurationFile: File = File("./data/configuration.json"),
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

    fun setMusicFolder(path: String) {
        setConfiguration(getConfiguration().copy(musicFolder = path))
    }

    fun setDownloadOccurrence(occurrence: Int) {
        setConfiguration(getConfiguration().copy(downloadOccurrence = occurrence))
    }

    fun setAutoDownload(autoDownload: Boolean) {
        setConfiguration(getConfiguration().copy(autoDownload = autoDownload))
    }
}