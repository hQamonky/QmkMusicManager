package com.qmk.musicmanager.domain.manager

import com.qmk.musicmanager.domain.model.Music
import com.qmk.musicmanager.domain.model.Settings
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

class MopidyManagerTest {
    private val manager: MopidyManager = MopidyManager()
    private val configurationManager: ConfigurationManager = ConfigurationManager()

    @Before
    fun setUp() {
        configurationManager.setConfiguration(
            Settings(
                audioFolder = "src/test/MusicTestDir/Audio",
                playlistsFolder = "src/test/MusicTestDir/Playlists",
                archiveFolder = "src/test/MusicTestDir/Archive"
            )
        )
    }

    @After
    fun tearDown() {
        configurationManager.setConfiguration(Settings())
        File("src/test/MusicTestDir/Playlists").deleteRecursively()
    }


    @Test
    fun createPlaylistTest() {
        // Create playlist
        assert(manager.createPlaylist("MyPlaylist") == "Playlist MyPlaylist created.")
        assert(manager.createPlaylist("MyPlaylist") == "MyPlaylist already exists.")
        val playlistFile = File("src/test/MusicTestDir/Playlists/Mopidy/MyPlaylist.m3u8")
        assert(playlistFile.exists())
        val playlistFileLines = playlistFile.readLines().map { it }
        assert(playlistFileLines.isEmpty())
    }

    @Test
    fun renamePlaylistTest() {
        manager.createPlaylist("MyPlaylist")
        val oldPlaylist = File("src/test/MusicTestDir/Playlists/Mopidy/MyPlaylist.m3u8")
        assert(oldPlaylist.exists())
        manager.renamePlaylist("MyPlaylist", "New playlist name")
        val playlistFile = File("src/test/MusicTestDir/Playlists/Mopidy/New playlist name.m3u8")
        assert(playlistFile.exists())
        val playlistFileLines = playlistFile.readLines().map { it }
        assert(playlistFileLines.isEmpty())
    }

    @Test
    fun addMusicToPlaylistTest() {
        manager.createPlaylist("My Playlist")
        manager.addMusicToPlaylist(
            Music(
                fileName = "music - file",
                title = "",
                artist = "",
                platformId = "",
                uploaderId = "",
                uploadDate = ""
            ),
            "My Playlist"
        )
        val playlistFile = File("src/test/MusicTestDir/Playlists/Mopidy/My Playlist.m3u8")
        assert(playlistFile.exists())
        val playlistFileLines = playlistFile.readLines().map { it }
        assert(playlistFileLines.size == 1)
        assert(playlistFileLines[0] == "local:track:music%20-%20file.mp3")
        manager.addMusicToPlaylist(
            Music(
                fileName = "other - music",
                title = "",
                artist = "",
                platformId = "",
                uploaderId = "",
                uploadDate = ""
            ),
            "My Playlist"
        )
        val updatedPlaylistFile = playlistFile.readLines().map { it }
        assert(updatedPlaylistFile.size == 2)
        assert(playlistFileLines[0] == "local:track:music%20-%20file.mp3")
        assert(updatedPlaylistFile[1] == "local:track:other%20-%20music.mp3")
    }

    @Test
    fun getFilesFromPlaylistTest() {
        manager.createPlaylist("My Playlist")
        manager.addMusicToPlaylist(
            Music(
                fileName = "music - file",
                title = "",
                artist = "",
                platformId = "",
                uploaderId = "",
                uploadDate = ""
            ),
            "My Playlist"
        )
        val playlistFile = File("src/test/MusicTestDir/Playlists/Mopidy/My Playlist.m3u8")
        assert(playlistFile.exists())
        val playlistFileLines = playlistFile.readLines().map { it }
        assert(playlistFileLines.size == 1)
        assert(playlistFileLines[0] == "local:track:music%20-%20file.mp3")
        manager.addMusicToPlaylist(
            Music(
                fileName = "other - music",
                title = "",
                artist = "",
                platformId = "",
                uploaderId = "",
                uploadDate = ""
            ),
            "My Playlist"
        )
        val files = manager.getFilesFromPlaylist("My Playlist")
        assert(files.size == 2)
        assert(File(files[0]).name == "music - file.mp3")
        assert(File(files[1]).name == "other - music.mp3")
    }

    @Test
    fun archiveMusicTest() {
        val playlist1 = "playlist1"
        val playlist2 = "playlist2"
        val archives = "Archives"
        val playlist1File = File("src/test/MusicTestDir/Playlists/Mopidy/$playlist1.m3u8")
        val playlist2File = File("src/test/MusicTestDir/Playlists/Mopidy/$playlist2.m3u8")
        val archivesPlaylist = File("src/test/MusicTestDir/Playlists/Mopidy/$archives.m3u8")
        val music1 =
            Music(fileName = "music1", title = "", artist = "", platformId = "", uploaderId = "", uploadDate = "")
        val music2 =
            Music(fileName = "music2", title = "", artist = "", platformId = "", uploaderId = "", uploadDate = "")
        val music3 =
            Music(fileName = "music3", title = "", artist = "", platformId = "", uploaderId = "", uploadDate = "")
        manager.addMusicToPlaylist(music1, playlist1)
        manager.addMusicToPlaylist(music2, playlist1)
        manager.addMusicToPlaylist(music3, playlist1)
        manager.addMusicToPlaylist(music1, playlist2)
        manager.addMusicToPlaylist(music2, playlist2)

        manager.addMusicToPlaylist(music1, archives)
        manager.archiveMusic()

        val playlist1FileLines = playlist1File.readLines().map { it }
        assert(playlist1FileLines.size == 2)
        assert(playlist1FileLines[0] == "local:track:music2.mp3")
        assert(playlist1FileLines[1] == "local:track:music3.mp3")

        val playlist2FileLines = playlist2File.readLines().map { it }
        assert(playlist2FileLines.size == 1)
        assert(playlist2FileLines[0] == "local:track:music2.mp3")

        val archivePlaylistLines = archivesPlaylist.readLines().map { it }
        assert(archivePlaylistLines.size == 1)
        assert(archivePlaylistLines[0] == "local:track:$archives:music1.mp3")


        manager.addMusicToPlaylist(music3, archives)
        manager.archiveMusic()

        val newPlaylist1FileLines = playlist1File.readLines().map { it }
        assert(newPlaylist1FileLines.size == 1)
        assert(newPlaylist1FileLines[0] == "local:track:music2.mp3")

        val newPlaylist2FileLines = playlist2File.readLines().map { it }
        assert(newPlaylist2FileLines.size == 1)
        assert(newPlaylist2FileLines[0] == "local:track:music2.mp3")

        val newArchivePlaylistLines = archivesPlaylist.readLines().map { it }
        assert(newArchivePlaylistLines.size == 2)
        assert(newArchivePlaylistLines[0] == "local:track:$archives:music1.mp3")
        assert(newArchivePlaylistLines[1] == "local:track:$archives:music3.mp3")
    }

    @Test
    fun getMusicToArchiveTest() {
        val archives = "Archives"
        val music1 =
            Music(fileName = "music1", title = "", artist = "", platformId = "", uploaderId = "", uploadDate = "")
        val music2 =
            Music(fileName = "music2", title = "", artist = "", platformId = "", uploaderId = "", uploadDate = "")

        manager.addMusicToPlaylist(music1, archives)
        manager.archiveMusic()
        manager.addMusicToPlaylist(music2, archives)

        val musicToArchive = manager.getMusicToArchive()
        assert(musicToArchive.size == 1)
        assert(File(musicToArchive[0]).name == "music2.mp3")
    }

    @Test
    fun convertPowerAmpPlaylistTest() {
        val powerAmpManager = PowerAmpManager()
        val playlist = "playlist1"
        val music1 =
            Music(fileName = "music 1", title = "", artist = "", platformId = "", uploaderId = "", uploadDate = "")
        val music2 =
            Music(fileName = "music 2", title = "", artist = "", platformId = "", uploaderId = "", uploadDate = "")
        powerAmpManager.addMusicToPlaylist(music1, playlist)
        powerAmpManager.addMusicToPlaylist(music2, playlist)

        manager.convertPowerAmpPlaylist(playlist)

        val playlistFile = File("src/test/MusicTestDir/Playlists/Mopidy/$playlist.m3u8")
        val updatedPlaylistFile = playlistFile.readLines().map { it }
        assert(updatedPlaylistFile.size == 2)
        assert(updatedPlaylistFile[0] == "local:track:music%201.mp3")
        assert(updatedPlaylistFile[1] == "local:track:music%202.mp3")
    }

    @Test
    fun mergeMopidyPlaylistTest() {
        val powerAmpManager = PowerAmpManager()

        val playlist = "playlist1"
        val music1 =
            Music(fileName = "music 1", title = "", artist = "", platformId = "", uploaderId = "", uploadDate = "")
        val music2 =
            Music(fileName = "music 2", title = "", artist = "", platformId = "", uploaderId = "", uploadDate = "")
        manager.addMusicToPlaylist(music1, playlist)
        powerAmpManager.addMusicToPlaylist(music2, playlist)

        manager.mergePowerAmpPlaylist(playlist)

        val playlistFile = File("src/test/MusicTestDir/Playlists/Mopidy/$playlist.m3u8")
        val updatedPlaylistFile = playlistFile.readLines().map { it }
        assert(updatedPlaylistFile.size == 2)
        assert(updatedPlaylistFile[0] == "local:track:music%201.mp3")
        assert(updatedPlaylistFile[1] == "local:track:music%202.mp3")
    }
}