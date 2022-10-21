package com.qmk.musicmanager.controller

import com.qmk.musicmanager.model.Channel
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RequestMapping("/channels")
@RestController
class ChannelController {
    private var channels = emptyList<Channel>()

    @GetMapping
    fun getChannels() = channels

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun postChannels(@RequestBody channel: Channel) {

    }

    @GetMapping("/{id}")
    fun getChannel(@PathVariable id: String) {

    }

    @PostMapping("/{id}")
    fun postChannel(
        @PathVariable id: String,
        @RequestBody channel: Channel
    ) {

    }

    @DeleteMapping("/{id}")
    fun deleteChannel(@PathVariable id: String) {

    }
}