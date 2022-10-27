package com.qmk.musicmanager.model

data class Music(
    val id: String,
    val name: String,
    val title: String,
    val artist: String,
    val uploaderId: String,
    val uploadDate: String,
    val isNew: Boolean,
    val playlistIds: List<String>
)

data class MusicInfo(
    val id: String,
    val title: String,
    val upload_date: String,
    val channel: String,
    val channel_id: String
)

data class Metadata(
    val title: String,
    val artist: String,
    val album: String,
    val year: String,
    val comment: String
)