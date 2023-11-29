package com.qmk.musicmanager.api.route

import com.qmk.musicmanager.api.model.BasicAPIResponse
import com.qmk.musicmanager.api.model.ServerAction
import com.qmk.musicmanager.api.model.ServerBusy
import com.qmk.musicmanager.api.model.ServerError
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
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, playlists.toJson()))
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
            if (playlist is ServerError) {
                call.respond(
                    HttpStatusCode.OK,
                    BasicAPIResponse(false, playlist.toJson())
                )
                return@post
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true))
        }
    }
    route("/api/playlists/download") {
        post {
            val result = server.downLoadPlaylists()
            if (result is ServerBusy) {
                val message = when (result.response as ServerAction) {
                    ServerAction.DOWNLOADING_PLAYLISTS -> "Server is busy downloading playlists, try again later"
                    ServerAction.DOWNLOADING_PLAYLIST -> "Server is busy downloading a playlist, try again later"
                    ServerAction.ARCHIVING_MUSIC -> "Server is busy archiving music, try again later"
                    ServerAction.NONE -> "Internal server error."
                }
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, message))
                return@post
            }
            if (result is ServerError) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result.toJson()))
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
            val result = server.editPlaylist(playlist)
            if (result is ServerError) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result.toJson()))
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true))
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
            val result = server.downLoadPlaylist(playlistId)
            if (result is ServerBusy) {
                val message = when (result.response as ServerAction) {
                    ServerAction.DOWNLOADING_PLAYLISTS -> "Server is busy downloading playlists, try again later"
                    ServerAction.DOWNLOADING_PLAYLIST -> "Server is busy downloading a playlist, try again later"
                    ServerAction.ARCHIVING_MUSIC -> "Server is busy archiving music, try again later"
                    ServerAction.NONE -> "Internal server error."
                }
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, message))
                return@post
            }
            if (result is ServerError) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result.toJson()))
                return@post
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result.toJson()))
        }

    }
    route("/api/playlists/archive-music") {
        post {
            val result = server.archiveMusic()
            if (result is ServerBusy) {
                val message = when (result.response as ServerAction) {
                    ServerAction.DOWNLOADING_PLAYLISTS -> "Server is busy downloading playlists, try again later"
                    ServerAction.DOWNLOADING_PLAYLIST -> "Server is busy downloading a playlist, try again later"
                    ServerAction.ARCHIVING_MUSIC -> "Server is busy archiving music, try again later"
                    ServerAction.NONE -> "Internal server error."
                }
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, message))
                return@post
            }
            if (result is ServerError) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result.toJson()))
                return@post
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result.toJson()))
        }
    }
}
