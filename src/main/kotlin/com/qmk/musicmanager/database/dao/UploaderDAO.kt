package com.qmk.musicmanager.database.dao

import com.qmk.musicmanager.domain.model.NamingFormat
import com.qmk.musicmanager.domain.model.Uploader

interface UploaderDAO {
    suspend fun allUploaders(): List<Uploader>
    suspend fun uploader(id: String): Uploader?
    suspend fun addNewUploader(id: String, name: String, namingFormat: NamingFormat): Uploader?
    suspend fun editUploader(id: String, name: String, namingFormat: NamingFormat): Boolean
    suspend fun deleteUploader(id: String): Boolean
    suspend fun deleteAllUploaders(): Boolean
}