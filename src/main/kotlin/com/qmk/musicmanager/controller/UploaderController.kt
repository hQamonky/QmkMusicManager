package com.qmk.musicmanager.controller

import com.qmk.musicmanager.service.UploaderService
import com.qmk.musicmanager.exception.UploaderNotFoundException
import com.qmk.musicmanager.model.NamingFormat
import com.qmk.musicmanager.model.Uploader
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RequestMapping("/uploaders")
@RestController
class UploaderController(val service: UploaderService) {

    @GetMapping
    fun getUploaders() = service.find()

    @GetMapping("/{id}")
    fun getUploader(@PathVariable id: String): Uploader {
        val uploader = service.findById(id)
        return uploader ?: throw UploaderNotFoundException()
    }

    @PostMapping("/{id}")
    fun postUploader(
        @PathVariable id: String,
        @RequestBody namingFormat: NamingFormat
    ) {
        val uploader = service.findById(id)
        uploader?.copy(namingFormat = namingFormat)?.let { service.save(it) }
    }
}