package com.qmk.musicmanager.database.dao

import com.qmk.musicmanager.domain.model.NamingRule

interface NamingRuleDAO {
    suspend fun allNamingRules(): List<NamingRule>
    suspend fun namingRule(id: Int): NamingRule?
    suspend fun addNewNamingRule(replace: String, replaceBy: String, priority: Int): NamingRule?
    suspend fun editNamingRule(id: Int, replace: String, replaceBy: String, priority: Int): Boolean
    suspend fun deleteNamingRule(id: Int): Boolean

    suspend fun deleteAllNamingRules(): Boolean
}