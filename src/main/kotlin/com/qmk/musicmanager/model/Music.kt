package com.qmk.musicmanager.model

data class Music(
    val id: String,
    val name: String,
    val title: String,
    val artist: String,
    val channelId: String,
    val uploadDate: String,
    val isNew: Boolean
)