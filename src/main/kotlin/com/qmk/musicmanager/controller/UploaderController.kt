package com.qmk.musicmanager.controller

import com.qmk.musicmanager.service.UploaderService
import com.qmk.musicmanager.exception.UploaderNotFoundException
import com.qmk.musicmanager.model.NamingFormat
import com.qmk.musicmanager.model.Uploader
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RequestMapping("/uploaders")
@RestController
class UploaderController(val service: UploaderService) {

    @Operation(summary = "Get all uploaders.", description = "Returns 200 if successful")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful Operation"),
        ]
    )
    @GetMapping
    fun getUploaders() = service.find()

    @Operation(summary = "Get the specified uploader.", description = "Returns 200 if successful")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful Operation"),
        ]
    )
    @GetMapping("/{id}")
    fun getUploader(@PathVariable id: String): Uploader {
        val uploader = service.findById(id)
        return uploader ?: throw UploaderNotFoundException()
    }

    @Operation(summary = "Edit the naming format to use for the specified uploader.", description = "Returns 200 if successful")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful Operation"),
        ]
    )
    @PostMapping("/{id}")
    fun postUploader(
        @PathVariable id: String,
        @RequestBody namingFormat: NamingFormat
    ) {
        val uploader = service.findById(id)
        uploader?.copy(namingFormat = namingFormat)?.let { service.save(it) }
    }
}