package com.qmk.musicmanager.database.dao

import com.qmk.musicmanager.database.model.DatabaseFactory.dbQuery
import com.qmk.musicmanager.database.model.MusicTag
import com.qmk.musicmanager.database.model.Tags
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class TagDAOImpl : TagDAO {
    private fun resultRowToTag(row: ResultRow) = row[Tags.value]

    val tagDao: TagDAOImpl = TagDAOImpl()

    override suspend fun allTags(): List<String> = dbQuery {
        Tags.selectAll().map { resultRowToTag(it) }
    }

    override suspend fun renameTag(oldName: String, newName: String): Boolean = dbQuery {
        MusicTag.update({ MusicTag.tag eq oldName }) { it[MusicTag.tag] = newName }
        Tags.update({ Tags.value eq oldName }) { it[Tags.value] = newName } > 0
    }

    override suspend fun deleteTag(tag: String): Boolean = dbQuery {
        MusicTag.deleteWhere { MusicTag.tag eq tag }
        Tags.deleteWhere { Tags.value eq tag } >  0
    }

    override suspend fun deleteAllTags(): Boolean = dbQuery {
        MusicTag.deleteAll()
        Tags.deleteAll() > 0
    }
}