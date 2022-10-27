package com.qmk.musicmanager.controller

import com.qmk.musicmanager.service.PlaylistService
import com.qmk.musicmanager.exception.PlaylistNotFoundException
import com.qmk.musicmanager.manager.PlaylistManager
import com.qmk.musicmanager.model.Playlist
import com.qmk.musicmanager.model.PlaylistEntry
import com.qmk.musicmanager.service.UploaderService
import com.qmk.musicmanager.youtube.YoutubeController
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RequestMapping("/playlists")
@RestController
class PlaylistController(private val service: PlaylistService, uploaderService: UploaderService) {
    private final val youtubeController = YoutubeController()
    val manager = PlaylistManager(service, uploaderService, youtubeController)

    @GetMapping
    fun getPlaylists() = service.find()

    @GetMapping("/download")
    fun downloadPlaylists(): String {
        // TODO
        val playlists = service.find()
        var result = ""
        playlists.forEach { playlist ->
            println("Starting download of playlist : ${playlist.id}")
            result = youtubeController.downloadPlaylist(playlist) ?: "null"
            println(result)
        }
        return result
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun postPlaylists(@RequestBody entry: PlaylistEntry): Playlist? {
        return manager.createPlaylist(entry.name, entry.url)
    }

    @GetMapping("/{id}")
    fun getPlaylist(@PathVariable id: String): Playlist {
        val playlist = service.findById(id).firstOrNull { it.id == id }
        return playlist ?: throw PlaylistNotFoundException()
    }

    @PostMapping("/{id}")
    fun postPlaylist(@RequestBody playlist: Playlist) {
        service.save(playlist)
    }

    @DeleteMapping("/{id}")
    fun deletePlaylist(@PathVariable id: String) {
        service.remove(id)
    }

    @GetMapping("/{id}/download")
    fun downloadPlaylist(@PathVariable id: String) {
        // TODO
    }
}