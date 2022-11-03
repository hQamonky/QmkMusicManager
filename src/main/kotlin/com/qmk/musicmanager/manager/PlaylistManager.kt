package com.qmk.musicmanager.manager

import com.google.gson.Gson
import com.qmk.musicmanager.model.*
import com.qmk.musicmanager.service.MusicService
import com.qmk.musicmanager.service.NamingRuleService
import com.qmk.musicmanager.service.PlaylistService
import com.qmk.musicmanager.service.UploaderService
import com.qmk.musicmanager.youtube.YoutubeController
import java.io.File


class PlaylistManager(
    private val playlistService: PlaylistService,
    private val musicService: MusicService,
    private val uploaderService: UploaderService,
    private val namingRuleService: NamingRuleService,
    private val youtubeController: YoutubeController,
    private val configurationManager: ConfigurationManager = ConfigurationManager(),
    private val id3Manager: Id3Manager = Id3Manager()
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
        val playlists = playlistService.find()
        if (playlists.isEmpty()) return "Error : no playlists found."
        var result = "${playlists.size} found.\n"
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
            .map { it.id }
            .forEach { musicId ->
                val music = musicService.findById(musicId)
                if (music != null) {
                    result += "Not downloading ${music.name} because is has already been done.\n"
                    if (!music.playlistIds.contains(playlistId)) {
                        result += "Adding ${playlist.name} to music.\n"
                        musicService.newPlaylist(playlistId, musicId)
                    } else {
                        result += "${music.name} is already in ${playlist.name}.\n"
                    }
                } else {
                    result += downloadAndProcessMusic(musicId, playlistId)
                }
            }
        return result
    }

    private fun downloadAndProcessMusic(videoId: String, playlistId: String): String {
        // Get video info
        var result = "Getting info for $videoId...\n"
        val musicInfo = Gson().fromJson(youtubeController.getVideoInfo(videoId), MusicInfo::class.java)
        // Create uploader if not exist
        var uploader = uploaderService.findById(musicInfo.channel_id)
        if (uploader == null) {
            uploader = Uploader(id = musicInfo.channel_id, name = musicInfo.channel)
            uploaderService.new(uploader)
        }
        // Download music
        result += "Downloading ${musicInfo.title}...\n"
        val outputFile = "./workDir/tmp.mp3"
        val downloadResult = youtubeController.downloadMusic(musicInfo.id)
        result += "$downloadResult\n"
        // Set metadata
        result += "Setting ID3 tags...\n"
        val metadata = id3Manager.getMetadata(musicInfo, uploader.namingFormat, namingRuleService.find())
        result += "title = ${metadata.title}\n" +
                "artist = ${metadata.artist}\n" +
                "album = ${metadata.album}\n" +
                "year = ${metadata.year}\n" +
                "comment = ${metadata.comment}\n"
        id3Manager.setMetadata(File(outputFile), metadata)
        // Insert music in database
        val music = Music(
            id = musicInfo.id,
            name = metadata.name,
            title = metadata.title,
            artist = metadata.artist,
            uploaderId = uploader.id,
            uploadDate = musicInfo.upload_date,
            playlistIds = listOf(playlistId)
        )
        musicService.new(music)
        result += "Music ${music.id} was created.\n"
        // Move final file to music folder
        val musicFolder = configurationManager.getConfiguration().musicFolder
        File(outputFile).let { sourceFile ->
            sourceFile.copyTo(target = File("${musicFolder}/${metadata.name}.mp3"), overwrite = true)
            sourceFile.delete()
        }
        return result
    }
}