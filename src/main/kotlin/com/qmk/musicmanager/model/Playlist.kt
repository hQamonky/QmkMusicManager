package com.qmk.musicmanager.model

data class Playlist(
    val id: String,
    val name: String,
    val musicIds: List<String> = emptyList(),
    val uploaderId: String
)

data class PlaylistEntry(
    val name: String,
    val url: String
)

data class PlaylistInfo(
    val id: String,
    val title: String,
    val uploader: String,
    val uploader_id: String
)
