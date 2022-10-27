package com.qmk.musicmanager.manager

import com.google.gson.Gson
import com.qmk.musicmanager.service.UploaderService
import com.qmk.musicmanager.service.PlaylistService
import com.qmk.musicmanager.model.*
import com.qmk.musicmanager.youtube.YoutubeController

class PlaylistManager(
    private val playlistService: PlaylistService,
    private val youtubeController: YoutubeController
) {
    fun createPlaylist(name: String, url: String): Playlist {
        val gson = Gson()
        val playlistInfo = gson.fromJson(youtubeController.getPlaylistInfo(url), PlaylistInfo::class.java)
        val playlist = Playlist(
            id = playlistInfo.id,
            name = name
        )
        val doesNotExist = playlistService.findById(playlist.id).isEmpty()
        if (doesNotExist) playlistService.new(playlist)
        return playlist
    }
}