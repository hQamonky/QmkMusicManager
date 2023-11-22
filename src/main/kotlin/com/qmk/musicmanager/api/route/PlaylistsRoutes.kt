package com.qmk.musicmanager.api.route

import com.qmk.musicmanager.api.model.BasicAPIResponse
import com.qmk.musicmanager.domain.model.Playlist
import com.qmk.musicmanager.domain.model.PlaylistEntry
import com.qmk.musicmanager.server
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.playlistsRoutes() {
    route("/api/playlists") {
        get {
            val playlists = server.getPlaylists()
            call.respond(HttpStatusCode.OK, playlists)
        }
        post {
            val request = call.receiveNullable<PlaylistEntry>()
            if (request == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val playlistId = server.getPlaylistId(request.url)
            if (server.doesPlaylistIdExist(playlistId)) {
                call.respond(
                    HttpStatusCode.OK,
                    BasicAPIResponse(false, "The playlist \"${request.url}\" has already been added.")
                )
                return@post
            }
            if (server.doesPlaylistNameExist(request.name)) {
                call.respond(
                    HttpStatusCode.OK,
                    BasicAPIResponse(false, "The playlist \"${request.name}\" already exists.")
                )
                return@post
            }
            val playlist = server.createPlaylist(request.name, playlistId)
            if (playlist == null) {
                call.respond(
                    HttpStatusCode.OK,
                    BasicAPIResponse(false, "Error while inserting playlist in database.")
                )
                return@post
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true))
        }
    }
    route("/api/playlists/download") {
        post {
            if (server.isDownloading) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, "Server is busy, try again later"))
                return@post
            }
            val result = server.downLoadPlaylists()
            if (result == null) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, "Server is busy, try again later"))
                return@post
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result.toString()))
        }
    }
    route("/api/playlists/{id}") {
        post {
            val playlistId = call.parameters["id"]
            val playlist = call.receiveNullable<Playlist>()
            if (playlistId == null || playlist == null || playlistId != playlist.id) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val successful = server.editPlaylist(playlist)
            call.respond(HttpStatusCode.OK, BasicAPIResponse(successful))
        }
    }
    route("/api/playlists/{id}/download") {
        post {
            val playlistId = call.parameters["id"]
            if (playlistId == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val playlistFound = server.doesPlaylistIdExist(playlistId)
            if (!playlistFound) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            if (server.isDownloading) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, "Server is busy, try again later"))
                return@post
            }
            val result = server.downLoadPlaylist(playlistId)
            if (result == null) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, "Server is busy, try again later"))
                return@post
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result.toString()))
        }

    }
    route("/api/playlists/archive-music") {
        post {
            val result = server.archiveMusic()
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result.toString()))
        }
    }
}
