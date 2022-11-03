package com.qmk.musicmanager.controller

import com.qmk.musicmanager.manager.MusicManager
import com.qmk.musicmanager.service.MusicService
import com.qmk.musicmanager.model.Music
import org.springframework.web.bind.annotation.*

@RequestMapping("/music")
@RestController
class MusicController(private val service: MusicService) {
    val manager = MusicManager(service)

    @GetMapping("/new")
    fun getNewMusic() = service.findNew()

    @PostMapping("/{id}")
    fun postMusic(@PathVariable id: String, @RequestBody music: Music) {
        manager.editMusic(music)
    }
}