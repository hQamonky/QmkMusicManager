package com.qmk.musicmanager.controller

import com.qmk.musicmanager.model.NamingFormat
import com.qmk.musicmanager.model.Settings
import com.qmk.musicmanager.service.SettingsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.*

@RequestMapping("/settings")
@RestController
class SettingsController(private val service: SettingsService) {
    @Operation(summary = "Get settings.", description = "Returns 200 if successful")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful Operation"),
        ]
    )
    @GetMapping
    fun getSettings(): Settings {
        return service.getConfiguration()
    }

    @Operation(summary = "Set settings configuration.", description = "Returns 200 if successful")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful Operation"),
        ]
    )
    @PostMapping
    fun postSettings(@RequestBody settings: Settings) {
        service.setConfiguration(settings)
    }

    @Operation(summary = "Set the music folder.", description = "Returns 200 if successful")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful Operation"),
        ]
    )
    @PostMapping("/music-folder")
    fun postMusicFolder(@RequestBody path: String) {
        service.setMusicFolder(path)
    }

    @Operation(summary = "Set the download occurrence.", description = "Value in hours.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful Operation"),
        ]
    )
    @PostMapping("/download-occurrence")
    fun postDownloadOccurrence(@RequestBody occurrence: Int) {
        service.setDownloadOccurrence(occurrence)
    }

    @Operation(summary = "Set auto-download.", description = "Returns 200 if successful")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful Operation"),
        ]
    )
    @PostMapping("/auto-download")
    fun postAutoDownload(@RequestBody autoDownload: Boolean) {
        service.setAutoDownload(autoDownload)
    }
}