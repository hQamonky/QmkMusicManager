package com.qmk.musicmanager.database.dao

import com.qmk.musicmanager.database.model.DatabaseFactory.dbQuery
import com.qmk.musicmanager.database.model.NamingRules
import com.qmk.musicmanager.domain.model.NamingRule
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class NamingRuleDAOImpl : NamingRuleDAO {
    private fun resultRowToNamingRule(row: ResultRow) = NamingRule(
        id = row[NamingRules.id],
        replace = row[NamingRules.replace],
        replaceBy = row[NamingRules.replaceBy],
        priority = row[NamingRules.priority]
    )

    override suspend fun allNamingRules(): List<NamingRule> = dbQuery {
        NamingRules.selectAll().map(::resultRowToNamingRule)
    }

    override suspend fun namingRule(id: Int): NamingRule? = dbQuery {
        NamingRules
            .select { NamingRules.id eq id }
            .map(::resultRowToNamingRule)
            .singleOrNull()
    }

    override suspend fun addNewNamingRule(replace: String, replaceBy: String, priority: Int): NamingRule? = dbQuery {
        val insertStatement = NamingRules.insert {
            it[NamingRules.replace] = replace
            it[NamingRules.replaceBy] = replaceBy
            it[NamingRules.priority] = priority
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToNamingRule)
    }

    override suspend fun editNamingRule(id: Int, replace: String, replaceBy: String, priority: Int): Boolean =
        dbQuery {
            NamingRules.update({ NamingRules.id eq id }) {
                it[NamingRules.replace] = replace
                it[NamingRules.replaceBy] = replaceBy
                it[NamingRules.priority] = priority
            } > 0
        }

    override suspend fun deleteNamingRule(id: Int): Boolean = dbQuery {
        NamingRules.deleteWhere { NamingRules.id eq id } > 0
    }

    override suspend fun deleteAllNamingRules(): Boolean = dbQuery {
        NamingRules.deleteAll() > 0
    }
}