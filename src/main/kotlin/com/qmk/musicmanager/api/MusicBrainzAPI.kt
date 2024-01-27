package com.qmk.musicmanager.api

import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class MusicBrainzAPI(private val client: OkHttpClient = OkHttpClient()) {
    init {
        val appName = System.getProperty("appName") ?: "QmkMusicManager"
        val qmkMusicManagerVersion = System.getProperty("qmkMusicManagerVersion") ?: "Unknown"
        userAgent = "$appName/$qmkMusicManagerVersion ( $contactEmail )"
    }

    suspend fun search(entityType: SearchRequest.EntityType, query: String, limit: Int? = null, offset: String? = null): Response {
        val params = mutableListOf(RequestParameter("query", query))
        limit?.let { params.add(RequestParameter("limit", it.toString())) }
        offset?.let { params.add(RequestParameter("offset", it)) }
        return runRequest(SearchRequest(entityType, params))
    }

    suspend fun lookup(entityType: LookupRequest.EntityType, mBID: String, inc: List<String> = listOf()): Response {
        return runRequest(LookupRequest(entityType, mBID, inc))
    }

    suspend fun browse(entityType: BrowseRequest.EntityType, parameters: List<RequestParameter> = listOf()): Response {
        return runRequest(BrowseRequest(entityType, parameters))
    }

    class SearchRequest(
        entity: EntityType, private val params: List<RequestParameter>
    ) : MusicBrainzRequest(entity.value) {
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
        entity: EntityType, private val mBID: String, val inc: List<String> = listOf()
    ) : MusicBrainzRequest(entity.value) {
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

    class BrowseRequest(
        entity: EntityType, private val parameters: List<RequestParameter> = listOf()
    ) : MusicBrainzRequest(entity.value) {
        override fun buildUrl(): String {
            var url = "$baseUrl?fmt=json"
            parameters.forEach { parameter ->
                parameter.fieldValue?.let { parameterValue ->
                    url += "&${parameter.fieldName}=${parameterValue}"
                }
            }
            return url
        }

        enum class EntityType(val value: String) {
            AREA("area"),
            ARTIST("artist"),
            COLLECTION("collection"),
            EVENT("event"),
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

    private suspend fun runRequest(request: MusicBrainzRequest): Response {
        val timeSinceLastRequestCall = System.currentTimeMillis() - lastRequestCall
        if (timeSinceLastRequestCall < limitRateMilli) {
            delay(limitRateMilli - timeSinceLastRequestCall)
        }
        lastRequestCall = System.currentTimeMillis()
        return request.run(client)
    }

    companion object {
        private lateinit var userAgent: String
        private const val contactEmail = "wherlicq@gmail.com"
        private const val host = "musicbrainz.org"
        private const val version = 2
        private var lastRequestCall = System.currentTimeMillis()
        private const val limitRateMilli = 60 * 1000 // Limit rate is 1 request per second
    }
}