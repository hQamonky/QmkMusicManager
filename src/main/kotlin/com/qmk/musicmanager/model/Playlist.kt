package com.qmk.musicmanager.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Model for a playlist.")
data class Playlist(
    @field:Schema(
        description = "The id of the playlist. Using the youtube id for this.",
        example = "PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl",
        type = "String"
    )
    val id: String,
    @field:Schema(
        description = "The name of the playlist.",
        example = "Best of Willi Tracks",
        type = "String"
    )
    val name: String,
    @field:Schema(
        description = "The list of music ids contained in the playlist.",
        type = "List<String>"
    )
    val musicIds: List<String> = emptyList()
)

@Schema(description = "Model for adding a new playlist.")
data class PlaylistEntry(
    @field:Schema(
        description = "The name of the playlist.",
        example = "Best of Willi Tracks",
        type = "String"
    )
    val name: String,
    @field:Schema(
        description = "The url of the youtube playlist.",
        example = "https://www.youtube.com/playlist?list=PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl",
        type = "String"
    )
    val url: String
)

data class PlaylistInfo(
    val entries: List<Entry>,
    val id: String,
    val title: String
)

data class Entry(
    val id: String
)

@Schema(description = "Model for the result of a playlist download.")
data class DownloadResult(
    @field:Schema(
        description = "The name of the playlist.",
        example = "Best of Willi Tracks",
        type = "String"
    )
    val playlist: String,
    @field:Schema(
        description = "The list music that were skipped because they already have been downloaded.",
        example = "Best of Willi Tracks",
        type = "MutableList<String>"
    )
    val skipped: MutableList<String> = mutableListOf(),
    @field:Schema(
        description = "The list downloaded music.",
        example = "Best of Willi Tracks",
        type = "MutableList<String>"
    )
    val downloaded: MutableList<String> = mutableListOf()
)
