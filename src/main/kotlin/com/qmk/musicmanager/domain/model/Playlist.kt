package com.qmk.musicmanager.domain.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Model for a playlist.")
data class Playlist(
    @field:Schema(
        description = "The name of the playlist.",
        example = "Best of Willi Tracks",
        type = "String"
    )
    val name: String,
    @field:Schema(
        description = "The list of music contained in the playlist. Each entry is the file name of the music.",
        type = "List<String>"
    )
    val music: List<String> = emptyList()
)