package com.qmk.musicmanager.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Model for defining how to extract artist and title from a video title.")
data class NamingFormat(
    @field:Schema(
        description = "String that separates the song artist and song title in the video title.",
        example = " - ",
        type = "String"
    )
    val separator: String = " - ",
    @field:Schema(
        description = "Boolean to define if the arist is before or after the title in the video title.",
        example = "true",
        type = "Boolean"
    )
    val artist_before_title: Boolean = true
    )
