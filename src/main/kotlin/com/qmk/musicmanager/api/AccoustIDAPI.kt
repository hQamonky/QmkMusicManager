package com.qmk.musicmanager.api

import com.qmk.musicmanager.domain.manager.ConfigurationManager
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

    fun lookup(duration: Int, fingerprint: String, meta: List<String> = listOf()): Response {
        return LookupRequest(duration, fingerprint, meta).run(client)
    }

    class LookupRequest(
        private val duration: Int, private val fingerprint: String, private val meta: List<String> = listOf()
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

        fun run(client: OkHttpClient): Response {
            return client.newCall(buildRequest()).execute()
        }

        private fun buildRequest(): Request {
            return Request.Builder().url(buildUrl()).get()
                .build()
        }

        abstract fun buildUrl(): String
    }

    companion object {
        private lateinit var apiKey: String
        private const val host = "api.acoustid.org"
        private const val version = "v2"
    }
}