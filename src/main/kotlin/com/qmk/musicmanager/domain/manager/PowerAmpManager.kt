package com.qmk.musicmanager.domain.manager

import com.qmk.musicmanager.domain.extension.moveTo
import com.qmk.musicmanager.domain.model.Music
import java.io.File
import java.net.URI
import java.util.*

class PowerAmpManager(
    private val configurationManager: ConfigurationManager = ConfigurationManager()
) {
    private lateinit var musicDir: String
    private lateinit var playlistDir: String
    private val archivePlaylistName = "Archives"

    init {
        updateMembers()
    }

    private fun updateMembers() {
        musicDir = configurationManager.getConfiguration().musicFolder
        playlistDir = "$musicDir/Playlists/PowerAmp"
        val playlists = File(playlistDir)
        if (!playlists.exists()) playlists.mkdirs()
    }

    fun createPlaylist(name: String): String {
        updateMembers()
        val file = File("$playlistDir/$name.m3u8")
        return if (file.createNewFile()) {
            file.writeText("#EXTM3U\n")
            "Playlist $name created."
        } else {
            "$name already exists."
        }
    }

    fun renamePlaylist(oldName: String, newName: String) {
        updateMembers()
        File("$playlistDir/$oldName.m3u8").renameTo(File("$playlistDir/$newName.m3u8"))
    }

    fun addMusicToPlaylist(music: Music, playlistName: String) {
        updateMembers()
        val line = "primary/${File(musicDir).name}/${music.fileName}.${music.fileExtension}"
        val playlist = File("$playlistDir/$playlistName.m3u8")
        if (!playlist.exists()) playlist.writeText("#EXTM3U\n")
        playlist.appendText("#EXT-X-RATING:0\n$line\n")
    }

    fun getFilesFromPlaylist(playlistName: String): List<String> {
        updateMembers()
        val prefix = "primary/${File(musicDir).name}"
        val result = mutableListOf<String>()
        val playlist = File("$playlistDir/$playlistName.m3u8")
        if (!playlist.exists()) return emptyList()
        playlist.readLines().forEach {
            if (it.startsWith(prefix))
                result.add("${File(musicDir).absolutePath}${it.replace(prefix, "")}")
        }
        return result
    }

    fun archiveMusic() {
        updateMembers()
        val archivePlaylist = File("${playlistDir}/$archivePlaylistName.m3u8")
        if (!archivePlaylist.exists()) archivePlaylist.writeText("#EXTM3U\n")

        val tempFile = File("./workDir/power-amp-tmp-playlist-${UUID.randomUUID()}.m3u8")
        File(playlistDir).walk().forEach lit1@ { playlist ->
            if (playlist == archivePlaylist || playlist.isDirectory) return@lit1
            tempFile.writeText("#EXTM3U\n")
            playlist.readLines().forEach lit2@ { playlistEntry ->
                if (playlistEntry.startsWith("#EXTM3U") || playlistEntry.startsWith("#EXT-X-RATING:0"))
                    return@lit2
                var keepMusic = true
                archivePlaylist.readLines().forEach { archive ->
                    if (playlistEntry == archive) keepMusic = false
                }
                if (keepMusic) {
                    tempFile.appendText("#EXT-X-RATING:0\n$playlistEntry\n")
                }
            }
            tempFile.moveTo("${playlistDir}/${playlist.name}", true)
        }

        val newArchivesFile = File("./workDir/power-amp-tmp-playlist-${UUID.randomUUID()}.m3u8")
        newArchivesFile.writeText("#EXTM3U\n")
        archivePlaylist.readLines().forEach { archive ->
            val prefix = "primary/${File(musicDir).name}"
            if (archive.startsWith("$prefix/$archivePlaylistName/")) {
                newArchivesFile.appendText("#EXT-X-RATING:0\n$archive\n")
            } else if (archive.startsWith(prefix)) {
                val newLine = "#EXT-X-RATING:0\n${archive.replace(prefix, "$prefix/$archivePlaylistName")}\n"
                newArchivesFile.appendText(newLine)
            }
        }
        newArchivesFile.moveTo(archivePlaylist, true)
    }

    fun getMusicToArchive(): List<String> {
        updateMembers()
        val result = mutableListOf<String>()
        File("$playlistDir/$archivePlaylistName.m3u8")
            .readLines()
            .forEach {
                if (
                    !it.contains("primary/${File(musicDir).name}/$archivePlaylistName/") &&
                            it.startsWith("primary/")
                )
                    result.add(File(URI(it.replace("local:track:", "file:/")).path).toString())
            }
        return result
    }

    fun convertMopidyPlaylist(playlistName: String) {
        updateMembers()
        val mopidyPlaylist = MopidyManager().getFilesFromPlaylist(playlistName)
        val tempFile = File("./workDir/power-amp-tmp-playlist-${UUID.randomUUID()}.m3u8")
        tempFile.writeText("#EXTM3U\n")
        mopidyPlaylist.forEach {
            val line = "primary/${File(musicDir).name}/${File(it).name}"
            tempFile.appendText("#EXT-X-RATING:0\n$line\n")
        }
        tempFile.moveTo("${playlistDir}/$playlistName.m3u8", true)
    }

    fun mergeMopidyPlaylist(playlistName: String) {
        updateMembers()
        val mopidyPlaylist = MopidyManager().getFilesFromPlaylist(playlistName)
        val mergedPlaylist = getFilesFromPlaylist(playlistName).toMutableList()
        mopidyPlaylist.forEach {
            if (!mergedPlaylist.contains(it)) mergedPlaylist.add(it)
        }
        val tempFile = File("./workDir/power-amp-tmp-playlist-${UUID.randomUUID()}.m3u8")
        tempFile.writeText("#EXTM3U\n")
        mergedPlaylist.forEach {
            val line = "primary/${File(musicDir).name}/${File("$musicDir/$it").name}"
            tempFile.appendText("#EXT-X-RATING:0\n$line\n")
        }
        tempFile.moveTo("${playlistDir}/$playlistName.m3u8", true)
    }
}