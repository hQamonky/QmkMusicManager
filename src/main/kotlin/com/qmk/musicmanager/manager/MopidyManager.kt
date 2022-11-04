package com.qmk.musicmanager.manager

import com.qmk.musicmanager.extension.moveTo
import com.qmk.musicmanager.model.Music
import java.io.File
import java.net.URI

class MopidyManager(
    private val configurationManager: ConfigurationManager = ConfigurationManager()
) {
    private val musicDir = configurationManager.getConfiguration().musicFolder
    private val playlistDir = "$musicDir/Playlists/Mopidy"
    private val archivePlaylistName = "Archives"

    fun createPlaylist(name: String): String {
        val isNew = File("$playlistDir/$name.m3u8").createNewFile()
        return if (isNew) "Playlist $name created."
        else "$name already exists."
    }

    fun renamePlaylist(oldName: String, newName: String) {
        File("$playlistDir/$oldName.m3u8").renameTo(File("$playlistDir/$newName.m3u8"))
    }

    fun addMusicToPlaylist(music: Music, playlistName: String) {
        val line = File("$musicDir/${music.fileName}.${music.fileExtension}")
            .toURI()
            .toString()
            .replace("file:${File(musicDir).absolutePath}/", "local:track:")
        File("$playlistDir/$playlistName.m3u8").appendText("$line\n")
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
            val prefix = "local:track:"
            archives.forEach { archive ->
                if (archive.startsWith(prefix + "Archive:"))
                    newArchivesFile.appendText(archive)
                else
                    newArchivesFile.appendText(archive.replace(prefix, prefix + "Archive:"))
            }
            newArchivesFile.moveTo("${playlistDir}/$archivePlaylistName.m3u8", true)
        }
    }

    fun getFilesFromPlaylist(playlistName: String): List<String> {
        return File("$playlistDir/$playlistName.m3u8")
            .readLines()
            .map {
                File(URI(it.replace(
                    "local:track:",
                    "file:${File(musicDir).absolutePath}/"
                )).path).toString()
            }
    }

    fun getMusicToArchive(): List<String> {
        val result = mutableListOf<String>()
        File("$playlistDir/$archivePlaylistName.m3u8")
            .readLines()
            .forEach {
                if (!it.contains("local:track:Archive:"))
                    result.add(File(URI(it.replace(
                        "local:track:",
                        "file:${File(musicDir).absolutePath}/"
                    )).path).toString())
            }
        return result
    }

    fun convertPowerAmpPlaylist(playlistName: String) {
        val powerAmpPlaylist = PowerAmpManager().getFilesFromPlaylist(playlistName)
        val tempFile = File("./workDir/tmpPlaylist.m3u8")
        powerAmpPlaylist.forEach {
            val line = File("$musicDir/$it")
                .toURI()
                .toString()
                .replace("file:${File(musicDir).absolutePath}/", "local:track:")
            tempFile.appendText("$line\n")
        }
        tempFile.moveTo("${playlistDir}/$playlistName.m3u8")
    }

    fun mergePowerAmpPlaylist(playlistName: String) {
        val powerAmpPlaylist = PowerAmpManager().getFilesFromPlaylist(playlistName)
        val mergedPlaylist = getFilesFromPlaylist(playlistName).toMutableList()
        powerAmpPlaylist.forEach {
            if (!mergedPlaylist.contains(it)) mergedPlaylist.add(it)
        }
        val tempFile = File("./workDir/tmpPlaylist.m3u8")
        mergedPlaylist.forEach {
            val line = File("$musicDir/$it")
                .toURI()
                .toString()
                .replace("file:${File(musicDir).absolutePath}/", "local:track:")
            tempFile.appendText("$line\n")
        }
        tempFile.moveTo("${playlistDir}/$playlistName.m3u8")
    }
}