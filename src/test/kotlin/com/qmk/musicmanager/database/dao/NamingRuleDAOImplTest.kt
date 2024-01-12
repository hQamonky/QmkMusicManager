package com.qmk.musicmanager.database.dao

import com.qmk.musicmanager.database.model.DatabaseFactory
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class NamingRuleDAOImplTest {

    private lateinit var namingRuleDAO: NamingRuleDAO

    @Before
    fun setUp() {
        DatabaseFactory.init()
        namingRuleDAO = NamingRuleDAOImpl()
    }

    @After
    fun tearDown() {
        runBlocking {
            namingRuleDAO.deleteAllNamingRules()
        }
    }

    @Test
    fun allNamingRules() = runTest {
        namingRuleDAO.addNewNamingRule(
            replace = " Lyrics",
            replaceBy = "",
            priority = 2
        )
        namingRuleDAO.addNewNamingRule(
            replace = " Official Video",
            replaceBy = "",
            priority = 2
        )
        val namingRules = namingRuleDAO.allNamingRules()
        assert(namingRules.size == 2)
    }

    @Test
    fun namingRule() = runTest {
        namingRuleDAO.addNewNamingRule(
            replace = " Lyrics",
            replaceBy = "",
            priority = 2
        )
        val namingRules = namingRuleDAO.allNamingRules()
        assert(namingRules.isNotEmpty())
        val namingRuleId = namingRules[0].id
        assert(namingRuleId != null)
        val namingRule = namingRuleDAO.namingRule(namingRuleId!!)
        assert(namingRule?.replace == " Lyrics")
        assert(namingRule?.replaceBy == "")
        assert(namingRule?.priority == 2)
    }

    @Test
    fun addNewNamingRule() = runTest {
        namingRuleDAO.addNewNamingRule(
            replace = " Official Video",
            replaceBy = "",
            priority = 3
        )
        val namingRules = namingRuleDAO.allNamingRules()
        assert(namingRules.isNotEmpty())
        val namingRuleId = namingRules[0].id
        assert(namingRuleId != null)
        val namingRule = namingRuleDAO.namingRule(namingRuleId!!)
        assert(namingRule?.replace == " Official Video")
        assert(namingRule?.replaceBy == "")
        assert(namingRule?.priority == 3)
    }

    @Test
    fun editNamingRule() = runTest {
        namingRuleDAO.addNewNamingRule(
            replace = " Lyrics",
            replaceBy = "",
            priority = 2
        )
        val namingRules = namingRuleDAO.allNamingRules()
        assert(namingRules.isNotEmpty())
        val namingRuleId = namingRules[0].id
        assert(namingRuleId != null)

        namingRuleDAO.editNamingRule(
            id = namingRuleId!!,
            replace = " Official Video",
            replaceBy = " OFFICIAL",
            priority = 1,
        )

        val namingRule = namingRuleDAO.namingRule(namingRuleId)
        assert(namingRule?.replace == " Official Video")
        assert(namingRule?.replaceBy == " OFFICIAL")
        assert(namingRule?.priority == 1)
    }

    @Test
    fun deleteNamingRule() = runTest {
        namingRuleDAO.addNewNamingRule(
            replace = " Lyrics",
            replaceBy = "",
            priority = 2
        )
        val namingRules = namingRuleDAO.allNamingRules()
        assert(namingRules.isNotEmpty())
        val namingRuleId = namingRules[0].id
        assert(namingRuleId != null)
        var namingRule = namingRuleDAO.namingRule(namingRuleId!!)
        assert(namingRule != null)

        namingRuleDAO.deleteNamingRule(namingRuleId)

        namingRule = namingRuleDAO.namingRule(namingRuleId)
        assert(namingRule == null)
    }

    @Test
    fun deleteAllNamingRules() = runTest {
        namingRuleDAO.addNewNamingRule(
            replace = " Lyrics",
            replaceBy = "",
            priority = 2
        )
        var namingRules = namingRuleDAO.allNamingRules()
        assert(namingRules.isNotEmpty())

        namingRuleDAO.deleteAllNamingRules()

        namingRules = namingRuleDAO.allNamingRules()
        assert(namingRules.isEmpty())
    }
}