package com.qmk.musicmanager.controller.route

import com.qmk.musicmanager.controller.model.BasicAPIResponse
import com.qmk.musicmanager.controller.model.MigrateMetadata
import com.qmk.musicmanager.controller.model.ServerError
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
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result))
                return@post
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true))
        }
    }
    route("/api/soft-reset") {
        post {
            val result = server.softReset()
            if (result is ServerError) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result))
                return@post
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true))
        }
    }
    route("/api/yt-dlp/update") {
        post {
            val result = server.updateYtDlp()
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result))
        }
    }
    route("/api/migrate-metadata") {
        post {
            val result = server.migrateMetadata()
            call.respond(
                HttpStatusCode.OK, BasicAPIResponse(
                    when (result) {
                        is MigrateMetadata -> true
                        is ServerError -> false
                        else -> false
                    }, result
                )
            )
        }
    }
}
