package com.qmk.musicmanager.data

import com.qmk.musicmanager.model.Channel
import com.qmk.musicmanager.model.NamingFormat
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class ChannelService(val db: JdbcTemplate) {
    fun find(): List<Channel> = selectAll(db)

    fun findById(id: Int): List<Channel> = select(db, id)

    fun new(channel: Channel) {
        insert(db, channel.name, channel.namingFormat.separator, channel.namingFormat.artist_before_title)
    }

    fun save(channel: Channel) {
        update(db, channel.id, channel.namingFormat.separator, channel.namingFormat.artist_before_title)
    }

    fun remove(id: Int) {
        delete(db, id)
    }

    // --- Requests ---

    private fun selectAll(db: JdbcTemplate): List<Channel> =
        db.query("SELECT * FROM Channels") { response, _ ->
            Channel(
                response.getInt("id"),
                response.getString("channel"),
                NamingFormat(
                    response.getString("separator"),
                    response.getBoolean("artist_before_title")
                )
            )
        }

    private fun select(db: JdbcTemplate, identifier: Int): List<Channel> =
        db.query("SELECT * FROM Channels WHERE channel = $identifier") { response, _ ->
            Channel(
                response.getInt("id"),
                response.getString("channel"),
                NamingFormat(
                    response.getString("separator"),
                    response.getBoolean("artist_before_title")
                )
            )
        }

    private fun insert(db: JdbcTemplate, channel: String, separator: String, artist_before_title: Boolean): Int =
        db.update("INSERT INTO Channels VALUES ($channel, $separator, $artist_before_title)")

    private fun update(db: JdbcTemplate, id: Int, separator: String, artist_before_title: Boolean): Int =
        db.update(
            "UPDATE Channels SET " +
                    "separator = $separator, " +
                    "artist_before_title = $artist_before_title " +
                    "WHERE " +
                    "channel = $id"
        )

    private fun delete(db: JdbcTemplate, id: Int): Int =
        db.update("DELETE FROM Channels WHERE channel = $id")
}


