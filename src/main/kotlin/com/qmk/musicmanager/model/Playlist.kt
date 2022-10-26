package com.qmk.musicmanager.model

data class Playlist(
    val id : String? = null,
    val youtubeId: String,
    val name: String,
    val musicIds: List<String> = emptyList(),
    val channelId: String
)
