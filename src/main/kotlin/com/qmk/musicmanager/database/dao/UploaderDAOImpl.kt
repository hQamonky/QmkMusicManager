package com.qmk.musicmanager.database.dao

import com.qmk.musicmanager.database.model.DatabaseFactory.dbQuery
import com.qmk.musicmanager.database.model.Uploaders
import com.qmk.musicmanager.domain.model.NamingFormat
import com.qmk.musicmanager.domain.model.Uploader
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class UploaderDAOImpl : UploaderDAO {
    private fun resultRowToUploader(row: ResultRow) = Uploader(
        id = row[Uploaders.id],
        name = row[Uploaders.name],
        namingFormat = resultToNamingFormat(row),
        platform = row[Uploaders.platform]
    )

    private fun resultToNamingFormat(row: ResultRow) = NamingFormat(
        separator = row[Uploaders.separator],
        artistBeforeTitle = row[Uploaders.isArtistBeforeTitle]
    )

    override suspend fun allUploaders(): List<Uploader> = dbQuery {
        Uploaders.selectAll().map(::resultRowToUploader)
    }

    override suspend fun uploader(id: String): Uploader? = dbQuery {
        Uploaders
            .select { Uploaders.id eq id }
            .map(::resultRowToUploader)
            .singleOrNull()
    }

    override suspend fun addNewUploader(
        id: String,
        name: String,
        namingFormat: NamingFormat,
        platform: String
    ): Uploader? = dbQuery {
        val insertStatement = Uploaders.insert {
            it[Uploaders.id] = id
            it[Uploaders.name] = name
            it[separator] = namingFormat.separator
            it[isArtistBeforeTitle] = namingFormat.artistBeforeTitle
            it[Uploaders.platform] = platform
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToUploader)
    }

    override suspend fun editUploader(id: String, namingFormat: NamingFormat): Boolean = dbQuery {
        Uploaders.update({ Uploaders.id eq id }) {
            it[separator] = namingFormat.separator
            it[isArtistBeforeTitle] = namingFormat.artistBeforeTitle
        } > 0
    }

    override suspend fun deleteUploader(id: String): Boolean = dbQuery {
        Uploaders.deleteWhere { Uploaders.id eq id } > 0
    }

    override suspend fun deleteAllUploaders(): Boolean = dbQuery {
        Uploaders.deleteAll() > 0
    }
}