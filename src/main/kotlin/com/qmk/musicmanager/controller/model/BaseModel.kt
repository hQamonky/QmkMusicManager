package com.qmk.musicmanager.controller.model

abstract class BaseModel (type: ClientCommandType)

enum class ClientCommandType {
    CONNECT_CLIENT, GET_SERVER_STATUS
}