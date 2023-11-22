package com.qmk.musicmanager.api

import com.qmk.musicmanager.api.model.Client
import com.qmk.musicmanager.database.dao.*
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

    var isDownloading: Boolean = false
        private set

    fun clientConnected(client: Client) {
        clients[client.id] = client
    }

    /**
     * Removes all database entries, adds default naming rules and fills database from music file's metadata.
     * @return error message. Returns null if succeeded.
     */
    suspend fun factoryReset(): String? {
        val removedAllEntries = dataManager.removeAllEntries()
        dataManager.addDefaultNamingRules()
        dataManager.addFilesToDatabase()

        if (!removedAllEntries) {
            return "Failed to empty database."
        }
        // TODO : add return to dataManager.addDefaultNamingRules() and dataManager.addFilesToDatabase() to handle
        //  errors
        return null
    }

    suspend fun updateYoutubeDl(): String {
        return youtubeManager.updateYoutubeDl() ?: "Unknown server error."
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

    suspend fun getPlaylists(): List<Playlist> {
        return playlistManager.getPlaylists()
    }

    suspend fun createPlaylist(name: String, id: String): Playlist? {
        return playlistManager.create(name, id)
    }

    suspend fun editPlaylist(playlist: Playlist): Boolean {
        return playlistManager.edit(playlist)
    }


    suspend fun downLoadPlaylists(): List<DownloadResult>? {
        // TODO : Implement return types
        // For the moment, returns null if busy
        if (isDownloading) return null
        isDownloading = true
        val downloadResult = playlistManager.download()
        isDownloading = false
        return downloadResult
    }

    suspend fun downLoadPlaylist(playlistId: String): DownloadResult? {
        // TODO : Implement return types
        // For the moment, returns null if busy
        if (isDownloading) return null
        isDownloading = true
        val downloadResult = playlistManager.download(playlistId)
        isDownloading = false
        return downloadResult
    }

    suspend fun archiveMusic(): List<String> {
        return playlistManager.archiveMusic()
    }

    suspend fun editMusic(music: Music) : Boolean {
        return musicManager.editMusic(music)
    }

    suspend fun getNewMusic() : List<Music> {
        return musicManager.getNewMusic()
    }

    suspend fun getNamingRules() : List<NamingRule>{
        return namingRuleDAO.allNamingRules()
    }

    suspend fun addNamingRule(replace: String, replaceBy: String, priority: Int) : NamingRule? {
        return namingRuleDAO.addNewNamingRule(replace, replaceBy, priority)
    }

    suspend fun getNamingRule(id: Int) : NamingRule? {
        return namingRuleDAO.namingRule(id)
    }

    suspend fun editNamingRule(id: Int, replace: String, replaceBy: String, priority: Int) : Boolean {
        return namingRuleDAO.editNamingRule(id, replace, replaceBy, priority)
    }

    suspend fun deleteNamingRule(id: Int) : Boolean {
        return namingRuleDAO.deleteNamingRule(id)
    }

    suspend fun getSettings() : Settings {
        return configurationManager.getConfiguration()
    }

    suspend fun setSettings(settings: Settings) : Boolean {
        configurationManager.setConfiguration(settings)
        // TODO : implement error handling
        return true
    }

    suspend fun setMusicFolder(path: String) : Boolean  {
        configurationManager.setMusicFolder(path)
        // TODO : implement error handling
        return true
    }

    suspend fun setDownloadOccurrence(occurrence: Int) : Boolean {
        configurationManager.setDownloadOccurrence(occurrence)
        // TODO : Restart download timer
        // TODO : implement error handling
        return true
    }

    suspend fun setAutoDownload(autoDownload: Boolean) : Boolean {
        configurationManager.setAutoDownload(autoDownload)
        // TODO : Stop or start auto download
        // TODO : implement error handling
        return true
    }

    suspend fun getUploaders() : List<Uploader>{
        return uploaderDAO.allUploaders()
    }

    suspend fun getUploader(id: String) : Uploader? {
        return uploaderDAO.uploader(id)
    }

    suspend fun editUploaderNamingFormat(id: String, namingFormat: NamingFormat) : Boolean {
        val uploader = uploaderDAO.uploader(id) ?: return false
        return uploaderDAO.editUploader(id, uploader.name, namingFormat)
    }
}