package com.qmk.musicmanager.database.model

import org.jetbrains.exposed.sql.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.transactions.*
import org.jetbrains.exposed.sql.transactions.experimental.*

object DatabaseFactory {
    fun init() {
        val driverClassName = "org.h2.Driver"
        val jdbcURL = "jdbc:h2:file:./build/db"
        val database = Database.connect(jdbcURL, driverClassName)
        transaction(database) {
            SchemaUtils.create(Playlists)
            SchemaUtils.create(Music)
            SchemaUtils.create(PlaylistMusic)
            SchemaUtils.create(Uploaders)
            SchemaUtils.create(NamingRules)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

object Playlists : Table() {
    val id = varchar("id", 128)
    val name = varchar("name", 128)

    override val primaryKey = PrimaryKey(id)
}

object Music : Table() {
    val id = varchar("id", 128)
    val fileName = varchar("file_name", 1024)
    val fileExtension = varchar("file_extension", 128)
    val title = varchar("title", 128)
    val artist = varchar("artist", 128)
    val uploader = varchar("uploader", 128)
    val uploadDate = varchar("upload_date", 128)
    val isNew = bool("is_new")

    override val primaryKey = PrimaryKey(id)
}

object PlaylistMusic : Table() {
    val id = integer("id").autoIncrement()
    val playlistId = varchar("playlist_id", 128)
    val musicId = varchar("music_id", 128)

    override val primaryKey = PrimaryKey(id)
}

object Uploaders : Table() {
    val id = varchar("id", 128)
    val name = varchar("name", 128)
    val separator = varchar("separator", 128)
    val isArtistBeforeTitle = bool("artist_before_title")

    override val primaryKey = PrimaryKey(id)
}

object NamingRules : Table() {
    val id = integer("id").autoIncrement()
    val replace = varchar("replace", 128)
    val replaceBy = varchar("replaceBy", 128)
    val priority = integer("priority")

    override val primaryKey = PrimaryKey(id)
}