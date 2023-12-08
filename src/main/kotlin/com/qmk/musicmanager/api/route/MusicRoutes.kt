package com.qmk.musicmanager.api.route

import com.qmk.musicmanager.api.model.BasicAPIResponse
import com.qmk.musicmanager.api.model.ServerError
import com.qmk.musicmanager.domain.model.Music
import com.qmk.musicmanager.server
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.musicRoutes() {
    route("/api/music/{id}") {
        post {
            val musicId = call.parameters["id"]
            val music = call.receiveNullable<Music>()
            if (musicId == null || music == null || musicId != music.platformId) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val result = server.editMusic(music)
            if (result is ServerError) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result.toJson()))
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true))
        }
    }
    route("/api/music/new") {
        get {
            val result = server.getNewMusic()
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result.toJson()))
        }
    }
}
