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
    val entries: List<Entry>,
    val id: String,
    val title: String
)

data class Entry(
    val id: String
)

data class DownloadResult(
    val playlist: String,
    val skipped: MutableList<String> = mutableListOf(),
    val downloaded: MutableList<String> = mutableListOf()
)
