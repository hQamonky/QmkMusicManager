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

class CurrentProcess(
    process: ServerAction,
) : ServerResponse(process)

class ProcessChange(
    oldProcess: ServerAction,
    newProcess: ServerAction
) : ServerResponse(ProcessChangeResponse(oldProcess, newProcess)) {
    data class ProcessChangeResponse(
        val oldProcess: ServerAction,
        val newProcess: ServerAction
    )
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

class UpdateYtDl(
    response: String
) : ServerResponse(response)

class GetPlaylists(
    response: List<Playlist>
) : ServerResponse(response)

class GetPlaylist(
    response: Playlist
) : ServerResponse(response)

class GetYoutubePlaylists(
    response: List<PlatformPlaylist>
) : ServerResponse(response)

class GetYoutubePlaylist(
    response: PlatformPlaylist
) : ServerResponse(response)

class AddPlatformPlaylist(
    response: PlatformPlaylist
) : ServerResponse(response)

class RenamePlaylist : ServerResponse(true)

class EditYoutubePlaylist : ServerResponse(true)

class DeletePlaylist : ServerResponse(true)

class DownloadPlaylistsLaunched : ServerResponse(true)
class DownloadPlaylists(
    response: List<DownloadResult>
) : ServerResponse(response)

class DownloadPlaylistLaunched : ServerResponse(true)
class DownloadPlaylist(
    response: DownloadResult
) : ServerResponse(response)

class ArchiveMusicLaunched : ServerResponse(true)
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

class SetAudioFolder : ServerResponse(true)

class SetPlaylistsFolder : ServerResponse(true)

class SetArchiveFolder : ServerResponse(true)

class SetAudioFormat : ServerResponse(true)

class SetDownloadOccurrence : ServerResponse(true)

class SetAutoDownload : ServerResponse(true)

class SetRapidapiKey : ServerResponse(true)

class GetUploaders(
    response: List<Uploader>
) : ServerResponse(response)

class GetUploader(
    response: Uploader
) : ServerResponse(response)

class EditUploaderNamingFormat : ServerResponse(true)

class MopidyLocalScan(
    response: String
) : ServerResponse(response)

class MigrateMetadata(result: String) : ServerResponse(result)