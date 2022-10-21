package com.qmk.musicmanager.model

data class Playlist(
    val id : String,
    val youtubeId: String,
    val name: String,
    val elements: List<String>,
    val channelId: String
)
