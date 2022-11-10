package com.qmk.musicmanager.controller

import com.qmk.musicmanager.manager.DataManager
import com.qmk.musicmanager.manager.YoutubeManager
import com.qmk.musicmanager.service.DataService
import com.qmk.musicmanager.service.MusicService
import com.qmk.musicmanager.service.NamingRuleService
import org.springframework.web.bind.annotation.*


/**
 * Tutorials research
 * - Run on raspberry pi : https://pete32.medium.com/kotlin-or-java-on-a-raspberry-pi-de092d318df9
 *      https://javalin.io/2020/09/05/javalin-raspberry-pi-example.html
 *
 */


@RestController
class MainController(
    private val dataService: DataService,
    musicService: MusicService,
    namingRuleService: NamingRuleService
) {
    val dataManager = DataManager(musicService, namingRuleService)
    private val youtubeManager = YoutubeManager()

    @GetMapping("/factory-reset")
    fun factoryReset(): String {
        dataService.emptyDatabase()
        dataManager.addDefaultNamingRules()
        dataManager.addFilesToDatabase()
        return "Database reset."
    }

    @GetMapping("/youtube-dl/update")
    fun updateYoutubeDl(): String {
        return youtubeManager.update() ?: "null"
    }
}