package com.qmk.musicmanager.service

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class DataService(val db: JdbcTemplate) {

    fun emptyDatabase() {
        emptyTable(db, "Playlists")
        emptyTable(db, "Music")
        emptyTable(db, "Playlist_Music")
        emptyTable(db, "Uploaders")
        emptyTable(db, "NamingRules")
    }

    private fun emptyTable(db: JdbcTemplate, table: String) {
        db.update("DELETE FROM $table")
    }
}