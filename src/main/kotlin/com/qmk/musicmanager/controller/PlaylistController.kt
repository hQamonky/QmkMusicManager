package com.qmk.musicmanager.controller

import com.qmk.musicmanager.service.PlaylistService
import com.qmk.musicmanager.exception.PlaylistNotFoundException
import com.qmk.musicmanager.manager.PlaylistManager
import com.qmk.musicmanager.model.Playlist
import com.qmk.musicmanager.model.PlaylistEntry
import com.qmk.musicmanager.service.MusicService
import com.qmk.musicmanager.service.NamingRuleService
import com.qmk.musicmanager.service.UploaderService
import com.qmk.musicmanager.youtube.YoutubeController
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.io.File

@RequestMapping("/playlists")
@RestController
class PlaylistController(
    private val service: PlaylistService,
    musicService: MusicService,
    uploaderService: UploaderService,
    namingRuleService: NamingRuleService
) {
    private final val youtubeController = YoutubeController()
    val manager = PlaylistManager(
        service,
        musicService,
        uploaderService,
        namingRuleService,
        youtubeController
    )

    @GetMapping
    fun getPlaylists() = service.find()

    @GetMapping("/download")
    fun downloadPlaylists(): String {
        return manager.downloadPlaylists()
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
        manager.downloadPlaylist(id)
    }
}