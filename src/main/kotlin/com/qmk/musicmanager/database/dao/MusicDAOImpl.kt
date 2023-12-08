package com.qmk.musicmanager.database.dao

import com.qmk.musicmanager.database.model.DatabaseFactory.dbQuery
import com.qmk.musicmanager.database.model.MusicTag
import com.qmk.musicmanager.database.model.PlaylistMusic
import com.qmk.musicmanager.database.model.Tags
import com.qmk.musicmanager.domain.model.Music
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

typealias MusicTable = com.qmk.musicmanager.database.model.Music

class MusicDAOImpl : MusicDAO {
    private suspend fun resultRowToMusic(row: ResultRow) = Music(
        fileName = row[MusicTable.fileName],
        fileExtension = row[MusicTable.fileExtension],
        title = row[MusicTable.title],
        artist = row[MusicTable.artist],
        platformId = row[MusicTable.platformId],
        uploaderId = row[MusicTable.uploaderId],
        uploadDate = row[MusicTable.uploadDate],
        playlists = playlistsOfMusic(row[MusicTable.fileName]),
        tags = tagsOfMusic(row[MusicTable.fileName]),
        isNew = row[MusicTable.isNew]
    )

    private suspend fun playlistsOfMusic(music: String): List<String> = dbQuery {
        PlaylistMusic.select { PlaylistMusic.music eq music }.map { it[PlaylistMusic.playlist] }
    }

    private suspend fun tagsOfMusic(music: String): List<String> = dbQuery {
        MusicTag.select { MusicTag.music eq music }.map { it[MusicTag.tag] }
    }

    private suspend fun addTagIfNotExist(tag: String): Boolean = dbQuery {
        if (Tags.select { Tags.value eq tag }.map { it[Tags.value] }.singleOrNull() == null) {
            Tags.insert { it[value] = tag }.resultedValues?.singleOrNull() != null
        } else {
            true
        }
    }

    override suspend fun allMusic(): List<Music> = dbQuery {
        MusicTable.selectAll().map { resultRowToMusic(it) }
    }

    override suspend fun music(fileName: String): Music? = dbQuery {
        MusicTable.select { MusicTable.fileName eq fileName }.map { resultRowToMusic(it) }.singleOrNull()
    }

    override suspend fun getMusicFromPlatformId(id: String): Music? = dbQuery {
        MusicTable.select { MusicTable.platformId eq id }.map { resultRowToMusic(it) }.singleOrNull()
    }

    override suspend fun newMusic(): List<Music> = dbQuery {
        MusicTable.select { MusicTable.isNew eq true }.map { resultRowToMusic(it) }
    }

    override suspend fun addNewMusic(
        fileName: String,
        fileExtension: String,
        title: String,
        artist: String,
        platformId: String,
        uploaderId: String,
        uploadDate: String,
        tags: List<String>,
        isNew: Boolean
    ): Music? = dbQuery {
        val insertStatement = MusicTable.insert {
            it[MusicTable.fileName] = fileName
            it[MusicTable.fileExtension] = fileExtension
            it[MusicTable.title] = title
            it[MusicTable.artist] = artist
            it[MusicTable.platformId] = platformId
            it[MusicTable.uploaderId] = uploaderId
            it[MusicTable.uploadDate] = uploadDate
            it[MusicTable.isNew] = true
        }
        tags.forEach { tag ->
            addTagToMusic(tag, fileName)
        }
        insertStatement.resultedValues?.singleOrNull()?.let { resultRowToMusic(it) }
    }

    override suspend fun editMusic(
        fileName: String,
        fileExtension: String,
        title: String,
        artist: String,
        platformId: String,
        uploaderId: String,
        uploadDate: String,
        isNew: Boolean
    ): Boolean = dbQuery {
        MusicTable.update({ MusicTable.platformId eq platformId }) {
            it[MusicTable.fileName] = fileName
            it[MusicTable.fileExtension] = fileExtension
            it[MusicTable.title] = title
            it[MusicTable.artist] = artist
            it[MusicTable.uploaderId] = uploaderId
            it[MusicTable.uploadDate] = uploadDate
            it[MusicTable.isNew] = isNew
        } > 0
    }

    override suspend fun deleteMusic(fileName: String): Boolean = dbQuery {
        removeMusicFromAllPlaylists(fileName)
        MusicTable.deleteWhere { MusicTable.fileName eq fileName } > 0
    }

    override suspend fun deleteAllMusic(): Boolean {
        PlaylistMusic.deleteAll()
        return MusicTable.deleteAll() > 0
    }

    override suspend fun removeMusicFromAllPlaylists(fileName: String): Boolean = dbQuery {
        PlaylistMusic.deleteWhere { music eq fileName } > 0
    }

    override suspend fun tagsFromMusic(fileName: String): List<String> = dbQuery {
        MusicTag.select { MusicTag.music eq fileName }.map { it[MusicTag.tag] }
    }

    override suspend fun addTagToMusic(tag: String, fileName: String): Boolean = dbQuery {
        if (Tags.select { Tags.value eq tag }.map { it[Tags.value] }.singleOrNull() == null) {
            Tags.insert { it[value] = tag }
        }
        val insertStatement = MusicTag.insert {
            it[MusicTag.tag] = tag
            it[MusicTag.music] = music
        }
        insertStatement.resultedValues?.singleOrNull() != null
    }

    override suspend fun removeTagFromMusic(tag: String, fileName: String): Boolean = dbQuery {
        val result = MusicTag.deleteWhere { (MusicTag.music eq music) and (MusicTag.tag eq tag) } > 0
        if (result) {
            val isTagUnused = MusicTag.select { MusicTag.tag eq tag }.map { it[MusicTag.tag] }.isEmpty()
            if (isTagUnused) {
                Tags.deleteWhere { Tags.value eq tag }
            }
        }
        result
    }

    val musicDao: MusicDAOImpl = MusicDAOImpl()
}