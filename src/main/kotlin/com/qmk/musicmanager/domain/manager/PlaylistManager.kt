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
    private val platformPlaylistDAO: PlatformPlaylistDAO,
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

    suspend fun getYoutubePlaylistId(playlistUrl: String): String {
        val gson = Gson()
        val playlistInfo = gson.fromJson(
            youtubeManager.getPlaylistInfo(playlistUrl, DownloadTool.YT_DLP),
            PlaylistInfo::class.java
        )
        return playlistInfo.id
    }

    suspend fun doesPlaylistIdExist(name: String): Boolean {
        val playlist = playlistDAO.playlist(name)
        return playlist != null
    }

    suspend fun create(name: String): Playlist? {
        val playlist = playlistDAO.addNewPlaylist(name)
        if (playlist != null) {
            mopidyManager.createPlaylist(playlist.name)
            powerAmpManager.createPlaylist(playlist.name)
        }
        return playlist
    }

    suspend fun renamePlaylist(oldName: String, newName: String): Boolean {
        val oldPlaylist = playlistDAO.playlist(oldName) ?: throw PlaylistNotFoundException()
        if (oldPlaylist.name != newName) {
            mopidyManager.renamePlaylist(oldPlaylist.name, newName)
            powerAmpManager.renamePlaylist(oldPlaylist.name, newName)
        }
        return playlistDAO.renamePlaylist(oldPlaylist.name, newName)
    }

    suspend fun addMusicToPlaylist(music: Music, playListName: String): Boolean {
        val playlist = playlistDAO.playlist(playListName) ?: return false
        if (!playlistDAO.addMusicToPlaylist(music.fileName, playlist.name)) return false
        insertMusicInPlaylistFiles(music, playlist.name)
        return true
    }

    suspend fun removeMusicFromPlaylist(music: Music, playListName: String): Boolean {
        val playlist = playlistDAO.playlist(playListName) ?: return false
        if (!playlistDAO.removeMusicFromPlaylist(music.fileName, playlist.name)) return false
        removeMusicFromPlaylistFiles(music, playlist.name)
        return true
    }

    suspend fun download(): List<DownloadResult> {
        val playlists = platformPlaylistDAO.allPlaylists()
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

    suspend fun download(plPlaylistId: String): DownloadResult {
        val plPlaylist = platformPlaylistDAO.playlist(plPlaylistId)
        if (plPlaylist == null) {
            println("Error : playlist not found.")
            throw PlaylistNotFoundException()
        }
        println("Making sure yt-dlp is up to date...")
        println(youtubeManager.updateYtDlp())
        println("Downloading ${plPlaylist.name}...")
        val result = DownloadResult(playlist = plPlaylist.name)
        val playlists = platformPlaylistDAO.playlistsFromPlPlaylist(plPlaylistId)
        val gson = Gson()
        gson.fromJson(
            youtubeManager.getPlaylistInfo("${youtubeManager.playlistUrl}${plPlaylist.id}", DownloadTool.YT_DLP),
            PlaylistInfo::class.java
        ).entries
            .map { it.id }
            .forEach { plMusicId ->
                val music = musicDAO.getMusicFromPlatformId(plMusicId)
                if (music != null) {
                    println("Not downloading ${music.fileName} because is has already been done.")
                    result.skipped.add(music.fileName)
                    playlists.forEach { playlist ->
                        if (!music.playlists.contains(playlist)) {
                            println("Adding music to $playlist.")
                            playlistDAO.addMusicToPlaylist(music.fileName, playlist)
                            id3Manager.addMusicToPlaylist(File("${music.fileName}.${music.fileExtension}"), playlist)
                            insertMusicInPlaylistFiles(music, plPlaylist.name)
                        } else {
                            println("${music.fileName} is already in $playlist.")
                        }
                    }
                } else {
                    val downloadedMusic = downloadAndProcessMusic(plMusicId, plPlaylist)
                    if (downloadedMusic != null) result.downloaded.add(downloadedMusic.fileName)
                }
            }
        return result
    }

    private suspend fun downloadAndProcessMusic(
        videoId: String,
        plPlaylist: PlatformPlaylist,
        platform: String = "youtube"
    ): Music? {
        val plPlaylistId = plPlaylist.id
        // Get video info
        println("Getting info for $videoId...")
        val musicInfo =
            Gson().fromJson(youtubeManager.getVideoInfo(videoId, DownloadTool.YT_DLP), MusicInfo::class.java)
        // Create uploader if not exist
        var uploader = uploaderDAO.uploader(musicInfo.channel_id)
        if (uploader == null) {
            uploader = Uploader(id = musicInfo.channel_id, name = musicInfo.channel, platform = platform)
            uploaderDAO.addNewUploader(uploader.id, uploader.name, uploader.namingFormat, platform)
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
        val metadata = when (platform) {
            "youtube" -> {
                val metadataWithoutPlaylists = id3Manager.getMetadataFromYoutube(
                    musicInfo,
                    uploader.namingFormat,
                    namingRuleDAO.allNamingRules()
                )
                metadataWithoutPlaylists.copy(
                    comments = metadataWithoutPlaylists.comments?.copy(
                        playlists = platformPlaylistDAO.playlistsFromPlPlaylist(
                            plPlaylistId
                        )
                    )
                )
            }

            else -> null
        }
        println(
            "title = ${metadata?.title}\n" +
                    "artist = ${metadata?.artist}\n" +
                    "album = ${metadata?.album}\n" +
                    "year = ${metadata?.year}\n" +
                    "downloadDate = ${metadata?.comments?.downloadDate}\n" +
                    "id = ${metadata?.comments?.source?.id}\n" +
                    "uploaderId = ${metadata?.comments?.source?.uploaderId}\n" +
                    "uploadDate = ${metadata?.comments?.source?.uploadDate}\n"
        )
        if (metadata == null) {
            println("Error getting metadata. ")
            return null
        }
        id3Manager.setMetadata(File(outputFile), metadata)
        val music = Music(
            platformId = musicInfo.id,
            fileName = metadata.name,
            title = metadata.title,
            artist = metadata.artist,
            uploaderId = uploader.id,
            uploadDate = musicInfo.upload_date,
            playlists = metadata.comments?.playlists ?: listOf()
        )
        // Move final file to music folder
        val audioFolder = configurationManager.getConfiguration().audioFolder
        File(outputFile).moveTo("${audioFolder}/${metadata.name}.${music.fileExtension}", true)
        // Insert music in database
        musicDAO.addNewMusic(
            fileName = music.fileName,
            fileExtension = music.fileExtension,
            title = music.title,
            artist = music.artist,
            platformId = music.platformId,
            uploaderId = music.uploaderId,
            uploadDate = music.uploadDate,
            tags = listOf(),
            isNew = true
        )
        // Insert music in playlist files
        music.playlists.forEach {
            insertMusicInPlaylistFiles(music, it)
        }
        println("Music ${music.platformId} was created.")
        return music
    }

    private fun insertMusicInPlaylistFiles(music: Music, playlistName: String) {
        if (!mopidyManager.isMusicInPlaylist(music, playlistName))
            mopidyManager.addMusicToPlaylist(music, playlistName)
        if (!powerAmpManager.isMusicInPlaylist(music, playlistName))
            powerAmpManager.addMusicToPlaylist(music, playlistName)
    }

    private fun removeMusicFromPlaylistFiles(music: Music, playlistName: String) {
        if (!mopidyManager.isMusicInPlaylist(music, playlistName))
            mopidyManager.removeMusicFromPlaylist(music, playlistName)
        if (!powerAmpManager.isMusicInPlaylist(music, playlistName))
            powerAmpManager.removeMusicFromPlaylist(music, playlistName)
    }

    suspend fun archiveMusic(): List<String> {
        val archivePlaylist = mopidyManager.archivePlaylistName
        val archiveFolder = File(configurationManager.getConfiguration().audioFolder)
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