package com.qmk.musicmanager.database.dao

import com.qmk.musicmanager.domain.model.Music

interface MusicDAO {
    suspend fun allMusic(): List<Music>
    suspend fun music(id: String): Music?
    suspend fun newMusic(): List<Music>
    suspend fun addNewMusic(
        id: String,
        fileName: String,
        fileExtension: String,
        title: String,
        artist: String,
        uploaderId: String,
        uploadDate: String,
        isNew: Boolean
    ): Music?

    suspend fun editMusic(
        id: String,
        fileName: String,
        fileExtension: String,
        title: String,
        artist: String,
        uploaderId: String,
        uploadDate: String,
        isNew: Boolean
    ): Boolean

    suspend fun deleteMusic(id: String): Boolean

    suspend fun deleteAllMusic(): Boolean

    suspend fun removeMusicFromAllPlaylists(musicId: String): Boolean
}