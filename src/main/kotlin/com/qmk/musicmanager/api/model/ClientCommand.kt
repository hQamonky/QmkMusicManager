package com.qmk.musicmanager.api.model

class ClientCommand {
    object ConnectClient : BaseModel(ClientCommandType.CONNECT_CLIENT)
    object GetServerStatus : BaseModel(ClientCommandType.GET_SERVER_STATUS)
}