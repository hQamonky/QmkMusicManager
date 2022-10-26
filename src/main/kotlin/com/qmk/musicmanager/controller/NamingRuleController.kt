package com.qmk.musicmanager.controller

import com.qmk.musicmanager.data.NamingRuleService
import com.qmk.musicmanager.exception.ChannelNotFoundException
import com.qmk.musicmanager.exception.NamingRuleNotFoundException
import com.qmk.musicmanager.model.NamingRule
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RequestMapping("/naming-rules")
@RestController
class NamingRuleController(val service: NamingRuleService) {

    @GetMapping
    fun getNamingRules() = service.find()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun postNamingRules(@RequestBody namingRule: NamingRule) {
        service.new(namingRule)
    }

    @GetMapping("/{id}")
    fun getNamingRule(@PathVariable id: String): NamingRule {
        val namingRule = service.findById(id).firstOrNull { it.id == id }
        return namingRule ?: throw NamingRuleNotFoundException()
    }

    @PostMapping("/{id}")
    fun postNamingRule(
        @PathVariable id: String,
        @RequestBody namingRule: NamingRule
    ) {
        service.save(namingRule)
    }

    @DeleteMapping("/{id}")
    fun deleteNamingRule(@PathVariable id: String) {
        service.remove(id)
    }
}