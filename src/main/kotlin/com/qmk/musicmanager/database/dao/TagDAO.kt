package com.qmk.musicmanager.database.dao

interface TagDAO {
    suspend fun allTags(): List<String>

    suspend fun renameTag(oldName: String, newName: String): Boolean

    suspend fun deleteTag(tag: String): Boolean

    suspend fun deleteAllTags(): Boolean
}