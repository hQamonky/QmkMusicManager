package com.qmk.musicmanager.database.dao

import com.qmk.musicmanager.database.model.DatabaseFactory
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class PlatformPlaylistDAOImplTest {

    private lateinit var platformPlaylistDAO: PlatformPlaylistDAO
    private lateinit var playlistDAO: PlaylistDAO

    @Before
    fun setUp() {
        DatabaseFactory.init()
        platformPlaylistDAO = PlatformPlaylistDAOImpl()
        playlistDAO = PlaylistDAOImpl()
    }

    @After
    fun tearDown() {
        runBlocking {
            platformPlaylistDAO.deleteAllPlaylists()
            playlistDAO.deleteAllPlaylists()
        }
    }

    @Test
    fun allPlaylists() = runTest {
        platformPlaylistDAO.addNewPlaylist(
            id = "PLCVGGn6GhhDsqtlR4F1F9hbllr2My19Uq",
            name = "Best Of WilliTracks 2023 part 2",
            platform = "youtube",
            playlists = listOf("Casual", "QMK Music")
        )
        platformPlaylistDAO.addNewPlaylist(
            id = "PLCVGGn6GhhDuOGUVJyni23Vig3YknDBHZ",
            name = "WilliChill 2023 part 2",
            platform = "youtube",
            playlists = listOf("Chill", "QMK Music")
        )
        val plPlaylists = platformPlaylistDAO.allPlaylists()
        assert(plPlaylists.size == 2)
    }

    @Test
    fun playlist() = runTest {
        val plPlaylist = platformPlaylistDAO.addNewPlaylist(
            id = "PLCVGGn6GhhDsqtlR4F1F9hbllr2My19Uq",
            name = "Best Of WilliTracks 2023 part 2",
            platform = "youtube",
            playlists = listOf("Casual", "QMK Music")
        )
        assert(plPlaylist != null)
        val plToCheck = platformPlaylistDAO.playlist(plPlaylist!!.id)
        assert(plToCheck?.id == "PLCVGGn6GhhDsqtlR4F1F9hbllr2My19Uq")
        assert(plToCheck?.name == "Best Of WilliTracks 2023 part 2")
        assert(plToCheck?.platform == "youtube")
        assert(plToCheck?.playlists?.size == 2)
    }

    @Test
    fun addNewPlaylist() = runTest {
        val plPlaylist = platformPlaylistDAO.addNewPlaylist(
            id = "PLCVGGn6GhhDsqtlR4F1F9hbllr2My19Uq",
            name = "Best Of WilliTracks 2023 part 2",
            platform = "youtube",
            playlists = listOf("Casual", "QMK Music")
        )
        assert(plPlaylist?.id == "PLCVGGn6GhhDsqtlR4F1F9hbllr2My19Uq")
        assert(plPlaylist?.name == "Best Of WilliTracks 2023 part 2")
        assert(plPlaylist?.platform == "youtube")
        assert(plPlaylist?.playlists?.size == 2)
    }

    @Test
    fun editPlaylist() = runTest {
        var plPlaylist = platformPlaylistDAO.addNewPlaylist(
            id = "PLCVGGn6GhhDsqtlR4F1F9hbllr2My19Uq",
            name = "Best Of WilliTracks 2023 part 2",
            platform = "youtube",
            playlists = listOf("Casual", "QMK Music")
        )
        assert(plPlaylist?.playlists?.size == 2)
        platformPlaylistDAO.editPlaylist(plPlaylist!!.id, listOf("Chill"))
        plPlaylist = platformPlaylistDAO.playlist(plPlaylist.id)
        assert(plPlaylist?.playlists?.size == 1)
        assert(plPlaylist!!.playlists[0] == "Chill")
    }

    @Test
    fun deletePlaylist() = runTest {
        var plPlaylist = platformPlaylistDAO.addNewPlaylist(
            id = "PLCVGGn6GhhDsqtlR4F1F9hbllr2My19Uq",
            name = "Best Of WilliTracks 2023 part 2",
            platform = "youtube",
            playlists = listOf("Casual", "QMK Music")
        )
        assert(plPlaylist != null)
        platformPlaylistDAO.deletePlaylist(plPlaylist!!.id)
        plPlaylist = platformPlaylistDAO.playlist(plPlaylist.id)
        assert(plPlaylist == null)
    }

    @Test
    fun deleteAllPlaylists() = runTest {
        platformPlaylistDAO.addNewPlaylist(
            id = "PLCVGGn6GhhDsqtlR4F1F9hbllr2My19Uq",
            name = "Best Of WilliTracks 2023 part 2",
            platform = "youtube",
            playlists = listOf("Casual", "QMK Music")
        )
        platformPlaylistDAO.addNewPlaylist(
            id = "PLCVGGn6GhhDuOGUVJyni23Vig3YknDBHZ",
            name = "WilliChill 2023 part 2",
            platform = "youtube",
            playlists = listOf("Chill", "QMK Music")
        )
        var plPlaylists = platformPlaylistDAO.allPlaylists()
        assert(plPlaylists.size == 2)
        platformPlaylistDAO.deleteAllPlaylists()
        plPlaylists = platformPlaylistDAO.allPlaylists()
        assert(plPlaylists.isEmpty())
    }

    @Test
    fun playlistsFromPlPlaylist() = runTest {
        val plPlaylist = platformPlaylistDAO.addNewPlaylist(
            id = "PLCVGGn6GhhDsqtlR4F1F9hbllr2My19Uq",
            name = "Best Of WilliTracks 2023 part 2",
            platform = "youtube",
            playlists = listOf("Casual", "QMK Music")
        )
        assert(plPlaylist?.id == "PLCVGGn6GhhDsqtlR4F1F9hbllr2My19Uq")
        val bestOfWilliTracksPlaylists = platformPlaylistDAO.playlistsFromPlPlaylist(plPlaylist!!.id)
        assert(bestOfWilliTracksPlaylists.size == 2)
        assert(bestOfWilliTracksPlaylists.contains("Casual"))
        assert(bestOfWilliTracksPlaylists.contains("QMK Music"))
    }

    @Test
    fun plPlaylistsFromPlaylist() = runTest {
        platformPlaylistDAO.addNewPlaylist(
            id = "PLCVGGn6GhhDsqtlR4F1F9hbllr2My19Uq",
            name = "Best Of WilliTracks 2023 part 2",
            platform = "youtube",
            playlists = listOf("Casual", "QMK Music")
        )
        platformPlaylistDAO.addNewPlaylist(
            id = "PLCVGGn6GhhDuOGUVJyni23Vig3YknDBHZ",
            name = "WilliChill 2023 part 2",
            platform = "youtube",
            playlists = listOf("Chill", "QMK Music")
        )
        val qmkMusic = platformPlaylistDAO.plPlaylistsFromPlaylist("QMK Music")
        assert(qmkMusic.size == 2)
        assert(qmkMusic.contains("PLCVGGn6GhhDsqtlR4F1F9hbllr2My19Uq"))
        assert(qmkMusic.contains("PLCVGGn6GhhDuOGUVJyni23Vig3YknDBHZ"))
    }

    @Test
    fun addPlaylistToPlaylist() = runTest {
        platformPlaylistDAO.addNewPlaylist(
            id = "PLCVGGn6GhhDsqtlR4F1F9hbllr2My19Uq",
            name = "Best Of WilliTracks 2023 part 2",
            platform = "youtube",
            playlists = listOf("Casual")
        )
        playlistDAO.addNewPlaylist("QMK Music")
        platformPlaylistDAO.addPlaylistToPlaylist("QMK Music", "PLCVGGn6GhhDsqtlR4F1F9hbllr2My19Uq")
        val qmkMusic = platformPlaylistDAO.plPlaylistsFromPlaylist("QMK Music")
        assert(qmkMusic.contains("PLCVGGn6GhhDsqtlR4F1F9hbllr2My19Uq"))
    }

    @Test
    fun removePlaylistFromPlaylist() = runTest {
        var plPlaylist = platformPlaylistDAO.addNewPlaylist(
            id = "PLCVGGn6GhhDsqtlR4F1F9hbllr2My19Uq",
            name = "Best Of WilliTracks 2023 part 2",
            platform = "youtube",
            playlists = listOf("Casual", "QMK Music")
        )
        assert(plPlaylist?.playlists?.contains("Casual") == true)
        val success = platformPlaylistDAO.removePlaylistFromPlaylist("Casual", plPlaylist!!.id)
        println(success)
        plPlaylist = platformPlaylistDAO.playlist(plPlaylist.id)
        assert(plPlaylist?.playlists?.size == 1)
        assert(plPlaylist?.playlists?.contains("Casual") == false)
    }
}