package com.qmk.musicmanager

import com.qmk.musicmanager.api.MusicManagerServer
import com.qmk.musicmanager.api.route.*
import com.qmk.musicmanager.plugins.configureMonitoring
import com.qmk.musicmanager.plugins.configureRouting
import com.qmk.musicmanager.plugins.configureSerialization
import com.qmk.musicmanager.plugins.configureSockets
import com.qmk.musicmanager.api.session.MusicManagerSession
import com.qmk.musicmanager.database.model.DatabaseFactory
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

val server = MusicManagerServer()

fun Application.module() {

    DatabaseFactory.init()

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
    configureRouting()

    install(Routing) {
        systemRoutes()
        playlistsRoutes()
        musicRoutes()
        namingRulesRoutes()
        settingsRoutes()
        uploadersRoutes()
    }
}
