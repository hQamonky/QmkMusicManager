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

class MusicManagerTest {
    private lateinit var musicManager: MusicManager
    private lateinit var playlistManager: PlaylistManager
    private lateinit var configurationManager: ConfigurationManager
    private lateinit var id3Manager: Id3Manager
    private lateinit var musicFile: File
    private lateinit var musicDAO: MusicDAO
    private lateinit var playlistDAO: PlaylistDAO
    private lateinit var platformPlaylistDAO: PlatformPlaylistDAO
    private lateinit var namingRuleDAO: NamingRuleDAO
    private lateinit var uploaderDAO: UploaderDAO
    private lateinit var mopidyManager: MopidyManager
    private lateinit var powerAmpManager: PowerAmpManager
    private lateinit var dataManager: DataManager

    @Before
    fun setUp() {
        DatabaseFactory.init()
        configurationManager = ConfigurationManager()
        configurationManager.setConfiguration(
            Settings(
                audioFolder = "src/test/MusicTestDir/Audio",
                playlistsFolder = "src/test/MusicTestDir/Playlists",
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
        musicManager = MusicManager(musicDAO, configurationManager, id3Manager, playlistManager)
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
            dataManager.addFilesToDatabase()
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            playlistManager.deletePlaylist("My Playlist 1")
            playlistManager.deletePlaylist("My Playlist 2")
            dataManager.removeAllEntries()
            musicFile.delete()
            configurationManager.setConfiguration(Settings())
        }
    }

    @Test
    fun editMusic() = runTest {
        var music = musicDAO.music(musicFile.nameWithoutExtension)
        assert(music != null)
        assert(music?.title == "music")
        assert(music?.artist == "test")
        assert(music?.playlists?.size == 2)
        assert(music?.playlists?.contains("My Playlist 1") == true)
        assert(music?.playlists?.contains("My Playlist 2") == true)
        assert(music?.tags?.size == 2)
        assert(music?.tags?.contains("tag1") == true)
        assert(music?.tags?.contains("tag2") == true)
        assert(music?.isNew == false)

        musicManager.editMusic(music!!.copy(
            title = "newTitle",
            artist = "newArtist",
            playlists = listOf("My Playlist 2"),
            tags = listOf("tag1", "tag3"),
            isNew = true
        ))

        music = musicDAO.music(musicFile.nameWithoutExtension)
        assert(music?.title == "newTitle")
        assert(music?.artist == "newArtist")
        assert(music?.playlists?.size == 1)
        assert(music?.playlists?.contains("My Playlist 2") == true)
        assert(music?.tags?.size == 2)
        assert(music?.tags?.contains("tag1") == true)
        assert(music?.tags?.contains("tag3") == true)
        assert(music?.isNew == true)
    }
}