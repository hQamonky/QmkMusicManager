package com.qmk.musicmanager.data

import com.qmk.musicmanager.model.Channel
import com.qmk.musicmanager.model.NamingFormat
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class ChannelService(val db: JdbcTemplate) {
    fun find(): List<Channel> = selectAll(db)

    fun findById(id: String): List<Channel> = select(db, id)

    fun new(channel: Channel) {
        insert(db, channel.name, channel.namingFormat.separator, channel.namingFormat.artist_before_title)
    }

    fun save(channel: Channel) {
        update(db, channel.name, channel.namingFormat.separator, channel.namingFormat.artist_before_title)
    }

    fun remove(name: String) {
        delete(db, name)
    }
}

// --- Requests ---

private fun selectAll(db: JdbcTemplate): List<Channel> =
    db.query("SELECT * FROM Channels") { response, _ ->
        Channel(
            response.getString("channel"),
            NamingFormat(
                response.getString("separator"),
                response.getBoolean("artist_before_title")
            )
        )
    }

private fun select(db: JdbcTemplate, identifier: String): List<Channel> =
    db.query("SELECT * FROM Channels WHERE channel = $identifier") { response, _ ->
        Channel(
            response.getString("channel"),
            NamingFormat(
                response.getString("separator"),
                response.getBoolean("artist_before_title")
            )
        )
    }

private fun insert(db: JdbcTemplate, channel: String, separator: String, artist_before_title: Boolean): Int =
    db.update("INSERT INTO Channels VALUES ($channel, $separator, $artist_before_title)")

private fun update(db: JdbcTemplate, channel: String, separator: String, artist_before_title: Boolean): Int =
    db.update("UPDATE Channels SET " +
        "separator = $separator, " +
        "artist_before_title = $artist_before_title " +
        "WHERE " +
        "channel = $channel"
    )

private fun delete(db: JdbcTemplate, channel: String): Int =
    db.update("DELETE FROM Channels WHERE channel = $channel")


