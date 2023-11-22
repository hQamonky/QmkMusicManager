package com.qmk.musicmanager.database.dao

import com.qmk.musicmanager.domain.model.Playlist

interface PlaylistDAO {
    suspend fun doesPlaylistNameExist(name: String): Boolean
    suspend fun allPlaylists(): List<Playlist>
    suspend fun playlist(id: String): Playlist?
    suspend fun addNewPlaylist(id: String, name: String): Playlist?
    suspend fun editPlaylist(id: String, name: String): Boolean
    suspend fun deletePlaylist(id: String): Boolean
    suspend fun deleteAllPlaylists(): Boolean
    suspend fun musicFromPlaylist(playlistId: String): List<String>
    suspend fun addMusicToPlaylist(musicId: String, playlistId: String): Boolean
    suspend fun removeMusicFromPlaylist(musicId: String, playlistId: String): Boolean
}