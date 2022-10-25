package com.qmk.musicmanager.controller

import com.qmk.musicmanager.data.PlaylistService
import com.qmk.musicmanager.model.Playlist
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RequestMapping("/playlists")
@RestController
class PlaylistController(val service: PlaylistService) {

    @GetMapping
    fun getPlaylists() = service.find()

    @GetMapping("/download")
    fun downloadPlaylists() {
        // TODO
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun postPlaylists(@RequestBody playlist: Playlist) {
        service.new(playlist)
    }

    @GetMapping("/{id}")
    fun getPlaylist(@PathVariable id: Int) {
        service.findById(id)
    }

    @PostMapping("/{id}")
    fun postPlaylist(@RequestBody playlist: Playlist) {
        service.save(playlist)
    }

    @DeleteMapping("/{id}")
    fun deletePlaylist(@PathVariable id: Int) {
        service.remove(id)
    }

    @GetMapping("/{id}/download")
    fun downloadPlaylist(@PathVariable id: String) {
        // TODO
    }
}