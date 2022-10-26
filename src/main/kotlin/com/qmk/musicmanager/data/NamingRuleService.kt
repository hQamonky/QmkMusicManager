package com.qmk.musicmanager.data

import com.qmk.musicmanager.model.NamingRule
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.query
import org.springframework.stereotype.Service
import java.util.*

@Service
class NamingRuleService(val db: JdbcTemplate) {
    fun find(): List<NamingRule> = selectAll(db)

    fun findById(id: String): List<NamingRule> = select(db, id)

    fun new(namingRule: NamingRule) {
        insert(db, UUID.randomUUID().toString(), namingRule.replace, namingRule.replaceBy, namingRule.priority)
    }

    fun save(namingRule: NamingRule) {
        val id = namingRule.id ?: UUID.randomUUID().toString()
        update(db, id, namingRule.replace, namingRule.replaceBy, namingRule.priority)
    }

    fun remove(id: String) {
        delete(db, id)
    }

    // --- Requests ---

    private fun selectAll(db: JdbcTemplate): List<NamingRule> =
        db.query("SELECT * FROM NamingRules ORDER BY priority") { response, _ ->
            NamingRule(
                response.getString("id"),
                response.getString("replace"),
                response.getString("replace_by"),
                response.getInt("priority")
            )
        }

    private fun select(db: JdbcTemplate, identifier: String): List<NamingRule> =
        db.query("SELECT * FROM NamingRules WHERE id = ?", identifier) { response, _ ->
            NamingRule(
                response.getString("id"),
                response.getString("replace"),
                response.getString("replace_by"),
                response.getInt("priority")
            )
        }

    private fun insert(db: JdbcTemplate, id: String, replace: String, replace_by: String, priority: Int): Int =
        db.update("INSERT INTO NamingRules (id, replace, replace_by, priority) VALUES (?, ?, ?, ?)",
            id, replace, replace_by, priority)

    private fun update(db: JdbcTemplate, identifier: String, replace: String, replace_by: String, priority: Int): Int =
        db.update(
            "UPDATE NamingRules SET " +
                    "replace = ?, " +
                    "replace_by = ?, " +
                    "priority = ? " +
                    "WHERE " +
                    "id = ?",
            replace, replace_by, priority, identifier
        )

    private fun delete(db: JdbcTemplate, identifier: String): Int =
        db.update("DELETE FROM NamingRules WHERE id = ?", identifier)
}

