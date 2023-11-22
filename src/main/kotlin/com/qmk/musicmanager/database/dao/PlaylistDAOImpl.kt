package com.qmk.musicmanager.database.dao

import com.qmk.musicmanager.database.model.DatabaseFactory.dbQuery
import com.qmk.musicmanager.database.model.PlaylistMusic
import com.qmk.musicmanager.database.model.Playlists
import com.qmk.musicmanager.domain.model.Playlist
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class PlaylistDAOImpl : PlaylistDAO {
    private fun resultRowToPlaylist(row: ResultRow) = Playlist(
        id = row[Playlists.id],
        name = row[Playlists.name]
    )

    override suspend fun doesPlaylistNameExist(name: String): Boolean = dbQuery {
        Playlists
            .select { Playlists.name eq name }
            .map(::resultRowToPlaylist)
            .singleOrNull() != null
    }

    override suspend fun allPlaylists(): List<Playlist> = dbQuery {
        Playlists.selectAll().map(::resultRowToPlaylist)
    }

    override suspend fun playlist(id: String): Playlist? = dbQuery {
        Playlists
            .select { Playlists.id eq id }
            .map(::resultRowToPlaylist)
            .singleOrNull()
    }

    override suspend fun addNewPlaylist(id: String, name: String): Playlist? = dbQuery {
        val insertStatement = Playlists.insert {
            it[Playlists.id] = id
            it[Playlists.name] = name
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToPlaylist)
    }

    override suspend fun editPlaylist(id: String, name: String): Boolean = dbQuery {
        Playlists.update({ Playlists.id eq id }) {
            it[Playlists.name] = name
        } > 0
    }

    override suspend fun deletePlaylist(id: String): Boolean = dbQuery {
        Playlists.deleteWhere { PlaylistMusic.playlistId eq id }
        Playlists.deleteWhere { Playlists.id eq id } > 0
    }

    override suspend fun deleteAllPlaylists(): Boolean {
        PlaylistMusic.deleteAll()
        return Playlists.deleteAll() > 0
    }

    override suspend fun musicFromPlaylist(playlistId: String): List<String> = dbQuery {
        PlaylistMusic
            .select { PlaylistMusic.playlistId eq playlistId }
            .map { it[PlaylistMusic.musicId] }
    }

    override suspend fun addMusicToPlaylist(musicId: String, playlistId: String): Boolean = dbQuery {
        val insertStatement = PlaylistMusic.insert {
            it[PlaylistMusic.playlistId] = playlistId
            it[PlaylistMusic.musicId] = musicId
        }
        insertStatement.resultedValues?.singleOrNull()?.let { true } ?: false
    }

    override suspend fun removeMusicFromPlaylist(musicId: String, playlistId: String): Boolean = dbQuery {
        Playlists.deleteWhere { (PlaylistMusic.musicId eq musicId) and (PlaylistMusic.playlistId eq playlistId) } > 0
    }
}