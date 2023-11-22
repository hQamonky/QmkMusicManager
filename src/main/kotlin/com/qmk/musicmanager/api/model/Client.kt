package com.qmk.musicmanager.api.model

import io.ktor.websocket.*

data class Client(
    val id: String,
    var socket: WebSocketSession
)
