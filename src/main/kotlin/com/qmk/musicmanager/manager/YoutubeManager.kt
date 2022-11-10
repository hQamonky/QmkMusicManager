package com.qmk.musicmanager.manager

import com.qmk.musicmanager.extension.runCommand
import com.qmk.musicmanager.model.Playlist

class YoutubeManager(private val youtubeDl: String = "youtube-dl") {

    private val videoUrl = "https://www.youtube.com/watch?v="
    val playlistUrl = "https://www.youtube.com/playlist?list="

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

    fun getVideoInfo(videoId: String, usingYoutubeDl: Boolean = true): String? {
        return if (usingYoutubeDl) "$youtubeDl -ci -J $videoUrl$videoId".runCommand()
        else "https://www.youtube.com/oembed?url=$videoId&format=json".runCommand()
    }

    fun downloadMusic(url: String): String? {
        return "$youtubeDl -ci -x --audio-format mp3 --embed-thumbnail -o ./workDir/tmp.'%(ext)s' $url".runCommand()
    }

    fun downloadPlaylist(playlist: Playlist): String? {
        return getPlaylistInfo(playlist.id)
    }
}