package com.qmk.musicmanager.domain.manager

import com.qmk.musicmanager.database.dao.MusicDAOImpl
import com.qmk.musicmanager.database.dao.NamingRuleDAOImpl
import com.qmk.musicmanager.database.dao.PlaylistDAOImpl
import com.qmk.musicmanager.database.dao.UploaderDAOImpl
import io.ktor.util.*
import org.jaudiotagger.audio.exceptions.CannotReadException
import java.io.File

class DataManager(
    private val playlistDAO: PlaylistDAOImpl,
    private val musicDAO: MusicDAOImpl,
    private val namingRuleDAO: NamingRuleDAOImpl,
    private val uploaderDAO: UploaderDAOImpl,
    private val mopidyManager: MopidyManager,
    private val powerAmpManager: PowerAmpManager
) {
    suspend fun removeAllEntries(): Boolean {
        val playlistDeleted = playlistDAO.deleteAllPlaylists()
        val musicDeleted = musicDAO.deleteAllMusic()
        val namingRulesDeleted = namingRuleDAO.deleteAllNamingRules()
        val uploaderDeleted = uploaderDAO.deleteAllUploaders()

        return playlistDeleted == musicDeleted == namingRulesDeleted == uploaderDeleted
    }

    suspend fun addDefaultNamingRules(): Boolean {
        // Priority 1
        namingRuleDAO.addNewNamingRule(replace = " / ", replaceBy = " ", priority = 1) ?: return false
        namingRuleDAO.addNewNamingRule(replace = " ‒ ", replaceBy = " - ", priority = 1) ?: return false
        // Priority 2
        namingRuleDAO.addNewNamingRule(replace = " [NCS Release]", replaceBy = "", priority = 2) ?: return false
        namingRuleDAO.addNewNamingRule(replace = " [Monstercat Release]", replaceBy = "", priority = 2) ?: return false
        namingRuleDAO.addNewNamingRule(replace = " [Diversity Release]", replaceBy = "", priority = 2) ?: return false
        namingRuleDAO.addNewNamingRule(replace = " [NCS Official Video]", replaceBy = "", priority = 2) ?: return false
        namingRuleDAO.addNewNamingRule(replace = " [Monstercat FREE Release]", replaceBy = "", priority = 2)
            ?: return false
        namingRuleDAO.addNewNamingRule(
            replace = " [Monstercat Official Music Video]",
            replaceBy = "",
            priority = 2
        ) ?: return false
        namingRuleDAO.addNewNamingRule(replace = " [Monstercat EP Release]", replaceBy = "", priority = 2)
            ?: return false
        namingRuleDAO.addNewNamingRule(replace = " [Tasty Release]", replaceBy = "", priority = 2) ?: return false
        namingRuleDAO.addNewNamingRule(replace = " | Diversity Release", replaceBy = "", priority = 2) ?: return false
        namingRuleDAO.addNewNamingRule(replace = " (Lyrics _ Lyric Video)", replaceBy = "", priority = 2)
            ?: return false
        namingRuleDAO.addNewNamingRule(replace = " | HQ Videoclip", replaceBy = "", priority = 2) ?: return false
        namingRuleDAO.addNewNamingRule(replace = " | Official Videoclip", replaceBy = "", priority = 2) ?: return false
        namingRuleDAO.addNewNamingRule(replace = " | Videoclip", replaceBy = "", priority = 2) ?: return false
        namingRuleDAO.addNewNamingRule(replace = " (Videoclip) ♦ Hardstyle ♦", replaceBy = "", priority = 2)
            ?: return false
        namingRuleDAO.addNewNamingRule(
            replace = " ♦ Hardstyle Remix (Videoclip) ♦",
            replaceBy = "",
            priority = 2
        ) ?: return false
        namingRuleDAO.addNewNamingRule(replace = " [Videoclip]", replaceBy = "", priority = 2) ?: return false
        namingRuleDAO.addNewNamingRule(replace = " (Official Music Video)", replaceBy = "", priority = 2)
            ?: return false
        namingRuleDAO.addNewNamingRule(replace = " (Official Video Clip)", replaceBy = "", priority = 2) ?: return false
        namingRuleDAO.addNewNamingRule(replace = " (Official Videoclip)", replaceBy = "", priority = 2) ?: return false
        namingRuleDAO.addNewNamingRule(replace = " (Official Video)", replaceBy = "", priority = 2) ?: return false
        namingRuleDAO.addNewNamingRule(replace = " (Official Preview)", replaceBy = "", priority = 2) ?: return false
        namingRuleDAO.addNewNamingRule(replace = " (official music video)", replaceBy = "", priority = 2)
            ?: return false
        namingRuleDAO.addNewNamingRule(replace = " | Complexity Release", replaceBy = "", priority = 2) ?: return false
        namingRuleDAO.addNewNamingRule(replace = "[Audio]", replaceBy = "", priority = 2) ?: return false
        namingRuleDAO.addNewNamingRule(replace = "【𝙻𝚈𝚁𝙸𝙲𝚂】", replaceBy = "", priority = 2) ?: return false
        namingRuleDAO.addNewNamingRule(replace = "(Official Audio)", replaceBy = "", priority = 2) ?: return false
        namingRuleDAO.addNewNamingRule(replace = "[Official Audio]", replaceBy = "", priority = 2) ?: return false
        namingRuleDAO.addNewNamingRule(replace = "	(Lyrics)", replaceBy = "", priority = 2) ?: return false
        namingRuleDAO.addNewNamingRule(replace = " (Lyrics)", replaceBy = "", priority = 2) ?: return false
        namingRuleDAO.addNewNamingRule(replace = " (Letra / Lyrics)", replaceBy = "", priority = 2) ?: return false
        namingRuleDAO.addNewNamingRule(replace = " LYRICS", replaceBy = "", priority = 2) ?: return false
        namingRuleDAO.addNewNamingRule(replace = " (HD)", replaceBy = "", priority = 2) ?: return false
        // Priority 3
        namingRuleDAO.addNewNamingRule(replace = "♦ Hardstyle ♦", replaceBy = "(Hardstyle)", priority = 3)
            ?: return false
        return true
    }

    suspend fun addFilesToDatabase() {
        val id3Manager = Id3Manager()
        val musicDir = File(ConfigurationManager().getConfiguration().musicFolder)

        musicDir.walk().forEach lit@{
            if (it.isDirectory || it.extension == "m3u8") return@lit
            try {
                val metadata = id3Manager.getMetadata(it)
                val id = metadata.comments?.source?.id ?: "qmk${generateNonce()}"
                val music = musicDAO.music(id) ?: musicDAO.addNewMusic(
                    id = id,
                    fileName = it.name,
                    fileExtension = it.extension,
                    title = metadata.title,
                    artist = metadata.artist,
                    uploaderId = metadata.comments?.source?.uploaderId ?: "",
                    uploadDate = metadata.comments?.source?.uploadDate ?: "",
                    isNew = false
                ) ?: return@lit
                metadata.comments?.playlists?.forEach { playlistName ->
                    mopidyManager.createPlaylist(playlistName)
                    powerAmpManager.createPlaylist(playlistName)
                    mopidyManager.addMusicToPlaylist(music, playlistName)
                    powerAmpManager.addMusicToPlaylist(music, playlistName)
                }
            } catch (e: CannotReadException) {
                println("Error getting metadata : file is most likely not a music file.")
                return@lit
            }
        }
    }
}