package com.qmk.musicmanager.domain.manager

import com.qmk.musicmanager.api.MusicBrainzAPI
import com.qmk.musicmanager.domain.model.Settings
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import java.io.File
import java.util.*

class DeezerManagerTest {
    private lateinit var configurationManager: ConfigurationManager
    private val manager = DeezerManager()
    private lateinit var musicFile: File

    @Before
    fun setUp() {
        configurationManager = ConfigurationManager()
        configurationManager.setConfiguration(
            Settings(
                audioFolder = "src/test/MusicTestDir/Audio",
                playlistsFolder = "src/test/MusicTestDir/Playlists",
                archiveFolder = "src/test/MusicTestDir/Archive"
            )
        )
        musicFile = File("src/test/MusicTestDir/Audio/AllttA - Drugs.mp3")
    }

    @After
    fun tearDown() {
        configurationManager.setConfiguration(Settings())
    }

    @Test
    fun searchFullMetadata() = runBlocking {
        val duration = manager.getAudioDuration(musicFile)
        val result = manager.searchFullMetadata("Drugs", "AllttA", duration)
        assert(result != null)
        assert(result?.artist?.name?.lowercase(Locale.getDefault())?.contains("alltta") == true)
        assert(result?.title?.lowercase(Locale.getDefault())?.contains("drugs") == true)
    }

    @Test
    fun search() {
    }

    @Test
    fun advancedSearch() {
    }
}