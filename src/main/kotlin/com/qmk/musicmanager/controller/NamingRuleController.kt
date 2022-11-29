package com.qmk.musicmanager.controller

import com.qmk.musicmanager.service.NamingRuleService
import com.qmk.musicmanager.exception.NamingRuleNotFoundException
import com.qmk.musicmanager.model.NamingRule
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RequestMapping("/naming-rules")
@RestController
class NamingRuleController(val service: NamingRuleService) {

    @Operation(summary = "Get all naming rules.", description = "Returns 200 if successful")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful Operation"),
        ]
    )
    @GetMapping
    fun getNamingRules() = service.find()

    @Operation(summary = "Create a new naming rule.", description = "Returns 201 if successful")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Successfully Created"),
        ]
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun postNamingRules(@RequestBody namingRule: NamingRule) {
        service.new(namingRule)
    }

    @Operation(summary = "Get the specified naming rule.", description = "Returns 200 if successful")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful Operation"),
        ]
    )
    @GetMapping("/{id}")
    fun getNamingRule(@PathVariable id: String): NamingRule {
        val namingRule = service.findById(id).firstOrNull { it.id == id }
        return namingRule ?: throw NamingRuleNotFoundException()
    }

    @Operation(summary = "Edit the specified naming rule.", description = "Returns 200 if successful")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful Operation"),
        ]
    )
    @PostMapping("/{id}")
    fun postNamingRule(
        @PathVariable id: String,
        @RequestBody namingRule: NamingRule
    ) {
        service.save(namingRule)
    }

    @Operation(summary = "Delete the specified naming rule.", description = "Returns 200 if successful")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful Operation"),
        ]
    )
    @DeleteMapping("/{id}")
    fun deleteNamingRule(@PathVariable id: String) {
        service.remove(id)
    }
}