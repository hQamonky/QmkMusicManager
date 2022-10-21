package com.qmk.musicmanager.controller

import com.qmk.musicmanager.model.Playlist
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RequestMapping("/playlists")
@RestController
class PlaylistController {
    private var playlists = emptyList<Playlist>()

    @GetMapping
    fun getPlaylists() = playlists

    @GetMapping("/download")
    fun downloadPlaylists() {

    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun postPlaylists(@RequestBody playlist: Playlist) {

    }

    @GetMapping("/{id}")
    fun getPlaylist(@PathVariable id: String) {

    }

    @PostMapping("/{id}")
    fun postPlaylist(@RequestBody playlist: Playlist) {

    }

    @DeleteMapping("/{id}")
    fun deletePlaylist(@PathVariable id: String) {

    }

    @GetMapping("/{id}/download")
    fun downloadPlaylist(@PathVariable id: String) {

    }
}