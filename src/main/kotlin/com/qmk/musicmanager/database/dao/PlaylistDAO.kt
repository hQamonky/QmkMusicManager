package com.qmk.musicmanager.database.dao

import com.qmk.musicmanager.domain.model.Playlist

interface PlaylistDAO {
    suspend fun allPlaylists(): List<Playlist>
    suspend fun playlist(name: String): Playlist?
    suspend fun addNewPlaylist(name: String): Playlist?
    suspend fun renamePlaylist(oldName: String, newName: String): Boolean
    suspend fun deletePlaylist(name: String): Boolean
    suspend fun deleteAllPlaylists(): Boolean
    suspend fun musicFromPlaylist(playlist: String): List<String>
    suspend fun addMusicToPlaylist(music: String, playlist: String): Boolean
    suspend fun removeMusicFromPlaylist(music: String, playlist: String): Boolean
}