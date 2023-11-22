package com.qmk.musicmanager.api.route

import com.qmk.musicmanager.api.model.BasicAPIResponse
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
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, settings.toString()))
        }
        post {
            val settings = call.receiveNullable<Settings>()
            if (settings == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val success = server.setSettings(settings)
            call.respond(HttpStatusCode.OK, BasicAPIResponse(success))
        }
    }
    route("/api/settings/music-folder") {
        post {
            val path = call.receiveNullable<String>()
            if (path == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val success = server.setMusicFolder(path)
            call.respond(HttpStatusCode.OK, BasicAPIResponse(success))
        }
    }
    route("/api/settings/download-occurrence") {
        post {
            val occurrence = call.receiveNullable<Int>()
            if (occurrence == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val success = server.setDownloadOccurrence(occurrence)
            call.respond(HttpStatusCode.OK, BasicAPIResponse(success))
        }
    }
    route("/api/settings/auto-download") {
        post {
            val autoDownload = call.receiveNullable<Boolean>()
            if (autoDownload == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val success = server.setAutoDownload(autoDownload)
            call.respond(HttpStatusCode.OK, BasicAPIResponse(success))
        }
    }
    route("/api/settings/archive-folder") {
        post {
            // TODO : Implement feature
        }
    }
}
