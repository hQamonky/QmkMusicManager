package com.qmk.musicmanager.data

import com.qmk.musicmanager.model.Channel
import com.qmk.musicmanager.model.NamingFormat
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.query
import org.springframework.stereotype.Service
import java.util.*

@Service
class ChannelService(val db: JdbcTemplate) {
    fun find(): List<Channel> = selectAll(db)

    fun findById(id: String): List<Channel> {
        return select(db, id)
    }

    fun new(channel: Channel) {
        insert(
            db,
            UUID.randomUUID().toString(),
            channel.name,
            channel.namingFormat.separator,
            channel.namingFormat.artist_before_title
        )
    }

    fun save(channel: Channel) {
        val id = channel.id ?: UUID.randomUUID().toString()
        update(db, id, channel.name, channel.namingFormat.separator, channel.namingFormat.artist_before_title)
    }

    fun remove(id: String) {
        delete(db, id)
    }

    // --- Requests ---

    private fun selectAll(db: JdbcTemplate): List<Channel> =
        db.query("SELECT * FROM Channels") { response, _ ->
            Channel(
                response.getString("id"),
                response.getString("channel"),
                NamingFormat(
                    response.getString("separator"),
                    response.getBoolean("artist_before_title")
                )
            )
        }

    private fun select(db: JdbcTemplate, identifier: String): List<Channel> {
        return db.query("SELECT * FROM Channels WHERE id = ?", identifier) { response, _ ->
            Channel(
                response.getString("id"),
                response.getString("channel"),
                NamingFormat(
                    response.getString("separator"),
                    response.getBoolean("artist_before_title")
                )
            )
        }
    }

    private fun insert(
        db: JdbcTemplate,
        id: String,
        channel: String,
        separator: String,
        artist_before_title: Boolean
    ): Int = db.update("INSERT INTO Channels VALUES (?, ?, ?, ?)", id, channel, separator, artist_before_title)

    private fun update(
        db: JdbcTemplate,
       id: String,
       name: String,
       separator: String,
       artist_before_title: Boolean
    ): Int =
        db.update(
            "UPDATE Channels SET " +
                    "channel = ?, " +
                    "separator = ?, " +
                    "artist_before_title = ? " +
                    "WHERE " +
                    "id = ?",
            name, separator, artist_before_title, id
        )

    private fun delete(db: JdbcTemplate, id: String): Int =
        db.update("DELETE FROM Channels WHERE id = ?", id)
}


