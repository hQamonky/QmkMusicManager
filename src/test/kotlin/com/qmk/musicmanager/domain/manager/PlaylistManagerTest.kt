package com.qmk.musicmanager.domain.manager

import com.google.gson.Gson
import com.qmk.musicmanager.database.dao.*
import com.qmk.musicmanager.database.model.DatabaseFactory
import com.qmk.musicmanager.domain.model.CommentsTag
import com.qmk.musicmanager.domain.model.Music
import com.qmk.musicmanager.domain.model.Settings
import com.qmk.musicmanager.domain.model.SourceTag
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag
import org.junit.After
import org.junit.Before
import org.junit.Test

import java.io.File

class PlaylistManagerTest {
    private val playlistDir = "src/test/MusicTestDir/Playlists"
    private lateinit var playlistManager: PlaylistManager
    private lateinit var configurationManager: ConfigurationManager
    private lateinit var id3Manager: Id3Manager
    private lateinit var musicDAO: MusicDAO
    private lateinit var playlistDAO: PlaylistDAO
    private lateinit var platformPlaylistDAO: PlatformPlaylistDAO
    private lateinit var namingRuleDAO: NamingRuleDAO
    private lateinit var uploaderDAO: UploaderDAO
    private lateinit var mopidyManager: MopidyManager
    private lateinit var powerAmpManager: PowerAmpManager
    private lateinit var dataManager: DataManager
    private lateinit var musicFile: File
    private lateinit var deezerManager: DeezerManager

    @Before
    fun setUp() {
        DatabaseFactory.init()
        configurationManager = ConfigurationManager()
        configurationManager.setConfiguration(
            Settings(
                audioFolder = "src/test/MusicTestDir/Audio",
                playlistsFolder = playlistDir,
                archiveFolder = "src/test/MusicTestDir/Archive"
            )
        )
        id3Manager = Id3Manager()
        musicDAO = MusicDAOImpl()
        playlistDAO = PlaylistDAOImpl()
        platformPlaylistDAO = PlatformPlaylistDAOImpl()
        uploaderDAO = UploaderDAOImpl()
        namingRuleDAO = NamingRuleDAOImpl()
        mopidyManager = MopidyManager(configurationManager)
        powerAmpManager = PowerAmpManager(configurationManager)
        deezerManager = DeezerManager()
        playlistManager = PlaylistManager(
            playlistDAO,
            platformPlaylistDAO,
            musicDAO,
            uploaderDAO,
            namingRuleDAO,
            YoutubeManager(),
            configurationManager,
            id3Manager,
            mopidyManager,
            powerAmpManager,
            deezerManager
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
        val initialFile = File("src/test/MusicTestDir/Audio/Cara yeaaah VFWJd69f9F0.mp3")
        musicFile = File("src/test/MusicTestDir/Audio/test - music.mp3")
        initialFile.copyTo(musicFile, true)
        val f = AudioFileIO.read(musicFile)
        val tag: Tag = f.tag
        tag.setField(FieldKey.TITLE, "music")
        tag.setField(FieldKey.ARTIST, "test")
        tag.setField(FieldKey.ALBUM, "MyChannel")
        tag.setField(FieldKey.GENRE, "")
        tag.setField(
            FieldKey.COMMENT, CommentsTag(
                source = SourceTag(
                    id = "videoId",
                    platform = "youtube",
                    uploaderId = "myChannelId",
                    uploader = "MyChannel",
                    uploadDate = "17/12/1992"
                ),
                playlists = listOf("My Playlist 1", "My Playlist 2"),
                customTags = listOf("tag1", "tag2"),
                downloadDate = "2024-01-18"
            ).toJson(Gson())
        )
        f.commit()
        runBlocking {
            musicDAO.addNewMusic(
                "test - music",
                "mp3",
                "music",
                "test",
                "videoId",
                "myChannelId",
                "17/12/1992",
                listOf("tag1", "tag2"),
                true
            )
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            dataManager.removeAllEntries()
            File(playlistDir).deleteRecursively()
            musicFile.delete()
            configurationManager.setConfiguration(Settings())
        }
    }

    @Test
    fun getYoutubePlaylistId() {
        val plId =
            playlistManager.getYoutubePlaylistId("https://www.youtube.com/playlist?list=PLCVGGn6GhhDsqtlR4F1F9hbllr2My19Uq")
        assert(plId == "PLCVGGn6GhhDsqtlR4F1F9hbllr2My19Uq")
    }

    @Test
    fun create() = runTest {
        val playlistName = "My Playlist"
        val playlist = playlistManager.create(playlistName)
        assert(playlist != null)
        val plFromDAO = playlistDAO.playlist(playlistName)
        assert(plFromDAO != null)
        val plFromMopidy = File("$playlistDir/Mopidy/$playlistName.m3u8")
        assert(plFromMopidy.exists())
        val plFromPowerAmp = File("$playlistDir/PowerAmp/$playlistName.m3u8")
        assert(plFromPowerAmp.exists())
    }

    @Test
    fun renamePlaylist() = runTest {
        playlistManager.create("My Playlist 1")
        playlistManager.create("My Playlist 2")
        playlistDAO.addMusicToPlaylist(musicFile.nameWithoutExtension, "My Playlist 1")
        playlistDAO.addMusicToPlaylist(musicFile.nameWithoutExtension, "My Playlist 2")

        val result = playlistManager.renamePlaylist("My Playlist 1", "Best of WilliTracks")
        assert(result)

        val music = musicDAO.music(musicFile.nameWithoutExtension)
        assert(music?.playlists?.size == 2)
        assert(music?.playlists?.contains("My Playlist 1") == false)
        assert(music?.playlists?.contains("Best of WilliTracks") == true)

        val metadata = id3Manager.getMetadata(musicFile)
        assert(metadata.comments?.playlists?.contains("My Playlist 1") == false)
        assert(metadata.comments?.playlists?.contains("Best of WilliTracks") == true)

        val oldPlFromDAO = playlistDAO.playlist("My Playlist 1")
        assert(oldPlFromDAO == null)
        val newPlFromDAO = playlistDAO.playlist("Best of WilliTracks")
        assert(newPlFromDAO != null)

        val oldMopidyPl = File("$playlistDir/Mopidy/My Playlist 1.m3u8")
        assert(!oldMopidyPl.exists())
        val newMopidyPl = File("$playlistDir/Mopidy/Best of WilliTracks.m3u8")
        assert(newMopidyPl.exists())

        val oldPowerAmpPl = File("$playlistDir/PowerAmp/My Playlist 1.m3u8")
        assert(!oldPowerAmpPl.exists())
        val newPowerAmpPl = File("$playlistDir/PowerAmp/Best of WilliTracks.m3u8")
        assert(newPowerAmpPl.exists())
    }

    @Test
    fun deletePlaylist() = runTest {
        val playlistName = "My Playlist 2"
        playlistManager.create("My Playlist 1")
        playlistDAO.addMusicToPlaylist(musicFile.nameWithoutExtension, playlistName)
        val playlist = playlistManager.create(playlistName)
        assert(playlist != null)
        var plFromDAO = playlistDAO.playlist(playlistName)
        assert(plFromDAO != null)
        var plFromMopidy = File("$playlistDir/Mopidy/$playlistName.m3u8")
        assert(plFromMopidy.exists())
        var plFromPowerAmp = File("$playlistDir/PowerAmp/$playlistName.m3u8")
        assert(plFromPowerAmp.exists())

        playlistManager.deletePlaylist(playlistName)

        plFromDAO = playlistDAO.playlist(playlistName)
        assert(plFromDAO == null)
        plFromMopidy = File("$playlistDir/Mopidy/$playlistName.m3u8")
        assert(!plFromMopidy.exists())
        plFromPowerAmp = File("$playlistDir/PowerAmp/$playlistName.m3u8")
        assert(!plFromPowerAmp.exists())
        val musicPlaylists = id3Manager.getMetadata(musicFile).comments?.playlists
        assert(musicPlaylists?.contains(playlistName) == false)
    }

    @Test
    fun getYoutubePlaylist() = runBlocking {
        playlistManager.create("My Playlist 1")
        playlistManager.create("My Playlist 2")
        playlistManager.createYoutubePlaylist(
            "https://www.youtube.com/playlist?list=PLCVGGn6GhhDsqtlR4F1F9hbllr2My19Uq",
            listOf("My Playlist 1", "My Playlist 3")
        )

        val playlist = playlistManager.getYoutubePlaylist("PLCVGGn6GhhDsqtlR4F1F9hbllr2My19Uq")
        assert(playlist?.playlists?.size == 2)
        assert(playlist?.playlists?.contains("My Playlist 1") == true)
        assert(playlist?.playlists?.contains("My Playlist 3") == true)
        assert(playlist?.name == "Best of WilliTracks 2023 part 2")
    }

    @Test
    fun createYoutubePlaylist() = runBlocking {
        playlistManager.create("My Playlist 1")
        playlistManager.create("My Playlist 2")
        playlistManager.createYoutubePlaylist(
            "https://www.youtube.com/playlist?list=PLCVGGn6GhhDsqtlR4F1F9hbllr2My19Uq",
            listOf("My Playlist 1", "My Playlist 3")
        )
        val plPlaylist = platformPlaylistDAO.playlist("PLCVGGn6GhhDsqtlR4F1F9hbllr2My19Uq")
        assert(plPlaylist?.playlists?.size == 2)
        assert(plPlaylist?.playlists?.contains("My Playlist 1") == true)
        assert(plPlaylist?.playlists?.contains("My Playlist 3") == true)
        assert(plPlaylist?.name == "Best of WilliTracks 2023 part 2")

        val playlist3 = playlistDAO.playlist("My Playlist 3")
        assert(playlist3 != null)
    }

    @Test
    fun editYoutubePlaylist() = runBlocking {
        playlistManager.create("My Playlist 1")
        playlistManager.create("My Playlist 2")
        playlistManager.createYoutubePlaylist(
            "https://www.youtube.com/playlist?list=PLCVGGn6GhhDsqtlR4F1F9hbllr2My19Uq",
            listOf("My Playlist 1", "My Playlist 3")
        )

        playlistManager.editYoutubePlaylist(
            "PLCVGGn6GhhDsqtlR4F1F9hbllr2My19Uq",
            listOf("My Playlist 2")
        )

        val plPlaylist = platformPlaylistDAO.playlist("PLCVGGn6GhhDsqtlR4F1F9hbllr2My19Uq")
        assert(plPlaylist?.playlists?.size == 1)
        assert(plPlaylist?.playlists?.contains("My Playlist 2") == true)
    }

    @Test
    fun deletePlatformPlaylist() = runBlocking {
        playlistManager.create("My Playlist 1")
        playlistManager.create("My Playlist 2")
        playlistManager.createYoutubePlaylist(
            "https://www.youtube.com/playlist?list=PLCVGGn6GhhDsqtlR4F1F9hbllr2My19Uq",
            listOf("My Playlist 1", "My Playlist 3")
        )

        playlistManager.deletePlatformPlaylist("PLCVGGn6GhhDsqtlR4F1F9hbllr2My19Uq")

        val plPlaylist = platformPlaylistDAO.playlist("PLCVGGn6GhhDsqtlR4F1F9hbllr2My19Uq")
        assert(plPlaylist == null)
    }

    @Test
    fun addMusicToPlaylist() = runTest {
        val playlistName = "My Playlist 2"
        id3Manager.updateMetadata(file = musicFile, playlists = listOf())
        playlistManager.create(playlistName)
        val music = musicDAO.music(musicFile.nameWithoutExtension)
        assert(music != null)

        val result = playlistManager.addMusicToPlaylist(music!!, playlistName)
        assert(result)
        val playlist = playlistDAO.playlist(playlistName)
        assert(playlist?.music?.contains(music.fileName) == true)
        val mopidyPl = mopidyManager.getFilesFromPlaylist(playlistName).map { File(it).nameWithoutExtension }
        assert(mopidyPl.contains(music.fileName))
        val powerAmpPl = powerAmpManager.getFilesFromPlaylist(playlistName).map { File(it).nameWithoutExtension }
        assert(powerAmpPl.contains(music.fileName))
    }

    @Test
    fun removeMusicFromPlaylist() = runTest {
        val playlistName = "My Playlist 2"
        id3Manager.updateMetadata(file = musicFile, playlists = listOf())
        playlistManager.create(playlistName)
        val music = musicDAO.music(musicFile.nameWithoutExtension)
        assert(music != null)
        playlistManager.addMusicToPlaylist(music!!, playlistName)

        val result = playlistManager.removeMusicFromPlaylist(music, playlistName)
        assert(result)
        val playlist = playlistDAO.playlist(playlistName)
        assert(playlist?.music?.contains(music.fileName) == false)
        val mopidyPl = mopidyManager.getFilesFromPlaylist(playlistName).map { File(it).nameWithoutExtension }
        assert(!mopidyPl.contains(music.fileName))
        val powerAmpPl = powerAmpManager.getFilesFromPlaylist(playlistName).map { File(it).nameWithoutExtension }
        assert(!powerAmpPl.contains(music.fileName))
    }

    @Test
    fun download() = runBlocking {
        playlistManager.create("My Playlist 1")
        playlistManager.create("My Playlist 2")
        playlistManager.createYoutubePlaylist(
            "https://www.youtube.com/playlist?list=PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl",
            listOf("My Playlist 1")
        )
        playlistManager.createYoutubePlaylist(
            "https://www.youtube.com/playlist?list=PLCVGGn6GhhDvZ0iCVONTYnzkjW3ZA6jwf",
            listOf("My Playlist 2")
        )
        val result = playlistManager.download()
        assert(result.size == 2)
    }

    @Test
    fun downloadYoutubePlaylist() = runBlocking {
        playlistManager.create("My Playlist 1")
        val ytPlaylist = playlistManager.createYoutubePlaylist(
            "https://www.youtube.com/playlist?list=PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl",
            listOf("My Playlist 1")
        )

        val result = playlistManager.downloadYoutubePlaylist(ytPlaylist)
        assert(result.downloaded.size == 2)
    }

    @Test
    fun archiveMusic() = runTest {
        val pl1 = "My Playlist 1"
        val pl2 = "My Playlist 2"
        val archivesPl = mopidyManager.archivePlaylistName
        playlistManager.create(pl1)
        playlistManager.create(pl2)
        var music: Music? = Music(
            fileName = musicFile.nameWithoutExtension,
            title = "",
            artist = "",
            platformId = "",
            uploaderId = "",
            uploadDate = "",
            playlists = listOf(pl1, pl2, archivesPl)
        )
        playlistManager.addMusicToPlaylist(music!!, pl1)
        playlistManager.addMusicToPlaylist(music, pl2)

        playlistManager.create(archivesPl)
        playlistManager.addMusicToPlaylist(music, archivesPl)

        val result = playlistManager.archiveMusic()
        assert(result.size == 1)

        music = musicDAO.music(musicFile.nameWithoutExtension)
        assert(music != null)
        assert(music?.playlists?.contains(archivesPl) == false)
        assert(music?.playlists?.contains(pl1) == false)
        assert(music?.playlists?.contains(pl2) == false)
        assert(!mopidyManager.isMusicInPlaylist(music!!, pl1))
        assert(!mopidyManager.isMusicInPlaylist(music, pl2))
        assert(!powerAmpManager.isMusicInPlaylist(music, pl1))
        assert(!powerAmpManager.isMusicInPlaylist(music, pl2))
        assert(!musicFile.exists())
        val newFile = File("src/test/MusicTestDir/Archive/${music.fileName}.${music.fileExtension}")
        val metadata = id3Manager.getMetadata(newFile)
        assert(metadata.comments?.playlists?.isEmpty() == true)
        newFile.delete()
    }
}