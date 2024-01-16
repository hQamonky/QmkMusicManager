package com.qmk.musicmanager.database.dao

import com.qmk.musicmanager.database.model.DatabaseFactory
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class PlaylistDAOImplTest {

    private lateinit var playlistDAO: PlaylistDAO

    @Before
    fun setUp() {
        DatabaseFactory.init()
        playlistDAO = PlaylistDAOImpl()
    }

    @After
    fun tearDown() {
        runBlocking {
            playlistDAO.deleteAllPlaylists()
        }
    }

    @Test
    fun allPlaylists() = runTest {
        playlistDAO.addNewPlaylist("Casual")
        playlistDAO.addNewPlaylist("Chill")
        val playlists = playlistDAO.allPlaylists()
        assert(playlists.size == 2)
    }

    @Test
    fun playlist() = runTest {
        playlistDAO.addNewPlaylist("Casual")
        val playlist = playlistDAO.playlist("Casual")
        assert(playlist?.name == "Casual")
    }

    @Test
    fun addNewPlaylist() = runTest {
        val playlist = playlistDAO.addNewPlaylist("Casual")
        assert(playlist?.name == "Casual")
    }

    @Test
    fun renamePlaylist() = runTest {
        playlistDAO.addNewPlaylist("Casual")
        playlistDAO.renamePlaylist("Casual", "Chill")
        val playlist = playlistDAO.playlist("Chill")
        assert(playlist?.name == "Chill")
    }

    @Test
    fun deletePlaylist() = runTest {
        var playlist = playlistDAO.addNewPlaylist("Casual")
        assert(playlist?.name == "Casual")
        playlistDAO.deletePlaylist("Casual")
        playlist = playlistDAO.playlist("Casual")
        assert(playlist == null)
    }

    @Test
    fun deleteAllPlaylists() = runTest {
        playlistDAO.addNewPlaylist("Casual")
        playlistDAO.addNewPlaylist("Chill")
        var playlists = playlistDAO.allPlaylists()
        assert(playlists.size == 2)
        playlistDAO.deleteAllPlaylists()
        playlists = playlistDAO.allPlaylists()
        assert(playlists.isEmpty())
    }

    @Test
    fun musicFromPlaylist() = runTest {
        playlistDAO.addNewPlaylist("Casual")
        playlistDAO.addMusicToPlaylist("J-Wright - Winter's Over (Prod. Beatcraze)", "Casual")
        playlistDAO.addMusicToPlaylist("Retromigration - BO", "Casual")
        val musicList = playlistDAO.musicFromPlaylist("Casual")
        assert(musicList.size == 2)
        assert(musicList.contains("J-Wright - Winter's Over (Prod. Beatcraze)"))
        assert(musicList.contains("Retromigration - BO"))
    }

    @Test
    fun addMusicToPlaylist() = runTest {
        playlistDAO.addNewPlaylist("Casual")
        playlistDAO.addMusicToPlaylist("J-Wright - Winter's Over (Prod. Beatcraze)", "Casual")
        val playlist = playlistDAO.playlist("Casual")
        assert(playlist?.music?.size == 1)
        assert(playlist?.music?.contains("J-Wright - Winter's Over (Prod. Beatcraze)") == true)
    }

    @Test
    fun removeMusicFromPlaylist() = runTest {
        playlistDAO.addNewPlaylist("Casual")
        playlistDAO.addMusicToPlaylist("J-Wright - Winter's Over (Prod. Beatcraze)", "Casual")
        playlistDAO.addMusicToPlaylist("Retromigration - BO", "Casual")
        var musicList = playlistDAO.musicFromPlaylist("Casual")
        assert(musicList.size == 2)
        playlistDAO.removeMusicFromPlaylist("J-Wright - Winter's Over (Prod. Beatcraze)", "Casual")
        musicList = playlistDAO.musicFromPlaylist("Casual")
        assert(musicList.size == 1)
        assert(!musicList.contains("J-Wright - Winter's Over (Prod. Beatcraze)"))
    }
}