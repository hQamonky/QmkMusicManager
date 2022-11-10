package com.qmk.musicmanager.service

import com.google.gson.Gson
import com.qmk.musicmanager.manager.ConfigurationManager
import com.qmk.musicmanager.model.Settings
import org.springframework.stereotype.Service
import java.io.File

@Service
class SettingsService(
    private val manager: ConfigurationManager = ConfigurationManager()
) {
    fun getConfiguration(): Settings {
        return manager.getConfiguration()
    }

    fun setConfiguration(settings: Settings) {
        manager.setConfiguration(settings)
    }

    fun setMusicFolder(path: String) {
       manager.setMusicFolder(path)
    }

    fun setDownloadOccurrence(occurrence: Int) {
        manager.setDownloadOccurrence(occurrence)
    }

    fun setAutoDownload(autoDownload: Boolean) {
        manager.setAutoDownload(autoDownload)
    }
}