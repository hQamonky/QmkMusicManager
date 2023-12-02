package com.qmk.musicmanager.domain.model

import com.google.gson.Gson
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Model for server settings.")
data class Settings(
    @field:Schema(
        description = "Boolean to define if playlists download should be automatically trigger at a set amount of time (defined by downloadOccurrence).",
        example = "true",
        type = "Boolean"
    )
    val autoDownload: Boolean = true,
    @field:Schema(
        description = "The interval of time (in minutes) at which playlists are automatically downloaded. Has no effect if autoDownload is set to false.",
        example = "60",
        type = "Int",
        minimum = "1"
    )
    val downloadOccurrence: Int = 60,
    @field:Schema(
        description = "The path of the music folder on the server.",
        example = "/home/qmk/Music",
        type = "String"
    )
    val musicFolder: String = "./Music",
    @field:Schema(
        description = "The audio format in which the music are to be downloaded. Currently supported: best (default), aac, alac, flac, m4a, mp3, opus, vorbis, wav",
        example = "mp3",
        type = "String"
    )
    val audioFormat: String = "mp3",
    @field:Schema(
        description = "API key for the rapidapi API. You can get a key by signing up at https://rapidapi.com.",
        example = "84353a50186bb56afd86ap1bdd9ejsna03bd6a6f69msh1bbb8",
        type = "String"
    )
    val rapidapiKey: String = "",
) {
    fun toJson(): String {
        return Gson().toJson(this)
    }
}
