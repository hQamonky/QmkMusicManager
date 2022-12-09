package com.qmk.musicmanager.manager

import com.google.gson.Gson
import com.qmk.musicmanager.model.Music
import com.qmk.musicmanager.model.NamingRule
import com.qmk.musicmanager.service.MusicService
import com.qmk.musicmanager.service.NamingRuleService
import java.io.File

class DataManager(
    private val musicService: MusicService,
    private val namingRuleService: NamingRuleService,
) {
    fun addDefaultNamingRules() {
        // Priority 1
        namingRuleService.new(NamingRule(replace = " / ", replaceBy = " ", priority = 1))
        namingRuleService.new(NamingRule(replace = " ‒ ", replaceBy = " - ", priority = 1))
        // Priority 2
        namingRuleService.new(NamingRule(replace = " [NCS Release]", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = " [Monstercat Release]", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = " [Diversity Release]", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = " [NCS Official Video]", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = " [Monstercat FREE Release]", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = " [Monstercat Official Music Video]", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = " [Monstercat EP Release]", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = " [Tasty Release]", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = " | Diversity Release", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = " (Lyrics _ Lyric Video)", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = " | HQ Videoclip", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = " | Official Videoclip", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = " | Videoclip", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = " (Videoclip) ♦ Hardstyle ♦", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = " ♦ Hardstyle Remix (Videoclip) ♦", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = " [Videoclip]", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = " (Official Music Video)", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = " (Official Video Clip)", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = " (Official Videoclip)", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = " (Official Video)", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = " (Official Preview)", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = " (official music video)", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = " | Complexity Release", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = "[Audio]", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = "【𝙻𝚈𝚁𝙸𝙲𝚂】", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = "(Official Audio)", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = "[Official Audio]", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = "	(Lyrics)", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = " (Lyrics)", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = " (Letra / Lyrics)", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = " LYRICS", replaceBy = "", priority = 2))
        namingRuleService.new(NamingRule(replace = " (HD)", replaceBy = "", priority = 2))
        // Priority 3
        namingRuleService.new(NamingRule(replace = "♦ Hardstyle ♦", replaceBy = "(Hardstyle)", priority = 3))
    }

    fun addFilesToDatabase() {
        val id3Manager = Id3Manager()
        val musicDir = File(ConfigurationManager().getConfiguration().musicFolder)

        musicDir.walk().forEach lit@ {
            if (it.isDirectory || it.extension == "m3u8") return@lit
            val metadata = id3Manager.getMetadata(it)
            val comment = Gson().fromJson(metadata.comment, Comment::class.java)
            if (musicService.findById(comment.id) == null) {
                musicService.add(
                    Music(
                    id = comment.id,
                    fileName = it.name,
                    fileExtension = it.extension,
                    title = metadata.title,
                    artist = metadata.artist,
                    uploaderId = metadata.album,
                    uploadDate = metadata.year,
                    isNew = false
                )
                )
            }
        }
    }
}

data class Comment(
    val platform: String,
    val id: String
)