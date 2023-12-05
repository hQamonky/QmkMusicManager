package com.qmk.musicmanager.database.dao

import com.qmk.musicmanager.domain.model.Music

interface TagDAO {
    suspend fun allTags(): List<String>

    suspend fun renameTag(oldName: String, newName: String): Boolean

    suspend fun deleteTag(tag: String): Boolean

    suspend fun deleteAllTags(): Boolean
}