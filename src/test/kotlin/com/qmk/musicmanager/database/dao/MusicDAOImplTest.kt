package com.qmk.musicmanager.database.dao

import com.qmk.musicmanager.database.model.DatabaseFactory
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class MusicDAOImplTest {

    private lateinit var musicDAO: MusicDAO

    @Before
    fun setUp() {
        DatabaseFactory.init()
        musicDAO = MusicDAOImpl()
    }

    @After
    fun tearDown() {
        runBlocking {
            musicDAO.deleteAllMusic()
        }
    }

    @Test
    fun allMusic() = runTest {
        musicDAO.addNewMusic(
            "J-Wright - Winter's Over (Prod. Beatcraze)",
            "mp3",
            "Winter's Over (Prod. Beatcraze)",
            "J-Wright",
            "dW0VLeJ83uE",
            "SwagyTracks",
            "2018",
            listOf("Rap", "Chill"),
            true
        )
        musicDAO.addNewMusic(
            "Retromigration - BO",
            "mp3",
            "BO",
            "Retromigration",
            "uSafW5cudGQ",
            "DelicieuseMusique",
            "2023",
            listOf("House", "Chill"),
            true
        )
        val music = musicDAO.allMusic()
        assert(music.size == 2)
    }

    @Test
    fun music() = runTest {
        musicDAO.addNewMusic(
            "J-Wright - Winter's Over (Prod. Beatcraze)",
            "mp3",
            "Winter's Over (Prod. Beatcraze)",
            "J-Wright",
            "dW0VLeJ83uE",
            "SwagyTracks",
            "2018",
            listOf("Rap", "Chill"),
            true
        )
        val music = musicDAO.music("J-Wright - Winter's Over (Prod. Beatcraze)")
        assert(music?.fileName == "J-Wright - Winter's Over (Prod. Beatcraze)")
        assert(music?.fileExtension == "mp3")
        assert(music?.title == "Winter's Over (Prod. Beatcraze)")
        assert(music?.artist == "J-Wright")
        assert(music?.platformId == "dW0VLeJ83uE")
        assert(music?.uploaderId == "SwagyTracks")
        assert(music?.uploadDate == "2018")
        assert(music?.tags?.size == 2)
        assert(music?.isNew == true)
    }

    @Test
    fun getMusicFromPlatformId() = runTest {
        musicDAO.addNewMusic(
            "J-Wright - Winter's Over (Prod. Beatcraze)",
            "mp3",
            "Winter's Over (Prod. Beatcraze)",
            "J-Wright",
            "dW0VLeJ83uE",
            "SwagyTracks",
            "2018",
            listOf("Rap", "Chill"),
            true
        )
        val music = musicDAO.getMusicFromPlatformId("dW0VLeJ83uE")
        assert(music?.fileName == "J-Wright - Winter's Over (Prod. Beatcraze)")
        assert(music?.fileExtension == "mp3")
        assert(music?.title == "Winter's Over (Prod. Beatcraze)")
        assert(music?.artist == "J-Wright")
        assert(music?.platformId == "dW0VLeJ83uE")
        assert(music?.uploaderId == "SwagyTracks")
        assert(music?.uploadDate == "2018")
        assert(music?.tags?.size == 2)
        assert(music?.isNew == true)
    }

    @Test
    fun newMusic() = runTest {
        musicDAO.addNewMusic(
            "J-Wright - Winter's Over (Prod. Beatcraze)",
            "mp3",
            "Winter's Over (Prod. Beatcraze)",
            "J-Wright",
            "dW0VLeJ83uE",
            "SwagyTracks",
            "2018",
            listOf("Rap", "Chill"),
            true
        )
        musicDAO.addNewMusic(
            "Retromigration - BO",
            "mp3",
            "BO",
            "Retromigration",
            "uSafW5cudGQ",
            "DelicieuseMusique",
            "2023",
            listOf("House", "Chill"),
            true
        )
        musicDAO.addNewMusic(
            "Upgrade - Baileys",
            "mp3",
            "Baileys",
            "Upgrade",
            "o-anXVb4Yms",
            "UpgradeMusic1",
            "",
            listOf("Psytrance"),
            false
        )
        val music = musicDAO.newMusic()
        assert(music.size == 2)
    }

    @Test
    fun addNewMusic() = runTest {
        musicDAO.addNewMusic(
            "J-Wright - Winter's Over (Prod. Beatcraze)",
            "mp3",
            "Winter's Over (Prod. Beatcraze)",
            "J-Wright",
            "dW0VLeJ83uE",
            "SwagyTracks",
            "2018",
            listOf("Rap", "Chill"),
            false
        )
        val music = musicDAO.music("J-Wright - Winter's Over (Prod. Beatcraze)")
        assert(music?.fileName == "J-Wright - Winter's Over (Prod. Beatcraze)")
        assert(music?.fileExtension == "mp3")
        assert(music?.title == "Winter's Over (Prod. Beatcraze)")
        assert(music?.artist == "J-Wright")
        assert(music?.platformId == "dW0VLeJ83uE")
        assert(music?.uploaderId == "SwagyTracks")
        assert(music?.uploadDate == "2018")
        assert(music?.tags?.size == 2)
        assert(music?.isNew == false)
    }

    @Test
    fun editMusic() = runTest {
        musicDAO.addNewMusic(
            "J-Wright - Winter's Over (Prod. Beatcraze)",
            "mp3",
            "Winter's Over (Prod. Beatcraze)",
            "J-Wright",
            "dW0VLeJ83uE",
            "SwagyTracks",
            "2018",
            listOf("Rap", "Chill"),
            true
        )
        musicDAO.editMusic(
            "J-Wright - Winter's Over (Prod. Beatcraze)",
            "wav",
            "Baileys",
            "Upgrade",
            "o-anXVb4Yms",
            "UpgradeMusic1",
            "2019",
            false
        )
        val music = musicDAO.music("J-Wright - Winter's Over (Prod. Beatcraze)")
        assert(music?.fileName == "J-Wright - Winter's Over (Prod. Beatcraze)")
        assert(music?.fileExtension == "wav")
        assert(music?.title == "Baileys")
        assert(music?.artist == "Upgrade")
        assert(music?.platformId == "o-anXVb4Yms")
        assert(music?.uploaderId == "UpgradeMusic1")
        assert(music?.uploadDate == "2019")
        assert(music?.isNew == false)
    }

    @Test
    fun deleteMusic() = runTest {
        musicDAO.addNewMusic(
            "J-Wright - Winter's Over (Prod. Beatcraze)",
            "mp3",
            "Winter's Over (Prod. Beatcraze)",
            "J-Wright",
            "dW0VLeJ83uE",
            "SwagyTracks",
            "2018",
            listOf("Rap", "Chill"),
            false
        )
        val music = musicDAO.music("J-Wright - Winter's Over (Prod. Beatcraze)")
        assert(music != null)
        musicDAO.deleteMusic("J-Wright - Winter's Over (Prod. Beatcraze)")
        val musicList = musicDAO.allMusic()
        assert(musicList.isEmpty())
    }

    @Test
    fun deleteAllMusic() = runTest {
        musicDAO.addNewMusic(
            "J-Wright - Winter's Over (Prod. Beatcraze)",
            "mp3",
            "Winter's Over (Prod. Beatcraze)",
            "J-Wright",
            "dW0VLeJ83uE",
            "SwagyTracks",
            "2018",
            listOf("Rap", "Chill"),
            true
        )
        musicDAO.addNewMusic(
            "Retromigration - BO",
            "mp3",
            "BO",
            "Retromigration",
            "uSafW5cudGQ",
            "DelicieuseMusique",
            "2023",
            listOf("House", "Chill"),
            true
        )
        val music = musicDAO.allMusic()
        assert(music.size == 2)
        musicDAO.deleteAllMusic()
        val musicList = musicDAO.allMusic()
        assert(musicList.isEmpty())
    }

    @Test
    fun removeMusicFromAllPlaylists() = runTest {
        val playlistDAO = PlaylistDAOImpl()
        playlistDAO.addNewPlaylist("Best Of WilliTracks")
        playlistDAO.addNewPlaylist("WilliChill")
        musicDAO.addNewMusic(
            "J-Wright - Winter's Over (Prod. Beatcraze)",
            "mp3",
            "Winter's Over (Prod. Beatcraze)",
            "J-Wright",
            "dW0VLeJ83uE",
            "SwagyTracks",
            "2018",
            listOf("Rap", "Chill"),
            true
        )

        playlistDAO.addMusicToPlaylist("J-Wright - Winter's Over (Prod. Beatcraze)", "Best Of WilliTracks")
        var bestOfWilliTracks = playlistDAO.playlist("Best Of WilliTracks")
        assert(bestOfWilliTracks?.music?.size == 1)
        assert(bestOfWilliTracks!!.music[0] == "J-Wright - Winter's Over (Prod. Beatcraze)")

        playlistDAO.addMusicToPlaylist("J-Wright - Winter's Over (Prod. Beatcraze)", "WilliChill")
        var williChill = playlistDAO.playlist("WilliChill")
        assert(williChill?.music?.size == 1)
        assert(williChill!!.music[0] == "J-Wright - Winter's Over (Prod. Beatcraze)")

        musicDAO.removeMusicFromAllPlaylists("J-Wright - Winter's Over (Prod. Beatcraze)")
        bestOfWilliTracks = playlistDAO.playlist("Best Of WilliTracks")
        assert(bestOfWilliTracks?.music?.size == 0)
        williChill = playlistDAO.playlist("WilliChill")
        assert(williChill?.music?.size == 0)

        playlistDAO.deletePlaylist("Best Of WilliTracks")
        playlistDAO.deletePlaylist("WilliChill")
    }

    @Test
    fun tagsFromMusic() = runTest {
        musicDAO.addNewMusic(
            "J-Wright - Winter's Over (Prod. Beatcraze)",
            "mp3",
            "Winter's Over (Prod. Beatcraze)",
            "J-Wright",
            "dW0VLeJ83uE",
            "SwagyTracks",
            "2018",
            listOf("Rap", "Chill"),
            true
        )
        val tags = musicDAO.tagsFromMusic("J-Wright - Winter's Over (Prod. Beatcraze)")
        assert(tags.size == 2)
        assert(tags[0] == "Rap")
        assert(tags[1] == "Chill")
    }

    @Test
    fun addTagToMusic() = runTest {
        musicDAO.addNewMusic(
            "J-Wright - Winter's Over (Prod. Beatcraze)",
            "mp3",
            "Winter's Over (Prod. Beatcraze)",
            "J-Wright",
            "dW0VLeJ83uE",
            "SwagyTracks",
            "2018",
            listOf("Rap"),
            true
        )
        var tags = musicDAO.tagsFromMusic("J-Wright - Winter's Over (Prod. Beatcraze)")
        assert(tags.size == 1)
        assert(tags[0] == "Rap")

        musicDAO.addTagToMusic("Chill", "J-Wright - Winter's Over (Prod. Beatcraze)")
        tags = musicDAO.tagsFromMusic("J-Wright - Winter's Over (Prod. Beatcraze)")
        assert(tags.size == 2)
        assert(tags[0] == "Rap")
        assert(tags[1] == "Chill")
    }

    @Test
    fun removeTagFromMusic() = runTest {
        musicDAO.addNewMusic(
            "J-Wright - Winter's Over (Prod. Beatcraze)",
            "mp3",
            "Winter's Over (Prod. Beatcraze)",
            "J-Wright",
            "dW0VLeJ83uE",
            "SwagyTracks",
            "2018",
            listOf("Rap", "Chill"),
            true
        )
        var tags = musicDAO.tagsFromMusic("J-Wright - Winter's Over (Prod. Beatcraze)")
        assert(tags.size == 2)
        assert(tags[0] == "Rap")
        assert(tags[1] == "Chill")

        musicDAO.removeTagFromMusic("Rap", "J-Wright - Winter's Over (Prod. Beatcraze)")
        tags = musicDAO.tagsFromMusic("J-Wright - Winter's Over (Prod. Beatcraze)")
        assert(tags.size == 1)
        assert(tags[0] == "Chill")
    }
}