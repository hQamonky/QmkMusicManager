package com.qmk.musicmanager.database.dao

import com.qmk.musicmanager.domain.model.Music

interface MusicDAO {
    suspend fun allMusic(): List<Music>
    suspend fun music(fileName: String): Music?
    suspend fun newMusic(): List<Music>
    suspend fun addNewMusic(
        fileName: String,
        fileExtension: String,
        title: String,
        artist: String,
        id: String,
        uploaderId: String,
        uploadDate: String,
        tags: List<String>,
        isNew: Boolean
    ): Music?

    suspend fun editMusic(
        fileName: String,
        fileExtension: String,
        title: String,
        artist: String,
        id: String,
        uploaderId: String,
        uploadDate: String,
        isNew: Boolean
    ): Boolean

    suspend fun deleteMusic(fileName: String): Boolean

    suspend fun deleteAllMusic(): Boolean

    suspend fun removeMusicFromAllPlaylists(fileName: String): Boolean

    suspend fun tagsFromMusic(music: String): List<String>

    suspend fun addTagToMusic(tag: String, fileName: String): Boolean

    suspend fun removeTagFromMusic(tag: String, fileName: String): Boolean
}