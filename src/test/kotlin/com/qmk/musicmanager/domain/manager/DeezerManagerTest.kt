package com.qmk.musicmanager.domain.manager

import com.qmk.musicmanager.database.dao.*
import com.qmk.musicmanager.database.model.DatabaseFactory
import com.qmk.musicmanager.domain.model.MusicInfo
import com.qmk.musicmanager.domain.model.NamingFormat
import com.qmk.musicmanager.domain.model.Settings
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.*

class DeezerManagerTest {
    private lateinit var configurationManager: ConfigurationManager
    private val manager = DeezerManager()
    private lateinit var dataManager: DataManager
    private lateinit var namingRuleDAO: NamingRuleDAO
    private lateinit var id3Manager: Id3Manager
    private lateinit var musicFile: File

    @Before
    fun setUp() {
        runBlocking {
            configurationManager = ConfigurationManager()
            configurationManager.setConfiguration(
                Settings(
                    audioFolder = "src/test/MusicTestDir/Audio",
                    playlistsFolder = "src/test/MusicTestDir/Playlists",
                    archiveFolder = "src/test/MusicTestDir/Archive"
                )
            )
            id3Manager = Id3Manager()
            DatabaseFactory.init()
            namingRuleDAO = NamingRuleDAOImpl()
            dataManager = DataManager(
                configurationManager,
                PlaylistDAOImpl(),
                PlatformPlaylistDAOImpl(),
                MusicDAOImpl(),
                namingRuleDAO,
                UploaderDAOImpl(),
                TagDAOImpl(),
                MopidyManager(configurationManager),
                PowerAmpManager(configurationManager)
            )
            dataManager.addDefaultNamingRules()
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            configurationManager.setConfiguration(Settings())
            dataManager.removeAllEntries()
        }
    }

    @Test
    fun `Search full metadata of Drugs from AllttA`() = runBlocking {
        musicFile = File("src/test/MusicTestDir/Audio/AllttA - Drugs.mp3")
        val duration = manager.getAudioDuration(musicFile)
        val result = manager.searchFullMetadata("Drugs", "AllttA", duration)
        assert(result != null)
        assert(result?.artist?.name?.lowercase(Locale.getDefault())?.contains("alltta") == true)
        assert(result?.title?.lowercase(Locale.getDefault())?.contains("drugs") == true)
    }

    @Test
    fun `Search full metadata of 1001 Arabian Nights (Lyrics) - ItaloBrothers, Chipz file`() = runBlocking {
        musicFile = File("src/test/MusicTestDir/Audio/1001 Arabian Nights (Lyrics) - ItaloBrothers, Chipz.mp3")
        val metadata = id3Manager.getMetadata(musicFile)
        val musicInfo = MusicInfo(
            metadata.comments?.source?.id ?: "",
            "1001 Arabian Nights (Lyrics) - ItaloBrothers, Chipz",
            metadata.comments?.source?.uploadDate ?: "",
            metadata.comments?.source?.uploader ?: "",
            metadata.comments?.source?.uploaderId ?: ""
        )
        val metadataFromYt = id3Manager.getMetadataFromYoutube(musicInfo, NamingFormat(), namingRuleDAO.allNamingRules())
        val duration = manager.getAudioDuration(musicFile)
        val result = manager.findFullMetadata(metadataFromYt.title, metadataFromYt.artist, duration)
        assert(result != null)
        assert(result?.artist?.lowercase(Locale.getDefault())?.contains("italobrothers") == true)
        assert(result?.title?.lowercase(Locale.getDefault())?.contains("1001 arabian nights") == true)
    }

    @Test
    fun `Search full metadata of 1001 Arabian Nights (Lyrics) - ItaloBrothers, Chipz text`() = runBlocking {
        val result = manager.findFullMetadata("ItaloBrothers, Chipz", "1001 Arabian Nights (Lyrics)", 175)
        assert(result != null)
        assert(result?.artist?.lowercase(Locale.getDefault())?.contains("italobrothers") == true)
        assert(result?.title?.lowercase(Locale.getDefault())?.contains("1001 arabian nights") == true)
    }

    @Test
    fun `Search full metadata of Aaron Ahrends - Focus (Nicone Remix) text`() = runBlocking {
        val result = manager.findFullMetadata("Focus (Niconé Remix)", "Aaron Ahrends", 462)
        assert(result != null)
        assert(result?.artist?.lowercase(Locale.getDefault())?.contains("aaron ahrends") == true)
        assert(result?.title?.lowercase(Locale.getDefault())?.contains("focus") == true)
    }

    @Test
    fun `Search full metadata of Aaron Ahrends - Focus Nicone Remix text`() = runBlocking {
        val result = manager.findFullMetadata("Focus Niconé Remix", "Aaron Ahrends", 462)
        assert(result != null)
        assert(result?.artist?.lowercase(Locale.getDefault())?.contains("aaron ahrends") == true)
        assert(result?.title?.lowercase(Locale.getDefault())?.contains("focus") == true)
    }

    @Test
    fun search() {
    }

    @Test
    fun advancedSearch() {
    }
}