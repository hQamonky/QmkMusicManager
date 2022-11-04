package com.qmk.musicmanager.manager

import com.qmk.musicmanager.extension.moveTo
import com.qmk.musicmanager.model.Music
import java.io.File
import java.net.URI

class PowerAmpManager(
    private val configurationManager: ConfigurationManager = ConfigurationManager()
) {
    private val musicDir = configurationManager.getConfiguration().musicFolder
    private val playlistDir = "$musicDir/Playlists/PowerAmp"
    private val archivePlaylistName = "Archives"

    fun createPlaylist(name: String): String {
        val file = File("$playlistDir/$name.m3u8")
        return if (file.createNewFile()) {
            file.appendText("#EXTM3U\n")
            "Playlist $name created."
        } else {
            "$name already exists."
        }
    }

    fun renamePlaylist(oldName: String, newName: String) {
        File("$playlistDir/$oldName.m3u8").renameTo(File("$playlistDir/$newName.m3u8"))
    }

    fun addMusicToPlaylist(music: Music, playlistName: String) {
        val line = "primary/${File(musicDir).name}/${music.fileName}.${music.fileExtension}"
        File("$playlistDir/$playlistName.m3u8").appendText("#EXT-X-RATING:0\n$line")
    }

    fun archiveMusic() {
        val archives = File("$playlistDir/$archivePlaylistName.m3u8").readLines()
        val tempFile = File("./workDir/tmpPlaylist.m3u8")
        File(playlistDir).walk().forEach lit@ {  playlist ->
            if (playlist.name == archivePlaylistName) return@lit
            playlist.readLines().forEach { playlistEntry ->
                var keepMusic = true
                archives.forEach { archive ->
                    if (playlistEntry == archive) keepMusic = false
                }
                if (keepMusic) {
                    tempFile.writeText(playlistEntry)
                }
            }
            tempFile.moveTo("${playlistDir}/${playlist.name}.m3u8", true)
            val newArchivesFile = File("./workDir/archivesPlaylist.m3u8")
            val prefix = "primary/$musicDir"
            archives.forEach { archive ->
                if (archive.startsWith("$prefix/Archive"))
                    newArchivesFile.appendText(archive)
                else
                    newArchivesFile.appendText(archive.replace(prefix, "$prefix/Archive"))
            }
            newArchivesFile.moveTo("${playlistDir}/$archivePlaylistName.m3u8", true)
        }
    }

    fun getFilesFromPlaylist(playlistName: String): List<String> {
        val prefix = "primary/${File(musicDir).name}"
        val result = mutableListOf<String>()
        File("$playlistDir/$playlistName.m3u8")
            .readLines()
            .forEach {
                if (it.startsWith(prefix))
                    result.add("$musicDir/${it.replace(prefix, "")}")
            }
        return result
    }

    fun getMusicToArchive(): List<String> {
        val result = mutableListOf<String>()
        File("$playlistDir/$archivePlaylistName.m3u8")
            .readLines()
            .forEach {
                if (!it.contains("primary/${File(musicDir).name}/Archive:"))
                    result.add(File(URI(it.replace("local:track:", "file:///")).path).toString())
            }
        return result
    }

    fun convertMopidyPlaylist(playlistName: String) {
        val mopidyPlaylist = MopidyManager().getFilesFromPlaylist(playlistName)
        val tempFile = File("./workDir/tmpPlaylist.m3u8")
        mopidyPlaylist.forEach {
            val line = "primary/${File(musicDir).name}/${File("$musicDir/$it").name}"
            tempFile.appendText("#EXT-X-RATING:0\n$line")
        }
        tempFile.moveTo("${playlistDir}/$playlistName.m3u8")
    }

    fun mergeMopidyPlaylist(playlistName: String) {
        val mopidyPlaylist = MopidyManager().getFilesFromPlaylist(playlistName)
        val mergedPlaylist = getFilesFromPlaylist(playlistName).toMutableList()
        mopidyPlaylist.forEach {
            if (!mergedPlaylist.contains(it)) mergedPlaylist.add(it)
        }
        val tempFile = File("./workDir/tmpPlaylist.m3u8")
        mergedPlaylist.forEach {
            val line = "primary/${File(musicDir).name}/${File("$musicDir/$it").name}"
            tempFile.appendText("#EXT-X-RATING:0\n$line")
        }
        tempFile.moveTo("${playlistDir}/$playlistName.m3u8")
    }
}