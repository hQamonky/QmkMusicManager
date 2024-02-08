package com.qmk.musicmanager.controller.route

import com.google.gson.Gson
import com.qmk.musicmanager.domain.manager.ConfigurationManager
import com.qmk.musicmanager.domain.model.Settings
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SettingsRoutesKtTest {
    private val route = "/api/settings"
    private lateinit var configurationManager: ConfigurationManager

    @Before
    fun setUp() {
        configurationManager = ConfigurationManager()
        configurationManager.setConfiguration(Settings())
    }

    @After
    fun tearDown() {
        configurationManager.setConfiguration(Settings())
    }

    @Test
    fun getSettingsRoutes() = testApplication {
        val response = client.get(route)
        assertEquals(HttpStatusCode.OK, response.status)
        val responseJson = Gson().fromJson(response.bodyAsText(), Settings::class.java)
        assertEquals(Settings().toJson(), responseJson.toJson())
    }

    @Test
    fun postSettingsRoutes() = testApplication {
        val response = client.post(route) {
            contentType(ContentType.Application.Json)
            setBody(Settings(autoDownload = false).toJson())
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val settings = configurationManager.getConfiguration()
        assertEquals(false, settings.autoDownload)
    }

    @Test
    fun postAudioFolder() = testApplication {
        val response = client.post("$route/audio-folder") {
            contentType(ContentType.Application.Json)
            setBody("My/audio")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val settings = configurationManager.getConfiguration()
        assertEquals("My/audio", settings.audioFolder)
    }

    @Test
    fun postPlaylistsFolder() = testApplication {
        val response = client.post("$route/playlists-folder") {
            contentType(ContentType.Application.Json)
            setBody("My/playlists")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val settings = configurationManager.getConfiguration()
        assertEquals("My/playlists", settings.playlistsFolder)
    }

    @Test
    fun postArchiveFolder() = testApplication {
        val response = client.post("$route/archive-folder") {
            contentType(ContentType.Application.Json)
            setBody("My/archive")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val settings = configurationManager.getConfiguration()
        assertEquals("My/archive", settings.archiveFolder)
    }

    @Test
    fun postAudioFormat() = testApplication {
        val response = client.post("$route/audio-format") {
            contentType(ContentType.Application.Json)
            setBody("wav")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val settings = configurationManager.getConfiguration()
        assertEquals("wav", settings.audioFormat)
    }

    @Test
    fun postDownloadOccurrence() = testApplication {
        val response = client.post("$route/download-occurrence") {
            contentType(ContentType.Application.Json)
            setBody("12")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val settings = configurationManager.getConfiguration()
        assertEquals(12, settings.downloadOccurrence)
    }

    @Test
    fun postAutoDownload() = testApplication {
        val response = client.post("$route/auto-download") {
            contentType(ContentType.Application.Json)
            setBody("false")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val settings = configurationManager.getConfiguration()
        assertEquals(false, settings.autoDownload)
    }
}