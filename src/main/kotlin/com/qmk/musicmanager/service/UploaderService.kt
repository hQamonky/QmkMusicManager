package com.qmk.musicmanager.service

import com.qmk.musicmanager.model.Uploader
import com.qmk.musicmanager.model.NamingFormat
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.query
import org.springframework.stereotype.Service

@Service
class UploaderService(val db: JdbcTemplate) {
    fun find(): List<Uploader> = selectAll(db)

    fun findById(id: String): Uploader? {
        val uploaderById = select(db, id)
        return if (uploaderById.isNotEmpty()) uploaderById[0]
        else null
    }

    fun new(uploader: Uploader) {
        insert(db, uploader.id, uploader.name, uploader.namingFormat.separator, uploader.namingFormat.artist_before_title)
    }

    fun save(uploader: Uploader) {
        update(db, uploader.id, uploader.name, uploader.namingFormat.separator, uploader.namingFormat.artist_before_title)
    }

    fun remove(id: String) {
        delete(db, id)
    }

    // --- Requests ---

    private fun selectAll(db: JdbcTemplate): List<Uploader> =
        db.query("SELECT * FROM Uploaders") { response, _ ->
            Uploader(
                response.getString("id"),
                response.getString("name"),
                NamingFormat(
                    response.getString("separator"),
                    response.getBoolean("artist_before_title")
                )
            )
        }

    private fun select(db: JdbcTemplate, identifier: String): List<Uploader> {
        return db.query("SELECT * FROM Uploaders WHERE id = ?", identifier) { response, _ ->
            Uploader(
                response.getString("id"),
                response.getString("name"),
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
        name: String,
        separator: String,
        artist_before_title: Boolean
    ): Int = db.update("INSERT INTO Uploaders VALUES (?, ?, ?, ?)", id, name, separator, artist_before_title)

    private fun update(
        db: JdbcTemplate,
       id: String,
       name: String,
       separator: String,
       artist_before_title: Boolean
    ): Int =
        db.update(
            "UPDATE Uploaders SET " +
                    "name = ?, " +
                    "separator = ?, " +
                    "artist_before_title = ? " +
                    "WHERE " +
                    "id = ?",
            name, separator, artist_before_title, id
        )

    private fun delete(db: JdbcTemplate, id: String): Int =
        db.update("DELETE FROM Uploaders WHERE id = ?", id)
}


