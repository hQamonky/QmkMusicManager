package com.qmk.musicmanager.data

import com.qmk.musicmanager.model.NamingRule
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class NamingRuleService(val db: JdbcTemplate) {
    fun find(): List<NamingRule> = selectAll(db)

    fun findById(id: Int): List<NamingRule> = select(db, id)

    fun new(namingRule: NamingRule) {
        insert(db, namingRule.replace, namingRule.replaceBy, namingRule.priority)
    }

    fun save(namingRule: NamingRule) {
        update(db, namingRule.id, namingRule.replace, namingRule.replaceBy, namingRule.priority)
    }

    fun remove(id: Int) {
        delete(db, id)
    }

    // --- Requests ---

    private fun selectAll(db: JdbcTemplate): List<NamingRule> =
        db.query("SELECT * FROM NamingRules ORDER BY priority") { response, _ ->
            NamingRule(
                response.getInt("id"),
                response.getString("replace"),
                response.getString("replace_by"),
                response.getInt("priority")
            )
        }

    private fun select(db: JdbcTemplate, identifier: Int): List<NamingRule> =
        db.query("SELECT * FROM NamingRules WHERE id = $identifier") { response, _ ->
            NamingRule(
                response.getInt("id"),
                response.getString("replace"),
                response.getString("replace_by"),
                response.getInt("priority")
            )
        }

    private fun insert(db: JdbcTemplate, replace: String, replace_by: String, priority: Int): Int =
        db.update("INSERT INTO NamingRules (replace, replace_by, priority) VALUES ($replace, $replace_by, $priority)")

    private fun update(db: JdbcTemplate, identifier: Int, replace: String, replace_by: String, priority: Int): Int =
        db.update(
            "UPDATE NamingRules SET " +
                    "replace = $replace, " +
                    "replace_by = $replace_by, " +
                    "priority = $priority " +
                    "WHERE " +
                    "id = $identifier"
        )

    private fun delete(db: JdbcTemplate, identifier: Int): Int =
        db.update("DELETE FROM NamingRules WHERE id = $identifier")
}

