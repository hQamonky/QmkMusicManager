package com.qmk.musicmanager.manager

import com.google.gson.Gson
import com.qmk.musicmanager.extension.moveTo
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
    private val id3Manager: Id3Manager = Id3Manager(),
    private val mopidyManager: MopidyManager = MopidyManager(),
    private val powerAmpManager: PowerAmpManager = PowerAmpManager()
) {
    fun create(name: String, url: String): Playlist {
        val gson = Gson()
        val playlistInfo = gson.fromJson(youtubeController.getPlaylistInfo(url), PlaylistInfo::class.java)
        val playlist = Playlist(
            id = playlistInfo.id,
            name = name
        )
        val doesNotExist = playlistService.findById(playlist.id) == null
        if (doesNotExist) {
            playlistService.new(playlist)
            mopidyManager.createPlaylist(playlist.name)
            powerAmpManager.createPlaylist(playlist.name)
        }
        return playlist
    }

    fun edit(playlist: Playlist) {
        val oldPlaylist = playlistService.findById(playlist.id) ?: return
        if (oldPlaylist.name != playlist.name) {
            mopidyManager.renamePlaylist(oldPlaylist.name, playlist.name)
            powerAmpManager.renamePlaylist(oldPlaylist.name, playlist.name)
        }
        playlistService.save(playlist)
    }

    fun download(): String {
        val playlists = playlistService.find()
        if (playlists.isEmpty()) return "Error : no playlists found."
        var result = "${playlists.size} found.\n"
        playlists.forEach { playlist ->
            result += "Start process for ${playlist.name} :\n"
            result += download(playlist.id)
        }
        return result
    }

    fun download(playlistId: String): String {
        var result = ""
        val playlist = playlistService.findById(playlistId) ?: return "Error : playlist not found."
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
                    result += "Not downloading ${music.fileName} because is has already been done.\n"
                    if (!music.playlistIds.contains(playlistId)) {
                        result += "Adding ${playlist.name} to music.\n"
                        musicService.newPlaylist(playlistId, musicId)
                    } else {
                        result += "${music.fileName} is already in ${playlist.name}.\n"
                    }
                } else {
                    result += downloadAndProcessMusic(musicId, playlist)
                }
            }
        return result
    }

    private fun downloadAndProcessMusic(videoId: String, playlist: Playlist): String {
        val playlistId = playlist.id
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
            fileName = metadata.name,
            title = metadata.title,
            artist = metadata.artist,
            uploaderId = uploader.id,
            uploadDate = musicInfo.upload_date,
            playlistIds = listOf(playlistId)
        )
        musicService.new(music)
        mopidyManager.addMusicToPlaylist(music, playlist.name)
        powerAmpManager.addMusicToPlaylist(music, playlist.name)
        result += "Music ${music.id} was created.\n"
        // Move final file to music folder
        val musicFolder = configurationManager.getConfiguration().musicFolder
        File(outputFile).moveTo("${musicFolder}/${metadata.name}.${music.fileExtension}", true)
        return result
    }

    fun archiveMusic(): String {
        val musicFolder = configurationManager.getConfiguration().musicFolder
        val archivePlaylist = mopidyManager.archivePlaylistName
        val archiveFolder = File("$musicFolder/$archivePlaylist")
        if (!archiveFolder.exists()) archiveFolder.mkdir()
        val musicToArchive = mopidyManager.getMusicToArchive()
        mopidyManager.mergePowerAmpPlaylist(archivePlaylist)
        powerAmpManager.convertMopidyPlaylist(archivePlaylist)
        mopidyManager.archiveMusic()
        powerAmpManager.archiveMusic()
        musicToArchive.forEach {
            val music = File(it)
            music.moveTo("${archiveFolder.path}/${music.name}")
        }
        return "Archived music."
    }
}