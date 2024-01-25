package com.qmk.musicmanager.controller.route

import com.qmk.musicmanager.controller.model.*
import com.qmk.musicmanager.domain.model.Playlist
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
    route("/api/playlists/{name}") {
        get {
            val playlistName = call.parameters["name"]
            if (playlistName == null ) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            when (val result = server.getPlaylist(playlistName)) {
                is ServerError -> call.respond(
                        HttpStatusCode.OK,
                        BasicAPIResponse(false, "The playlist \"$playlistName\" does not exist.")
                    )
                is GetPlaylist -> call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result.toJson()))
            }
        }
        post {
            val playlistName = call.parameters["name"]
            val playlist = call.receiveNullable<Playlist>()
            if (playlistName == null || playlist == null || playlistName == playlist.name) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            if (!server.doesPlaylistExist(playlistName)) {
                call.respond(
                    HttpStatusCode.OK,
                    BasicAPIResponse(false, "The playlist \"$playlistName\" does not exist.")
                )
                return@post
            }
            val result = server.renamePlaylist(playlistName, playlist.name)
            if (result is ServerError) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result.toJson()))
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true))
        }
        delete {
            val playlistName = call.parameters["name"]
            if (playlistName == null ) {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }
            when (val result = server.deletePlaylist(playlistName)) {
                is ServerError -> call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result.toJson()))
                is DeletePlaylist -> call.respond(HttpStatusCode.OK, BasicAPIResponse(true))
            }
        }
    }
    route("/api/playlists/{name}/download") {
        post {
            val playlistName = call.parameters["name"]
            if (playlistName == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val playlistFound = server.doesPlaylistExist(playlistName)
            if (!playlistFound) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val result = server.downLoadPlaylist(playlistName)
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
