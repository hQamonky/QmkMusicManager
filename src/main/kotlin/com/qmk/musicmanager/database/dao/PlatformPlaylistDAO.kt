package com.qmk.musicmanager.database.dao

import com.qmk.musicmanager.domain.model.PlatformPlaylist

interface PlatformPlaylistDAO {
    suspend fun allPlaylists(): List<PlatformPlaylist>
    suspend fun playlist(id: String): PlatformPlaylist?
    suspend fun addNewPlaylist(id: String, name: String, platform: String, playlists: List<String>): PlatformPlaylist?
    suspend fun editPlaylist(id: String, playlists: List<String>): Boolean
    suspend fun deletePlaylist(id: String): Boolean
    suspend fun deleteAllPlaylists(): Boolean
    suspend fun playlistsFromPlaylist(id: String): List<String>
    suspend fun addPlaylistToPlaylist(playlist: String, id: String): Boolean
    suspend fun removePlaylistFromPlaylist(playlist: String, id: String): Boolean
}