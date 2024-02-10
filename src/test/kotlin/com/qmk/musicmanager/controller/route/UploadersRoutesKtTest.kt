package com.qmk.musicmanager.controller.route

import com.google.gson.Gson
import com.qmk.musicmanager.controller.model.BasicAPIResponse
import com.qmk.musicmanager.database.dao.*
import com.qmk.musicmanager.database.model.DatabaseFactory
import com.qmk.musicmanager.domain.manager.ConfigurationManager
import com.qmk.musicmanager.domain.manager.DataManager
import com.qmk.musicmanager.domain.manager.MopidyManager
import com.qmk.musicmanager.domain.manager.PowerAmpManager
import com.qmk.musicmanager.domain.model.NamingFormat
import com.qmk.musicmanager.domain.model.Settings
import com.qmk.musicmanager.domain.model.Uploader
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test

class UploadersRoutesKtTest {
    private val gson = Gson()
    private val route = "/api/uploaders"
    private val configurationManager = ConfigurationManager()
    private val uploaderDAO = UploaderDAOImpl()
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
        val mopidyManager = MopidyManager(configurationManager)
        val powerAmpManager = PowerAmpManager(configurationManager)
        dataManager = DataManager(
            configurationManager,
            playlistDAO,
            platformPlaylistDAO,
            musicDAO,
            NamingRuleDAOImpl(),
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
    fun getUploadersRoute() = testApplication {
        uploaderDAO.addNewUploader(
            "channel1Id",
            "channel1Name",
            NamingFormat(),
            "youtube"
        )
        uploaderDAO.addNewUploader(
            "channel2Id",
            "channel2Name",
            NamingFormat(),
            "youtube"
        )

        val response = client.get(route)
        assertEquals(HttpStatusCode.OK, response.status)
        val body = gson.fromJson(response.bodyAsText(), BasicAPIResponse::class.java)
        assertEquals(true, body.successful)
        val uploaders = gson.fromJson(body.message, Array<Uploader>::class.java).asList()
        assertEquals(2, uploaders.size)
    }

    @Test
    fun getUploaderRoute() = testApplication {
        uploaderDAO.addNewUploader(
            "channel1Id",
            "channel1Name",
            NamingFormat(),
            "youtube"
        )

        val response = client.get("$route/channel1Id")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = gson.fromJson(response.bodyAsText(), BasicAPIResponse::class.java)
        assertEquals(true, body.successful)
        val uploader = gson.fromJson(body.message, Uploader::class.java)
        assertEquals("channel1Name", uploader.name)
    }

    @Test
    fun postUploaderRoute() = testApplication {
        val initUploader = Uploader(
            "channel1Id",
            "channel1Name",
            NamingFormat(),
            "youtube"
        )
        uploaderDAO.addNewUploader(
            "channel1Id",
            "channel1Name",
            NamingFormat(),
            "youtube"
        )

        val response = client.post("$route/channel1Id") {
            contentType(ContentType.Application.Json)
            setBody(
                gson.toJson(
                    NamingFormat(
                        separator = " / ",
                        artistBeforeTitle = false
                    )
                )
            )
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val namingFormat = uploaderDAO.uploader(initUploader.id)?.namingFormat
        assertEquals(" / ", namingFormat?.separator)
        assertEquals(false, namingFormat?.artistBeforeTitle)
    }
}