package com.qmk.musicmanager.domain.manager

import com.qmk.musicmanager.domain.extension.moveTo
import com.qmk.musicmanager.domain.model.Music
import java.io.File
import java.net.URI
import java.util.*

class MopidyManager(
    private val configurationManager: ConfigurationManager = ConfigurationManager()
) {
    private lateinit var musicDir: String
    private lateinit var playlistDir: String
    val archivePlaylistName = "Archives"

    init {
        updateMembers()
    }

    private fun updateMembers() {
        musicDir = configurationManager.getConfiguration().musicFolder
        playlistDir = "$musicDir/Playlists/Mopidy"
        val playlists = File(playlistDir)
        if (!playlists.exists()) playlists.mkdirs()
    }

    fun createPlaylist(name: String): String {
        updateMembers()
        val isNew = File("$playlistDir/$name.m3u8").createNewFile()
        return if (isNew) "Playlist $name created."
        else "$name already exists."
    }

    fun renamePlaylist(oldName: String, newName: String) {
        updateMembers()
        File("$playlistDir/$oldName.m3u8").renameTo(File("$playlistDir/$newName.m3u8"))
    }

    fun addMusicToPlaylist(music: Music, playlistName: String) {
        updateMembers()
        val line = convertFileToMopidyPath(File("$musicDir/${music.fileName}.${music.fileExtension}"))
        File("$playlistDir/$playlistName.m3u8").appendText("$line\n")
    }

    fun getFilesFromPlaylist(playlistName: String): List<String> {
        updateMembers()
        val playlist = File("$playlistDir/$playlistName.m3u8")
        return if (!playlist.exists()) emptyList()
        else playlist.readLines().map {
            val uri = URI(it.replace(
                "local:track:",
                "file:${File(musicDir).absolutePath}/"
            ))
            val file = File(uri.path)
            file.toString()
        }
    }

    fun archiveMusic() {
        updateMembers()
        val archivePlaylist = File("${playlistDir}/$archivePlaylistName.m3u8")
        if (!archivePlaylist.exists()) archivePlaylist.createNewFile()

        val tempFile = File("./workDir/mopidy-tmp-playlist-${UUID.randomUUID()}.m3u8")
        File(playlistDir).walk().forEach lit1@ { playlist ->
            if (playlist == archivePlaylist || playlist.isDirectory) return@lit1
            tempFile.createNewFile()
            playlist.readLines().forEach { playlistEntry ->
                var keepMusic = true
                archivePlaylist.readLines().forEach { archive ->
                    if (playlistEntry == archive) keepMusic = false
                }
                if (keepMusic) {
                    tempFile.appendText("$playlistEntry\n")
                }
            }
            tempFile.moveTo("${playlistDir}/${playlist.name}", true)
        }

        val newArchivesFile = File("./workDir/mopidy-tmp-playlist-${UUID.randomUUID()}.m3u8")
        newArchivesFile.createNewFile()
        archivePlaylist.readLines().forEach { archive ->
            val prefix = "local:track:"
            if (archive.startsWith("$prefix$archivePlaylistName:")) {
                newArchivesFile.appendText("$archive\n")
            } else if (archive.startsWith(prefix)) {
                val newLine = "${archive.replace(prefix, "$prefix$archivePlaylistName:")}\n"
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
                if (!it.contains("local:track:$archivePlaylistName:"))
                    result.add(File(URI(it.replace(
                        "local:track:",
                        "file:${File(musicDir).absolutePath}/"
                    )).path).toString())
            }
        return result
    }

    fun convertPowerAmpPlaylist(playlistName: String) {
        updateMembers()
        val powerAmpPlaylist = PowerAmpManager().getFilesFromPlaylist(playlistName)
        val tempFile = File("./workDir/mopidy-tmp-playlist-${UUID.randomUUID()}.m3u8")
        tempFile.createNewFile()
        powerAmpPlaylist.forEach {
            val line = convertFileToMopidyPath(File(it))
            tempFile.appendText("$line\n")
        }
        tempFile.moveTo("${playlistDir}/$playlistName.m3u8", true)
    }

    fun mergePowerAmpPlaylist(playlistName: String) {
        updateMembers()
        val powerAmpPlaylist = PowerAmpManager().getFilesFromPlaylist(playlistName)
        val mergedPlaylist = getFilesFromPlaylist(playlistName).toMutableList()
        powerAmpPlaylist.forEach {
            if (!mergedPlaylist.contains(it)) mergedPlaylist.add(it)
        }
        val tempFile = File("./workDir/mopidy-tmp-playlist-${UUID.randomUUID()}.m3u8")
        tempFile.createNewFile()
        mergedPlaylist.forEach {
            val line = convertFileToMopidyPath(File(it))
            tempFile.appendText("$line\n")
        }
        tempFile.moveTo("${playlistDir}/$playlistName.m3u8", true)
    }

    private fun convertFileToMopidyPath(file: File): String {
        val initialUri = file
            .toURI()
            .toASCIIString()
            .replace("file:${File(musicDir).absolutePath}/", "")
        // Some characters are not encoded when using file.toURI(), so we have to encode them
        var finalUri = ""
        for (char in initialUri) {
            val newChar = when (char) {
                '!' -> "%21"
                '*' -> "%2A"
                '\'' -> "%27"
                '(' -> "%28"
                ')' -> "%29"
                ';' -> "%3B"
                ':' -> "%3A"
                '@' -> "%40"
                '&' -> "%26"
                '=' -> "%3D"
                '+' -> "%2B"
                '$' -> "%24"
                ',' -> "%2C"
                '/' -> "%2F"
                else -> char
            }
            finalUri += newChar
        }
        var subFolders = file.parentFile.absolutePath
            .replace(File(musicDir).absolutePath, "")
            .replace("/", ":")
        if (subFolders != "") subFolders += ":"
        return "local:track:$subFolders$finalUri"
    }
}