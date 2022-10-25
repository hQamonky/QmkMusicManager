package com.qmk.musicmanager.data

import com.qmk.musicmanager.model.Music
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import kotlin.time.measureTimedValue

@Service
class MusicService(val db: JdbcTemplate) {
    fun find(): List<Music> = selectAll(db)

    fun findById(id: Int): List<Music> = select(db, id)

    fun findNew(): List<Music> = selectNew(db)

    fun new(music: Music) {
        insert(db, music.id, music.name, music.title, music.artist, music.channelId)
        music.playlistIds.forEach {
            insertPlaylist(db, it, music.id)
        }
    }

    fun save(music: Music) {
        update(db, music.id, music.title, music.artist, music.isNew)
    }

    fun remove(id: Int) {
        delete(db, id)
    }

    // --- Requests ---

    private fun selectAll(db: JdbcTemplate): List<Music> =
        db.query("SELECT * FROM Music") { response, _ ->
            Music(
                response.getInt("id"),
                response.getString("name"),
                response.getString("title"),
                response.getString("artist"),
                response.getString("channel"),
                response.getString("upload_date"),
                response.getBoolean("isNew"),
                selectPlaylist(db, response.getInt("id"))
            )
        }

    private fun select(db: JdbcTemplate, identifier: Int): List<Music> =
        db.query("SELECT * FROM Music WHERE id = $identifier") { response, _ ->
            Music(
                response.getInt("id"),
                response.getString("name"),
                response.getString("title"),
                response.getString("artist"),
                response.getString("channel"),
                response.getString("upload_date"),
                response.getBoolean("isNew"),
                selectPlaylist(db, response.getInt("id"))
            )
        }

    private fun selectNew(db: JdbcTemplate): List<Music> =
        db.query("SELECT * FROM Music WHERE new = 'true'") { response, _ ->
            Music(
                response.getInt("id"),
                response.getString("name"),
                response.getString("title"),
                response.getString("artist"),
                response.getString("channel"),
                response.getString("upload_date"),
                response.getBoolean("is_new"),
                selectPlaylist(db, response.getInt("id"))
            )
        }

    private fun insert(
        db: JdbcTemplate,
        identifier: Int,
        name: String,
        title: String,
        artist: String,
        channel: String
    ): Int = db.update("INSERT INTO Music VALUES ($identifier, $name, $title, $artist, $channel, 'true')")

    private fun update(db: JdbcTemplate, identifier: Int, title: String, artist: String, isNew: Boolean): Int =
        db.update(
            "UPDATE Music SET " +
                    "title = $title, " +
                    "artist = $artist, " +
                    "is_new = $isNew " +
                    "WHERE " +
                    "id = $identifier"
        )

    private fun delete(db: JdbcTemplate, identifier: Int): Int =
        db.update("DELETE FROM Music WHERE id = $identifier")

    private fun selectPlaylist(db: JdbcTemplate, identifier: Int): List<Int> =
        db.query("SELECT * FROM Playlist_Music WHERE id_music = $identifier") { response, _ ->
            response.getInt("id_playlist")
        }

    private fun insertPlaylist(db: JdbcTemplate, id_playlist: Int, id_music: Int): Int =
        db.update("INSERT INTO Playlist_Music (id_playlist, id_music) VALUES ($id_playlist, $id_music)")
}