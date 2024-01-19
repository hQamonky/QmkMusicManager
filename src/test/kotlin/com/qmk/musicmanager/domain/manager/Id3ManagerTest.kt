package com.qmk.musicmanager.domain.manager

import com.google.gson.Gson
import com.qmk.musicmanager.domain.model.*
import com.qmk.musicmanager.domain.model.Metadata
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag
import org.junit.After
import org.junit.Before
import org.junit.Test

import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Id3ManagerTest {
    private lateinit var manager: Id3Manager
    private lateinit var musicFile: File

    @Before
    fun setUp() {
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
                downloadDate = "2024-01-18"
            ).toJson(Gson())
        )
        f.commit()
        manager = Id3Manager()
    }

    @After
    fun tearDown() {
        musicFile.delete()
    }

    @Test
    fun getMetadataFromYoutube() {
        val videoInfo = MusicInfo(
            "dW0VLeJ83uE",
            "J-Wright - Winter's Over (Prod. Beatcraze) (Lyrics)",
            "13/04/2018",
            "SwagyTracks",
            "SwagyTracksId"
        )
        val namingRules = listOf(NamingRule(null, " (Lyrics)", "", 2))
        val metadata = manager.getMetadataFromYoutube(videoInfo, NamingFormat(), namingRules)
        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        assert(metadata.name == "J-Wright - Winters Over (Prod Beatcraze) (Lyrics)")
        assert(metadata.title == "Winter's Over (Prod. Beatcraze)")
        assert(metadata.artist == "J-Wright")
        assert(metadata.genre == "")
        assert(metadata.album == "SwagyTracks")
        assert(metadata.year == "")
        assert(metadata.comments?.source?.id == "dW0VLeJ83uE")
        assert(metadata.comments?.source?.platform == "youtube")
        assert(metadata.comments?.source?.uploaderId == "SwagyTracksId")
        assert(metadata.comments?.source?.uploader == "SwagyTracks")
        assert(metadata.comments?.source?.uploadDate == "13/04/2018")
        assert(metadata.comments?.playlists?.isEmpty() == true)
        assert(metadata.comments?.customTags?.isEmpty() == true)
        assert(metadata.comments?.downloadDate == currentDate)
    }

    @Test
    fun getMetadata() {
        val metadata = manager.getMetadata(musicFile)
        assert(metadata.name == "test - music")
        assert(metadata.title == "music")
        assert(metadata.artist == "test")
        assert(metadata.genre == "")
        assert(metadata.album == "MyChannel")
        assert(metadata.year == "")
        assert(metadata.comments?.source?.id == "videoId")
        assert(metadata.comments?.source?.platform == "youtube")
        assert(metadata.comments?.source?.uploaderId == "myChannelId")
        assert(metadata.comments?.source?.uploader == "MyChannel")
        assert(metadata.comments?.source?.uploadDate == "17/12/1992")
        assert(metadata.comments?.playlists?.isEmpty() == true)
        assert(metadata.comments?.customTags?.isEmpty() == true)
        assert(metadata.comments?.downloadDate == "2024-01-18")
    }

    @Test
    fun setMetadata() {
        var metadata = manager.getMetadata(musicFile)
        assert(metadata.name == "test - music")
        assert(metadata.title == "music")
        assert(metadata.artist == "test")
        assert(metadata.genre == "")
        assert(metadata.album == "MyChannel")
        assert(metadata.year == "")
        assert(metadata.comments?.source?.id == "videoId")
        assert(metadata.comments?.source?.platform == "youtube")
        assert(metadata.comments?.source?.uploaderId == "myChannelId")
        assert(metadata.comments?.source?.uploader == "MyChannel")
        assert(metadata.comments?.source?.uploadDate == "17/12/1992")
        assert(metadata.comments?.playlists?.isEmpty() == true)
        assert(metadata.comments?.customTags?.isEmpty() == true)
        assert(metadata.comments?.downloadDate == "2024-01-18")

        manager.setMetadata(
            musicFile, Metadata(
                name = "test - music",
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

        metadata = manager.getMetadata(musicFile)
        assert(metadata.name == "test - music")
        assert(metadata.title == "Winter's Over (Prod. Beatcraze)")
        assert(metadata.artist == "J-Wright")
        assert(metadata.genre == "")
        assert(metadata.album == "SwagyTracks")
        assert(metadata.year == "")
        assert(metadata.comments?.source?.id == "dW0VLeJ83uE")
        assert(metadata.comments?.source?.platform == "youtube")
        assert(metadata.comments?.source?.uploaderId == "SwagyTracksId")
        assert(metadata.comments?.source?.uploader == "SwagyTracks")
        assert(metadata.comments?.source?.uploadDate == "13/04/2018")
        assert(metadata.comments?.downloadDate == "2019-11-03")
    }

    @Test
    fun updateMetadata() {
        var metadata = manager.getMetadata(musicFile)
        assert(metadata.name == "test - music")
        assert(metadata.title == "music")
        assert(metadata.artist == "test")
        assert(metadata.genre == "")
        assert(metadata.album == "MyChannel")
        assert(metadata.year == "")
        assert(metadata.comments?.source?.id == "videoId")
        assert(metadata.comments?.source?.platform == "youtube")
        assert(metadata.comments?.source?.uploaderId == "myChannelId")
        assert(metadata.comments?.source?.uploader == "MyChannel")
        assert(metadata.comments?.source?.uploadDate == "17/12/1992")
        assert(metadata.comments?.playlists?.isEmpty() == true)
        assert(metadata.comments?.customTags?.isEmpty() == true)
        assert(metadata.comments?.downloadDate == "2024-01-18")

        manager.updateMetadata(
            file = musicFile,
            title = "Winter's Over (Prod. Beatcraze)",
            genre = "",
            album = "SwagyTracks",
            year = "",
            playlists = listOf("Chill")
        )
        metadata = manager.getMetadata(musicFile)
        assert(metadata.title == "Winter's Over (Prod. Beatcraze)")
        assert(metadata.artist == "test")
        assert(metadata.genre == "")
        assert(metadata.album == "SwagyTracks")
        assert(metadata.year == "")
        assert(metadata.comments?.source?.id == "videoId")
        assert(metadata.comments?.source?.platform == "youtube")
        assert(metadata.comments?.source?.uploaderId == "myChannelId")
        assert(metadata.comments?.source?.uploader == "MyChannel")
        assert(metadata.comments?.source?.uploadDate == "17/12/1992")
        assert(metadata.comments?.playlists?.size == 1)
        assert(metadata.comments?.customTags?.isEmpty() == true)

        manager.updateMetadata(
            file = musicFile,
            artist = "J-Wright",
            customTags = listOf("Tag")
        )
        metadata = manager.getMetadata(musicFile)
        assert(metadata.title == "Winter's Over (Prod. Beatcraze)")
        assert(metadata.artist == "J-Wright")
        assert(metadata.genre == "")
        assert(metadata.album == "SwagyTracks")
        assert(metadata.year == "")
        assert(metadata.comments?.source?.id == "videoId")
        assert(metadata.comments?.source?.platform == "youtube")
        assert(metadata.comments?.source?.uploaderId == "myChannelId")
        assert(metadata.comments?.source?.uploader == "MyChannel")
        assert(metadata.comments?.source?.uploadDate == "17/12/1992")
        assert(metadata.comments?.playlists?.size == 1)
        assert(metadata.comments?.customTags?.size == 1)
    }

    @Test
    fun addMusicToPlaylist() {
        var metadata = manager.getMetadata(musicFile)
        assert(metadata.comments?.playlists?.isEmpty() == true)

        manager.addMusicToPlaylist(musicFile, "Chill")
        metadata = manager.getMetadata(musicFile)
        assert(metadata.comments?.playlists?.size == 1)
        assert(metadata.comments!!.playlists[0] == "Chill")

        manager.addMusicToPlaylist(musicFile, "Chill")
        metadata = manager.getMetadata(musicFile)
        assert(metadata.comments?.playlists?.size == 1)
        assert(metadata.comments!!.playlists[0] == "Chill")

        manager.addMusicToPlaylist(musicFile, "Casual")
        metadata = manager.getMetadata(musicFile)
        assert(metadata.comments?.playlists?.size == 2)
    }

    @Test
    fun removeMusicFromPlaylist() {
        var metadata = manager.getMetadata(musicFile)
        assert(metadata.comments?.playlists?.isEmpty() == true)

        manager.addMusicToPlaylist(musicFile, "Chill")
        manager.addMusicToPlaylist(musicFile, "Casual")
        metadata = manager.getMetadata(musicFile)
        assert(metadata.comments?.playlists?.size == 2)

        manager.removeMusicFromPlaylist(musicFile, "Chill")
        metadata = manager.getMetadata(musicFile)
        assert(metadata.comments?.playlists?.size == 1)
        assert(metadata.comments?.playlists?.contains("Chill") == false)
    }

    @Test
    fun renamePlaylistForMusic() {
        var metadata = manager.getMetadata(musicFile)
        assert(metadata.comments?.playlists?.isEmpty() == true)

        manager.addMusicToPlaylist(musicFile, "Chill")
        manager.addMusicToPlaylist(musicFile, "Casual")
        metadata = manager.getMetadata(musicFile)
        assert(metadata.comments?.playlists?.size == 2)
        assert(metadata.comments?.playlists?.contains("Chill") == true)
        assert(metadata.comments?.playlists?.contains("Casual") == true)

        manager.renamePlaylistForMusic("Casual", "Best of WilliTracks", musicFile)
        metadata = manager.getMetadata(musicFile)
        assert(metadata.comments?.playlists?.size == 2)
        assert(metadata.comments?.playlists?.contains("Chill") == true)
        assert(metadata.comments?.playlists?.contains("Best of WilliTracks") == true)
        assert(metadata.comments?.playlists?.contains("Casual") == false)
    }

    @Test
    fun addCustomTagToMusic() {
        var metadata = manager.getMetadata(musicFile)
        assert(metadata.comments?.customTags?.isEmpty() == true)

        manager.addCustomTagToMusic( "Rap", musicFile)
        metadata = manager.getMetadata(musicFile)
        assert(metadata.comments?.customTags?.size == 1)
        assert(metadata.comments!!.customTags[0] == "Rap")

        manager.addCustomTagToMusic("Rap", musicFile)
        metadata = manager.getMetadata(musicFile)
        assert(metadata.comments?.customTags?.size == 1)
        assert(metadata.comments!!.customTags[0] == "Rap")

        manager.addCustomTagToMusic("Favorite", musicFile)
        metadata = manager.getMetadata(musicFile)
        assert(metadata.comments?.customTags?.size == 2)
    }

    @Test
    fun removeCustomTagFromMusic() {
        var metadata = manager.getMetadata(musicFile)
        assert(metadata.comments?.customTags?.isEmpty() == true)

        manager.addCustomTagToMusic("Rap", musicFile)
        manager.addCustomTagToMusic("Favorite", musicFile)
        metadata = manager.getMetadata(musicFile)
        assert(metadata.comments?.customTags?.size == 2)

        manager.removeCustomTagFromMusic("Rap", musicFile)
        metadata = manager.getMetadata(musicFile)
        assert(metadata.comments?.customTags?.size == 1)
        assert(metadata.comments?.customTags?.contains("Rap") == false)
    }
}