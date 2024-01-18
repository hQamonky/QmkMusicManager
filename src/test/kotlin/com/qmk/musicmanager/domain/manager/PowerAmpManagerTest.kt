package com.qmk.musicmanager.domain.manager

import com.qmk.musicmanager.domain.model.Music
import com.qmk.musicmanager.domain.model.Settings
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

internal class PowerAmpManagerTest {
    private val manager: PowerAmpManager = PowerAmpManager()
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
        File("src/test/MusicTestDir/Playlists").deleteRecursively()
    }

    @After
    fun tearDown() {
        configurationManager.setConfiguration(Settings())
        File("src/test/MusicTestDir/Playlists").deleteRecursively()
    }

    @Test
    fun createPlaylist() {
        assert(manager.createPlaylist("MyPlaylist") == "Playlist MyPlaylist created.")
        assert(manager.createPlaylist("MyPlaylist") == "MyPlaylist already exists.")
        val playlistFile = File("src/test/MusicTestDir/Playlists/PowerAmp/MyPlaylist.m3u8")
        assert(playlistFile.exists())
        val playlistFileLines = playlistFile.readLines().map { it }
        assert(playlistFileLines.size == 1)
        assert(playlistFileLines[0] == "#EXTM3U")
    }

    @Test
    fun renamePlaylist() {
        manager.createPlaylist("MyPlaylist")
        val oldPlaylist = File("src/test/MusicTestDir/Playlists/PowerAmp/MyPlaylist.m3u8")
        assert(oldPlaylist.exists())
        manager.renamePlaylist("MyPlaylist", "New playlist name")
        val playlistFile = File("src/test/MusicTestDir/Playlists/PowerAmp/New playlist name.m3u8")
        assert(playlistFile.exists())
        val playlistFileLines = playlistFile.readLines().map { it }
        assert(playlistFileLines.size == 1)
        assert(playlistFileLines[0] == "#EXTM3U")
    }

    @Test
    fun addMusicToPlaylist() {
        manager.createPlaylist("MyPlaylist")
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
        val playlistFile = File("src/test/MusicTestDir/Playlists/PowerAmp/My Playlist.m3u8")
        assert(playlistFile.exists())
        val playlistFileLines = playlistFile.readLines().map { it }
        assert(playlistFileLines.size == 3)
        assert(playlistFileLines[0] == "#EXTM3U")
        assert(playlistFileLines[1] == "#EXT-X-RATING:0")
        assert(playlistFileLines[2] == "primary/Audio/music - file.mp3")
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
        assert(updatedPlaylistFile.size == 5)
        assert(updatedPlaylistFile[0] == "#EXTM3U")
        assert(updatedPlaylistFile[1] == "#EXT-X-RATING:0")
        assert(updatedPlaylistFile[2] == "primary/Audio/music - file.mp3")
        assert(updatedPlaylistFile[3] == "#EXT-X-RATING:0")
        assert(updatedPlaylistFile[4] == "primary/Audio/other - music.mp3")
    }

    @Test
    fun getFilesFromPlaylist() {
        manager.createPlaylist("MyPlaylist")
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
        val playlistFile = File("src/test/MusicTestDir/Playlists/PowerAmp/My Playlist.m3u8")
        assert(playlistFile.exists())
        val playlistFileLines = playlistFile.readLines().map { it }
        assert(playlistFileLines.size == 3)
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
        assert(updatedPlaylistFile.size == 5)
        val files = manager.getFilesFromPlaylist("My Playlist")
        assert(files.size == 2)
        assert(File(files[0]).name == "music - file.mp3")
        assert(File(files[1]).name == "other - music.mp3")
    }

    @Test
    fun archiveMusic() {
        val playlist1 = "playlist1"
        val playlist2 = "playlist2"
        val archives = "Archives"
        val playlist1File = File("src/test/MusicTestDir/Playlists/PowerAmp/$playlist1.m3u8")
        val playlist2File = File("src/test/MusicTestDir/Playlists/PowerAmp/$playlist2.m3u8")
        val archivesPlaylist = File("src/test/MusicTestDir/Playlists/PowerAmp/$archives.m3u8")
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
        assert(playlist1FileLines.size == 5)
        assert(playlist1FileLines[0] == "#EXTM3U")
        assert(playlist1FileLines[1] == "#EXT-X-RATING:0")
        assert(playlist1FileLines[2] == "primary/Audio/music2.mp3")
        assert(playlist1FileLines[3] == "#EXT-X-RATING:0")
        assert(playlist1FileLines[4] == "primary/Audio/music3.mp3")

        val playlist2FileLines = playlist2File.readLines().map { it }
        assert(playlist2FileLines.size == 3)
        assert(playlist2FileLines[0] == "#EXTM3U")
        assert(playlist2FileLines[1] == "#EXT-X-RATING:0")
        assert(playlist2FileLines[2] == "primary/Audio/music2.mp3")

        val archivePlaylistLines = archivesPlaylist.readLines().map { it }
        assert(archivePlaylistLines.size == 3)
        assert(archivePlaylistLines[0] == "#EXTM3U")
        assert(archivePlaylistLines[1] == "#EXT-X-RATING:0")
        assert(archivePlaylistLines[2] == "primary/Audio/$archives/music1.mp3")


        manager.addMusicToPlaylist(music3, archives)
        manager.archiveMusic()

        val newPlaylist1FileLines = playlist1File.readLines().map { it }
        assert(newPlaylist1FileLines.size == 3)
        assert(newPlaylist1FileLines[0] == "#EXTM3U")
        assert(newPlaylist1FileLines[1] == "#EXT-X-RATING:0")
        assert(newPlaylist1FileLines[2] == "primary/Audio/music2.mp3")

        val newPlaylist2FileLines = playlist2File.readLines().map { it }
        assert(newPlaylist2FileLines.size == 3)
        assert(newPlaylist2FileLines[0] == "#EXTM3U")
        assert(newPlaylist2FileLines[1] == "#EXT-X-RATING:0")
        assert(newPlaylist2FileLines[2] == "primary/Audio/music2.mp3")

        val newArchivePlaylistLines = archivesPlaylist.readLines().map { it }
        assert(newArchivePlaylistLines.size == 5)
        assert(newArchivePlaylistLines[0] == "#EXTM3U")
        assert(newArchivePlaylistLines[1] == "#EXT-X-RATING:0")
        assert(newArchivePlaylistLines[2] == "primary/Audio/$archives/music1.mp3")
        assert(newArchivePlaylistLines[3] == "#EXT-X-RATING:0")
        assert(newArchivePlaylistLines[4] == "primary/Audio/$archives/music3.mp3")
    }

    @Test
    fun getMusicToArchive() {
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
    fun convertMopidyPlaylist() {
        val mopidyManager = MopidyManager()
        val playlist = "playlist1"
        val music1 =
            Music(fileName = "music 1", title = "", artist = "", platformId = "", uploaderId = "", uploadDate = "")
        val music2 =
            Music(fileName = "music 2", title = "", artist = "", platformId = "", uploaderId = "", uploadDate = "")
        mopidyManager.addMusicToPlaylist(music1, playlist)
        mopidyManager.addMusicToPlaylist(music2, playlist)

        manager.convertMopidyPlaylist(playlist)

        val playlistFile = File("src/test/MusicTestDir/Playlists/PowerAmp/$playlist.m3u8")
        val updatedPlaylistFile = playlistFile.readLines().map { it }
        assert(updatedPlaylistFile.size == 5)
        assert(updatedPlaylistFile[0] == "#EXTM3U")
        assert(updatedPlaylistFile[1] == "#EXT-X-RATING:0")
        assert(updatedPlaylistFile[2] == "primary/Audio/music 1.mp3")
        assert(updatedPlaylistFile[3] == "#EXT-X-RATING:0")
        assert(updatedPlaylistFile[4] == "primary/Audio/music 2.mp3")
    }

    @Test
    fun mergeMopidyPlaylist() {
        val mopidyManager = MopidyManager()
        val playlist = "playlist1"
        val music1 =
            Music(fileName = "music 1", title = "", artist = "", platformId = "", uploaderId = "", uploadDate = "")
        val music2 =
            Music(fileName = "music 2", title = "", artist = "", platformId = "", uploaderId = "", uploadDate = "")
        manager.addMusicToPlaylist(music1, playlist)
        mopidyManager.addMusicToPlaylist(music2, playlist)

        manager.mergeMopidyPlaylist(playlist)

        val playlistFile = File("src/test/MusicTestDir/Playlists/PowerAmp/$playlist.m3u8")
        val updatedPlaylistFile = playlistFile.readLines().map { it }
        assert(updatedPlaylistFile.size == 5)
        assert(updatedPlaylistFile[0] == "#EXTM3U")
        assert(updatedPlaylistFile[1] == "#EXT-X-RATING:0")
        assert(updatedPlaylistFile[2] == "primary/Audio/music 1.mp3")
        assert(updatedPlaylistFile[3] == "#EXT-X-RATING:0")
        assert(updatedPlaylistFile[4] == "primary/Audio/music 2.mp3")
    }

    @Test
    fun removeMusicFromPlaylist() {
        manager.createPlaylist("My Playlist")
        val music1 = Music(
            fileName = "music - file",
            title = "",
            artist = "",
            platformId = "",
            uploaderId = "",
            uploadDate = ""
        )
        val music2 = Music(
            fileName = "other - music",
            title = "",
            artist = "",
            platformId = "",
            uploaderId = "",
            uploadDate = ""
        )
        manager.addMusicToPlaylist(music1, "My Playlist")
        manager.addMusicToPlaylist(music2, "My Playlist")
        var playlistFileLines = File("src/test/MusicTestDir/Playlists/PowerAmp/My Playlist.m3u8").readLines().map { it }
        assert(playlistFileLines.size == 5)
        assert(playlistFileLines[0] == "#EXTM3U")
        assert(playlistFileLines[1] == "#EXT-X-RATING:0")
        assert(playlistFileLines[2] == "primary/Audio/music - file.mp3")
        assert(playlistFileLines[3] == "#EXT-X-RATING:0")
        assert(playlistFileLines[4] == "primary/Audio/other - music.mp3")

        manager.removeMusicFromPlaylist(music1, "My Playlist")
        playlistFileLines = File("src/test/MusicTestDir/Playlists/PowerAmp/My Playlist.m3u8").readLines().map { it }
        assert(playlistFileLines.size == 3)
        assert(playlistFileLines[0] == "#EXTM3U")
        assert(playlistFileLines[1] == "#EXT-X-RATING:0")
        assert(playlistFileLines[2] == "primary/Audio/other - music.mp3")

        manager.removeMusicFromPlaylist(music2, "My Playlist")
        playlistFileLines = File("src/test/MusicTestDir/Playlists/PowerAmp/My Playlist.m3u8").readLines().map { it }
        assert(playlistFileLines.size == 1)
        assert(playlistFileLines[0] == "#EXTM3U")
    }

    @Test
    fun isMusicInPlaylist() {
        val playlist = "MyPlaylist"
        manager.createPlaylist(playlist)
        val music1 = Music(
            fileName = "music - file",
            title = "",
            artist = "",
            platformId = "",
            uploaderId = "",
            uploadDate = ""
        )
        val music2 = Music(
            fileName = "other - music",
            title = "",
            artist = "",
            platformId = "",
            uploaderId = "",
            uploadDate = ""
        )
        manager.addMusicToPlaylist(music1, playlist)
        val playlistFileLines = File("src/test/MusicTestDir/Playlists/PowerAmp/$playlist.m3u8").readLines().map { it }
        assert(playlistFileLines.size == 3)
        assert(playlistFileLines[0] == "#EXTM3U")
        assert(playlistFileLines[1] == "#EXT-X-RATING:0")
        assert(playlistFileLines[2] == "primary/Audio/music - file.mp3")

        assert(manager.isMusicInPlaylist(music1, playlist))
        assert(!manager.isMusicInPlaylist(music2, playlist))
    }
}