package com.qmk.musicmanager.model

data class Music(
    val id: String,
    val fileName: String,
    val fileExtension: String = "mp3",
    val title: String,
    val artist: String,
    val uploaderId: String,
    val uploadDate: String,
    val isNew: Boolean = true,
    val playlistIds: List<String> = emptyList()
)

data class MusicInfo(
    val id: String,
    val title: String,
    val upload_date: String,
    val channel: String,
    val channel_id: String
)

data class Metadata(
    val name: String,
    val title: String,
    val artist: String,
    val album: String,
    val year: String,
    val comment: String
)

data class MusicResult(
    val music: Music,
    val logs: String
)