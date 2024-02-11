package com.qmk.musicmanager.controller.route

import com.google.gson.Gson
import com.qmk.musicmanager.controller.model.BasicAPIResponse
import com.qmk.musicmanager.controller.model.GetNamingRule
import com.qmk.musicmanager.controller.model.GetNamingRules
import com.qmk.musicmanager.database.dao.*
import com.qmk.musicmanager.database.model.DatabaseFactory
import com.qmk.musicmanager.domain.manager.ConfigurationManager
import com.qmk.musicmanager.domain.manager.DataManager
import com.qmk.musicmanager.domain.manager.MopidyManager
import com.qmk.musicmanager.domain.manager.PowerAmpManager
import com.qmk.musicmanager.domain.model.NamingRule
import com.qmk.musicmanager.domain.model.Settings
import com.qmk.musicmanager.extension.fromJson
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NamingRulesRoutesKtTest {
    private val gson = Gson()
    private val route = "/api/naming-rules"
    private val configurationManager = ConfigurationManager()
    private val namingRuleDAO = NamingRuleDAOImpl()
    private lateinit var dataManager: DataManager

    @Before
    fun setUp() {
        DatabaseFactory.init()
        configurationManager.setConfiguration(
            Settings(
                audioFolder = "src/test/MusicTestDir/Audio",
                playlistsFolder = "src/test/MusicTestDir/Playlists",
                archiveFolder = "src/test/MusicTestDir/Archive"
            )
        )
        val playlistDAO = PlaylistDAOImpl()
        val platformPlaylistDAO = PlatformPlaylistDAOImpl()
        val musicDAO = MusicDAOImpl()
        val uploaderDAO = UploaderDAOImpl()
        val mopidyManager = MopidyManager(configurationManager)
        val powerAmpManager = PowerAmpManager(configurationManager)
        dataManager = DataManager(
            configurationManager,
            playlistDAO,
            platformPlaylistDAO,
            musicDAO,
            namingRuleDAO,
            uploaderDAO,
            TagDAOImpl(),
            mopidyManager,
            powerAmpManager
        )
    }

    @After
    fun tearDown() = runBlocking {
        dataManager.removeAllEntries()
        configurationManager.setConfiguration(Settings())
    }

    @Test
    fun getNamingRulesRoute() = testApplication {
        namingRuleDAO.addNewNamingRule(" Lyrics", "", 2)
        namingRuleDAO.addNewNamingRule(" Official Video", "", 2)

        val response = client.get(route)
        assertEquals(HttpStatusCode.OK, response.status)
        val body = gson.fromJson(response.bodyAsText(), BasicAPIResponse::class.java)
        assertEquals(true, body.successful)
        val serverResponse = body.message?.fromJson(GetNamingRules::class.java)
        val namingRules = serverResponse?.response?.fromJson(Array<NamingRule>::class.java)?.asList()
        assertEquals(2, namingRules?.size)
    }

    @Test
    fun postNamingRulesRoute() = testApplication {
        val response = client.post(route) {
            contentType(ContentType.Application.Json)
            setBody(
                gson.toJson(
                    NamingRule(
                        null,
                        " Lyrics",
                        "",
                        2
                    )
                )
            )
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val namingRules = namingRuleDAO.allNamingRules()
        assertEquals(1, namingRules.size)
        assert(namingRules.map { it.replace }.contains(" Lyrics"))
    }

    @Test
    fun getNamingRuleRoute() = testApplication {
        val initNamingRule = namingRuleDAO.addNewNamingRule(" Lyrics", "", 2)

        val response = client.get("$route/${initNamingRule?.id}")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = gson.fromJson(response.bodyAsText(), BasicAPIResponse::class.java)
        assertEquals(true, body.successful)
        val serverResponse = body.message?.fromJson(GetNamingRule::class.java)
        val namingRule = serverResponse?.response?.fromJson(NamingRule::class.java)
        assertEquals(initNamingRule, namingRule)
    }

    @Test
    fun postNamingRuleRoute() = testApplication {
        val initNamingRule = namingRuleDAO.addNewNamingRule(" Lyrics", "", 2)

        val response = client.post("$route/${initNamingRule?.id}") {
            contentType(ContentType.Application.Json)
            setBody(
                gson.toJson(
                    initNamingRule?.copy(
                        replace = " LYRIC",
                        replaceBy = " - ",
                        priority = 1
                    )
                )
            )
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val namingRule = namingRuleDAO.namingRule(initNamingRule!!.id!!)
        assertEquals(" LYRIC", namingRule?.replace)
        assertEquals(" - ", namingRule?.replaceBy)
        assertEquals(1, namingRule?.priority)
    }

    @Test
    fun deleteNamingRuleRoute() = testApplication {
        val initNamingRule = namingRuleDAO.addNewNamingRule(" Lyrics", "", 2)

        val response = client.delete("$route/${initNamingRule?.id}")
        assertEquals(HttpStatusCode.OK, response.status)
        val namingRule = namingRuleDAO.namingRule(initNamingRule!!.id!!)
        assertEquals(null, namingRule)
    }
}