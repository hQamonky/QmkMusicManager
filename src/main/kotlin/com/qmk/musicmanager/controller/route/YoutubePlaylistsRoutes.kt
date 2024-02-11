package com.qmk.musicmanager.controller.route

import com.qmk.musicmanager.controller.model.*
import com.qmk.musicmanager.domain.model.PlatformPlaylist
import com.qmk.musicmanager.domain.model.PlaylistEntry
import com.qmk.musicmanager.server
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.youtubePlaylistsRoutes() {
    route("/api/playlists/youtube") {
        get {
            val playlists = server.getYoutubePlaylists()
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, playlists))
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
            val playlist = server.addPlatformPlaylist(request.url, request.playlists)
            if (playlist is ServerError) {
                call.respond(
                    HttpStatusCode.OK, BasicAPIResponse(false, playlist)
                )
                return@post
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true))
        }
    }
    route("/api/playlists/youtube/download") {
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
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result))
                return@post
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result))
        }
    }
    route("/api/playlists/youtube/{id}") {
        get {
            val playlistId = call.parameters["id"]
            if (playlistId == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            when (val result = server.getPlatformPlaylist(playlistId, "youtube")) {
                is ServerError -> call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result.response))
                is GetYoutubePlaylist -> call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result))
            }
        }
        post {
            val playlistId = call.parameters["id"]
            val playlist = call.receiveNullable<PlatformPlaylist>()
            if (playlistId == null || playlist == null || playlistId != playlist.id) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val result = server.editPlaylist(playlist)
            if (result is ServerError) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result))
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true))
        }
        delete {
            val playlistId = call.parameters["id"]
            if (playlistId == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }
            when (val result = server.deletePlatformPlaylist(playlistId)) {
                is ServerError -> call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result))
                is DeletePlaylist -> call.respond(HttpStatusCode.OK, BasicAPIResponse(true))
            }
        }
    }
    route("/api/playlists/youtube/{id}/download") {
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
            val result = server.downLoadYoutubePlaylist(playlistId)
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
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result))
                return@post
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result))
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
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result))
                return@post
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result))
        }
    }
}
