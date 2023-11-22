package com.qmk.musicmanager.database.dao

import com.qmk.musicmanager.database.model.DatabaseFactory.dbQuery
import com.qmk.musicmanager.database.model.PlaylistMusic
import com.qmk.musicmanager.database.model.Playlists
import com.qmk.musicmanager.domain.model.Music
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

typealias MusicTable = com.qmk.musicmanager.database.model.Music

class MusicDAOImpl : MusicDAO {
    private suspend fun resultRowToMusic(row: ResultRow) = Music(
        id = row[MusicTable.id],
        fileName = row[MusicTable.fileName],
        fileExtension = row[MusicTable.fileExtension],
        title = row[MusicTable.title],
        artist = row[MusicTable.artist],
        uploaderId = row[MusicTable.uploader],
        uploadDate = row[MusicTable.uploadDate],
        isNew = row[MusicTable.isNew],
        playlistIds = playlistsOfMusic(row[MusicTable.id])
    )

    private suspend fun playlistsOfMusic(musicId: String): List<String> = dbQuery {
        PlaylistMusic
            .select { PlaylistMusic.musicId eq musicId }
            .map { it[PlaylistMusic.playlistId] }
    }

    override suspend fun allMusic(): List<Music> = dbQuery {
        MusicTable.selectAll().map { resultRowToMusic(it) }
    }

    override suspend fun music(id: String): Music? = dbQuery {
        MusicTable
            .select { MusicTable.id eq id }
            .map { resultRowToMusic(it) }
            .singleOrNull()
    }

    override suspend fun newMusic(): List<Music> = dbQuery {
        MusicTable
            .select { MusicTable.isNew eq true }
            .map { resultRowToMusic(it) }
    }

    override suspend fun addNewMusic(
        id: String,
        fileName: String,
        fileExtension: String,
        title: String,
        artist: String,
        uploaderId: String,
        uploadDate: String,
        isNew: Boolean
    ): Music? = dbQuery {
        val insertStatement = MusicTable.insert {
            it[MusicTable.id] = id
            it[MusicTable.fileName] = fileName
            it[MusicTable.fileExtension] = fileExtension
            it[MusicTable.title] = title
            it[MusicTable.artist] = artist
            it[MusicTable.uploader] = uploaderId
            it[MusicTable.uploadDate] = uploadDate
            it[MusicTable.isNew] = true
        }
        insertStatement.resultedValues?.singleOrNull()?.let { resultRowToMusic(it) }
    }

    override suspend fun editMusic(
        id: String,
        fileName: String,
        fileExtension: String,
        title: String,
        artist: String,
        uploaderId: String,
        uploadDate: String,
        isNew: Boolean
    ): Boolean = dbQuery {
        MusicTable.update({ MusicTable.id eq id }) {
            it[MusicTable.fileName] = fileName
            it[MusicTable.fileExtension] = fileExtension
            it[MusicTable.title] = title
            it[MusicTable.artist] = artist
            it[MusicTable.uploader] = uploaderId
            it[MusicTable.uploadDate] = uploadDate
            it[MusicTable.isNew] = isNew
        } > 0
    }

    override suspend fun deleteMusic(id: String): Boolean = dbQuery {
        removeMusicFromAllPlaylists(id)
        MusicTable.deleteWhere { MusicTable.id eq id } > 0
    }

    override suspend fun deleteAllMusic(): Boolean {
        PlaylistMusic.deleteAll()
        return MusicTable.deleteAll() > 0
    }

    override suspend fun removeMusicFromAllPlaylists(musicId: String): Boolean = dbQuery {
        Playlists.deleteWhere { PlaylistMusic.musicId eq musicId } > 0
    }

    val musicDao: MusicDAOImpl = MusicDAOImpl()
}