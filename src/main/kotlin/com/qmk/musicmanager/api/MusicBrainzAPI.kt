package com.qmk.musicmanager.api

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class MusicBrainzAPI(private val client: OkHttpClient = OkHttpClient()) {
    init {
        val appName = System.getProperty("appName") ?: "QmkMusicManager"
        val qmkMusicManagerVersion = System.getProperty("qmkMusicManagerVersion") ?: "Unknown"
        userAgent = "$appName/$qmkMusicManagerVersion ( $contactEmail )"
    }

    fun search(entityType: SearchRequest.EntityType, query: String, limit: Int? = null, offset: String? = null): Response {
        val params = mutableListOf(RequestParameter("query", query))
        limit?.let { params.add(RequestParameter("limit", it.toString())) }
        offset?.let { params.add(RequestParameter("offset", it)) }
        return SearchRequest(entityType.value, params).run(client)
    }

    fun lookup(entityType: LookupRequest.EntityType, mBID: String, inc: List<String> = listOf()): Response {
        return LookupRequest(entityType.value, mBID, inc).run(client)
    }

    class SearchRequest(
        entity: String, private val params: List<RequestParameter>
    ) : MusicBrainzRequest(entity) {
        override fun buildUrl(): String {
            var url = "$baseUrl?fmt=json"
            params.forEach { param ->
                param.fieldValue?.let {
                    url += "&${param.fieldName}=$it"
                }
            }
            return url
        }

        enum class EntityType(val value: String) {
            ANNOTATION("annotation"),
            AREA("area"),
            ARTIST("artist"),
            CD_STUB("cdstub"),
            EVENT("event"),
            INSTRUMENT("instrument"),
            LABEL("label"),
            PLACE("place"),
            RECORDING("recording"),
            RELEASE("release"),
            RELEASE_GROUP("release-group"),
            SERIES("series"),
            TAG("tag"),
            WORK("work"),
            URL("url")
        }
    }

    class LookupRequest(
        entity: String, private val mBID: String, val inc: List<String> = listOf()
    ) : MusicBrainzRequest(entity) {
        override fun buildUrl(): String {
            var url = "$baseUrl$mBID?fmt=json"
            if (inc.isNotEmpty()) {
                url += "&inc="
                var isFirstParam = true
                inc.forEach {
                    if (!isFirstParam) url += "+" else isFirstParam = false
                    url += it
                }
            }
            return url
        }

        enum class EntityType(val value: String) {
            AREA("area"),
            ARTIST("artist"),
            EVENT("event"),
            GENRE("genre"),
            INSTRUMENT("instrument"),
            LABEL("label"),
            PLACE("place"),
            RECORDING("recording"),
            RELEASE("release"),
            RELEASE_GROUP("release-group"),
            SERIES("series"),
            WORK("work"),
            URL("url")
        }
    }

    abstract class MusicBrainzRequest(
        entity: String
    ) {
        protected val baseUrl = "https://$host/ws/$version/$entity/"

        fun run(client: OkHttpClient): Response {
            return client.newCall(buildRequest()).execute()
        }

        private fun buildRequest(): Request {
            return Request.Builder().url(buildUrl()).get()
                .addHeader("User-Agent", userAgent)
                .build()
        }

        abstract fun buildUrl(): String
    }

    data class RequestParameter(
        val fieldName: String, val fieldValue: String?
    )

    companion object {
        private lateinit var userAgent: String
        private const val contactEmail = "wherlicq@gmail.com"
        private const val host = "musicbrainz.org"
        private const val version = 2
    }
}