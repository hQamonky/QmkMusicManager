package com.qmk.musicmanager.controller.route

import com.qmk.musicmanager.database.dao.*
import com.qmk.musicmanager.database.model.DatabaseFactory
import com.qmk.musicmanager.domain.manager.*
import com.qmk.musicmanager.domain.model.Settings
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SystemRoutesKtTest {
    private val configurationManager = ConfigurationManager()
    private lateinit var namingRuleDAO: NamingRuleDAO
    private lateinit var playlistManager: PlaylistManager
    private lateinit var dataManager: DataManager

    @Before
    fun setUp() {
        DatabaseFactory.init()
        configurationManager.setConfiguration(Settings(
            audioFolder = "src/test/MusicTestDir/Audio",
            playlistsFolder = "src/test/MusicTestDir/Playlists",
            archiveFolder = "src/test/MusicTestDir/Archive"
        ))
        val playlistDAO = PlaylistDAOImpl()
        val platformPlaylistDAO = PlatformPlaylistDAOImpl()
        val musicDAO = MusicDAOImpl()
        val uploaderDAO = UploaderDAOImpl()
        val mopidyManager = MopidyManager(configurationManager)
        val powerAmpManager = PowerAmpManager(configurationManager)
        namingRuleDAO = NamingRuleDAOImpl()
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
    fun factoryResetRoute() = testApplication {
        val response = client.post("/api/factory-reset")
        assertEquals(HttpStatusCode.OK, response.status)
        val namingRules = namingRuleDAO.allNamingRules()
        assert(namingRules.isNotEmpty())
        val playlists = playlistManager.getPlaylists()
        assert(playlists.isNotEmpty())
    }

    @Test
    fun migrateMetadataRoute() = testApplication {
        val response = client.post("/api/migrate-metadata")
        assertEquals(HttpStatusCode.OK, response.status)
    }
}