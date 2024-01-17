package com.qmk.musicmanager.domain.manager

import com.qmk.musicmanager.database.model.DatabaseFactory
import com.qmk.musicmanager.domain.model.Settings
import org.junit.After
import org.junit.Test

import org.junit.Before

class ConfigurationManagerTest {

    private lateinit var confManager: ConfigurationManager

    @Before
    fun setUp() {
        DatabaseFactory.init()
        confManager = ConfigurationManager()
    }

    @After
    fun tearDown() {
        confManager.setConfiguration(Settings())
    }

    @Test
    fun getConfiguration() {
        val conf = confManager.getConfiguration()
        assert(conf.autoDownload)
        assert(conf.downloadOccurrence == 60)
        assert(conf.audioFolder == "./Music/Audio")
        assert(conf.playlistsFolder == "./Music/Playlists")
        assert(conf.archiveFolder == "./Music/Archive")
        assert(conf.audioFormat == "mp3")
    }

    @Test
    fun setConfiguration() {
        var conf = confManager.getConfiguration()
        assert(conf.autoDownload)
        assert(conf.downloadOccurrence == 60)
        assert(conf.audioFolder == "./Music/Audio")
        assert(conf.playlistsFolder == "./Music/Playlists")
        assert(conf.archiveFolder == "./Music/Archive")
        assert(conf.audioFormat == "mp3")
        confManager.setConfiguration(
            Settings(
                false,
                100,
                "Audio",
                "Playlists",
                "Archive",
                "wav",
                ""
            )
        )
        conf = confManager.getConfiguration()
        assert(!conf.autoDownload)
        assert(conf.downloadOccurrence == 100)
        assert(conf.audioFolder == "Audio")
        assert(conf.playlistsFolder == "Playlists")
        assert(conf.archiveFolder == "Archive")
        assert(conf.audioFormat == "wav")
    }

    @Test
    fun setAudioFolder() {
        var conf = confManager.getConfiguration()
        assert(conf.audioFolder == "./Music/Audio")
        confManager.setAudioFolder("new/Audio/folder")
        conf = confManager.getConfiguration()
        assert(conf.audioFolder == "new/Audio/folder")
    }

    @Test
    fun setPlaylistsFolder() {
        var conf = confManager.getConfiguration()
        assert(conf.playlistsFolder == "./Music/Playlists")
        confManager.setPlaylistsFolder("new/Playlists/folder")
        conf = confManager.getConfiguration()
        assert(conf.playlistsFolder == "new/Playlists/folder")
    }

    @Test
    fun setArchiveFolder() {
        var conf = confManager.getConfiguration()
        assert(conf.archiveFolder == "./Music/Archive")
        confManager.setArchiveFolder("new/Archive/folder")
        conf = confManager.getConfiguration()
        assert(conf.archiveFolder == "new/Archive/folder")
    }

    @Test
    fun setAudioFormat() {
        var conf = confManager.getConfiguration()
        assert(conf.audioFormat == "mp3")
        confManager.setAudioFormat("wav")
        conf = confManager.getConfiguration()
        assert(conf.audioFormat == "wav")
    }

    @Test
    fun setDownloadOccurrence() {
        var conf = confManager.getConfiguration()
        assert(conf.downloadOccurrence == 60)
        confManager.setDownloadOccurrence(100)
        conf = confManager.getConfiguration()
        assert(conf.downloadOccurrence == 100)
    }

    @Test
    fun setAutoDownload() {
        var conf = confManager.getConfiguration()
        assert(conf.autoDownload)
        confManager.setAutoDownload(false)
        conf = confManager.getConfiguration()
        assert(!conf.autoDownload)
    }
}