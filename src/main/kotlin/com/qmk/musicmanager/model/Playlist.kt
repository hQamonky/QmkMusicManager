package com.qmk.musicmanager.model

data class Playlist(
    val id : Int,
    val youtubeId: String,
    val name: String,
    val musicIds: List<String>,
    val channelId: String
)
