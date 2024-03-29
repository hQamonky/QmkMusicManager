package com.qmk.musicmanager.domain.manager

import com.google.gson.Gson
import com.qmk.musicmanager.database.dao.*
import com.qmk.musicmanager.domain.exception.NoPlaylistsFoundException
import com.qmk.musicmanager.domain.exception.PlaylistNotFoundException
import com.qmk.musicmanager.domain.extension.moveTo
import com.qmk.musicmanager.domain.model.*
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
    private val powerAmpManager: PowerAmpManager = PowerAmpManager(),
    private val deezerManager: DeezerManager = DeezerManager()
) {
    suspend fun getPlaylists(): List<Playlist> {
        return playlistDAO.allPlaylists()
    }

    suspend fun getPlaylist(name: String): Playlist? {
        return playlistDAO.playlist(name)
    }

    suspend fun doesPlaylistExist(name: String): Boolean {
        return playlistDAO.playlist(name) != null
    }

    fun getYoutubePlaylistId(playlistUrl: String): String {
        val gson = Gson()
        val playlistInfo = gson.fromJson(
            youtubeManager.getPlaylistInfo(playlistUrl, DownloadTool.YT_DLP),
            PlaylistInfo::class.java
        )
        return playlistInfo.id
    }

    suspend fun doesPlaylistIdExist(id: String): Boolean {
        val playlist = platformPlaylistDAO.playlist(id)
        return playlist != null
    }

    suspend fun create(name: String): Playlist? {
        val playlist = playlistDAO.playlist(name)?.let { return it } ?: playlistDAO.addNewPlaylist(name)
        if (playlist != null) {
            mopidyManager.createPlaylist(playlist.name)
            powerAmpManager.createPlaylist(playlist.name)
        }
        return playlist
    }

    suspend fun renamePlaylist(oldName: String, newName: String): Boolean {
        val playlist = playlistDAO.playlist(oldName) ?: throw PlaylistNotFoundException()
        if (playlist.name != newName) {
            mopidyManager.renamePlaylist(oldName, newName)
            powerAmpManager.renamePlaylist(oldName, newName)
        }
        val audioDir = configurationManager.getConfiguration().audioFolder
        playlist.music.forEach { musicFileName ->
            val music = musicDAO.music(musicFileName) // TODO : Check and throw "MusicNotFoundException
            music?.let {
                id3Manager.renamePlaylistForMusic(oldName, newName, it.toFile(audioDir))
            }
        }
        return playlistDAO.renamePlaylist(playlist.name, newName)
    }

    suspend fun deletePlaylist(name: String): Boolean {
        val playlist = playlistDAO.playlist(name) ?: return false
        playlist.music.forEach {
            val music = musicDAO.music(it) ?: return false
            id3Manager.removeMusicFromPlaylist(music.toFile(configurationManager.getConfiguration().audioFolder), name)
        }
        mopidyManager.deletePlaylist(name)
        powerAmpManager.deletePlaylist(name)
        return playlistDAO.deletePlaylist(name)
    }

    suspend fun getYoutubePlaylist(id: String): PlatformPlaylist? {
        return platformPlaylistDAO.playlist(id)
    }

    suspend fun createYoutubePlaylist(url: String, playlists: List<String>): PlatformPlaylist? {
        val playlistInfo = Gson().fromJson(
            youtubeManager.getPlaylistInfo(url, DownloadTool.YT_DLP),
            PlaylistInfo::class.java
        )
        return platformPlaylistDAO.addNewPlaylist(
            playlistInfo.id,
            playlistInfo.title,
            "youtube",
            playlists.mapNotNull { playlist ->
                create(playlist)?.name
            })
    }

    suspend fun editYoutubePlaylist(id: String, playlists: List<String>): Boolean {
        return platformPlaylistDAO.editPlaylist(id, playlists)
    }

    suspend fun deletePlatformPlaylist(id: String): Boolean {
        return platformPlaylistDAO.deletePlaylist(id)
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
        val plPlaylists = platformPlaylistDAO.allPlaylists()
        if (plPlaylists.isEmpty()) {
            println("Error : no playlists found.")
            throw NoPlaylistsFoundException()
        }
        println("${plPlaylists.size} found.")
        val result = mutableListOf<DownloadResult>()
        plPlaylists.forEach { plPlaylist ->
            println("Start process for ${plPlaylist.name} :")
            result.add(downloadYoutubePlaylist(plPlaylist))
        }
        return result
    }

    suspend fun download(name: String): List<DownloadResult> {
        val youtubePlaylists = platformPlaylistDAO.plPlaylistsFromPlaylist(name)
        val result: MutableList<DownloadResult> = mutableListOf()
        youtubePlaylists.forEach { id ->
            val plPlaylist = platformPlaylistDAO.playlist(id)
            result.add(downloadYoutubePlaylist(plPlaylist))
        }
        return result
    }

    suspend fun downloadYoutubePlaylist(plPlaylist: PlatformPlaylist?): DownloadResult {
        if (plPlaylist == null) {
            println("Error : playlist not found.")
            throw PlaylistNotFoundException()
        }
        println("Making sure yt-dlp is up to date...")
        println(youtubeManager.updateYtDlp())
        println("Downloading ${plPlaylist.name}...")
        val result = DownloadResult(playlist = plPlaylist.name)
        val gson = Gson()
        val musicFromPlaylist = gson.fromJson(
            youtubeManager.getPlaylistInfo("${youtubeManager.playlistUrl}${plPlaylist.id}", DownloadTool.YT_DLP),
            PlaylistInfo::class.java
        ).entries
            .mapNotNull {
                it?.id
            }
        println(musicFromPlaylist.size)
        musicFromPlaylist.forEach { plMusicId ->
            val music = musicDAO.getMusicFromPlatformId(plMusicId)
            if (music != null) {
                println("Not downloading ${music.fileName} because is has already been done.")
                result.skipped.add(music.fileName)
                plPlaylist.playlists.forEach { playlist ->
                    if (!music.playlists.contains(playlist)) {
                        println("Adding music to $playlist.")
                        playlistDAO.addMusicToPlaylist(music.fileName, playlist)
                        id3Manager.addMusicToPlaylist(
                            music.toFile(configurationManager.getConfiguration().audioFolder),
                            playlist
                        )
                        insertMusicInPlaylistFiles(music, playlist)
                    } else {
                        println("${music.fileName} is already in $playlist.")
                    }
                }
            } else {
                val downloadedMusic = downloadAndProcessMusic(plMusicId, plPlaylist)
                if (downloadedMusic != null) result.downloaded.add(downloadedMusic.fileName)
                else result.failed.add(plMusicId)
            }
        }
        return result
    }

    private suspend fun downloadAndProcessMusic(
        plMusicId: String,
        plPlaylist: PlatformPlaylist,
        platform: String = "youtube"
    ): Music? {
        // Get video info
        println("Getting info for $plMusicId...")
        val musicInfo =
            Gson().fromJson(youtubeManager.getVideoInfo(plMusicId, DownloadTool.YT_DLP), MusicInfo::class.java)
        if (musicInfo == null) {
            println("$plMusicId is unavailable.")
            return null
        }
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
        val metadataFromPlatform = when (platform) {
            "youtube" -> {
                val metadataWithoutPlaylists = id3Manager.getMetadataFromYoutube(
                    musicInfo,
                    uploader.namingFormat,
                    namingRuleDAO.allNamingRules()
                )
                metadataWithoutPlaylists.copy(
                    comments = metadataWithoutPlaylists.comments?.copy(
                        playlists = plPlaylist.playlists
                    )
                )
            }

            else -> return null
        }
        val deezerMetadata = run {
            val fileDuration = deezerManager.getAudioDuration(File(outputFile))
            deezerManager.findFullMetadata(
                metadataFromPlatform.title,
                metadataFromPlatform.artist,
                fileDuration
            )
        }

        val metadata = if (deezerMetadata != null) {
            metadataFromPlatform.copy(
                title = deezerMetadata.title,
                artist = deezerMetadata.artist,
                genre = deezerMetadata.genre,
                album = deezerMetadata.album,
                year = deezerMetadata.releaseDate
            )
        } else metadataFromPlatform

        println(
            "title = ${metadata.title}\n" +
                    "artist = ${metadata.artist}\n" +
                    "album = ${metadata.album}\n" +
                    "year = ${metadata.year}\n" +
                    "downloadDate = ${metadata.comments?.downloadDate}\n" +
                    "id = ${metadata.comments?.source?.id}\n" +
                    "uploaderId = ${metadata.comments?.source?.uploaderId}\n" +
                    "uploadDate = ${metadata.comments?.source?.uploadDate}\n" +
                    "playlists = ${metadata.comments?.playlists.toString()}\n"
        )
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
        if (mopidyManager.isMusicInPlaylist(music, playlistName))
            mopidyManager.removeMusicFromPlaylist(music, playlistName)
        if (powerAmpManager.isMusicInPlaylist(music, playlistName))
            powerAmpManager.removeMusicFromPlaylist(music, playlistName)
    }

    suspend fun archiveMusic(): List<String> {
        val archivePlaylist = mopidyManager.archivePlaylistName
        val archiveFolder = File(configurationManager.getConfiguration().archiveFolder)
        if (!archiveFolder.exists()) archiveFolder.mkdir()
        val musicToArchive = mopidyManager.getMusicToArchive()
        mopidyManager.mergePowerAmpPlaylist(archivePlaylist)
        powerAmpManager.convertMopidyPlaylist(archivePlaylist)
        mopidyManager.archiveMusic()
        powerAmpManager.archiveMusic()
        val result = mutableListOf<String>()
        musicToArchive.forEach {
            val file = File(it)
            val music = musicDAO.music(file.nameWithoutExtension)
            music?.playlists?.forEach { playlist ->
                removeMusicFromPlaylist(music, playlist)
            }
            id3Manager.updateMetadata(file = file, playlists = listOf())
            file.moveTo("${archiveFolder.path}/${file.name}")
            result.add(file.name)
        }
        return result
    }
}