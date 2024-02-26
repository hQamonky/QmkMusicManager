package com.qmk.musicmanager

import com.google.gson.Gson
import com.qmk.musicmanager.controller.MusicManagerServer
import com.qmk.musicmanager.controller.route.*
import com.qmk.musicmanager.controller.session.MusicManagerSession
import com.qmk.musicmanager.database.model.DatabaseFactory
import com.qmk.musicmanager.plugins.configureMonitoring
import com.qmk.musicmanager.plugins.configureSerialization
import com.qmk.musicmanager.plugins.configureSockets
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*

fun main() {
    embeddedServer(Netty, port = 8092, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

lateinit var server: MusicManagerServer
val gson = Gson()

fun Application.module() {

    DatabaseFactory.init()
    server = MusicManagerServer()

    install(Routing) {
//        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
//        openAPI(path="openapi", swaggerFile = "openapi/documentation.yaml")
        systemRoutes()
        playlistsRoutes()
        youtubePlaylistsRoutes()
        musicRoutes()
        namingRulesRoutes()
        settingsRoutes()
        uploadersRoutes()
    }

    install(Sessions) {
        cookie<MusicManagerSession>("SESSION")
    }
    intercept(ApplicationCallPipeline.Plugins) {
        if (call.sessions.get<MusicManagerSession>() == null) {
            val clientId = call.parameters["clientId"] ?: ""
            call.sessions.set(MusicManagerSession(clientId, generateNonce()))
        }
    }

    configureSerialization()
    configureSockets()
    configureMonitoring()
//    configureRouting()

//    install(WebSockets)
}
