package com.qmk.musicmanager.model

data class Music(
    val id: Int,
    val name: String,
    val title: String,
    val artist: String,
    val channelId: String,
    val uploadDate: String,
    val isNew: Boolean,
    val playlistIds: List<Int>
)