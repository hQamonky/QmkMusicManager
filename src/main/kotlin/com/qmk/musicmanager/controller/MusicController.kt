package com.qmk.musicmanager.controller

import com.qmk.musicmanager.model.Music
import org.springframework.web.bind.annotation.*

@RequestMapping("/music")
@RestController
class MusicController {
    private var musicList = emptyList<Music>()

    @GetMapping("/new")
    fun getNewMusic() = musicList

    @PostMapping("/{id}")
    fun postMusic(@PathVariable id: String) {

    }
}