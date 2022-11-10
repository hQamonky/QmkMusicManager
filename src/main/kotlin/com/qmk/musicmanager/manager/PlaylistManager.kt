package com.qmk.musicmanager.manager

import com.google.gson.Gson
import com.qmk.musicmanager.extension.moveTo
import com.qmk.musicmanager.model.*
import com.qmk.musicmanager.service.MusicService
import com.qmk.musicmanager.service.NamingRuleService
import com.qmk.musicmanager.service.PlaylistService
import com.qmk.musicmanager.service.UploaderService
import java.io.File


class PlaylistManager(
    private val playlistService: PlaylistService,
    private val musicService: MusicService,
    private val uploaderService: UploaderService,
    private val namingRuleService: NamingRuleService,
    private val youtubeManager: YoutubeManager,
    private val configurationManager: ConfigurationManager = ConfigurationManager(),
    private val id3Manager: Id3Manager = Id3Manager(),
    private val mopidyManager: MopidyManager = MopidyManager(),
    private val powerAmpManager: PowerAmpManager = PowerAmpManager()
) {
    fun create(name: String, url: String): Playlist {
        val gson = Gson()
        val playlistInfo = gson.fromJson(youtubeManager.getPlaylistInfo(url), PlaylistInfo::class.java)
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

    fun download(): List<DownloadResult> {
        val playlists = playlistService.find()
        if (playlists.isEmpty()) println("Error : no playlists found.")
        println("${playlists.size} found.")
        val result = mutableListOf<DownloadResult>()
        playlists.forEach { playlist ->
            println("Start process for ${playlist.name} :")
            download(playlist.id)?.let { result.add(it) }
        }
        return result
    }

    fun download(playlistId: String): DownloadResult? {
        val playlist = playlistService.findById(playlistId)
        if (playlist == null) {
            println("Error : playlist not found.")
            return null
        }
        println("Downloading ${playlist.name}...")
        val result = DownloadResult(playlist = playlist.name)
        val gson = Gson()
        gson.fromJson(
            youtubeManager.getPlaylistInfo("${youtubeManager.playlistUrl}${playlist.id}"),
            PlaylistInfo::class.java
        ).entries
            .map { it.id }
            .forEach { musicId ->
                var music = musicService.findById(musicId)
                if (music != null) {
                    println("Not downloading ${music.fileName} because is has already been done.")
                    result.skipped.add(music.fileName)
                    if (!music.playlistIds.contains(playlistId)) {
                        println("Adding ${playlist.name} to music.")
                        musicService.newPlaylist(playlistId, musicId)
                    } else {
                        println("${music.fileName} is already in ${playlist.name}.")
                    }
                } else {
                    music = downloadAndProcessMusic(musicId, playlist)
                    result.downloaded.add(music.fileName)
                }
            }
        return result
    }

    private fun downloadAndProcessMusic(videoId: String, playlist: Playlist): Music {
        val playlistId = playlist.id
        // Get video info
        println("Getting info for $videoId...")
        val musicInfo = Gson().fromJson(youtubeManager.getVideoInfo(videoId), MusicInfo::class.java)
        // Create uploader if not exist
        var uploader = uploaderService.findById(musicInfo.channel_id)
        if (uploader == null) {
            uploader = Uploader(id = musicInfo.channel_id, name = musicInfo.channel)
            uploaderService.new(uploader)
        }
        // Download music
        println("Downloading ${musicInfo.title}...")
        val outputFile = "./workDir/tmp.mp3"
        val downloadResult = youtubeManager.downloadMusic(musicInfo.id)
        println("$downloadResult")
        // Set metadata
        println("Setting ID3 tags...")
        val metadata = id3Manager.getMetadata(musicInfo, uploader.namingFormat, namingRuleService.find())
        println("title = ${metadata.title}\n" +
                "artist = ${metadata.artist}\n" +
                "album = ${metadata.album}\n" +
                "year = ${metadata.year}\n" +
                "comment = ${metadata.comment}")
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
        println("Music ${music.id} was created.")
        // Move final file to music folder
        val musicFolder = configurationManager.getConfiguration().musicFolder
        File(outputFile).moveTo("${musicFolder}/${metadata.name}.${music.fileExtension}", true)
        return music
    }

    fun archiveMusic(): List<String> {
        val musicFolder = configurationManager.getConfiguration().musicFolder
        val archivePlaylist = mopidyManager.archivePlaylistName
        val archiveFolder = File("$musicFolder/$archivePlaylist")
        if (!archiveFolder.exists()) archiveFolder.mkdir()
        val musicToArchive = mopidyManager.getMusicToArchive()
        mopidyManager.mergePowerAmpPlaylist(archivePlaylist)
        powerAmpManager.convertMopidyPlaylist(archivePlaylist)
        mopidyManager.archiveMusic()
        powerAmpManager.archiveMusic()
        val result = mutableListOf<String>()
        musicToArchive.forEach {
            val music = File(it)
            music.moveTo("${archiveFolder.path}/${music.name}")
            result.add(music.name)
        }
        return result
    }
}