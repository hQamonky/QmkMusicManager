package com.qmk.musicmanager.manager

import com.qmk.musicmanager.model.Music
import com.qmk.musicmanager.model.Settings
import org.junit.jupiter.api.Test
import java.io.File

internal class PowerAmpManagerTest {
    private val manager: PowerAmpManager = PowerAmpManager()
    private val configurationManager: ConfigurationManager =
        ConfigurationManager(configuration = Settings(musicFolder = "src/test/MusicTestDir"))

    @Test
    fun createPlaylistTest() {
        File("src/test/MusicTestDir/Playlists/PowerAmp").deleteRecursively()
        // Change music folder
        configurationManager.setConfiguration(Settings(musicFolder = "src/test/MusicTestDir"))
        // Create playlist
        assert(manager.createPlaylist("MyPlaylist") == "Playlist MyPlaylist created.")
        assert(manager.createPlaylist("MyPlaylist") == "MyPlaylist already exists.")
        val playlistFile = File("src/test/MusicTestDir/Playlists/PowerAmp/MyPlaylist.m3u8")
        assert(playlistFile.exists())
        val playlistFileLines = playlistFile.readLines().map { it }
        assert(playlistFileLines.size == 1)
        assert(playlistFileLines[0] == "#EXTM3U")
    }

    @Test
    fun renamePlaylistTest() {
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
    fun addMusicToPlaylistTest() {
        File("src/test/MusicTestDir/Playlists/PowerAmp").deleteRecursively()
        manager.addMusicToPlaylist(
            Music(
                id = "",
                fileName = "music - file",
                title = "",
                artist = "",
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
        assert(playlistFileLines[2] == "primary/MusicTestDir/music - file.mp3")
        manager.addMusicToPlaylist(
            Music(
                id = "",
                fileName = "other - music",
                title = "",
                artist = "",
                uploaderId = "",
                uploadDate = ""
            ),
            "My Playlist"
        )
        val updatedPlaylistFile = playlistFile.readLines().map { it }
        assert(updatedPlaylistFile.size == 5)
        assert(updatedPlaylistFile[0] == "#EXTM3U")
        assert(updatedPlaylistFile[1] == "#EXT-X-RATING:0")
        assert(updatedPlaylistFile[2] == "primary/MusicTestDir/music - file.mp3")
        assert(updatedPlaylistFile[3] == "#EXT-X-RATING:0")
        assert(updatedPlaylistFile[4] == "primary/MusicTestDir/other - music.mp3")
    }

    @Test
    fun getFilesFromPlaylistTest() {
        val files = manager.getFilesFromPlaylist("My Playlist")
        assert(files.size == 2)
        assert(File(files[0]).name == "music - file.mp3")
        assert(File(files[1]).name == "other - music.mp3")
    }

    @Test
    fun archiveMusicTest() {
        File("src/test/MusicTestDir/Playlists/PowerAmp").deleteRecursively()

        val playlist1 = "playlist1"
        val playlist2 = "playlist2"
        val archives = "Archives"
        val playlist1File = File("src/test/MusicTestDir/Playlists/PowerAmp/$playlist1.m3u8")
        val playlist2File = File("src/test/MusicTestDir/Playlists/PowerAmp/$playlist2.m3u8")
        val archivesPlaylist = File("src/test/MusicTestDir/Playlists/PowerAmp/$archives.m3u8")
        val music1 = Music(id = "", fileName = "music1", title = "", artist = "", uploaderId = "", uploadDate = "")
        val music2 = Music(id = "", fileName = "music2", title = "", artist = "", uploaderId = "", uploadDate = "")
        val music3 = Music(id = "", fileName = "music3", title = "", artist = "", uploaderId = "", uploadDate = "")
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
        assert(playlist1FileLines[2] == "primary/MusicTestDir/music2.mp3")
        assert(playlist1FileLines[3] == "#EXT-X-RATING:0")
        assert(playlist1FileLines[4] == "primary/MusicTestDir/music3.mp3")

        val playlist2FileLines = playlist2File.readLines().map { it }
        assert(playlist2FileLines.size == 3)
        assert(playlist2FileLines[0] == "#EXTM3U")
        assert(playlist2FileLines[1] == "#EXT-X-RATING:0")
        assert(playlist2FileLines[2] == "primary/MusicTestDir/music2.mp3")

        val archivePlaylistLines = archivesPlaylist.readLines().map { it }
        assert(archivePlaylistLines.size == 3)
        assert(archivePlaylistLines[0] == "#EXTM3U")
        assert(archivePlaylistLines[1] == "#EXT-X-RATING:0")
        assert(archivePlaylistLines[2] == "primary/MusicTestDir/$archives/music1.mp3")


        manager.addMusicToPlaylist(music3, archives)
        manager.archiveMusic()

        val newPlaylist1FileLines = playlist1File.readLines().map { it }
        assert(newPlaylist1FileLines.size == 3)
        assert(newPlaylist1FileLines[0] == "#EXTM3U")
        assert(newPlaylist1FileLines[1] == "#EXT-X-RATING:0")
        assert(newPlaylist1FileLines[2] == "primary/MusicTestDir/music2.mp3")

        val newPlaylist2FileLines = playlist2File.readLines().map { it }
        assert(newPlaylist2FileLines.size == 3)
        assert(newPlaylist2FileLines[0] == "#EXTM3U")
        assert(newPlaylist2FileLines[1] == "#EXT-X-RATING:0")
        assert(newPlaylist2FileLines[2] == "primary/MusicTestDir/music2.mp3")

        val newArchivePlaylistLines = archivesPlaylist.readLines().map { it }
        assert(newArchivePlaylistLines.size == 5)
        assert(newArchivePlaylistLines[0] == "#EXTM3U")
        assert(newArchivePlaylistLines[1] == "#EXT-X-RATING:0")
        assert(newArchivePlaylistLines[2] == "primary/MusicTestDir/$archives/music1.mp3")
        assert(newArchivePlaylistLines[3] == "#EXT-X-RATING:0")
        assert(newArchivePlaylistLines[4] == "primary/MusicTestDir/$archives/music3.mp3")
    }

    @Test
    fun getMusicToArchiveTest() {
        File("src/test/MusicTestDir/Playlists/PowerAmp").deleteRecursively()

        val archives = "Archives"
        val music1 = Music(id = "", fileName = "music1", title = "", artist = "", uploaderId = "", uploadDate = "")
        val music2 = Music(id = "", fileName = "music2", title = "", artist = "", uploaderId = "", uploadDate = "")

        manager.addMusicToPlaylist(music1, archives)
        manager.archiveMusic()
        manager.addMusicToPlaylist(music2, archives)

        val musicToArchive = manager.getMusicToArchive()
        assert(musicToArchive.size == 1)
        assert(File(musicToArchive[0]).name == "music2.mp3")
    }

    @Test
    fun convertMopidyPlaylistTest() {
        File("src/test/MusicTestDir/Playlists").deleteRecursively()

        val mopidyManager = MopidyManager()
        val playlist = "playlist1"
        val music1 = Music(id = "", fileName = "music 1", title = "", artist = "", uploaderId = "", uploadDate = "")
        val music2 = Music(id = "", fileName = "music 2", title = "", artist = "", uploaderId = "", uploadDate = "")
        mopidyManager.addMusicToPlaylist(music1, playlist)
        mopidyManager.addMusicToPlaylist(music2, playlist)

        manager.convertMopidyPlaylist(playlist)

        val playlistFile = File("src/test/MusicTestDir/Playlists/PowerAmp/$playlist.m3u8")
        val updatedPlaylistFile = playlistFile.readLines().map { it }
        assert(updatedPlaylistFile.size == 5)
        assert(updatedPlaylistFile[0] == "#EXTM3U")
        assert(updatedPlaylistFile[1] == "#EXT-X-RATING:0")
        assert(updatedPlaylistFile[2] == "primary/MusicTestDir/music 1.mp3")
        assert(updatedPlaylistFile[3] == "#EXT-X-RATING:0")
        assert(updatedPlaylistFile[4] == "primary/MusicTestDir/music 2.mp3")
    }

    @Test
    fun mergeMopidyPlaylistTest() {
        File("src/test/MusicTestDir/Playlists").deleteRecursively()

        val mopidyManager = MopidyManager()

        val playlist = "playlist1"
        val music1 = Music(id = "", fileName = "music 1", title = "", artist = "", uploaderId = "", uploadDate = "")
        val music2 = Music(id = "", fileName = "music 2", title = "", artist = "", uploaderId = "", uploadDate = "")
        manager.addMusicToPlaylist(music1, playlist)
        mopidyManager.addMusicToPlaylist(music2, playlist)

        manager.mergeMopidyPlaylist(playlist)

        val playlistFile = File("src/test/MusicTestDir/Playlists/PowerAmp/$playlist.m3u8")
        val updatedPlaylistFile = playlistFile.readLines().map { it }
        assert(updatedPlaylistFile.size == 5)
        assert(updatedPlaylistFile[0] == "#EXTM3U")
        assert(updatedPlaylistFile[1] == "#EXT-X-RATING:0")
        assert(updatedPlaylistFile[2] == "primary/MusicTestDir/music 1.mp3")
        assert(updatedPlaylistFile[3] == "#EXT-X-RATING:0")
        assert(updatedPlaylistFile[4] == "primary/MusicTestDir/music 2.mp3")
    }
}