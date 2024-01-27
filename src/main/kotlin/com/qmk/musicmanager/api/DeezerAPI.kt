package com.qmk.musicmanager.api

import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class DeezerAPI(private val client: OkHttpClient = OkHttpClient()) {

    suspend fun search(query: String): Response {
        return runRequest(SearchRequest(query))
    }

    suspend fun advanceSearch(
        artist: String? = null,
        album: String? = null,
        track: String? = null,
        label: String? = null,
        minDuration: Int? = null,
        maxDuration: Int? = null,
        minBPM: Int? = null,
        maxBPM: Int? = null
    ): Response {
        return runRequest(AdvanceSearchRequest(artist, album, track, label, minDuration, maxDuration, minBPM, maxBPM))
    }

    class SearchRequest(private val query: String) : DeezerApiRequest("search") {
        override fun buildUrl(): String {
            return "$baseUrl?q=$query"
        }
    }

    class AdvanceSearchRequest(
        private val artist: String? = null,
        private val album: String? = null,
        private val track: String? = null,
        private val label: String? = null,
        private val minDuration: Int? = null,
        private val maxDuration: Int? = null,
        private val minBPM: Int? = null,
        private val maxBPM: Int? = null
    ) : DeezerApiRequest("search") {
        override fun buildUrl(): String {
            var url = "$baseUrl?q="
            artist?.let { url += "artist:\"$it\" " }
            album?.let { url += "album:\"$it\" " }
            track?.let { url += "track:\"$it\" " }
            label?.let { url += "label:\"$it\" " }
            minDuration?.let { url += "dur_min:$it " }
            maxDuration?.let { url += "dur_max:$it " }
            minBPM?.let { url += "bpm_min:$it " }
            maxBPM?.let { url += "bpm_max:$it " }
            return url
        }
    }

    abstract class DeezerApiRequest(
        service: String
    ) {
        protected val baseUrl = "https://$host/$service"

        fun run(client: OkHttpClient): Response {
            return client.newCall(buildRequest()).execute()
        }

        private fun buildRequest(): Request {
            return Request.Builder().url(buildUrl()).get().build()
        }

        abstract fun buildUrl(): String
    }

    private suspend fun runRequest(request: DeezerApiRequest): Response {
        // Handle the request limit rate, which is of 50 requests per 5 seconds.
        val timeSinceLastRequestCall = System.currentTimeMillis() - lastRequestCall
        if (requestCount >= limitRateAmount) {
            if (timeSinceLastRequestCall < limitRateTimeMilli) {
                delay(limitRateTimeMilli - timeSinceLastRequestCall)
            }
            requestCount = 0
            lastRequestCall = System.currentTimeMillis()
        }
        requestCount++
        return request.run(client)
    }

    data class SearchResult(
        val data: List<TrackInfo>,
        val total: Int
    )

    data class TrackInfo(
        val id: String,
        val title: String,
        val duration: Int,
        val artist: Artist,
        val album: Album,
        val type: String
    ) {
        data class Artist(
            val id: String,
            val name: String
        )
        data class Album(
            val id: String,
            val title: String
        )
    }

    companion object {
        private const val host = "api.deezer.com"
        private var lastRequestCall = System.currentTimeMillis()
        private const val limitRateTimeMilli = 60 * 5
        private const val limitRateAmount = 50
        private var requestCount = 0
    }
}