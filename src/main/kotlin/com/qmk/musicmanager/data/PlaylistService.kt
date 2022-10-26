package com.qmk.musicmanager.data

import com.qmk.musicmanager.model.Playlist
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.query
import org.springframework.stereotype.Service
import java.util.*

@Service
class PlaylistService(val db: JdbcTemplate) {
    fun find(): List<Playlist> = selectAll(db)

    fun findById(id: String): List<Playlist> = select(db, id)

    fun new(playlist: Playlist) {
        insert(db, UUID.randomUUID().toString(), playlist.youtubeId, playlist.name, playlist.channelId)
    }

    fun save(playlist: Playlist) {
        val id = playlist.id ?: UUID.randomUUID().toString()
        update(db, id, playlist.youtubeId, playlist.name, playlist.channelId)
    }

    fun remove(id: String) {
        delete(db, id)
    }

    // --- Requests ---

    private fun selectAll(db: JdbcTemplate): List<Playlist> =
        db.query("SELECT * FROM Playlists") { response, _ ->
            Playlist(
                response.getString("id"),
                response.getString("youtube_id"),
                response.getString("name"),
                selectMusic(db, response.getString("id")),
                response.getString("uploader")
            )
        }

    private fun select(db: JdbcTemplate, identifier: String): List<Playlist> =
        db.query("SELECT * FROM Playlists WHERE id = ?", identifier) { response, _ ->
            Playlist(
                response.getString("id"),
                response.getString("youtube_id"),
                response.getString("name"),
                selectMusic(db, response.getString("id")),
                response.getString("uploader")
            )
        }

    private fun insert(db: JdbcTemplate, id: String, youtube_id: String, name: String, channelId: String): Int =
        db.update("INSERT INTO Playlists (id, youtube_id, name, uploader) VALUES (?, ?, ?, ?)", id, youtube_id, name, channelId)

    private fun update(db: JdbcTemplate, identifier: String, youtubeId: String, name: String, channelId: String): Int =
        db.update(
            "UPDATE Playlists SET " +
                    "youtube_id = ?, " +
                    "name = ?, " +
                    "uploader = ? " +
                    "WHERE id = ?",
            youtubeId, name, channelId, identifier
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
