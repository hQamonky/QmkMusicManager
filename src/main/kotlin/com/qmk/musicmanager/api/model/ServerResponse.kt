package com.qmk.musicmanager.api.model

import com.google.gson.Gson
import com.qmk.musicmanager.domain.model.*

abstract class ServerResponse(
    val response: Any
) {
    open fun toJson(): String {
        return Gson().toJson(response)
    }
}

class ServerError( // TODO : Implement error codes
    message: String
) : ServerResponse(ServerErrorResponse(0, message)) {
    data class ServerErrorResponse(
        val errorCode: Int,
        val message: String
    )
}

class ServerBusy(
    action: ServerAction
) : ServerResponse(action)

class FactoryReset : ServerResponse(true)

class UpdateYoutubeDl(
    response: String
) : ServerResponse(response)

class GetPlaylists(
    response: List<Playlist>
) : ServerResponse(response)

class CreatePlaylist(
    response: Playlist
) : ServerResponse(response)

class EditPlaylist : ServerResponse(true)

class DownloadPlaylists(
    response: List<DownloadResult>
) : ServerResponse(response)

class DownloadPlaylist(
    response: DownloadResult
) : ServerResponse(response)

class ArchiveMusic(
    response: List<String>
) : ServerResponse(response)

class EditMusic : ServerResponse(true)

class GetNewMusic(
    response: List<Music>
) : ServerResponse(response)

class GetNamingRules(
    response: List<NamingRule>
) : ServerResponse(response)

class AddNamingRule(
    response: NamingRule
) : ServerResponse(response)

class GetNamingRule(
    response: NamingRule
) : ServerResponse(response)

class EditNamingRule : ServerResponse(true)

class DeleteNamingRule : ServerResponse(true)

class GetSettings(
    response: Settings
) : ServerResponse(response)

class SetSettings : ServerResponse(true)

class SetMusicFolder : ServerResponse(true)

class SetDownloadOccurrence : ServerResponse(true)

class SetAutoDownload : ServerResponse(true)

class GetUploaders(
    response: List<Uploader>
) : ServerResponse(response)

class GetUploader(
    response: Uploader
) : ServerResponse(response)

class EditUploaderNamingFormat : ServerResponse(true)