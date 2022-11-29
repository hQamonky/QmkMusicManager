package com.qmk.musicmanager.model

import com.google.gson.Gson
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Model for server settings.")
data class Settings(
    @field:Schema(
        description = "The interval of time (in hours) at which playlists are automatically downloaded. Has no effect if autoDownload is set to false.",
        example = "1",
        type = "Int",
        minimum = "1"
    )
    val downloadOccurrence: Int = 1,
    @field:Schema(
        description = "The path of the music folder on the server.",
        example = "/home/qmk/Music",
        type = "String"
    )
    val musicFolder: String = "./Music",
    @field:Schema(
        description = "Boolean to define if playlists download should be automatically trigger at a set amount of time (defined by downloadOccurrence).",
        example = "true",
        type = "Boolean"
    )
    val autoDownload: Boolean = true
) {
    fun toJson(): String {
        return Gson().toJson(this)
    }
}
