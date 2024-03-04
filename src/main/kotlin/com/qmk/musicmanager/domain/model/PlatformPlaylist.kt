package com.qmk.musicmanager.domain.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Model for a playlist from a platform.")
data class PlatformPlaylist(
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
        description = "The platform where the playlist is from.",
        example = "youtube",
        type = "String"
    )
    val platform: String,
    @field:Schema(
        description = "The list of playlists to set the music from this playlist to.",
        type = "List<String>"
    )
    val playlists: List<String> = emptyList()
)

@Schema(description = "Model for adding a new playlist.")
data class PlaylistEntry(
    @field:Schema(
        description = "The name of the playlist.",
        example = "Best of Willi Tracks",
        type = "String"
    )
    val url: String,
    @field:Schema(
        description = "The platform where the playlist is from.",
        example = "youtube",
        type = "String"
    )
    val platform: String,
    @field:Schema(
        description = "The list of playlists to set the music from this playlist to.",
        type = "List<String>"
    )
    val playlists: List<String> = emptyList()
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
        description = "The list of music that were skipped because they already have been downloaded.",
        example = "qgcoqCyap7Y",
        type = "MutableList<String>"
    )
    val skipped: MutableList<String> = mutableListOf(),
    @field:Schema(
        description = "The list of downloaded music.",
        example = "qgcoqCyap7Y",
        type = "MutableList<String>"
    )
    val downloaded: MutableList<String> = mutableListOf(),
    @field:Schema(
        description = "The list of music which failed to download.",
        example = "aATSfC4hvEk",
        type = "MutableList<String>"
    )
    val failed: MutableList<String> = mutableListOf()
)
