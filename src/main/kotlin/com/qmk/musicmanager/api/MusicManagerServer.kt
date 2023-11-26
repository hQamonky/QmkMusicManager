package com.qmk.musicmanager.api

import com.qmk.musicmanager.api.model.*
import com.qmk.musicmanager.database.dao.*
import com.qmk.musicmanager.domain.exception.NoPlaylistsFoundException
import com.qmk.musicmanager.domain.manager.*
import com.qmk.musicmanager.domain.model.*
import java.util.concurrent.ConcurrentHashMap

class MusicManagerServer {
    private val clients = ConcurrentHashMap<String, Client>()

    private val playlistDAO = PlaylistDAOImpl()
    private val musicDAO = MusicDAOImpl()
    private val uploaderDAO = UploaderDAOImpl()
    private val namingRuleDAO = NamingRuleDAOImpl()

    private val configurationManager = ConfigurationManager()
    private val id3Manager = Id3Manager()
    private val mopidyManager = MopidyManager()
    private val powerAmpManager = PowerAmpManager()
    private val youtubeManager = YoutubeManager()

    private val dataManager = DataManager(
        playlistDAO = playlistDAO, musicDAO = musicDAO, uploaderDAO = uploaderDAO, namingRuleDAO = namingRuleDAO
    )
    private val playlistManager = PlaylistManager(
        playlistDAO = playlistDAO,
        musicDAO = musicDAO,
        uploaderDAO = uploaderDAO,
        namingRuleDAO = namingRuleDAO,
        youtubeManager = youtubeManager,
        configurationManager = configurationManager,
        id3Manager = id3Manager,
        mopidyManager = mopidyManager,
        powerAmpManager = powerAmpManager
    )
    private val musicManager =
        MusicManager(musicDAO = musicDAO, configurationManager = configurationManager, id3Manager = id3Manager)

    private var processingAction: ServerAction? = null

    fun clientConnected(client: Client) {
        clients[client.id] = client
    }

    /**
     * Removes all database entries, adds default naming rules and fills database from music file's metadata.
     * @return error message. Returns null if succeeded.
     */
    suspend fun factoryReset(): ServerResponse {
        val removedAllEntries = dataManager.removeAllEntries()
        if (!removedAllEntries) {
            return ServerError("Failed to empty database.")
        }
        val addedDefaultNamingRules = dataManager.addDefaultNamingRules()
        if (!addedDefaultNamingRules) {
            return ServerError("Failed to add default naming rules.")
        }
        dataManager.addFilesToDatabase()
        // TODO : add return to dataManager.addFilesToDatabase() to handle errors
        return FactoryReset()
    }

    suspend fun updateYoutubeDl(): ServerResponse {
        return youtubeManager.updateYoutubeDl()?.let { UpdateYoutubeDl(it) } ?: ServerError("Unknown server error.")
    }

    suspend fun getPlaylistId(playlistUrl: String): String {
        return playlistManager.getPlaylistId(playlistUrl)
    }

    suspend fun doesPlaylistIdExist(id: String): Boolean {
        return playlistManager.doesPlaylistIdExist(id)
    }

    suspend fun doesPlaylistNameExist(playlistName: String): Boolean {
        return playlistManager.doesPlaylistNameExist(playlistName)
    }

    suspend fun getPlaylists(): ServerResponse {
        return GetPlaylists(playlistManager.getPlaylists())
    }

    suspend fun createPlaylist(name: String, id: String): ServerResponse {
        return playlistManager.create(name, id)?.let { CreatePlaylist(it) } ?: ServerError("Error creating playlist.")
    }

    suspend fun editPlaylist(playlist: Playlist): ServerResponse {
        return if (playlistManager.edit(playlist)) EditPlaylist() else ServerError("Error editing playlist.")
    }


    suspend fun downLoadPlaylists(): ServerResponse {
        processingAction?.let { return ServerBusy(it) }
        processingAction = ServerAction.DOWNLOADING_PLAYLISTS
        return try {
            val downloadResult = playlistManager.download()
            processingAction = null
            DownloadPlaylists(downloadResult)
        } catch (e: NoPlaylistsFoundException) {
            processingAction = null
            ServerError(e.toString())
        }
    }

    suspend fun downLoadPlaylist(playlistId: String): ServerResponse {
        processingAction?.let { return ServerBusy(it) }
        processingAction = ServerAction.DOWNLOADING_PLAYLIST
        return try {
            val downloadResult = playlistManager.download(playlistId)
            processingAction = null
            DownloadPlaylist(downloadResult)
        } catch (e: NoPlaylistsFoundException) {
            processingAction = null
            ServerError(e.toString())
        }
    }

    suspend fun archiveMusic(): ServerResponse {
        processingAction?.let { return ServerBusy(it) }
        processingAction = ServerAction.ARCHIVING_MUSIC
        return try {
            val result = playlistManager.archiveMusic()
            processingAction = null
            ArchiveMusic(result)
        } catch (e: NoPlaylistsFoundException) {
            processingAction = null
            ServerError(e.toString())
        }
    }

    suspend fun editMusic(music: Music): ServerResponse {
        return if (musicManager.editMusic(music)) EditMusic() else ServerError("Error editing music.")
    }

    suspend fun getNewMusic(): ServerResponse {
        return GetNewMusic(musicManager.getNewMusic())
    }

    suspend fun getNamingRules(): ServerResponse {
        return GetNamingRules(namingRuleDAO.allNamingRules())
    }

    suspend fun addNamingRule(replace: String, replaceBy: String, priority: Int): ServerResponse {
        return namingRuleDAO.addNewNamingRule(replace, replaceBy, priority)?.let { AddNamingRule(it) }
            ?: ServerError("Error creating naming rule.")
    }

    suspend fun getNamingRule(id: Int): ServerResponse {
        return namingRuleDAO.namingRule(id)?.let { GetNamingRule(it) } ?: ServerError("Naming rule not found.")
    }

    suspend fun editNamingRule(id: Int, replace: String, replaceBy: String, priority: Int): ServerResponse {
        return if (namingRuleDAO.editNamingRule(
                id,
                replace,
                replaceBy,
                priority
            )
        ) EditNamingRule() else ServerError("Error editing naming rule.")
    }

    suspend fun deleteNamingRule(id: Int): ServerResponse {
        return if (namingRuleDAO.deleteNamingRule(id)) DeleteNamingRule()
        else ServerError("Error deleting naming rule.")
    }

    suspend fun getSettings(): ServerResponse {
        return GetSettings(configurationManager.getConfiguration())
    }

    suspend fun setSettings(settings: Settings): ServerResponse {
        configurationManager.setConfiguration(settings)
        // TODO : implement error handling
        return SetSettings()
    }

    suspend fun setMusicFolder(path: String): ServerResponse {
        configurationManager.setMusicFolder(path)
        // TODO : implement error handling
        return SetMusicFolder()
    }

    suspend fun setDownloadOccurrence(occurrence: Int): ServerResponse {
        configurationManager.setDownloadOccurrence(occurrence)
        // TODO : Restart download timer
        // TODO : implement error handling
        return SetDownloadOccurrence()
    }

    suspend fun setAutoDownload(autoDownload: Boolean): ServerResponse {
        configurationManager.setAutoDownload(autoDownload)
        // TODO : Stop or start auto download
        // TODO : implement error handling
        return SetAutoDownload()
    }

    suspend fun getUploaders(): ServerResponse {
        return GetUploaders(uploaderDAO.allUploaders())
    }

    suspend fun getUploader(id: String): ServerResponse {
        return uploaderDAO.uploader(id)?.let { GetUploader(it) } ?: ServerError("Uploader not found.")
    }

    suspend fun editUploaderNamingFormat(id: String, namingFormat: NamingFormat): ServerResponse {
        val uploader = uploaderDAO.uploader(id) ?: return ServerError("Uploader not found.")
        return if (uploaderDAO.editUploader(id, uploader.name, namingFormat)) EditUploaderNamingFormat() else ServerError("Error editing naming format.")
    }
}