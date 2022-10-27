package com.qmk.musicmanager.youtube

import com.qmk.musicmanager.extension.runCommand
import com.qmk.musicmanager.model.Playlist

class YoutubeController(private val youtubeDl: String = "youtube-dl") {

    private val videoUrl = "https://www.youtube.com/watch?v="
    private val playlistUrl = "https://www.youtube.com/playlist?list="

    fun pwd(): String? {
        return "pwd".runCommand()
    }

    fun version(): String? {
        return "$youtubeDl --version".runCommand()
    }

    fun update(): String? {
        val result = "$youtubeDl --update".runCommand()
        return if (result == "It looks like you installed youtube-dl with a package manager, pip, setup.py or a " +
            "tarball. Please use that to update.\n")
            "pip install youtube-dl --upgrade".runCommand()
        else
            result
    }

    fun getPlaylistInfo(url: String): String? {
        return "$youtubeDl -ci --flat-playlist -J $url".runCommand()
    }

    fun getVideoInfo(url: String, usingYoutubeDl: Boolean = true): String? {
        return if (usingYoutubeDl) "$youtubeDl -ci -J $url".runCommand()
        else "https://www.youtube.com/oembed?url=$url&format=json".runCommand()
    }

    fun downloadMusic(url: String, outputDir: String): String? {
        return "$youtubeDl -ci -x --audio-format mp3 --embed-thumbnail -o $outputDir $url".runCommand()
    }

    fun downloadPlaylist(playlist: Playlist): String? {
        return getPlaylistInfo(playlist.id)
    }
}