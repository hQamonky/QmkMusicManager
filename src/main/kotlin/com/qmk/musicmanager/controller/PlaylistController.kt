package com.qmk.musicmanager.controller

import com.qmk.musicmanager.service.PlaylistService
import com.qmk.musicmanager.exception.PlaylistNotFoundException
import com.qmk.musicmanager.manager.ConfigurationManager
import com.qmk.musicmanager.manager.PlaylistManager
import com.qmk.musicmanager.model.Playlist
import com.qmk.musicmanager.model.PlaylistEntry
import com.qmk.musicmanager.service.MusicService
import com.qmk.musicmanager.service.NamingRuleService
import com.qmk.musicmanager.service.UploaderService
import com.qmk.musicmanager.manager.YoutubeManager
import com.qmk.musicmanager.model.DownloadResult
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

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

    @PostConstruct
    fun init() {
        val ses = Executors.newSingleThreadScheduledExecutor()
        val settings = ConfigurationManager().getConfiguration()
        ses.scheduleAtFixedRate({
            if (settings.autoDownload)
                manager.download()
        }, 0, settings.downloadOccurrence.toLong(), TimeUnit.HOURS)
    }

    @GetMapping
    fun getPlaylists() = service.find()

    @GetMapping("/download")
    fun downloadPlaylists(): List<DownloadResult> {
        return manager.download()
    }

    @GetMapping("/archive-music")
    fun archiveMusic(): List<String> {
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
    fun downloadPlaylist(@PathVariable id: String): DownloadResult {
        return manager.download(id) ?: DownloadResult(id)
    }
}