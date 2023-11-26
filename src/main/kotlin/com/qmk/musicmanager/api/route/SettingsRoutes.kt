package com.qmk.musicmanager.api.route

import com.qmk.musicmanager.api.model.BasicAPIResponse
import com.qmk.musicmanager.api.model.ServerError
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
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, settings.response.toString()))
        }
        post {
            val settings = call.receiveNullable<Settings>()
            if (settings == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val result = server.setSettings(settings)
            if (result is ServerError) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result.response.toString()))
                return@post
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result.response.toString()))
        }
    }
    route("/api/settings/music-folder") {
        post {
            val path = call.receiveNullable<String>()
            if (path == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val result = server.setMusicFolder(path)
            if (result is ServerError) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result.response.toString()))
                return@post
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result.response.toString()))
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
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result.response.toString()))
                return@post
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result.response.toString()))
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
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result.response.toString()))
                return@post
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result.response.toString()))
        }
    }
    route("/api/settings/archive-folder") {
        post {
            // TODO : Implement feature
        }
    }
}
