package com.qmk.musicmanager.database.dao

import com.qmk.musicmanager.database.model.*
import com.qmk.musicmanager.database.model.DatabaseFactory.dbQuery
import com.qmk.musicmanager.domain.model.Playlist
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class PlaylistDAOImpl: PlaylistDAO {
    private suspend fun resultRowToPlaylist(row: ResultRow) = Playlist(
        name = row[Playlists.name],
        music = musicOfPlPlaylist(row[Playlists.name])
    )

    private suspend fun musicOfPlPlaylist(playlist: String): List<String> = dbQuery {
        PlaylistMusic.select { PlaylistMusic.playlist eq playlist }
            .map { it[PlaylistMusic.music] }
    }

    override suspend fun allPlaylists(): List<Playlist> = dbQuery {
        Playlists.selectAll().map { resultRowToPlaylist(it) }
    }

    override suspend fun playlist(name: String): Playlist? = dbQuery {
        Playlists
            .select { Playlists.name eq name }
            .map { resultRowToPlaylist(it) }
            .singleOrNull()
    }

    override suspend fun addNewPlaylist(name: String): Playlist? = dbQuery {
        val insertStatement = Playlists.insert {
            it[Playlists.name] = name
        }
        insertStatement.resultedValues?.singleOrNull()?.let { resultRowToPlaylist(it) }
    }

    override suspend fun renamePlaylist(oldName: String, newName: String): Boolean = dbQuery {
        Playlists.update({ Playlists.name eq oldName }) {
            it[Playlists.name] = newName
        } > 0
    }

    override suspend fun deletePlaylist(name: String): Boolean = dbQuery {
        PlaylistMusic.deleteWhere { PlaylistMusic.playlist eq name }
        val result = PlaylistPlatformPlaylist.deleteWhere { PlaylistPlatformPlaylist.playlist eq name } > 0
        Playlists.deleteWhere { Playlists.name eq name } > 0
    }

    override suspend fun deleteAllPlaylists(): Boolean = dbQuery {
        PlaylistMusic.deleteAll()
        PlatformPlaylists.deleteAll()
        PlaylistPlatformPlaylist.deleteAll()
        Playlists.deleteAll() > 0
    }

    override suspend fun musicFromPlaylist(playlist: String): List<String> = dbQuery {
        PlaylistMusic
            .select { PlaylistMusic.playlist eq playlist }
            .map { it[PlaylistMusic.music] }
    }

    override suspend fun addMusicToPlaylist(music: String, playlist: String): Boolean = dbQuery {
        val insertStatement = PlaylistMusic.insert {
            it[PlaylistMusic.playlist] = playlist
            it[PlaylistMusic.music] = music
        }
        insertStatement.resultedValues?.singleOrNull()?.let { true } ?: false
    }

    override suspend fun removeMusicFromPlaylist(music: String, playlist: String): Boolean = dbQuery {
        Playlists.deleteWhere { (PlaylistMusic.music eq music) and (PlaylistMusic.playlist eq playlist) } > 0
    }
}