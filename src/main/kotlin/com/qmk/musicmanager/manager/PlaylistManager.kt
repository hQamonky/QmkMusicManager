package com.qmk.musicmanager.manager

import com.google.gson.Gson
import com.qmk.musicmanager.model.*
import com.qmk.musicmanager.service.MusicService
import com.qmk.musicmanager.service.NamingRuleService
import com.qmk.musicmanager.service.PlaylistService
import com.qmk.musicmanager.service.UploaderService
import com.qmk.musicmanager.youtube.YoutubeController
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag
import java.io.File


class PlaylistManager(
    private val playlistService: PlaylistService,
    private val musicService: MusicService,
    private val uploaderService: UploaderService,
    private val namingRuleService: NamingRuleService,
    private val youtubeController: YoutubeController,
    private val workDir: File = File(".")
) {
    fun createPlaylist(name: String, url: String): Playlist {
        val gson = Gson()
        val playlistInfo = gson.fromJson(youtubeController.getPlaylistInfo(url), PlaylistInfo::class.java)
        val playlist = Playlist(
            id = playlistInfo.id,
            name = name
        )
        val doesNotExist = playlistService.findById(playlist.id).isEmpty()
        if (doesNotExist) playlistService.new(playlist)
        return playlist
    }

    fun downloadPlaylists(): String {
        var result = ""
        val playlists = playlistService.find()
        if (playlists.isEmpty()) return "Error : no playlists found."
        result = "${playlists.size} found.\n"
        playlists.forEach { playlist ->
            result += "Start process for ${playlist.name} :\n"
            result += downloadPlaylist(playlist.id)
        }
        return result
    }

    fun downloadPlaylist(playlistId: String): String {
        var result = ""
        val playlistById = playlistService.findById(playlistId)
        if (playlistById.isEmpty())
            return "Error : playlist not found."
        val playlist = playlistById[0]
        result += "Downloading ${playlist.name}...\n"
        val gson = Gson()
        gson.fromJson(
            youtubeController.getPlaylistInfo("${youtubeController.playlistUrl}${playlist.id}"),
            PlaylistInfo::class.java
        ).entries
            .map{ it.id }
            .forEach { musicId ->
                val musicById = musicService.findById(musicId)
                result += if (musicById.isNotEmpty()) {
                    "Not downloading ${musicById[0].name} because is has already been done.\n"
                } else {
                    downloadAndProcessMusic(musicId)
                }
            }
        // TODO : Add music_playlist if not exist
        return result
    }

    private fun downloadAndProcessMusic(videoId: String): String {
        // Get video info
        var result = "Getting info for $videoId...\n"
        val musicInfo = Gson().fromJson(youtubeController.getVideoInfo(videoId), MusicInfo::class.java)
        // Create uploader if not exist
        var uploader = uploaderService.findById(musicInfo.channel_id)
        if (uploader == null) {
            uploader = Uploader(id = musicInfo.channel_id, name = musicInfo.channel) // TODO : set to default naming format
            uploaderService.new(uploader)
        }
        // Download music
        result += "Downloading ${musicInfo.title}...\n"
        val outputFile = "${workDir.absolutePath}/${musicInfo.title}.mp3"
        val downloadResult = youtubeController.downloadMusic(
            musicInfo.id,
            outputFile
        )
        result += "$downloadResult\n\n"
        // Set metadata
        result += "Setting ID3 tags...\n"
        val metadata = setMetadata(File(outputFile), musicInfo, uploader.namingFormat)
        result += "title = ${metadata.title}\n" +
                "artist = ${metadata.artist}\n" +
                "album = ${metadata.album}\n" +
                "year = ${metadata.year}\n" +
                "comment = ${metadata.comment}\n"
        // TODO : Insert music in database
        return result
    }

    private fun setMetadata(
        file: File,
        videoInfo: MusicInfo,
        namingFormat: NamingFormat
    ): Metadata {
        // Naming rules
        val namingRules = namingRuleService.find()
        var formattedTitle = videoInfo.title
        namingRules.forEach {rule ->
            formattedTitle = formattedTitle.replace(rule.replace, rule.replaceBy)
        }
        val splitTitle = formattedTitle.split(namingFormat.separator)

        // ID3 tags example here : http://www.jthink.net/jaudiotagger/examples_write.jsp
        val f = AudioFileIO.read(file)
        val tag: Tag = f.tag

        // Title
        val title = if (splitTitle.size >= 2) {
            if (namingFormat.artist_before_title) splitTitle[1] else splitTitle[0]
        } else {
            formattedTitle
        }
        tag.setField(FieldKey.TITLE, title)

        // Artist
        val artist = if (splitTitle.size >= 2) {
            if (namingFormat.artist_before_title) splitTitle[0] else splitTitle[1]
        } else {
            videoInfo.channel
        }
        tag.setField(FieldKey.ARTIST, artist)

        // Album
        val album = videoInfo.channel
        tag.setField(FieldKey.ALBUM, album)

        // Year
        val year = videoInfo.upload_date.take(4)
        tag.setField(FieldKey.YEAR, year)

        // Comment
        val comment = "{\"platform\": \"youtube\", \"id\": \"${videoInfo.id}\"}"
        tag.setField(FieldKey.COMMENT, comment)

        f.commit()

        return Metadata(title, artist, album, year, comment)
    }
}