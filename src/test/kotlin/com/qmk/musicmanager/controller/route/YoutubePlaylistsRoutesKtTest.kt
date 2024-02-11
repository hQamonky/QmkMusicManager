package com.qmk.musicmanager.controller.route

import com.google.gson.Gson
import com.qmk.musicmanager.controller.model.BasicAPIResponse
import com.qmk.musicmanager.controller.model.GetYoutubePlaylist
import com.qmk.musicmanager.controller.model.GetYoutubePlaylists
import com.qmk.musicmanager.database.dao.*
import com.qmk.musicmanager.database.model.DatabaseFactory
import com.qmk.musicmanager.domain.manager.*
import com.qmk.musicmanager.domain.model.PlatformPlaylist
import com.qmk.musicmanager.domain.model.PlaylistEntry
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

class YoutubePlaylistsRoutesKtTest {
    private val route = "/api/playlists/youtube"
    private val configurationManager = ConfigurationManager()
    private lateinit var playlistManager: PlaylistManager
    private lateinit var dataManager: DataManager
    private val gson = Gson()

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
        val namingRuleDAO = NamingRuleDAOImpl()
        val uploaderDAO = UploaderDAOImpl()
        val mopidyManager = MopidyManager(configurationManager)
        val powerAmpManager = PowerAmpManager(configurationManager)
        playlistManager = PlaylistManager(
            playlistDAO,
            platformPlaylistDAO,
            musicDAO,
            uploaderDAO,
            namingRuleDAO,
            YoutubeManager(),
            configurationManager,
            Id3Manager(),
            mopidyManager,
            powerAmpManager,
            DeezerManager()
        )
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
    fun `Get YouTube playlists`() = testApplication {
        playlistManager.createYoutubePlaylist(
            "https://www.youtube.com/playlist?list=PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl",
            listOf("Casual", "Chill")
        )
        playlistManager.createYoutubePlaylist(
            "https://www.youtube.com/playlist?list=PLCVGGn6GhhDvZ0iCVONTYnzkjW3ZA6jwf",
            listOf("Casual", "Best of WilliTracks")
        )
        val response = client.get(route)
        assertEquals(HttpStatusCode.OK, response.status)
        val body = gson.fromJson(response.bodyAsText(), BasicAPIResponse::class.java)
        assertEquals(true, body.successful)
        val serverResponse = body.message?.fromJson(GetYoutubePlaylists::class.java)
        val playlists = serverResponse?.response?.fromJson(Array<PlatformPlaylist>::class.java)?.asList()
        assertEquals(2, playlists?.size)
    }

    @Test
    fun `Add YouTube playlists`() = testApplication {
        val playlistEntry = PlaylistEntry(
            url = "https://www.youtube.com/playlist?list=PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl",
            platform = "youtube",
            playlists = listOf("Casual", "Chill")
        )
        val response = client.post(route) {
            contentType(ContentType.Application.Json)
            setBody(gson.toJson(playlistEntry))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val playlists = playlistManager.getPlaylists().map{ it.name }
        assertEquals(2, playlists.size)
        assert(playlists.contains("Casual"))
        assert(playlists.contains("Chill"))
        val ytPlaylist = playlistManager.getYoutubePlaylist("PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl")
        assert(ytPlaylist != null)
    }

    @Test
    fun `Get YouTube playlist`() = testApplication {
        playlistManager.createYoutubePlaylist(
            "https://www.youtube.com/playlist?list=PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl",
            listOf("Casual", "Chill")
        )
        val response = client.get("$route/PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = gson.fromJson(response.bodyAsText(), BasicAPIResponse::class.java)
        assertEquals(true, body.successful)
        val serverResponse = body.message?.fromJson(GetYoutubePlaylist::class.java)
        val playlist = serverResponse?.response?.fromJson(PlatformPlaylist::class.java)
        assertEquals("PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl", playlist?.id)
    }

    @Test
    fun `Edit YouTube playlist`() = testApplication {
        playlistManager.createYoutubePlaylist(
            "https://www.youtube.com/playlist?list=PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl",
            listOf("Casual", "Chill")
        )

        val editedPlaylist = PlatformPlaylist(
            id = "PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl",
            name = "test",
            platform = "youtube",
            playlists = listOf("Best of WilliTracks")
        )
        val response = client.post("$route/PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl") {
            contentType(ContentType.Application.Json)
            setBody(gson.toJson(editedPlaylist))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val ytPlaylist = playlistManager.getYoutubePlaylist("PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl")
        assert(ytPlaylist != null)
        assertEquals(listOf("Best of WilliTracks"), ytPlaylist?.playlists)
    }

    @Test
    fun `Delete YouTube playlist`() = testApplication {
        playlistManager.createYoutubePlaylist(
            "https://www.youtube.com/playlist?list=PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl",
            listOf("Casual", "Chill")
        )

        val response = client.delete("$route/PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl")
        assertEquals(HttpStatusCode.OK, response.status)
        val ytPlaylist = playlistManager.getYoutubePlaylist("PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl")
        assert(ytPlaylist == null)
    }
}