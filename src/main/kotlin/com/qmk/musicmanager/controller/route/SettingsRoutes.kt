package com.qmk.musicmanager.controller.route

import com.qmk.musicmanager.controller.model.BasicAPIResponse
import com.qmk.musicmanager.controller.model.ServerError
import com.qmk.musicmanager.domain.model.Settings
import com.qmk.musicmanager.server
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.settingsRoutes() {
    route("/api/settings") {
        get {
            val settings = server.getSettings()
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, settings))
        }
        post {
            val settings = call.receiveNullable<Settings>()
            if (settings == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val result = server.setSettings(settings)
            if (result is ServerError) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result))
                return@post
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result))
        }
    }
    route("/api/settings/audio-folder") {
        post {
            val path = call.receiveNullable<String>()
            if (path == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val result = server.setAudioFolder(path)
            if (result is ServerError) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result))
                return@post
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result))
        }
    }
    route("/api/settings/playlists-folder") {
        post {
            val path = call.receiveNullable<String>()
            if (path == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val result = server.setPlaylistsFolder(path)
            if (result is ServerError) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result))
                return@post
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result))
        }
    }
    route("/api/settings/archive-folder") {
        post {
            val path = call.receiveNullable<String>()
            if (path == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val result = server.setArchiveFolder(path)
            if (result is ServerError) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result))
                return@post
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result))
        }
    }
    route("/api/settings/audio-format") {
        post {
            val format = call.receiveNullable<String>()
            if (format == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val result = server.setAudioFormat(format)
            if (result is ServerError) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result))
                return@post
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result))
        }
    }
    route("/api/settings/download-occurrence") {
        post {
            val occurrence = call.receiveNullable<Int>()
            if (occurrence == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val result = server.setDownloadOccurrence(occurrence)
            if (result is ServerError) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result))
                return@post
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result))
        }
    }
    route("/api/settings/auto-download") {
        post {
            val autoDownload = call.receiveNullable<Boolean>()
            if (autoDownload == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val result = server.setAutoDownload(autoDownload)
            if (result is ServerError) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result))
                return@post
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result))
        }
    }
    route("/api/settings/rapidapi-key") {
        post {
            val rapidapiKey = call.receiveNullable<String>()
            if (rapidapiKey == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val result = server.setRapidapiKey(rapidapiKey)
            if (result is ServerError) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result))
                return@post
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result))
        }
    }
    route("/api/settings/accoustid-api-key") {
        post {
            val apiKey = call.receiveNullable<String>()
            if (apiKey == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val result = server.setAccoustidApiKey(apiKey)
            if (result is ServerError) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result))
                return@post
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result))
        }
    }
}
