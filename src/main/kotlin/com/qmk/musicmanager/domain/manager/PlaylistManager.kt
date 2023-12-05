package com.qmk.musicmanager.domain.manager

import com.google.gson.Gson
import com.qmk.musicmanager.database.dao.*
import com.qmk.musicmanager.domain.exception.NoPlaylistsFoundException
import com.qmk.musicmanager.domain.exception.PlaylistNotFoundException
import com.qmk.musicmanager.domain.model.*
import com.qmk.musicmanager.domain.extension.moveTo
import java.io.File


class PlaylistManager(
    private val playlistDAO: PlaylistDAO,
    private val musicDAO: MusicDAO,
    private val uploaderDAO: UploaderDAO,
    private val namingRuleDAO: NamingRuleDAO,
    private val youtubeManager: YoutubeManager,
    private val configurationManager: ConfigurationManager = ConfigurationManager(),
    private val id3Manager: Id3Manager = Id3Manager(),
    private val mopidyManager: MopidyManager = MopidyManager(),
    private val powerAmpManager: PowerAmpManager = PowerAmpManager()
) {
    suspend fun getPlaylists(): List<Playlist> {
        return playlistDAO.allPlaylists()
    }

    suspend fun getPlaylistId(playlistUrl: String): String {
        val gson = Gson()
        val playlistInfo = gson.fromJson(
            youtubeManager.getPlaylistInfo(playlistUrl, DownloadTool.YT_DLP),
            PlaylistInfo::class.java
        )
        return playlistInfo.id
    }

    suspend fun doesPlaylistIdExist(id: String): Boolean {
        val playlist = playlistDAO.playlist(id)
        return playlist != null
    }

    suspend fun doesPlaylistNameExist(name: String): Boolean {
        return playlistDAO.doesPlaylistNameExist(name)
    }

    suspend fun create(name: String, id: String): Playlist? {
        val playlist = playlistDAO.addNewPlaylist(id, name)
        if (playlist != null) {
            mopidyManager.createPlaylist(playlist.name)
            powerAmpManager.createPlaylist(playlist.name)
        }
        return playlist
    }

    suspend fun edit(playlist: Playlist): Boolean {
        val oldPlaylist = playlistDAO.playlist(playlist.id) ?: throw PlaylistNotFoundException()
        if (oldPlaylist.name != playlist.name) {
            mopidyManager.renamePlaylist(oldPlaylist.name, playlist.name)
            powerAmpManager.renamePlaylist(oldPlaylist.name, playlist.name)
        }
        return playlistDAO.editPlaylist(playlist.id, playlist.name)
    }

    suspend fun download(): List<DownloadResult> {
        val playlists = playlistDAO.allPlaylists()
        if (playlists.isEmpty()) {
            println("Error : no playlists found.")
            throw NoPlaylistsFoundException()
        }
        println("${playlists.size} found.")
        val result = mutableListOf<DownloadResult>()
        playlists.forEach { playlist ->
            println("Start process for ${playlist.name} :")
            result.add(download(playlist.id))
        }
        return result
    }

    suspend fun download(playlistId: String): DownloadResult {
        val playlist = playlistDAO.playlist(playlistId)
        if (playlist == null) {
            println("Error : playlist not found.")
            throw PlaylistNotFoundException()
        }
        println("Making sure yt-dlp is up to date...")
        println(youtubeManager.updateYtDlp())
        println("Downloading ${playlist.name}...")
        val result = DownloadResult(playlist = playlist.name)
        val gson = Gson()
        gson.fromJson(
            youtubeManager.getPlaylistInfo("${youtubeManager.playlistUrl}${playlist.id}", DownloadTool.YT_DLP),
            PlaylistInfo::class.java
        ).entries
            .map { it.id }
            .forEach { musicId ->
                var music = musicDAO.music(musicId)
                if (music != null) {
                    println("Not downloading ${music.fileName} because is has already been done.")
                    result.skipped.add(music.fileName)
                    if (!music.playlists.contains(playlistId)) {
                        println("Adding ${playlist.name} to music.")
                        playlistDAO.addMusicToPlaylist(musicId, playlistId)
                        id3Manager.addMusicToPlaylist(File("${music.fileName}.${music.fileExtension}"), )
                        insertMusicInPlaylistFiles(music, playlist.name)
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

    private suspend fun downloadAndProcessMusic(videoId: String, playlist: Playlist): Music {
        val playlistId = playlist.id
        // Get video info
        println("Getting info for $videoId...")
        val musicInfo = Gson().fromJson(youtubeManager.getVideoInfo(videoId, DownloadTool.YT_DLP), MusicInfo::class.java)
        // Create uploader if not exist
        var uploader = uploaderDAO.uploader(musicInfo.channel_id)
        if (uploader == null) {
            uploader = Uploader(id = musicInfo.channel_id, name = musicInfo.channel)
            uploaderDAO.addNewUploader(uploader.id, uploader.name, uploader.namingFormat)
        }
        // Download music
        println("Downloading ${musicInfo.title}...")
        val audioFormat = configurationManager.getConfiguration().audioFormat
        val outputFileName = "./workDir/tmp"
        val outputFile = "$outputFileName.$audioFormat"
        val downloadResult =
            youtubeManager.downloadMusic(musicInfo.id, outputFileName, audioFormat, 0, DownloadTool.YT_DLP)
        println("$downloadResult")
        // Set metadata
        println("Setting ID3 tags...")
        val metadata = id3Manager.getMetadata(musicInfo, uploader.namingFormat, namingRuleDAO.allNamingRules())
        println(
            "title = ${metadata.title}\n" +
                    "artist = ${metadata.artist}\n" +
                    "album = ${metadata.album}\n" +
                    "year = ${metadata.year}\n" +
                    "downloadDate = ${metadata.comments?.downloadDate}\n" +
                    "id = ${metadata.comments?.source?.id}\n" +
                    "uploaderId = ${metadata.comments?.source?.uploaderId}\n" +
                    "uploadDate = ${metadata.comments?.source?.uploadDate}\n"
        )
        id3Manager.setMetadata(File(outputFile), metadata)
        val music = Music(
            id = musicInfo.id,
            fileName = metadata.name,
            title = metadata.title,
            artist = metadata.artist,
            uploaderId = uploader.id,
            uploadDate = musicInfo.upload_date,
            playlists = listOf(playlistId)
        )
        // Move final file to music folder
        val musicFolder = configurationManager.getConfiguration().musicFolder
        File(outputFile).moveTo("${musicFolder}/${metadata.name}.${music.fileExtension}", true)
        // Insert music in database
        musicDAO.addNewMusic(
            music.id,
            music.fileName,
            music.fileExtension,
            music.title,
            music.artist,
            music.uploaderId,
            music.uploadDate,
            isNew = true
        )
        // Insert music in playlist files
        insertMusicInPlaylistFiles(music, playlist.name)
        println("Music ${music.id} was created.")
        return music
    }

    private fun insertMusicInPlaylistFiles(music: Music, playlistName: String) {
        mopidyManager.addMusicToPlaylist(music, playlistName)
        powerAmpManager.addMusicToPlaylist(music, playlistName)
    }

    suspend fun archiveMusic(): List<String> {
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