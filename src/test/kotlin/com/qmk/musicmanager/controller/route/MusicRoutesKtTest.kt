package com.qmk.musicmanager.controller.route

import com.google.gson.Gson
import com.qmk.musicmanager.controller.model.BasicAPIResponse
import com.qmk.musicmanager.controller.model.GetNewMusic
import com.qmk.musicmanager.database.dao.*
import com.qmk.musicmanager.database.model.DatabaseFactory
import com.qmk.musicmanager.domain.manager.*
import com.qmk.musicmanager.domain.model.*
import com.qmk.musicmanager.extension.fromJson
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class MusicRoutesKtTest {
    private val gson = Gson()
    private val route = "/api/music"
    private val configurationManager = ConfigurationManager()
    private val musicDAO = MusicDAOImpl()
    private lateinit var playlistManager: PlaylistManager
    private lateinit var dataManager: DataManager
    private lateinit var musicFile: File

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
    fun tearDown() = runBlocking {
        playlistManager.deletePlaylist("My Playlist 1")
        playlistManager.deletePlaylist("My Playlist 2")
        dataManager.removeAllEntries()
        musicFile.delete()
        configurationManager.setConfiguration(Settings())
    }

    @Test
    fun `Edit music`() = testApplication {
        val editedMusic = Music(
            fileName = musicFile.nameWithoutExtension,
            fileExtension = musicFile.extension,
            title = "newTitle",
            artist = "newArtist",
            platformId = "videoId",
            uploaderId = "myChannelId",
            uploadDate = "17/12/1992",
            isNew = true
        )

        val response = client.post("$route/${musicFile.nameWithoutExtension}") {
            contentType(ContentType.Application.Json)
            setBody(gson.toJson(editedMusic))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = gson.fromJson(response.bodyAsText(), BasicAPIResponse::class.java)
        assertEquals(true, body.successful)
        val music = musicDAO.music(musicFile.nameWithoutExtension)
        assertEquals("newTitle", music?.title)
        assertEquals("newArtist", music?.artist)
        assert(music?.playlists?.isEmpty() == true)
        assert(music?.tags?.isEmpty() == true)
        assertEquals(true, music?.isNew)
    }

    @Test
    fun `Get new music`() = testApplication {
        musicDAO.editMusic(
            fileName = musicFile.nameWithoutExtension,
            fileExtension = musicFile.extension,
            title = "music",
            artist = "test",
            platformId = "videoId",
            uploaderId = "myChannelId",
            uploadDate = "17/12/1992",
            isNew = true
        )

        val response = client.get("$route/new")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = gson.fromJson(response.bodyAsText(), BasicAPIResponse::class.java)
        assertEquals(true, body.successful)
        val serverResponse = body.message?.fromJson(GetNewMusic::class.java)
        val newMusic = serverResponse?.response?.fromJson(Array<Music>::class.java)?.asList()
        assertEquals(1, newMusic?.size)
    }
}