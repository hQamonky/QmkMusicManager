package com.qmk.musicmanager.domain.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Model for a youtube channel.")
data class Uploader(
    @field:Schema(
        description = "The id of the channel. Using the youtube id for this.",
        example = "UCT8Y-bugDyR4ADHoQ-FOluw",
        type = "String"
    )
    val id: String,
    @field:Schema(
        description = "The name of the channel.",
        example = "Monstercat: Uncaged",
        type = "String"
    )
    val name: String,
    @field:Schema(
        description = "The naming format to use renaming a music downloaded from this channel.",
        type = "NamingFormat"
    )
    val namingFormat: NamingFormat = NamingFormat(),
    @field:Schema(
        description = "The platform where the uploader is from.",
        example = "youtube",
        type = "String"
    )
    val platform: String
)
