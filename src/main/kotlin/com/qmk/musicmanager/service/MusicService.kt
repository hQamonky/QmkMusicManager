package com.qmk.musicmanager.service

import com.qmk.musicmanager.model.Music
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.query
import org.springframework.stereotype.Service

@Service
class MusicService(val db: JdbcTemplate) {
    fun find(): List<Music> = selectAll(db)

    fun findById(id: String): List<Music> = select(db, id)

    fun findNew(): List<Music> = selectNew(db)

    fun new(music: Music) {
        insert(db, music.id, music.name, music.title, music.artist, music.uploaderId)
        music.playlistIds.forEach {
            insertPlaylist(db, it, music.id)
        }
    }

    fun save(music: Music) {
        update(db, music.id, music.title, music.artist, music.isNew)
    }

    fun remove(id: String) {
        delete(db, id)
    }

    // --- Requests ---

    private fun selectAll(db: JdbcTemplate): List<Music> =
        db.query("SELECT * FROM Music") { response, _ ->
            Music(
                response.getString("id"),
                response.getString("name"),
                response.getString("title"),
                response.getString("artist"),
                response.getString("uploader"),
                response.getString("upload_date"),
                response.getBoolean("isNew"),
                selectPlaylist(db, response.getString("id"))
            )
        }

    private fun select(db: JdbcTemplate, identifier: String): List<Music> =
        db.query("SELECT * FROM Music WHERE id = ?", identifier) { response, _ ->
            Music(
                response.getString("id"),
                response.getString("name"),
                response.getString("title"),
                response.getString("artist"),
                response.getString("uploader"),
                response.getString("upload_date"),
                response.getBoolean("isNew"),
                selectPlaylist(db, response.getString("id"))
            )
        }

    private fun selectNew(db: JdbcTemplate): List<Music> =
        db.query("SELECT * FROM Music WHERE new = 'true'") { response, _ ->
            Music(
                response.getString("id"),
                response.getString("name"),
                response.getString("title"),
                response.getString("artist"),
                response.getString("uploader"),
                response.getString("upload_date"),
                response.getBoolean("is_new"),
                selectPlaylist(db, response.getString("id"))
            )
        }

    private fun insert(
        db: JdbcTemplate,
        identifier: String,
        name: String,
        title: String,
        artist: String,
        uploader: String
    ): Int = db.update("INSERT INTO Music VALUES (?, ?, ?, ?, ?, 'true')", identifier, name, title, artist, uploader)

    private fun update(db: JdbcTemplate, identifier: String, title: String, artist: String, isNew: Boolean): Int =
        db.update(
            "UPDATE Music SET " +
                    "title = ?, " +
                    "artist = ?, " +
                    "is_new = ? " +
                    "WHERE " +
                    "id = ?",
            title, artist, isNew, identifier
        )

    private fun delete(db: JdbcTemplate, identifier: String): Int =
        db.update("DELETE FROM Music WHERE id = ?", identifier)

    private fun selectPlaylist(db: JdbcTemplate, identifier: String): List<String> =
        db.query("SELECT * FROM Playlist_Music WHERE id_music = ?", identifier) { response, _ ->
            response.getString("id_playlist")
        }

    private fun insertPlaylist(db: JdbcTemplate, id_playlist: String, id_music: String): Int =
        db.update("INSERT INTO Playlist_Music (id_playlist, id_music) VALUES (?, ?)", id_playlist, id_music)
}