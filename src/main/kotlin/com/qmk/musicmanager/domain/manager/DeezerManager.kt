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

    suspend fun findFullMetadata(title: String, artist: String, duration: Int): Metadata? {
        return (searchFullMetadata(
            title, artist, duration
        ) ?: searchFullMetadata(
            artist, title, duration
        ))?.toMetadata()
    }

    suspend fun findFullMetadata(title: String, artist: String, file: File): Metadata? {
        val duration = getAudioDuration(file)
        return (searchFullMetadata(
            title, artist, duration
        ) ?: searchFullMetadata(
            artist, title, duration
        ))?.toMetadata()
    }

    suspend fun getFullMetadata(title: String, artist: String, file: File): Metadata? {
        val duration = getAudioDuration(file)
        val result = advancedSearch(title, artist, duration - 20, duration + 20)
        return if (result != null && result.total > 0) {
            result.data[0].toMetadata()
        } else null
    }

    suspend fun findFullMetadata(query: String): Metadata? {
        return (searchFullMetadata(query))?.toMetadata()
    }

    suspend fun searchFullMetadata(title: String, artist: String, duration: Int): DeezerAPI.TrackInfo? {
        var result = advancedSearch(title, artist, duration - 20, duration + 20)
        if (result != null && result.total > 0) {
            return result.data[0]
        }
        result = advancedSearch(simplifyQuery(title), artist, duration - 20, duration + 20)
        if (result != null && result.total > 0) {
            return result.data[0]
        }
        result = search("$artist $title")
        if (result != null && result.total > 0) {
            return result.data[0]
        }
        result = search(simplifyQuery("$artist $title"))
        if (result != null && result.total > 0) {
            return result.data[0]
        }
        var simpleTitle = simplifyQuery(title)
        for (i in 1 until simpleTitle.split(" ").size) {
            simpleTitle = simpleTitle.removeLastWord()
            result = advancedSearch(simpleTitle, artist, duration - 20, duration + 20)
            if (result != null && result.total > 0) {
                return result.data[0]
            }
        }
        println(
            "DeezerManager : ${result?.total} songs found with title \"$title\", from artist \"$artist\" and with $duration duration."
        )
        return null
    }

    suspend fun searchFullMetadata(query: String): DeezerAPI.TrackInfo? {
        var result = search(query)
        if (result != null && result.total > 0) {
            return result.data[0]
        }
        result = search(simplifyQuery(query))
        if (result != null && result.total > 0) {
            return result.data[0]
        }
        var simpleQuery = simplifyQuery(query)
        for (i in 2 until simpleQuery.split(" ").size) {
            simpleQuery = simpleQuery.removeLastWord()
            result = search(simpleQuery)
            if (result != null && result.total > 0) {
                return result.data[0]
            }
        }
        println(
            "DeezerManager : ${result?.total} songs found for $query."
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

    private suspend fun getAlbumInfo(albumId: String): DeezerAPI.AlbumInfo? {
        val response = api.getAlbum(albumId)
        if (!response.isSuccessful) {
            println("Deezer API error : ${response.message} - error code : ${response.code}")
            return null
        }
        return Gson().fromJson(response.body?.string(), DeezerAPI.AlbumInfo::class.java)
    }

    private suspend fun search(query: String): DeezerAPI.SearchResult? {
        val response = api.search(query)
        return handleSearchResponse(response)
    }

    private suspend fun advancedSearch(
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

    data class Metadata(
        val title: String,
        val artist: String,
        val album: String,
        val genre: String,
        val releaseDate: String
    )


    private suspend fun DeezerAPI.TrackInfo.toMetadata(): Metadata {
        val album = getAlbumInfo(this.album.id)
        val genres = album?.genres?.data?.map { it.name }
        var genre = ""
        var isFirst = true
        genres?.forEach {
            if (isFirst) {
                isFirst = false
            } else {
                genre += ", "
            }
            genre += it
        }
        return Metadata(
            this.title,
            this.artist.name,
            this.album.title,
            genre,
            album?.release_date ?: ""
        )
    }
}