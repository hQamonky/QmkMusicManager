package com.qmk.musicmanager.controller

import com.qmk.musicmanager.service.UploaderService
import com.qmk.musicmanager.exception.UploaderNotFoundException
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
        @RequestBody uploader: Uploader
    ) {
        service.save(uploader)
    }
}