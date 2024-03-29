package com.qmk.musicmanager.domain.model

import io.swagger.v3.oas.annotations.media.Schema
import java.io.File

@Schema(description = "Model for a music file.")
data class Music(
    @field:Schema(
        description = "Name of the file on the disk.",
        example = "Bad Computer - Riddle [Monstercat Release]",
        type = "String"
    )
    val fileName: String,
    @field:Schema(
        description = "Extension of the file.",
        example = "mp3",
        type = "String"
    )
    val fileExtension: String = "mp3",
    @field:Schema(
        description = "Title of the song.",
        example = "Riddle",
        type = "String"
    )
    val title: String,
    @field:Schema(
        description = "The song artist.",
        example = "Bad Computer",
        type = "String"
    )
    val artist: String,
    @field:Schema(
        description = "The id of the music from the platform it came from. For example, this is the youtube video id if it came from youtube.",
        example = "ftshNCG_RPk",
        type = "String"
    )
    val platformId: String,
    @field:Schema(
        description = "The id of the channel that uploaded the video.",
        example = "UCT8Y-bugDyR4ADHoQ-FOluw",
        type = "String"
    )
    val uploaderId: String,
    @field:Schema(
        description = "Date at which the video was uploaded.",
        example = "13/04/2020",
        type = "String"
    )
    val uploadDate: String,
    @field:Schema(
        description = "List of playlist ids from playlists that contain this music.",
        type = "List<String>"
    )
    val playlists: List<String> = emptyList(),
    @field:Schema(
        description = "List of tags from the music file. This could be any word added by the user",
        type = "List<String>"
    )
    val tags: List<String> = emptyList(),
    @field:Schema(
        description = "Defines if the music is new or not.",
        example = "true",
        type = "Boolean"
    )
    val isNew: Boolean = true
) {
    fun toFile(audioDir: String): File {
        return File("$audioDir/${fileName}.${fileExtension}")
    }
}

@Schema(description = "Model for adding a new music.")
data class MusicInfo(
    @field:Schema(
        description = "The id of the music. Using the youtube id for this.",
        example = "ftshNCG_RPk",
        type = "String"
    )
    val id: String,
    @field:Schema(
        description = "Title of the song.",
        example = "Riddle",
        type = "String"
    )
    val title: String,
    @field:Schema(
        description = "Date at which the video was uploaded.",
        example = "13/04/2020",
        type = "String"
    )
    val upload_date: String,
    @field:Schema(
        description = "The name of the channel that uploaded the video.",
        example = "Monstercat: Uncaged",
        type = "String"
    )
    val channel: String,
    @field:Schema(
        description = "The id of the channel that uploaded the video.",
        example = "UCT8Y-bugDyR4ADHoQ-FOluw",
        type = "String"
    )
    val channel_id: String
)

@Schema(description = "Returned object when downloading a music.")
data class MusicResult(
    @field:Schema(
        description = "The downloaded music object.",
        type = "Music"
    )
    val music: Music,
    @field:Schema(
        description = "The logs associated with the download process.",
        type = "String"
    )
    val logs: String
)