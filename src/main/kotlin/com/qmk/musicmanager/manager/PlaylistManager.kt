package com.qmk.musicmanager.manager

import com.google.gson.Gson
import com.qmk.musicmanager.service.UploaderService
import com.qmk.musicmanager.service.PlaylistService
import com.qmk.musicmanager.model.*
import com.qmk.musicmanager.youtube.YoutubeController

class PlaylistManager(
    private val playlistService: PlaylistService,
    private val uploaderService: UploaderService,
    private val youtubeController: YoutubeController
) {
    fun createPlaylist(name: String, url: String): Playlist {
        val gson = Gson()
        val playlistInfo = gson.fromJson(youtubeController.getPlaylistInfo(url), PlaylistInfo::class.java)
        val uploader = Uploader(
            id = playlistInfo.uploader_id,
            name = playlistInfo.uploader,
            namingFormat = NamingFormat() // TODO : get default naming format from settings
        )
        // Create uploader if it does not already exist
        val doesNotExist = uploaderService.findById(uploader.id).isEmpty()
        if (doesNotExist) uploaderService.new(uploader)
        val playlist = Playlist(
            id = playlistInfo.id,
            name = name,
            uploaderId = uploader.id
        )
        playlistService.new(playlist)
        return playlist
    }
}