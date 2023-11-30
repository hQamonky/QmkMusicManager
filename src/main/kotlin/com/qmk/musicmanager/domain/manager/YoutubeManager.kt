package com.qmk.musicmanager.domain.manager

import com.qmk.musicmanager.domain.extension.runCommand
import com.qmk.musicmanager.domain.model.Playlist

class YoutubeManager {

    private val youtubeDl: String = DownloadTool.YOUTUBE_DL.value
    private val ytDlp: String = DownloadTool.YT_DLP.value

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
            "tarball. Please use that to update.\n"
        )
            "pip install youtube-dl --upgrade".runCommand()
        else
            result
    }

    fun updateYtDlp(): String? {
        return "$ytDlp -U".runCommand()
    }

    fun getPlaylistInfo(url: String, tool: DownloadTool = DownloadTool.YT_DLP): String? {
        return when (tool) {
            DownloadTool.YOUTUBE_DL ->
                "$youtubeDl -ci --flat-playlist -J $url".runCommand()

            DownloadTool.YT_DLP ->
                "$ytDlp -ci --flat-playlist -J $url".runCommand()
        }
    }

    fun getVideoInfo(videoId: String, tool: DownloadTool? = DownloadTool.YT_DLP): String? {
        return when (tool) {
            DownloadTool.YOUTUBE_DL -> "$youtubeDl -ci -J $videoUrl$videoId".runCommand()
            DownloadTool.YT_DLP -> "$ytDlp -ci -J $videoUrl$videoId".runCommand()
            else -> "curl -L https://www.youtube.com/oembed?url=$videoUrl$videoId&format=json".runCommand()
        }
    }

    fun downloadMusic(
        url: String,
        outputFile: String,
        audioFormat: String = "mp3",
        audioQuality: Int = 0,
        tool: DownloadTool = DownloadTool.YT_DLP
    ): String? {
        val fullUrl = if (url.contains("www.youtube.com")) url
        else "$videoUrl$url"
        return when (tool) {
            DownloadTool.YOUTUBE_DL ->
                "$youtubeDl -ci -x --audio-format $audioFormat --audio-quality $audioQuality --embed-thumbnail -o $outputFile.'%(ext)s' $fullUrl".runCommand()

            DownloadTool.YT_DLP ->
                "$ytDlp -ci -x --audio-format $audioFormat --audio-quality $audioQuality --embed-thumbnail -o $outputFile $fullUrl".runCommand()
        }
    }

    fun downloadPlaylist(playlist: Playlist): String? {
        return getPlaylistInfo(playlist.id)
    }
}

enum class DownloadTool(val value: String) {
    YOUTUBE_DL("youtube-dl"),
    YT_DLP("yt-dlp")
}
