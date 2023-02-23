package com.qmk.musicmanager.controller

import com.qmk.musicmanager.manager.DataManager
import com.qmk.musicmanager.manager.YoutubeManager
import com.qmk.musicmanager.service.DataService
import com.qmk.musicmanager.service.MusicService
import com.qmk.musicmanager.service.NamingRuleService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.*

@RestController
class MainController(
    private val dataService: DataService,
    musicService: MusicService,
    namingRuleService: NamingRuleService
) {
    val dataManager = DataManager(musicService, namingRuleService)
    private val youtubeManager = YoutubeManager()

    @Operation(summary = "Reset data to factory.", description = "Returns 200 if successful")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful Operation"),
        ]
    )
    @PostMapping("/factory-reset")
    fun factoryReset(): String {
        dataService.emptyDatabase()
        dataManager.addDefaultNamingRules()
        dataManager.addFilesToDatabase()
        return "Database reset."
    }

    @Operation(summary = "Update youtube-dl on the server.", description = "Returns the logs from the command line.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Logs"),
        ]
    )
    @PostMapping("/youtube-dl/update")
    fun updateYoutubeDl(): String {
        return youtubeManager.updateYoutubeDl() ?: "null"
    }
}