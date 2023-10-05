package com.qmk.musicmanager

import com.qmk.musicmanager.plugins.configureMonitoring
import com.qmk.musicmanager.plugins.configureRouting
import com.qmk.musicmanager.plugins.configureSerialization
import com.qmk.musicmanager.plugins.configureSockets
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureSockets()
    configureMonitoring()
    configureRouting()
}
