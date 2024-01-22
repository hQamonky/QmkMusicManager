package com.qmk.musicmanager.domain.manager

import com.qmk.musicmanager.database.dao.*
import com.qmk.musicmanager.database.model.DatabaseFactory
import com.qmk.musicmanager.domain.model.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

class DataManagerTest {
    private lateinit var manager: DataManager

    private lateinit var playlistDAO: PlaylistDAO
    private lateinit var platformPlaylistDAO: PlatformPlaylistDAO
    private lateinit var musicDAO: MusicDAO
    private lateinit var namingRuleDAO: NamingRuleDAO
    private lateinit var uploaderDAO: UploaderDAO
    private lateinit var tagDAO: TagDAO

    private lateinit var configurationManager: ConfigurationManager
    private lateinit var id3Manager: Id3Manager
    private lateinit var musicFile1: File
    private lateinit var musicFile2: File
    private lateinit var mopidyManager: MopidyManager
    private lateinit var powerAmpManager: PowerAmpManager

    @Before
    fun setUp() {
        DatabaseFactory.init()
        playlistDAO = PlaylistDAOImpl()
        platformPlaylistDAO = PlatformPlaylistDAOImpl()
        musicDAO = MusicDAOImpl()
        namingRuleDAO = NamingRuleDAOImpl()
        uploaderDAO = UploaderDAOImpl()
        tagDAO = TagDAOImpl()

        configurationManager = ConfigurationManager()
        configurationManager.setConfiguration(
            Settings(
                audioFolder = "src/test/MusicTestDir/Audio",
                playlistsFolder = "src/test/MusicTestDir/Playlists",
                archiveFolder = "src/test/MusicTestDir/Archive"
            )
        )
        id3Manager = Id3Manager()
        val initialFile = File("src/test/MusicTestDir/Audio/Cara yeaaah VFWJd69f9F0.mp3")
        musicFile1 = File("src/test/MusicTestDir/Audio/test - music 1.mp3")
        initialFile.copyTo(musicFile1, true)
        musicFile2 = File("src/test/MusicTestDir/Audio/test - music 2.mp3")
        initialFile.copyTo(musicFile2, true)

        mopidyManager = MopidyManager()
        powerAmpManager = PowerAmpManager()

        manager = DataManager(
            configurationManager,
            playlistDAO,
            platformPlaylistDAO,
            musicDAO,
            namingRuleDAO,
            uploaderDAO,
            tagDAO,
            mopidyManager,
            powerAmpManager
        )
    }

    @After
    fun tearDown() {
        configurationManager.setConfiguration(Settings())
        File("src/test/MusicTestDir/Playlists").deleteRecursively()
        musicFile1.delete()
        musicFile2.delete()

        runBlocking {
            playlistDAO.deleteAllPlaylists()
            platformPlaylistDAO.deleteAllPlaylists()
            musicDAO.deleteAllMusic()
            namingRuleDAO.deleteAllNamingRules()
            uploaderDAO.deleteAllUploaders()
            tagDAO.deleteAllTags()
        }
    }

    @Test
    fun removeAllEntries() = runTest {
        playlistDAO.addNewPlaylist("Casual")
        platformPlaylistDAO.addNewPlaylist(
            "youtubeId",
            "Best of WilliTracks 2024 part 1",
            "youtube",
            listOf("Casual")
        )
        musicDAO.addNewMusic(
            "test - music",
            "mp3",
            "music",
            "test",
            "musicId",
            "channelId",
            "date",
            listOf("tag"),
            true
        )
        namingRuleDAO.addNewNamingRule(replace = " / ", replaceBy = " ", priority = 1)
        uploaderDAO.addNewUploader(
            "channelId",
            "Channel Name",
            NamingFormat(),
            "youtube"
        )

        manager.removeAllEntries()

        val playlists = playlistDAO.allPlaylists()
        assert(playlists.isEmpty())
        val plPlaylists = platformPlaylistDAO.allPlaylists()
        assert(plPlaylists.isEmpty())
        val music = musicDAO.allMusic()
        assert(music.isEmpty())
        val namingRules = namingRuleDAO.allNamingRules()
        assert(namingRules.isEmpty())
        val tags = tagDAO.allTags()
        assert(tags.isEmpty())
    }

    @Test
    fun addDefaultNamingRules() = runTest {
        var namingRules = namingRuleDAO.allNamingRules()
        assert(namingRules.isEmpty())

        manager.addDefaultNamingRules()
        namingRules = namingRuleDAO.allNamingRules()
        assert(namingRules.isNotEmpty())
    }

    @Test
    fun addFilesToDatabase() = runTest {
        id3Manager.setMetadata(
            musicFile1, Metadata(
                name = "test - music 1",
                title = "music 1",
                artist = "test",
                genre = "",
                album = "MyChannel",
                year = "",
                comments = CommentsTag(
                    source = SourceTag(
                        id = "videoId",
                        platform = "youtube",
                        uploaderId = "myChannelId",
                        uploader = "MyChannel",
                        uploadDate = "17/12/1992"
                    ),
                    downloadDate = "2024-01-18"
                )
            )
        )
        id3Manager.updateMetadata(
            file = musicFile1,
            playlists = listOf("Casual", "Sunshine Rosé"),
            customTags = listOf("tag1", "tag2")
        )
        id3Manager.setMetadata(
            musicFile2, Metadata(
                name = "test - music 2",
                title = "Winter's Over (Prod. Beatcraze)",
                artist = "J-Wright",
                genre = "",
                album = "SwagyTracks",
                year = "",
                comments = CommentsTag(
                    source = SourceTag(
                        id = "dW0VLeJ83uE",
                        platform = "youtube",
                        uploaderId = "SwagyTracksId",
                        uploader = "SwagyTracks",
                        uploadDate = "13/04/2018"
                    ),
                    downloadDate = "2019-11-03"
                )
            )
        )
        id3Manager.updateMetadata(
            file = musicFile2,
            playlists = listOf("Casual", "Chill"),
            customTags = listOf("tag1", "tag3")
        )

        manager.addFilesToDatabase()


        val music = musicDAO.allMusic()
        assert(music.size == 3)
        val musicNames = music.map { it.fileName }
        assert(musicNames.contains("test - music 1"))
        assert(musicNames.contains("test - music 2"))
        val musicTitles = music.map { it.title }
        assert(musicTitles.contains("music 1"))
        assert(musicTitles.contains("Winter's Over (Prod. Beatcraze)"))
        val musicArtists = music.map { it.artist }
        assert(musicArtists.contains("test"))
        assert(musicArtists.contains("J-Wright"))
        val musicIds = music.map { it.platformId }
        assert(musicIds.contains("videoId"))
        assert(musicIds.contains("dW0VLeJ83uE"))
        val musicUploaderIds = music.map { it.uploaderId }
        assert(musicUploaderIds.contains("myChannelId"))
        assert(musicUploaderIds.contains("SwagyTracksId"))
        val musicUploadDates = music.map { it.uploadDate }
        assert(musicUploadDates.contains("17/12/1992"))
        assert(musicUploadDates.contains("13/04/2018"))

        val playlists = playlistDAO.allPlaylists()
        assert(playlists.size == 3)
        val playlistNames = playlists.map { it.name }
        assert(playlistNames.contains("Casual"))
        assert(playlistNames.contains("Sunshine Rosé"))
        assert(playlistNames.contains("Chill"))
        playlists.forEach {
            assert(it.music.isNotEmpty())
            if (it.name == "Casual") {
                assert(it.music.size == 2)
                assert(it.music.contains("test - music 1"))
                assert(it.music.contains("test - music 2"))
            }
            if (it.name == "Sunshine Rosé") {
                assert(it.music.size == 1)
                assert(it.music.contains("test - music 1"))
            }
            if (it.name == "Chill") {
                assert(it.music.size == 1)
                assert(it.music.contains("test - music 2"))
            }
        }

        val tags = tagDAO.allTags()
        assert(tags.size == 3)
        assert(tags.contains("tag1"))
        assert(tags.contains("tag2"))
        assert(tags.contains("tag3"))

        val mopidyCasual = mopidyManager.getFilesFromPlaylist("Casual").map { File(it).nameWithoutExtension }
        assert(mopidyCasual.size == 2)
        assert(mopidyCasual.contains("test - music 1"))
        assert(mopidyCasual.contains("test - music 2"))
        val mopidySunshine = mopidyManager.getFilesFromPlaylist("Sunshine Rosé").map { File(it).nameWithoutExtension }
        assert(mopidySunshine.size == 1)
        assert(mopidySunshine.contains("test - music 1"))
        val mopidyChill = mopidyManager.getFilesFromPlaylist("Chill").map { File(it).nameWithoutExtension }
        assert(mopidyChill.size == 1)
        assert(mopidyChill.contains("test - music 2"))

        val powerAmpCasual = powerAmpManager.getFilesFromPlaylist("Casual").map { File(it).nameWithoutExtension }
        assert(powerAmpCasual.size == 2)
        assert(powerAmpCasual.contains("test - music 1"))
        assert(powerAmpCasual.contains("test - music 2"))
        val powerAmpSunshine =
            powerAmpManager.getFilesFromPlaylist("Sunshine Rosé").map { File(it).nameWithoutExtension }
        assert(powerAmpSunshine.size == 1)
        assert(powerAmpSunshine.contains("test - music 1"))
        val powerAmpChill = powerAmpManager.getFilesFromPlaylist("Chill").map { File(it).nameWithoutExtension }
        assert(powerAmpChill.size == 1)
        assert(powerAmpChill.contains("test - music 2"))
    }

    @Test
    fun addRealFilesToDatabase() = runBlocking {
//        configurationManager.setConfiguration(
//            Settings(
//                audioFolder = "src/test/DataMigrationTest/TestAudio",
//                playlistsFolder = "src/test/DataMigrationTest/Playlists",
//                archiveFolder = "src/test/DataMigrationTest/Archive"
//            )
//        )
//
//        val casual = mopidyManager.getFilesFromPlaylist("Casual")
//        val chill = mopidyManager.getFilesFromPlaylist("Chill")
//        val deepHouseMix = mopidyManager.getFilesFromPlaylist("Deep House Mix")
//        val mix = mopidyManager.getFilesFromPlaylist("Mix")
//        val partyHard = mopidyManager.getFilesFromPlaylist("Party Hard")
//        val reflectOnLife = mopidyManager.getFilesFromPlaylist("Reflect on Life")
//        val relax = mopidyManager.getFilesFromPlaylist("Relax")
//        val sunshineRose = mopidyManager.getFilesFromPlaylist("Sunshine Rosé")
//        val vicGazole = mopidyManager.getFilesFromPlaylist("Vic Gazole")
//        assert(casual.isNotEmpty())
//        assert(chill.isNotEmpty())
//        assert(deepHouseMix.isNotEmpty())
//        assert(mix.isNotEmpty())
//        assert(partyHard.isNotEmpty())
//        assert(reflectOnLife.isNotEmpty())
//        assert(relax.isNotEmpty())
//        assert(sunshineRose.isNotEmpty())
//        assert(vicGazole.isNotEmpty())
//
//        configurationManager.setPlaylistsFolder("src/test/DataMigrationTest/TestPlaylists")
//        manager.addFilesToDatabase()
//
//        val newCasual = mopidyManager.getFilesFromPlaylist("Casual")
//        val newChill = mopidyManager.getFilesFromPlaylist("Chill")
//        val newDeepHouseMix = mopidyManager.getFilesFromPlaylist("Deep House Mix")
//        val newMix = mopidyManager.getFilesFromPlaylist("Mix")
//        val newPartyHard = mopidyManager.getFilesFromPlaylist("Party Hard")
//        val newReflectOnLife = mopidyManager.getFilesFromPlaylist("Reflect on Life")
//        val newRelax = mopidyManager.getFilesFromPlaylist("Relax")
//        val newSunshineRose = mopidyManager.getFilesFromPlaylist("Sunshine Rosé")
//        val newVicGazole = mopidyManager.getFilesFromPlaylist("Vic Gazole")
//        assert(casual.size == newCasual.size)
//        assert(chill.size == newChill.size)
//        assert(deepHouseMix.size == newDeepHouseMix.size)
//        assert(mix.size == newMix.size)
//        assert(partyHard.size == newPartyHard.size)
//        assert(reflectOnLife.size == newReflectOnLife.size)
//        assert(relax.size == newRelax.size)
//        assert(sunshineRose.size == newSunshineRose.size)
//        assert(vicGazole.size == newVicGazole.size)
//
//        val newCasualPA = powerAmpManager.getFilesFromPlaylist("Casual")
//        val newChillPA = powerAmpManager.getFilesFromPlaylist("Chill")
//        val newDeepHouseMixPA = powerAmpManager.getFilesFromPlaylist("Deep House Mix")
//        val newMixPA = powerAmpManager.getFilesFromPlaylist("Mix")
//        val newPartyHardPA = powerAmpManager.getFilesFromPlaylist("Party Hard")
//        val newReflectOnLifePA = powerAmpManager.getFilesFromPlaylist("Reflect on Life")
//        val newRelaxPA = powerAmpManager.getFilesFromPlaylist("Relax")
//        val newSunshineRosePA = powerAmpManager.getFilesFromPlaylist("Sunshine Rosé")
//        val newVicGazolePA = powerAmpManager.getFilesFromPlaylist("Vic Gazole")
//        assert(casual.size == newCasualPA.size)
//        assert(chill.size == newChillPA.size)
//        assert(deepHouseMix.size == newDeepHouseMixPA.size)
//        assert(mix.size == newMixPA.size)
//        assert(partyHard.size == newPartyHardPA.size)
//        assert(reflectOnLife.size == newReflectOnLifePA.size)
//        assert(relax.size == newRelaxPA.size)
//        assert(sunshineRose.size == newSunshineRosePA.size)
//        assert(vicGazole.size == newVicGazolePA.size)
    }
}