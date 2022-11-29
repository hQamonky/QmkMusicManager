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
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
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

    @Operation(summary = "Get all playlists.", description = "Returns 200 if successful")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful Operation"),
        ]
    )
    @GetMapping
    fun getPlaylists() = service.find()

    @Operation(summary = "Download all playlists.", description = "Returns the download result.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful Operation"),
        ]
    )
    @PostMapping("/download")
    fun downloadPlaylists(): List<DownloadResult> {
        return manager.download()
    }

    @Operation(summary = "Archive music.", description = "Archive music which are in the \"Remove\" playlist.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Returns the list of music that were successfully archived."),
        ]
    )
    @PostMapping("/archive-music")
    fun archiveMusic(): List<String> {
        return manager.archiveMusic()
    }

    @Operation(summary = "Create a new playlist.", description = "Returns 201 if successful")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Successful Operation"),
        ]
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun postPlaylists(@RequestBody entry: PlaylistEntry): Playlist? {
        return manager.create(entry.name, entry.url)
    }

    @Operation(summary = "Get the specified playlist.", description = "Returns 200 if successful")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful Operation"),
        ]
    )
    @GetMapping("/{id}")
    fun getPlaylist(@PathVariable id: String): Playlist {
        val playlist = service.findById(id)
        return playlist ?: throw PlaylistNotFoundException()
    }

    @Operation(summary = "Edit the specified playlist.", description = "Returns 200 if successful")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful Operation"),
        ]
    )
    @PostMapping("/{id}")
    fun postPlaylist(@RequestBody playlist: Playlist) {
        manager.edit(playlist)
    }

    @Operation(summary = "Delete the specified playlist.", description = "Returns 200 if successful")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful Operation"),
        ]
    )
    @DeleteMapping("/{id}")
    fun deletePlaylist(@PathVariable id: String) {
        service.remove(id)
    }

    @Operation(summary = "Download the specified playlist.", description = "Returns 200 if successful")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful Operation"),
        ]
    )
    @PostMapping("/{id}/download")
    fun downloadPlaylist(@PathVariable id: String): DownloadResult {
        return manager.download(id) ?: DownloadResult(id)
    }
}