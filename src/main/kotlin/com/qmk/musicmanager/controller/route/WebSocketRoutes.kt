package com.qmk.musicmanager.controller.route

import com.google.gson.JsonParser
import com.qmk.musicmanager.controller.model.*
import com.qmk.musicmanager.controller.session.MusicManagerSession
import com.qmk.musicmanager.gson
import com.qmk.musicmanager.server
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach

fun Route.musicManagerWebSocketRoute() {
    route("ws/qmk-music-manager") {
        standardWebSocket { socket, clientId, _, payload ->
            when (payload) {
                is ClientCommand.ConnectClient -> {
                    if (server.doesClientExist(clientId)) {
                        return@standardWebSocket
                    }
                    server.connectClient(Client(clientId, socket))
                }
                is ClientCommand.GetServerStatus -> {
                    if (server.doesClientExist(clientId)) {
                        return@standardWebSocket
                    }
                    server.notifyStatusToClient(clientId)
                }
            }
        }
    }
}

fun Route.standardWebSocket(
    handleFrame: suspend (
        socket: DefaultWebSocketServerSession,
        clientId: String,
        message: String,
        payload: BaseModel
    ) -> Unit
) {
    webSocket {
        val session = call.sessions.get<MusicManagerSession>()
        if (session == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session."))
            return@webSocket
        }
        try {
            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    val message = frame.readText()
                    val jsonObject = JsonParser.parseString(message).asJsonObject
                    val type = when (jsonObject.get("type").asString) {
                        ClientCommandType.CONNECT_CLIENT.name -> ClientCommand.ConnectClient::class.java
                        ClientCommandType.GET_SERVER_STATUS.name -> ClientCommand.GetServerStatus::class.java
                        else -> BaseModel::class.java
                    }
                    val payload = gson.fromJson(message, type)
                    handleFrame(this, session.clientId, message, payload)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            server.disconnectClient(session.clientId)
        }
    }
}