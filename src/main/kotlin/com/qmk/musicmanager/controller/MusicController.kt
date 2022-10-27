package com.qmk.musicmanager.controller

import com.qmk.musicmanager.service.MusicService
import com.qmk.musicmanager.model.Music
import org.springframework.web.bind.annotation.*

@RequestMapping("/music")
@RestController
class MusicController(val service: MusicService) {

    @GetMapping("/new")
    fun getNewMusic() = service.findNew()

    @PostMapping("/{id}")
    fun postMusic(@PathVariable music: Music) {
        service.save(music)
    }
}