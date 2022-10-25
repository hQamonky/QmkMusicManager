package com.qmk.musicmanager.controller

import com.qmk.musicmanager.exception.NamingRuleNotFoundException
import com.qmk.musicmanager.model.NamingRule
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*


/**
 * Tutorials research
 * - Create a database : https://kotlinlang.org/docs/jvm-spring-boot-restful.html#before-you-start
 * - Get and set ID3 tags : https://stackoverflow.com/questions/9707572/how-to-get-and-set-change-id3-tag-metadata-of-audio-files
 * - Run a terminal command : https://stackoverflow.com/questions/35421699/how-to-invoke-external-command-from-within-kotlin-code
 * - Run on raspberry pi : https://pete32.medium.com/kotlin-or-java-on-a-raspberry-pi-de092d318df9
 *      https://javalin.io/2020/09/05/javalin-raspberry-pi-example.html
 *
 */


@RestController
class MainController {

    @GetMapping("/factory-reset")
    fun factoryReset() {

    }

    @GetMapping("/youtube-dl/update")
    fun updateYoutubeDl() {

    }
}