package com.qmk.musicmanager.database.dao

import com.qmk.musicmanager.database.model.DatabaseFactory.dbQuery
import com.qmk.musicmanager.database.model.PlatformPlaylists
import com.qmk.musicmanager.database.model.PlaylistPlatformPlaylist
import com.qmk.musicmanager.domain.model.PlatformPlaylist
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class PlatformPlaylistDAOImpl : PlatformPlaylistDAO {
    private suspend fun resultRowToPlaylist(row: ResultRow) = PlatformPlaylist(
        id = row[PlatformPlaylists.id],
        name = row[PlatformPlaylists.name],
        platform = row[PlatformPlaylists.platform],
        playlists = playlistsOfPlPlaylist(row[PlatformPlaylists.id])
    )

    private suspend fun playlistsOfPlPlaylist(plPlaylistId: String): List<String> = dbQuery {
        PlaylistPlatformPlaylist.select { PlaylistPlatformPlaylist.playlistPlatformId eq plPlaylistId }
            .map { it[PlaylistPlatformPlaylist.playlist] }
    }

    override suspend fun allPlaylists(): List<PlatformPlaylist> = dbQuery {
        PlatformPlaylists.selectAll().map { resultRowToPlaylist(it) }
    }

    override suspend fun playlist(id: String): PlatformPlaylist? = dbQuery {
        PlatformPlaylists.select { PlatformPlaylists.id eq id }.map { resultRowToPlaylist(it) }.singleOrNull()
    }

    override suspend fun addNewPlaylist(
        id: String, name: String, platform: String, playlists: List<String>
    ): PlatformPlaylist? = dbQuery {
        val insertStatement = PlatformPlaylists.insert {
            it[PlatformPlaylists.id] = id
            it[PlatformPlaylists.name] = name
            it[PlatformPlaylists.platform] = platform
        }
        playlists.forEach { playlist ->
            PlaylistPlatformPlaylist.insert {
                it[PlaylistPlatformPlaylist.playlistPlatformId] = id
                it[PlaylistPlatformPlaylist.playlist] = playlist
            }
        }
        insertStatement.resultedValues?.singleOrNull()?.let { resultRowToPlaylist(it).copy(playlists = playlists) }
    }

    override suspend fun editPlaylist(id: String, playlists: List<String>): Boolean = dbQuery {
        var result = PlaylistPlatformPlaylist.deleteWhere { PlaylistPlatformPlaylist.playlistPlatformId eq id } > 0
        playlists.forEach { playlist ->
            val insertStatement = PlaylistPlatformPlaylist.insert {
                it[PlaylistPlatformPlaylist.playlistPlatformId] = id
                it[PlaylistPlatformPlaylist.playlist] = playlist
            }
            if (result) result = insertStatement.resultedValues?.singleOrNull() != null
        }
        result
    }

    override suspend fun deletePlaylist(id: String): Boolean = dbQuery {
        PlaylistPlatformPlaylist.deleteWhere { PlaylistPlatformPlaylist.playlistPlatformId eq id }
        PlatformPlaylists.deleteWhere { PlatformPlaylists.id eq id } > 0
    }

    override suspend fun deleteAllPlaylists(): Boolean = dbQuery {
        PlaylistPlatformPlaylist.deleteAll()
        PlatformPlaylists.deleteAll() > 0
    }

    override suspend fun playlistsFromPlPlaylist(id: String): List<String> = dbQuery {
        PlaylistPlatformPlaylist.select { PlaylistPlatformPlaylist.playlistPlatformId eq id }
            .map { it[PlaylistPlatformPlaylist.playlist] }
    }

    override suspend fun addPlaylistToPlaylist(playlist: String, id: String): Boolean = dbQuery {
        val insertStatement = PlaylistPlatformPlaylist.insert {
            it[PlaylistPlatformPlaylist.playlistPlatformId] = id
            it[PlaylistPlatformPlaylist.playlist] = playlist
        }
        insertStatement.resultedValues?.singleOrNull() != null
    }

    override suspend fun removePlaylistFromPlaylist(playlist: String, id: String): Boolean = dbQuery {
        PlaylistPlatformPlaylist.deleteWhere {
            (PlaylistPlatformPlaylist.playlist eq id) and (PlaylistPlatformPlaylist.playlist eq playlist)
        } > 0
    }

    private suspend fun deleteUnusedPlatformPlaylists(): Boolean = dbQuery {
        var finalResult = true
        allPlaylists().forEach { playlist ->
            val isUnused = PlaylistPlatformPlaylist.select { PlaylistPlatformPlaylist.playlistPlatformId eq playlist.id }
                .map { it[PlaylistPlatformPlaylist.playlistPlatformId] }.isEmpty()
            if (isUnused) {
                val result = PlatformPlaylists.deleteWhere { PlatformPlaylists.id eq playlist.id } > 0
                if (finalResult) finalResult = result
            }
        }
        finalResult
    }
}