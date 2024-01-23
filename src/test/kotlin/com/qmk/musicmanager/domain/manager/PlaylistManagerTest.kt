package com.qmk.musicmanager.domain.manager

import com.google.gson.Gson
import com.qmk.musicmanager.database.dao.*
import com.qmk.musicmanager.database.model.DatabaseFactory
import com.qmk.musicmanager.domain.model.CommentsTag
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
            powerAmpManager
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
    fun getPlaylists() {
    }

    @Test
    fun getPlaylist() {
    }

    @Test
    fun doesPlaylistExist() {
    }

    @Test
    fun getYoutubePlaylistId() {
    }

    @Test
    fun doesPlaylistIdExist() {
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
    fun renamePlaylist() {
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
    fun getYoutubePlaylist() {
    }

    @Test
    fun createYoutubePlaylist() {
    }

    @Test
    fun editYoutubePlaylist() {
    }

    @Test
    fun deletePlatformPlaylist() {
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
    fun download() {
    }

    @Test
    fun testDownload() {
    }

    @Test
    fun downloadYoutubePlaylist() {
    }

    @Test
    fun archiveMusic() {
    }
}