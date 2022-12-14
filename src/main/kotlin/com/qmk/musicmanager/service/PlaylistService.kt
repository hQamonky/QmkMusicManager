package com.qmk.musicmanager.service

import com.qmk.musicmanager.model.Playlist
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.query
import org.springframework.stereotype.Service

@Service
class PlaylistService(val db: JdbcTemplate) {
    fun find(): List<Playlist> = selectAll(db)

    fun findById(id: String): Playlist? {
        val playlistById = select(db, id)
        return if (playlistById.isNotEmpty()) playlistById[0]
        else null
    }

    fun new(playlist: Playlist): Int {
        return insert(db, playlist.id, playlist.name)
    }

    fun save(playlist: Playlist) {
        update(db, playlist.id, playlist.name)
    }

    fun remove(id: String) {
        delete(db, id)
    }

    // --- Requests ---

    private fun selectAll(db: JdbcTemplate): List<Playlist> =
        db.query("SELECT * FROM Playlists") { response, _ ->
            Playlist(
                response.getString("id"),
                response.getString("name"),
                selectMusic(db, response.getString("id"))
            )
        }

    private fun select(db: JdbcTemplate, identifier: String): List<Playlist> =
        db.query("SELECT * FROM Playlists WHERE id = ?", identifier) { response, _ ->
            Playlist(
                response.getString("id"),
                response.getString("name"),
                selectMusic(db, response.getString("id"))
            )
        }

    private fun insert(db: JdbcTemplate, id: String, name: String): Int =
        db.update("INSERT INTO Playlists (id, name) VALUES (?, ?)", id, name)

    private fun update(db: JdbcTemplate, identifier: String, name: String): Int =
        db.update(
            "UPDATE Playlists SET " +
                    "name = ? " +
                    "WHERE id = ?",
            name, identifier
        )

    private fun delete(db: JdbcTemplate, identifier: String): Int {
        db.update("DELETE FROM Playlists WHERE id = ?", identifier)
        deletePlaylistMusic(db, identifier)
        return 0 // TODO : Handle error
    }

    private fun selectMusic(db: JdbcTemplate, identifier: String): List<String> =
        db.query("SELECT * FROM Playlist_Music WHERE id_playlist = ?", identifier) { response, _ ->
            response.getString("id_music")
        }

    private fun deletePlaylistMusic(db: JdbcTemplate, identifier: String): Int =
        db.update("DELETE FROM Playlist_Music WHERE id_playlist = ?", identifier)
}
