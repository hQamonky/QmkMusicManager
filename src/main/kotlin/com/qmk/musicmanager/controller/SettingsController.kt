package com.qmk.musicmanager.controller

import com.qmk.musicmanager.model.NamingFormat
import com.qmk.musicmanager.model.Settings
import com.qmk.musicmanager.service.SettingsService
import org.springframework.web.bind.annotation.*

@RequestMapping("/settings")
@RestController
class SettingsController(private val service: SettingsService) {
    @GetMapping
    fun getSettings(): Settings {
        return service.getConfiguration()
    }

    @PostMapping
    fun postSettings(@RequestBody settings: Settings) {
        service.setConfiguration(settings)
    }

    @PostMapping("/music-folder")
    fun postMusicFolder(@RequestBody path: String) {
        service.setMusicFolder(path)
    }

    @PostMapping("/download-occurrence")
    fun postDownloadOccurrence(@RequestBody occurrence: Int) {
        service.setDownloadOccurrence(occurrence)
    }

    @PostMapping("/auto-download")
    fun postAutoDownload(@RequestBody autoDownload: Boolean) {
        service.setAutoDownload(autoDownload)
    }
}