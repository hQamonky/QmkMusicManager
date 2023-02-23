package com.qmk.musicmanager.manager

import com.qmk.musicmanager.extension.runCommand
import com.qmk.musicmanager.model.Playlist

class YoutubeManager {

    private val youtubeDl: String = DownloadTool.YOUTUBE_DL.value
    private val ytDlp: String = DownloadTool.YOUTUBE_DLP.value

    private val videoUrl = "https://www.youtube.com/watch?v="
    val playlistUrl = "https://www.youtube.com/playlist?list="

    fun pwd(): String? {
        return "pwd".runCommand()
    }

    fun version(): String? {
        return "$youtubeDl --version".runCommand()
    }

    fun updateYoutubeDl(): String? {
        val result = "$youtubeDl --update".runCommand()
        return if (result == "It looks like you installed youtube-dl with a package manager, pip, setup.py or a " +
            "tarball. Please use that to update.\n")
            "pip install youtube-dl --upgrade".runCommand()
        else
            result
    }

    fun updateYtDlp(): String? {
        return "$ytDlp -U".runCommand()
    }

    fun getPlaylistInfo(url: String, tool: DownloadTool = DownloadTool.YOUTUBE_DL): String? {
        return when(tool) {
            DownloadTool.YOUTUBE_DL ->
                "$youtubeDl -ci --flat-playlist -J $url".runCommand()
            DownloadTool.YOUTUBE_DLP ->
                "$ytDlp -ci --flat-playlist -J $url".runCommand()
        }
    }

    fun getVideoInfo(videoId: String, tool: DownloadTool? = DownloadTool.YOUTUBE_DL): String? {
        return when(tool) {
            DownloadTool.YOUTUBE_DL -> "$youtubeDl -ci -J $videoUrl$videoId".runCommand()
            DownloadTool.YOUTUBE_DLP -> "$ytDlp -ci -J $videoUrl$videoId".runCommand()
            else -> "curl -L https://www.youtube.com/oembed?url=$videoUrl$videoId&format=json".runCommand()
        }
    }

    fun downloadMusic(url: String, tool: DownloadTool = DownloadTool.YOUTUBE_DL): String? {
        val fullUrl = if (url.contains("www.youtube.com")) url
        else "$videoUrl$url"
        return when(tool) {
            DownloadTool.YOUTUBE_DL ->
                "$youtubeDl -ci -x --audio-format mp3 --embed-thumbnail -o ./workDir/tmp.'%(ext)s' $fullUrl".runCommand()
            DownloadTool.YOUTUBE_DLP ->
                "$ytDlp -ci -x --audio-format mp3 --embed-thumbnail -o ./workDir/tmp $fullUrl".runCommand()
        }
    }

    fun downloadPlaylist(playlist: Playlist): String? {
        return getPlaylistInfo(playlist.id)
    }
}

enum class DownloadTool(val value: String) {
    YOUTUBE_DL("youtube-dl"),
    YOUTUBE_DLP("yt-dlp")
}
