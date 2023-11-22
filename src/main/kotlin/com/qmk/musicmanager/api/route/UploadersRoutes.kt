package com.qmk.musicmanager.api.route

import com.qmk.musicmanager.api.model.BasicAPIResponse
import com.qmk.musicmanager.domain.model.NamingFormat
import com.qmk.musicmanager.domain.model.Uploader
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
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, uploaders.toString()))
        }
    }
    route("/api/uploaders/{id}") {
        get {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val uploader = server.getUploader(id)
            if (uploader == null) {
                call.respond(HttpStatusCode.NotFound, BasicAPIResponse(false, "Uploader not found."))
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, uploader.toString()))
        }
        post {
            val id = call.parameters["id"]
            val namingFormat = call.receiveNullable<NamingFormat>()
            if (id == null || namingFormat == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val successful =
                server.editUploaderNamingFormat(id, namingFormat)
            call.respond(HttpStatusCode.OK, BasicAPIResponse(successful))
        }
    }
}
