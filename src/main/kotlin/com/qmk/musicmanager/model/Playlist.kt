package com.qmk.musicmanager.model

data class Playlist(
    val id : String,
    val youtubeId: String,
    val name: String,
    val musicIds: List<String>,
    val channelId: String
)
