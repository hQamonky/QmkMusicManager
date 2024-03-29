package com.qmk.musicmanager.controller.route

import com.qmk.musicmanager.controller.model.BasicAPIResponse
import com.qmk.musicmanager.controller.model.ServerError
import com.qmk.musicmanager.domain.model.NamingFormat
import com.qmk.musicmanager.server
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.uploadersRoutes() {
    route("/api/uploaders") {
        get {
            val uploaders = server.getUploaders()
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, uploaders))
        }
    }
    route("/api/uploaders/{id}") {
        get {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val result = server.getUploader(id)
            if (result is ServerError) {
                call.respond(HttpStatusCode.NotFound, BasicAPIResponse(false, result))
                return@get
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result))
        }
        post {
            val id = call.parameters["id"]
            val namingFormat = call.receiveNullable<NamingFormat>()
            if (id == null || namingFormat == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val result = server.editUploaderNamingFormat(id, namingFormat)
            if (result is ServerError) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result))
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result))
        }
    }
}
