package com.qmk.musicmanager.controller

import com.qmk.musicmanager.controller.model.*
import com.qmk.musicmanager.database.dao.*
import com.qmk.musicmanager.domain.manager.*
import com.qmk.musicmanager.domain.model.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@OptIn(DelicateCoroutinesApi::class)
class MusicManagerServer {
    private val clients = ConcurrentHashMap<String, Client>()

    private val playlistDAO = PlaylistDAOImpl()
    private val platformPlaylistDAO = PlatformPlaylistDAOImpl()
    private val musicDAO = MusicDAOImpl()
    private val uploaderDAO = UploaderDAOImpl()
    private val namingRuleDAO = NamingRuleDAOImpl().apply {
        runBlocking {
            // TODO : Add default naming rules to database
//            if(allNamingRules().isEmpty()) {
//                addNewNamingRule("The drive to develop!", "...it's what keeps me going.")
//            }
        }
    }
    private val tagDAO = TagDAOImpl()

    private val configurationManager = ConfigurationManager()
    private val id3Manager = Id3Manager()
    private val mopidyManager = MopidyManager()
    private val powerAmpManager = PowerAmpManager()
    private val youtubeManager = YoutubeManager()
    private val deezerManager = DeezerManager()

    private val dataManager = DataManager(
        configurationManager = configurationManager,
        playlistDAO = playlistDAO,
        musicDAO = musicDAO,
        uploaderDAO = uploaderDAO,
        namingRuleDAO = namingRuleDAO,
        platformPlaylistDAO = platformPlaylistDAO,
        tagDAO = tagDAO,
        mopidyManager = mopidyManager,
        powerAmpManager = powerAmpManager
    )
    private val playlistManager = PlaylistManager(
        playlistDAO = playlistDAO,
        platformPlaylistDAO = platformPlaylistDAO,
        musicDAO = musicDAO,
        uploaderDAO = uploaderDAO,
        namingRuleDAO = namingRuleDAO,
        youtubeManager = youtubeManager,
        configurationManager = configurationManager,
        id3Manager = id3Manager,
        mopidyManager = mopidyManager,
        powerAmpManager = powerAmpManager,
        deezerManager = deezerManager
    )
    private val musicManager =
        MusicManager(
            musicDAO = musicDAO,
            configurationManager = configurationManager,
            id3Manager = id3Manager,
            playlistManager = playlistManager,
            deezerManager = deezerManager
        )

    private var processChangeListener: ((oldAction: ServerAction, newAction: ServerAction) -> Unit)? = null
    private var processingAction: ServerAction = ServerAction.NONE
        set(value) {
            synchronized(field) {
                val oldAction = field
                field = value
                processChangeListener?.let {
                    it(oldAction, value)
                }
            }
        }

    private var downloadTimer: Timer? = null
    private fun getNewDownloadTask(): TimerTask {
        return object : TimerTask() {
            override fun run() {
                GlobalScope.launch {
                    downLoadPlaylists()
                }
            }
        }
    }

    init {
        setProcessChangeListener { oldProcess, newProcess ->
            notifyAllClients(ProcessChange(oldProcess, newProcess))
        }
        handleDownloadTimerTask()
    }

    private fun setProcessChangeListener(listener: (oldAction: ServerAction, newAction: ServerAction) -> Unit) {
        processChangeListener = listener
    }

    private fun handleDownloadTimerTask() {
        val settings = configurationManager.getConfiguration()
        if (settings.autoDownload) {
            if (downloadTimer == null) downloadTimer = Timer()
            val occurrence = (settings.downloadOccurrence * 60 * 1000).toLong()
            downloadTimer?.schedule(getNewDownloadTask(), 0L, occurrence)
        } else {
            downloadTimer?.cancel()
            downloadTimer = null
        }
    }

    fun notifyAllClients(message: ServerResponse) {
        GlobalScope.launch {
            clients.forEach {
                val client = it.value
                if (client.socket.isActive) {
                    client.socket.send(Frame.Text(message.toJson()))
                } else {
                    disconnectClient(client.id)
                }
            }
        }
    }

    fun doesClientExist(clientId: String): Boolean {
        return clients[clientId] != null
    }

    fun connectClient(client: Client) {
        clients[client.id] = client
        GlobalScope.launch {
            if (client.socket.isActive) {
                client.socket.send(Frame.Text(CurrentProcess(processingAction).toJson()))
            } else {
                disconnectClient(client.id)
            }
        }
    }

    fun disconnectClient(clientId: String) {
        clients.remove(clientId)
    }

    fun notifyStatusToClient(clientId: String) {
        val client = clients[clientId] ?: return
        GlobalScope.launch {
            if (client.socket.isActive) {
                client.socket.send(Frame.Text(CurrentProcess(processingAction).toJson()))
            } else {
                disconnectClient(client.id)
            }
        }
    }

    /**
     * Removes all database entries, adds default naming rules and fills database from music file's metadata.
     * @return error message. Returns null if succeeded.
     */
    suspend fun factoryReset(): ServerResponse {
        dataManager.removeAllEntries()
        val addedDefaultNamingRules = dataManager.addDefaultNamingRules()
        if (!addedDefaultNamingRules) {
            return ServerError("Failed to add default naming rules.")
        }
        dataManager.addFilesToDatabase()
        // TODO : add return to dataManager.addFilesToDatabase() to handle errors
        return FactoryReset()
    }

    /**
     * Removes all database entries, adds default naming rules and fills database from music file's metadata.
     * @return error message. Returns null if succeeded.
     */
    suspend fun softReset(): ServerResponse {
        dataManager.removeMusicEntries()
        dataManager.addFilesToDatabase()
        // TODO : add return to dataManager.addFilesToDatabase() to handle errors
        return FactoryReset()
    }

    suspend fun updateYoutubeDl(): ServerResponse {
        return youtubeManager.updateYoutubeDl()?.let { UpdateYoutubeDl(it) } ?: ServerError("Unknown server error.")
    }

    suspend fun updateYtDlp(): ServerResponse {
        return youtubeManager.updateYtDlp()?.let { UpdateYtDl(it) } ?: ServerError("Unknown server error.")
    }

    suspend fun getPlaylistId(playlistUrl: String): String {
        return playlistManager.getYoutubePlaylistId(playlistUrl)
    }

    suspend fun doesPlaylistIdExist(id: String): Boolean {
        return playlistManager.doesPlaylistIdExist(id)
    }

    suspend fun doesPlaylistExist(name: String): Boolean {
        return playlistManager.doesPlaylistExist(name)
    }

    suspend fun getPlaylists(): ServerResponse {
        return GetPlaylists(playlistManager.getPlaylists())
    }

    suspend fun getPlaylist(name: String): ServerResponse {
        return playlistManager.getPlaylist(name)?.let { GetPlaylist(it) } ?: ServerError("Playlist not found.")
    }

    suspend fun addPlatformPlaylist(url: String, playlists: List<String>): ServerResponse {
        return playlistManager.createYoutubePlaylist(url, playlists)?.let { AddPlatformPlaylist(it) }
            ?: ServerError("Error creating playlist.")
    }

    suspend fun getPlatformPlaylist(id: String, platform: String): ServerResponse {
        return when (platform) {
            "youtube" -> playlistManager.getYoutubePlaylist(id)?.let { GetYoutubePlaylist(it) }
                ?: ServerError("Playlist not found.")

            else -> ServerError("Specified platform not found.")
        }
    }

    suspend fun renamePlaylist(oldName: String, newName: String): ServerResponse {
        return if (playlistManager.renamePlaylist(
                oldName,
                newName
            )
        ) RenamePlaylist() else ServerError("Error renaming playlist.")
    }

    suspend fun deletePlaylist(name: String): ServerResponse {
        return if (playlistManager.deletePlaylist(name)) DeletePlaylist() else ServerError("Error deleting playlist.")
    }

    suspend fun getYoutubePlaylists(): ServerResponse {
        val ytPlaylists = platformPlaylistDAO.allPlaylists().filter { it.platform == "youtube" }
        return GetYoutubePlaylists(ytPlaylists)
    }

    suspend fun editPlaylist(playlist: PlatformPlaylist): ServerResponse {
        return if (playlistManager.editYoutubePlaylist(
                playlist.id,
                playlist.playlists
            )
        ) EditYoutubePlaylist() else ServerError("Error editing playlist.")
    }

    suspend fun deletePlatformPlaylist(id: String): ServerResponse {
        return if (playlistManager.deletePlatformPlaylist(id)) DeletePlaylist() else ServerError("Error deleting playlist.")
    }

    suspend fun downLoadPlaylists(): ServerResponse {
        if (processingAction != ServerAction.NONE) {
            return ServerBusy(processingAction)
        }
        processingAction = ServerAction.DOWNLOADING_PLAYLISTS
        GlobalScope.launch {
            try {
                val downloadResult = playlistManager.download()
                notifyAllClients(DownloadPlaylists(downloadResult))
            } catch (e: Exception) {
                notifyAllClients(ServerError(e.toString()))
            } finally {
                processingAction = ServerAction.NONE
            }
            val mopidyLocalScanResult = mopidyManager.mopidyLocalScan()
            notifyAllClients(MopidyLocalScan(mopidyLocalScanResult))
        }
        return DownloadPlaylistsLaunched()
    }

    suspend fun downLoadPlaylist(playlistName: String): ServerResponse {
        if (processingAction != ServerAction.NONE) {
            return ServerBusy(processingAction)
        }
        processingAction = ServerAction.DOWNLOADING_PLAYLIST
        GlobalScope.launch {
            try {
                val downloadResult = playlistManager.download(playlistName)
                notifyAllClients(DownloadPlaylists(downloadResult))
            } catch (e: Exception) {
                notifyAllClients(ServerError(e.toString()))
            } finally {
                processingAction = ServerAction.NONE
            }
            val mopidyLocalScanResult = mopidyManager.mopidyLocalScan()
            notifyAllClients(MopidyLocalScan(mopidyLocalScanResult))
        }
        return DownloadPlaylistLaunched()
    }

    suspend fun downLoadYoutubePlaylist(playlistId: String): ServerResponse {
        if (processingAction != ServerAction.NONE) {
            return ServerBusy(processingAction)
        }
        processingAction = ServerAction.DOWNLOADING_PLAYLIST
        GlobalScope.launch {
            try {
                val plPlaylist = platformPlaylistDAO.playlist(playlistId)
                val downloadResult = playlistManager.downloadYoutubePlaylist(plPlaylist)
                notifyAllClients(DownloadPlaylist(downloadResult))
            } catch (e: Exception) {
                notifyAllClients(ServerError(e.toString()))
            } finally {
                processingAction = ServerAction.NONE
            }
            val mopidyLocalScanResult = mopidyManager.mopidyLocalScan()
            notifyAllClients(MopidyLocalScan(mopidyLocalScanResult))
        }
        return DownloadPlaylistLaunched()
    }

    suspend fun archiveMusic(): ServerResponse {
        if (processingAction != ServerAction.NONE) {
            return ServerBusy(processingAction)
        }
        processingAction = ServerAction.ARCHIVING_MUSIC
        GlobalScope.launch {
            try {
                val result = playlistManager.archiveMusic()
                notifyAllClients(ArchiveMusic(result))
            } catch (e: Exception) {
                notifyAllClients(ServerError(e.toString()))
            } finally {
                processingAction = ServerAction.NONE
            }
            val mopidyLocalScanResult = mopidyManager.mopidyLocalScan()
            notifyAllClients(MopidyLocalScan(mopidyLocalScanResult))
        }
        return ArchiveMusicLaunched()
    }

    suspend fun editMusic(music: Music): ServerResponse {
        return if (musicManager.editMusic(music)) {
            GlobalScope.launch {
                notifyAllClients(MopidyLocalScan(mopidyManager.mopidyLocalScan()))
            }
            EditMusic()
        } else ServerError("Error editing music.")
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
        val oldSettings = configurationManager.getConfiguration()
        configurationManager.setConfiguration(settings)
        // TODO : implement error handling
        if (oldSettings.autoDownload != settings.autoDownload || oldSettings.downloadOccurrence != settings.downloadOccurrence) {
            downloadTimer?.cancel()
            downloadTimer = null
            handleDownloadTimerTask()
        }
        return SetSettings()
    }

    suspend fun setAudioFolder(path: String): ServerResponse {
        configurationManager.setAudioFolder(path)
        // TODO : implement error handling
        return SetAudioFolder()
    }

    suspend fun setPlaylistsFolder(path: String): ServerResponse {
        configurationManager.setPlaylistsFolder(path)
        // TODO : implement error handling
        return SetPlaylistsFolder()
    }

    suspend fun setArchiveFolder(path: String): ServerResponse {
        configurationManager.setArchiveFolder(path)
        // TODO : implement error handling
        return SetArchiveFolder()
    }

    suspend fun setAudioFormat(format: String): ServerResponse {
        configurationManager.setAudioFormat(format)
        // TODO : implement error handling
        return SetAudioFormat()
    }

    suspend fun setDownloadOccurrence(occurrence: Int): ServerResponse {
        configurationManager.setDownloadOccurrence(occurrence)
        // TODO : implement error handling
        downloadTimer?.cancel()
        downloadTimer = Timer()
        val occurrenceInMillis = (occurrence * 60 * 1000).toLong()
        downloadTimer?.schedule(getNewDownloadTask(), 0L, occurrenceInMillis)
        return SetDownloadOccurrence()
    }

    suspend fun setAutoDownload(autoDownload: Boolean): ServerResponse {
        configurationManager.setAutoDownload(autoDownload)
        // TODO : implement error handling
        handleDownloadTimerTask()
        return SetAutoDownload()
    }

    suspend fun setRapidapiKey(key: String): ServerResponse {
        configurationManager.setRapidapiKey(key)
        // TODO : implement error handling
        return SetRapidapiKey()
    }

    suspend fun setAccoustidApiKey(key: String): ServerResponse {
        configurationManager.setAccoustidApiKey(key)
        // TODO : implement error handling
        return SetAccoustidApiKey()
    }

    suspend fun getUploaders(): ServerResponse {
        return GetUploaders(uploaderDAO.allUploaders())
    }

    suspend fun getUploader(id: String): ServerResponse {
        return uploaderDAO.uploader(id)?.let { GetUploader(it) } ?: ServerError("Uploader not found.")
    }

    suspend fun editUploaderNamingFormat(id: String, namingFormat: NamingFormat): ServerResponse {
        if (uploaderDAO.uploader(id) == null) return ServerError("Uploader not found.")
        return if (uploaderDAO.editUploader(
                id,
                namingFormat
            )
        ) EditUploaderNamingFormat() else ServerError("Error editing naming format.")
    }

    suspend fun addExternalFilesToPlaylists(playlists: List<String>): ServerResponse {
        val folder = configurationManager.getConfiguration().audioFolder
        val files = id3Manager.addUntaggedFilesToPlaylists(File(folder), playlists)
            ?: return ServerError("Error while getting audio files.")
        if (files.isEmpty()) return ServerError("No external music found.")
        files.forEach { musicFile ->
            val metadata = id3Manager.getMetadata(musicFile)
            val music = musicDAO.addNewMusic(
                fileName = musicFile.nameWithoutExtension,
                fileExtension = musicFile.extension,
                title = metadata.title,
                artist = metadata.artist,
                platformId = "",
                uploaderId = "",
                uploadDate = "",
                tags = listOf(),
                isNew = false
            ) ?: return ServerError("Error adding music to database.")
            playlists.forEach { playlist ->
                playlistManager.create(playlist)
                val result = playlistManager.addMusicToPlaylist(music, playlist)
                if (!result) {
                    return ServerError("Error adding ${musicFile.name} to $playlist.")
                }
            }
        }
        return AddExternalMusicToPlaylists()
    }

    suspend fun migrateMetadata(): ServerResponse {
        val result = TagsMigrationManager().convertAllFilesMetadata()
        return if (result == null) {
            MigrateMetadata("Success !")
        } else {
            ServerError(result)
        }
    }
}