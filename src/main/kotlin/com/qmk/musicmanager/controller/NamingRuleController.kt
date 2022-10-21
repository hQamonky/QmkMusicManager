package com.qmk.musicmanager.controller

import com.qmk.musicmanager.model.NamingRule
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RequestMapping("/naming-rules")
@RestController
class NamingRuleController {
    private var namingRules = emptyList<NamingRule>()

    @GetMapping
    fun getNamingRules() = namingRules

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun postNamingRules(@RequestBody namingRule: NamingRule) {

    }

    @GetMapping("/{id}")
    fun getNamingRule(@PathVariable id: String) {

    }

    @PostMapping("/{id}")
    fun postNamingRule(
        @PathVariable id: String,
        @RequestBody namingRule: NamingRule
    ) {

    }

    @DeleteMapping("/{id}")
    fun deleteNamingRule(@PathVariable id: String) {

    }
}