package com.qmk.musicmanager.domain.model

import com.google.gson.Gson

data class Metadata(
    val name: String,
    val title: String,
    val artist: String,
    val genre: String,
    val album: String,
    val year: String,
    val comments: CommentsTag?
)

data class CommentsTag(
    val source: SourceTag?,
    val playlists: List<String>,
    val customTags: List<String>,
    val downloadDate: String
) {
    fun toJson(gson: Gson = Gson()): String {
        return gson.toJson(this)
    }
}

data class SourceTag(
    val id: String,
    val platform: String,
    val uploaderId: String,
    val uploader: String,
    val uploadDate: String
)