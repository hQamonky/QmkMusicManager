package com.qmk.musicmanager.domain.manager

import com.qmk.musicmanager.domain.model.Settings
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import java.io.File

class AccoustIDManagerTest {
    private lateinit var configurationManager: ConfigurationManager
    private lateinit var manager: AccoustIDManager
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
        manager = AccoustIDManager(configurationManager)
        musicFile = File("src/test/MusicTestDir/Audio/AllttA - Drugs.mp3")
    }

    @After
    fun tearDown() {
        configurationManager.setConfiguration(Settings())
    }

    @Test
    fun searchInfo() = runBlocking {
        val result = manager.searchInfo(musicFile)
        assert(result != null)
    }
}