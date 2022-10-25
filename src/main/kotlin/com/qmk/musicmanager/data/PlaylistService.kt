package com.qmk.musicmanager.data

import com.qmk.musicmanager.model.Playlist
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class PlaylistService(val db: JdbcTemplate) {
    fun find(): List<Playlist> = selectAll(db)

    fun findById(id: Int): List<Playlist> = select(db, id)

    fun new(playlist: Playlist) {
        insert(db, playlist.youtubeId, playlist.name, playlist.channelId)
    }

    fun save(playlist: Playlist) {
        update(db, playlist.id, playlist.youtubeId, playlist.name, playlist.channelId)
    }

    fun remove(id: Int) {
        delete(db, id)
    }

    // --- Requests ---

    private fun selectAll(db: JdbcTemplate): List<Playlist> =
        db.query("SELECT * FROM Playlists") { response, _ ->
            Playlist(
                response.getInt("id"),
                response.getString("youtube_id"),
                response.getString("name"),
                selectMusic(db, response.getInt("id")),
                response.getString("channel_id")
            )
        }

    private fun select(db: JdbcTemplate, identifier: Int): List<Playlist> =
        db.query("SELECT * FROM Playlists WHERE id = $identifier") { response, _ ->
            Playlist(
                response.getInt("id"),
                response.getString("youtube_id"),
                response.getString("name"),
                selectMusic(db, response.getInt("id")),
                response.getString("channel_id")
            )
        }

    private fun insert(db: JdbcTemplate, youtube_id: String, name: String, uploader: String): Int =
        db.update("INSERT INTO Playlists (youtube_id, name, uploader) VALUES ($youtube_id, $name, $uploader)")

    private fun update(db: JdbcTemplate, identifier: Int, youtube_id: String, name: String, uploader: String): Int =
        db.update(
            "UPDATE Playlists SET " +
                    "youtube_id = $youtube_id, " +
                    "name = $name, " +
                    "uploader = $uploader " +
                    "WHERE id = $identifier"
        )

    private fun delete(db: JdbcTemplate, identifier: Int): Int {
        db.update("DELETE FROM Playlists WHERE id = $identifier")
        deletePlaylistMusic(db, identifier)
        return 0 // TODO : Handle error
    }

    private fun selectMusic(db: JdbcTemplate, identifier: Int): List<String> =
        db.query("SELECT * FROM Playlist_Music WHERE id_playlist = $identifier") { response, _ ->
            response.getString("id_music")
        }

    private fun deletePlaylistMusic(db: JdbcTemplate, identifier: Int): Int =
        db.update("DELETE FROM Playlist_Music WHERE id_playlist = $identifier")
}
