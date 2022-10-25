package com.qmk.musicmanager.controller

import com.qmk.musicmanager.model.NamingFormat
import com.qmk.musicmanager.model.Settings
import org.springframework.web.bind.annotation.*

@RequestMapping("/settings")
@RestController
class SettingsController {
//    private var settings: Settings = Settings()


    @GetMapping
    fun getSettings() {

    }

    @PostMapping("/naming-format")
    fun postNamingSeparator(@PathVariable namingFormat: NamingFormat) {

    }

    @PostMapping("/download-occurrence")
    fun postDownloadOccurrence(@PathVariable occurrence: Int) {

    }
}