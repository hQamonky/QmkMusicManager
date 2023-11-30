package com.qmk.musicmanager.api.route

import com.qmk.musicmanager.api.model.BasicAPIResponse
import com.qmk.musicmanager.api.model.ServerError
import com.qmk.musicmanager.server
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.systemRoutes() {
    route("/api/factory-reset") {
        post {
            val result = server.factoryReset()
            if (result is ServerError) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result.toJson()))
                return@post
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true))
        }
    }
    route("/api/yt-dlp/update") {
        post {
            val result = server.updateYtDlp()
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result.toJson()))
        }
    }
}
