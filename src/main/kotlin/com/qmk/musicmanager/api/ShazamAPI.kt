package com.qmk.musicmanager.api

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class ShazamAPI(
    rapidapiKey: String, private val client: OkHttpClient = OkHttpClient()
) {
    val songs = Songs(client)
    val charts = Charts(client)
    val artists = Artists(client)
    val shazamSongs = ShazamSongs(client)
    val albums = Albums(client)

    init {
        apiKey = rapidapiKey
    }

    fun eventsList(
        artistId: String,
        l: String? = null,
        from: String? = null,
        to: String? = null,
        limit: Int? = null,
        offset: Int? = null
    ): Response {
        return ShazamGetRequest(
            "shazam-events/list", listOf(
                RequestParameter("artistId", artistId),
                RequestParameter("l", l),
                RequestParameter("from", from),
                RequestParameter("to", to),
                RequestParameter("limit", limit?.toString()),
                RequestParameter("offset", offset?.toString())
            )
        ).run(client)
    }

    fun search(term: String, locale: String? = null, offset: Int? = null, limit: Int? = null): Response {
        return ShazamGetRequest(
            "search", listOf(
                RequestParameter("term", term),
                RequestParameter("locale", locale),
                RequestParameter("offset", offset?.toString()),
                RequestParameter("limit", limit?.toString())
            )
        ).run(client)
    }

    fun autoComplete(term: String, locale: String? = null): Response {
        return ShazamGetRequest(
            "auto-complete", listOf(
                RequestParameter("term", term), RequestParameter("locale", locale)
            )
        ).run(client)
    }

    class Songs(private val client: OkHttpClient) {
        fun v2Detect(
            body: String,
            timeZone: String? = null,
            identifier: String? = null,
            timestamp: String? = null,
            sampleMs: String? = null,
            locale: String? = null
        ): Response {
            return ShazamPostRequest(
                "songs/v2/detect", listOf(
                    RequestParameter("timezone", timeZone),
                    RequestParameter("identifier", identifier),
                    RequestParameter("timestamp", timestamp),
                    RequestParameter("samplems", sampleMs),
                    RequestParameter("locale", locale)
                ), body
            ).run(client)
        }

        fun v2GetDetails(id: String, l: String? = null): Response {
            return ShazamGetRequest(
                "songs/v2/get-details", listOf(
                    RequestParameter("id", id), RequestParameter("l", l)
                )
            ).run(client)
        }

        fun getRelatedArtist(id: String, l: String? = null): Response {
            return ShazamGetRequest(
                "songs/get-related-artist", listOf(
                    RequestParameter("id", id), RequestParameter("l", l)
                )
            ).run(client)
        }

        fun getCount(key: String): Response {
            return ShazamGetRequest(
                "songs/get-count", listOf(
                    RequestParameter("key", key)
                )
            ).run(client)
        }
    }

    class Charts(private val client: OkHttpClient) {
        fun list(): Response {
            return ShazamGetRequest("charts/list", listOf()).run(client)
        }

        fun track(
            locale: String? = null, listId: String? = null, pageSize: Int? = null, startFrom: Int? = null
        ): Response {
            return ShazamGetRequest(
                "charts/track", listOf(
                    RequestParameter("locale", locale),
                    RequestParameter("listid", listId),
                    RequestParameter("pageSize", pageSize.toString()),
                    RequestParameter("startFrom", startFrom.toString())
                )
            ).run(client)
        }
    }

    class Artists(private val client: OkHttpClient) {
        fun getDetails(id: String, l: String? = null): Response {
            return ShazamGetRequest(
                "artists/get-details", listOf(
                    RequestParameter("id", id), RequestParameter("l", l)
                )
            ).run(client)
        }

        fun getTopSongs(id: String, l: String? = null): Response {
            return ShazamGetRequest(
                "artists/get-top-songs", listOf(
                    RequestParameter("id", id), RequestParameter("l", l)
                )
            ).run(client)
        }

        fun getLatestRelease(id: String, l: String? = null): Response {
            return ShazamGetRequest(
                "artists/get-latest-release", listOf(
                    RequestParameter("id", id), RequestParameter("l", l)
                )
            ).run(client)
        }

        fun getSummary(id: String, l: String? = null): Response {
            return ShazamGetRequest(
                "artists/get-summary", listOf(
                    RequestParameter("id", id), RequestParameter("l", l)
                )
            ).run(client)
        }
    }

    class ShazamSongs(private val client: OkHttpClient) {
        fun getDetails(id: String, locale: String? = null): Response {
            return ShazamGetRequest(
                "shazam-songs/get-details", listOf(
                    RequestParameter("id", id), RequestParameter("locale", locale)
                )
            ).run(client)
        }

        fun listSimilarities(id: String, locale: String? = null): Response {
            return ShazamGetRequest(
                "shazam-songs/list-similarities", listOf(
                    RequestParameter("id", id), RequestParameter("locale", locale)
                )
            ).run(client)
        }
    }

    class Albums(private val client: OkHttpClient) {
        fun getDetails(id: String, l: String? = null): Response {
            return ShazamGetRequest(
                "albums/get-details", listOf(
                    RequestParameter("id", id), RequestParameter("l", l)
                )
            ).run(client)
        }

        fun getRelatedArtist(id: String, l: String? = null): Response {
            return ShazamGetRequest(
                "albums/get-related-artist", listOf(
                    RequestParameter("id", id), RequestParameter("l", l)
                )
            ).run(client)
        }
    }

    private class ShazamGetRequest(
        endpoint: String, params: List<RequestParameter>
    ) : ShazamRequest(endpoint, params) {
        override fun buildRequest(): Request {
            return Request.Builder().url(buildRequestUrl()).get().addHeader("X-RapidAPI-Key", apiKey)
                .addHeader("X-RapidAPI-Host", host).build()
        }
    }

    private class ShazamPostRequest(
        endpoint: String, params: List<RequestParameter>, private val body: String
    ) : ShazamRequest(endpoint, params) {
        override fun buildRequest(): Request {
            return Request.Builder().url(buildRequestUrl()).post(buildBody()).addHeader("content-type", "text/plain")
                .addHeader("X-RapidAPI-Key", apiKey).addHeader("X-RapidAPI-Host", host).build()
        }

        private fun buildBody(): RequestBody {
            val mediaType = "text/plain".toMediaTypeOrNull()
            return body.toRequestBody(mediaType)
        }
    }

    private abstract class ShazamRequest(
        private val endpoint: String, private val params: List<RequestParameter>
    ) {
        fun run(client: OkHttpClient): Response {
            return client.newCall(buildRequest()).execute()
        }

        abstract fun buildRequest(): Request

        fun buildRequestUrl(): String {
            var url = "https://$host/${endpoint}?"
            var isFirstParam = true
            params.forEach { param ->
                param.fieldValue?.let {
                    if (!isFirstParam) url += "&" else isFirstParam = false
                    url += "${param.fieldName}=$it"
                }
            }
            return url
        }
    }

    private data class RequestParameter(
        val fieldName: String, val fieldValue: String?
    )

    companion object {
        private lateinit var apiKey: String
        private const val host = "shazam.p.rapidapi.com"
    }
}