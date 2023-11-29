package com.qmk.musicmanager.api.model

abstract class BaseModel (type: ClientCommandType)

enum class ClientCommandType {
    CONNECT_CLIENT, GET_SERVER_STATUS
}