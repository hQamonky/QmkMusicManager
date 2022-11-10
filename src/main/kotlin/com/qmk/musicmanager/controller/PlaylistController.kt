package com.qmk.musicmanager.controller

import com.qmk.musicmanager.service.PlaylistService
import com.qmk.musicmanager.exception.PlaylistNotFoundException
import com.qmk.musicmanager.manager.PlaylistManager
import com.qmk.musicmanager.model.Playlist
import com.qmk.musicmanager.model.PlaylistEntry
import com.qmk.musicmanager.service.MusicService
import com.qmk.musicmanager.service.NamingRuleService
import com.qmk.musicmanager.service.UploaderService
import com.qmk.musicmanager.manager.YoutubeManager
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RequestMapping("/playlists")
@RestController
class PlaylistController(
    private val service: PlaylistService,
    musicService: MusicService,
    uploaderService: UploaderService,
    namingRuleService: NamingRuleService
) {
    private final val youtubeManager = YoutubeManager()
    val manager = PlaylistManager(
        service,
        musicService,
        uploaderService,
        namingRuleService,
        youtubeManager
    )

    @GetMapping
    fun getPlaylists() = service.find()

    @GetMapping("/download")
    fun downloadPlaylists(): String {
        return manager.download()
    }

    @GetMapping("/archive-music")
    fun archiveMusic(): String {
        return manager.archiveMusic()
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun postPlaylists(@RequestBody entry: PlaylistEntry): Playlist? {
        return manager.create(entry.name, entry.url)
    }

    @GetMapping("/{id}")
    fun getPlaylist(@PathVariable id: String): Playlist {
        val playlist = service.findById(id)
        return playlist ?: throw PlaylistNotFoundException()
    }

    @PostMapping("/{id}")
    fun postPlaylist(@RequestBody playlist: Playlist) {
        manager.edit(playlist)
    }

    @DeleteMapping("/{id}")
    fun deletePlaylist(@PathVariable id: String) {
        service.remove(id)
    }

    @GetMapping("/{id}/download")
    fun downloadPlaylist(@PathVariable id: String) {
        manager.download(id)
    }
}