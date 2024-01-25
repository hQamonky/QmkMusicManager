package com.qmk.musicmanager.controller.model

import io.ktor.websocket.*

data class Client(
    val id: String,
    var socket: WebSocketSession
)
