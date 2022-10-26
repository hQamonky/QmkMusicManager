package com.qmk.musicmanager.controller

import com.qmk.musicmanager.data.ChannelService
import com.qmk.musicmanager.exception.ChannelNotFoundException
import com.qmk.musicmanager.exception.PlaylistNotFoundException
import com.qmk.musicmanager.model.Channel
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RequestMapping("/channels")
@RestController
class ChannelController(val service: ChannelService) {

    @GetMapping
    fun getChannels() = service.find()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun postChannels(@RequestBody channel: Channel) {
        service.new(channel)
    }

    @GetMapping("/{id}")
    fun getChannel(@PathVariable id: String): Channel {
        val channel = service.findById(id).firstOrNull { it.id == id }
        return channel ?: throw ChannelNotFoundException()
    }

    @PostMapping("/{id}")
    fun postChannel(
        @PathVariable id: String,
        @RequestBody channel: Channel
    ) {
        service.save(channel)
    }

    @DeleteMapping("/{id}")
    fun deleteChannel(@PathVariable id: String) {
        service.remove(id)
    }
}