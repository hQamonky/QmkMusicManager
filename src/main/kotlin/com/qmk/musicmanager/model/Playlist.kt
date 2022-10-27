package com.qmk.musicmanager.model

data class Playlist(
    val id: String,
    val name: String,
    val musicIds: List<String> = emptyList()
)

data class PlaylistEntry(
    val name: String,
    val url: String
)

data class PlaylistInfo(
    val id: String,
    val title: String
)
