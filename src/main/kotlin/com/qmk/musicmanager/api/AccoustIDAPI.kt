package com.qmk.musicmanager.api

import com.qmk.musicmanager.domain.manager.ConfigurationManager
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class AccoustIDAPI(
    configurationManager: ConfigurationManager = ConfigurationManager(),
    private val client: OkHttpClient = OkHttpClient()
) {
    init {
        apiKey = configurationManager.getAccoustidApiKey() ?: ""
        if (apiKey.isBlank() || apiKey.isEmpty())
            println("Error getting AccoustID API key. Set the API key inside the ./data/accoustidapikey file, or set it via the dedicated endpoint.")
    }

    suspend fun lookup(duration: Double, fingerprint: String, meta: List<String> = listOf()): okhttp3.Response {
        return runRequest(LookupRequest(duration, fingerprint, meta))
    }

    suspend fun lookupRecordingIds(duration: Double, fingerprint: String): okhttp3.Response {
        return runRequest(LookupRequest(duration, fingerprint, listOf("recordingids")))
    }

    class LookupRequest(
        private val duration: Double, private val fingerprint: String, private val meta: List<String> = listOf()
    ) : AccoustIDRequest("lookup") {
        override fun buildUrl(): String {
            var url = "$baseUrl&duration=$duration"
            if (meta.isNotEmpty()) {
                url += "&meta="
                var isFirstParam = true
                meta.forEach {
                    if (!isFirstParam) url += "+" else isFirstParam = false
                    url += it
                }
            }
            return "$url&fingerprint=$fingerprint"
        }
    }

    abstract class AccoustIDRequest(
        endpoint: String
    ) {
        protected val baseUrl = "https://$host/$version/$endpoint?client=$apiKey"

        fun run(client: OkHttpClient): okhttp3.Response {
            return client.newCall(buildRequest()).execute()
        }

        private fun buildRequest(): Request {
            return Request.Builder().url(buildUrl()).get().build()
        }

        abstract fun buildUrl(): String
    }

    interface Response

    data class ErrorResponse(
        val error: Error,
        val status: String
    ) : Response {
        data class Error(
            val code: Int,
            val message: String
        )
    }

    data class LookupRecordingIdsResponse(
        val results: List<Results>,
        val status: String
    ) : Response {
        data class Results(
            val id: String,
            val recordings: List<Recordings>,
            val score: Int
        ) {
            data class Recordings(
                val id: String
            )
        }
    }

    private suspend fun runRequest(request: AccoustIDRequest): okhttp3.Response {
        val timeSinceLastRequestCall = System.currentTimeMillis() - lastRequestCall
        if (timeSinceLastRequestCall < limitRateMilli) {
            delay(limitRateMilli - timeSinceLastRequestCall)
        }
        lastRequestCall = System.currentTimeMillis()
        return request.run(client)
    }

    companion object {
        private lateinit var apiKey: String
        private const val host = "api.acoustid.org"
        private const val version = "v2"
        private var lastRequestCall = System.currentTimeMillis()
        private const val limitRateMilli = 340 // Limit rate is 3 requests per second
    }
}
