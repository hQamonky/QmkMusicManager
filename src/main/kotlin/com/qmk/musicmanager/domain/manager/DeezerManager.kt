package com.qmk.musicmanager.domain.manager

import com.google.gson.Gson
import com.qmk.musicmanager.api.DeezerAPI
import com.qmk.musicmanager.domain.extension.removeAnyKindOfParentheses
import com.qmk.musicmanager.domain.extension.removeLastWord
import okhttp3.Response
import org.jaudiotagger.audio.AudioFileIO
import java.io.File
import java.util.*

class DeezerManager(
    private val api: DeezerAPI = DeezerAPI()
) {
    fun getAudioDuration(file: File): Int {
        val audioFile = AudioFileIO.read(file)
        val audioHeader = audioFile.audioHeader
        return audioHeader.trackLength
    }

    suspend fun findFullMetadata(title: String, artist: String, duration: Int): DeezerAPI.TrackInfo? {
        return searchFullMetadata(title, artist, duration) ?: searchFullMetadata(artist, title, duration)
    }

    suspend fun searchFullMetadata(title: String, artist: String, duration: Int): DeezerAPI.TrackInfo? {
        var result = search("$artist $title")
        if (result != null && result.total > 0 && result.total < 10) {
            return result.data[0]
        }
        result = search(simplifyQuery("$artist $title"))
        if (result != null && result.total > 0 && result.total < 10) {
            return result.data[0]
        }
        result = advancedSearch(title, artist, duration - 20, duration + 20)
        if (result != null && result.total > 0 && result.total < 10) {
            return result.data[0]
        }
        val simpleTitle = simplifyQuery(title)
        for (i in simpleTitle.split(" ").size-1..1) {
            result = advancedSearch(simpleTitle.removeLastWord(), artist, duration - 20, duration + 20)
            if (result != null && result.total > 0 && result.total < 10) {
                return result.data[0]
            }
        }
        println(
            "DeezerManager : ${result?.total} songs found with title \"$title\", from artist \"$artist\" and with $duration duration."
        )
        return null
    }

    private fun simplifyQuery(query: String): String {
        var newQuery = query.removeAnyKindOfParentheses()
        if (newQuery.split(" ").size > 2) {
            newQuery = newQuery.lowercase(Locale.getDefault())
                .replace("lyrics", "").trim()
                .replace("official", "").trim()
                .replace("video", "").trim()
                .replace("clip", "").trim()
                .replace("music", "").trim()
                .replace("explicit", "").trim()
                .replace("release", "").trim()
        }
        return newQuery.trim()
    }

    suspend fun search(query: String): DeezerAPI.SearchResult? {
        val response = api.search(query)
        return handleSearchResponse(response)
    }

    suspend fun advancedSearch(
        title: String,
        artist: String,
        minDuration: Int,
        maxDuration: Int
    ): DeezerAPI.SearchResult? {
        val response = api.advanceSearch(
            track = title,
            artist = artist,
            minDuration = minDuration,
            maxDuration = maxDuration
        )
        return handleSearchResponse(response)
    }

    private fun handleSearchResponse(response: Response): DeezerAPI.SearchResult? {
        if (!response.isSuccessful) {
            println("Deezer API error : ${response.message} - error code : ${response.code}")
            return null
        }
        return Gson().fromJson(response.body?.string(), DeezerAPI.SearchResult::class.java)
    }
}