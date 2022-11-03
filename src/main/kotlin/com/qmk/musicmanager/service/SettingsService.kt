package com.qmk.musicmanager.service

import com.google.gson.Gson
import com.qmk.musicmanager.model.Settings
import org.springframework.stereotype.Service
import java.io.File

@Service
class SettingsService(
    private val configurationFile: File = File("./src/main/resources/configuration/configuration.json")
) {
    fun getConfiguration(): Settings {
        return Gson().fromJson(configurationFile.readText(Charsets.UTF_8), Settings::class.java)
    }

    fun setConfiguration(settings: Settings) {
        configurationFile.writeText(Gson().toJson(settings))
    }

    fun setMusicFolder(path: String) {
        setConfiguration(getConfiguration().copy(musicFolder = path))
    }

    fun setDownloadOccurrence(occurrence: Int) {
        setConfiguration(getConfiguration().copy(downloadOccurrence = occurrence))
    }
}