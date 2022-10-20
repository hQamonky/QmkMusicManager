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


@RequestMapping("/naming-rule")
@RestController
class MainController {
    private var namingRules = mutableListOf(
        NamingRule(1, " / ", " ", 1),
        NamingRule(2, " ‒ ", " - ", 1),
        NamingRule(3, " [NCS Release]", "", 2),
        NamingRule(4, " [Monstercat Release]", "", 2),
        NamingRule(5, " [Diversity Release]", "", 2),
    )

    @GetMapping("/homepage")
    fun getHomePage() = "Music Manager"

    @GetMapping("")
    fun getNamingRules() = namingRules

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    fun postNamingRule(@RequestBody namingRule: NamingRule): NamingRule {
        val maxId = namingRules.maxOfOrNull { it.id } ?: 0
        val nextId = maxId + 1
        val newRule = NamingRule(
            id = nextId,
            replace = namingRule.replace,
            replaceBy = namingRule.replaceBy,
            priority = namingRule.priority
        ) // 6
        namingRules.add(newRule)
        return newRule
    }

    @GetMapping("/{id}")
    fun getNamingRuleById(@PathVariable id: Int) : NamingRule? {
        val namingRule = namingRules.firstOrNull { it.id == id }
        return namingRule ?: throw NamingRuleNotFoundException()
    }

}