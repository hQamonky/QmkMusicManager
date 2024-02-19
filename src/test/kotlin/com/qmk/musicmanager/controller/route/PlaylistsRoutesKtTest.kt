package com.qmk.musicmanager.controller.route

import com.google.gson.Gson
import com.qmk.musicmanager.database.dao.*
import com.qmk.musicmanager.database.model.DatabaseFactory
import com.qmk.musicmanager.domain.manager.*
import com.qmk.musicmanager.domain.model.Settings
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class PlaylistsRoutesKtTest {
    private val route = "/api/playlists"
    private val configurationManager = ConfigurationManager()
    private lateinit var playlistManager: PlaylistManager
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
    fun `Add external music to playlists`() = testApplication {
        File("src/test/MusicTestDir/01-sum_41-underclass_hero.mp3")
            .copyTo(File("src/test/MusicTestDir/Audio/01-sum_41-underclass_hero.mp3"), true)
        File("src/test/MusicTestDir/07-sum_41-march_of_the_dogs.mp3")
            .copyTo(File("src/test/MusicTestDir/Audio/07-sum_41-march_of_the_dogs.mp3"), true)
        File("src/test/MusicTestDir/10-sum_41-pull_the_curtain.mp3")
            .copyTo(File("src/test/MusicTestDir/Audio/10-sum_41-pull_the_curtain.mp3"), true)
        val response = client.post("$route/add-external-files") {
            contentType(ContentType.Application.Json)
            setBody("[\"Punk Rock\", \"Chill\"]")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val playlists = playlistManager.getPlaylists().map{ it.name }
        assertEquals(2, playlists.size)
        assert(playlists.contains("Chill"))
        assert(playlists.contains("Punk Rock"))
    }
}