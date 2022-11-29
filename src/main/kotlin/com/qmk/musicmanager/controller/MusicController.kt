package com.qmk.musicmanager.controller

import com.qmk.musicmanager.manager.MusicManager
import com.qmk.musicmanager.service.MusicService
import com.qmk.musicmanager.model.Music
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.*

@RequestMapping("/music")
@RestController
class MusicController(private val service: MusicService) {
    val manager = MusicManager(service)

    @Operation(summary = "Get the list of new music.", description = "Returns 200 if successful")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful Operation"),
        ]
    )
    @GetMapping("/new")
    fun getNewMusic() = service.findNew()

    @Operation(summary = "Edit the info of a music file.", description = "Returns 200 if successful")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful Operation"),
        ]
    )
    @PostMapping("/{id}")
    fun postMusic(@PathVariable id: String, @RequestBody music: Music) {
        manager.editMusic(music)
    }
}