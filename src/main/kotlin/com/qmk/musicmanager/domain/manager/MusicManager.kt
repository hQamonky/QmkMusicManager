package com.qmk.musicmanager.domain.manager

import com.qmk.musicmanager.database.dao.MusicDAO
import com.qmk.musicmanager.domain.model.Music
import java.io.File


class MusicManager(
    private val musicDAO: MusicDAO,
    private val configurationManager: ConfigurationManager = ConfigurationManager(),
    private val id3Manager: Id3Manager = Id3Manager(),
    private val playlistManager: PlaylistManager,
    private val deezerManager: DeezerManager
) {
    suspend fun editMusic(music: Music): Boolean {
        val audioFolder = configurationManager.getConfiguration().audioFolder
        val oldMusic = musicDAO.music(music.fileName) ?: return false
        val searchWithDeezer = music.title != oldMusic.title || music.artist != oldMusic.artist
        // Get album and genre
        val deezerMetadata = if (searchWithDeezer) deezerManager.getFullMetadata(
            music.title,
            music.artist,
            music.toFile(configurationManager.getConfiguration().audioFolder)
        ) else null
        // Handle playlists
        val oldPlaylists = oldMusic.playlists
        val newPlaylists = music.playlists
        if (oldPlaylists != newPlaylists) {
            val playlistsToRemove = oldPlaylists.filter { !newPlaylists.contains(it) }
            playlistsToRemove.forEach {
                playlistManager.removeMusicFromPlaylist(music, it)
            }
            val playlistsToAdd = newPlaylists.filter { !oldPlaylists.contains(it) }
            playlistsToAdd.forEach {
                playlistManager.addMusicToPlaylist(music, it)
            }
        }
        // Handle tags
        val oldTags = oldMusic.tags
        val newTags = music.tags
        if (oldTags != newTags) {
            val tagsToRemove = oldTags.filter { !newTags.contains(it) }
            tagsToRemove.forEach {
                musicDAO.removeTagFromMusic(it, music.fileName)
            }
            val tagsToAdd = newTags.filter { !oldTags.contains(it) }
            tagsToAdd.forEach {
                musicDAO.addTagToMusic(it, music.fileName)
            }
        }
        // Apply metadata in file
        id3Manager.updateMetadata(
            file = File("${audioFolder}/${music.fileName}.${music.fileExtension}"),
            title = music.title,
            artist = music.artist,
            album = if (searchWithDeezer) deezerMetadata?.album ?: "" else null ,
            genre = if (searchWithDeezer) deezerMetadata?.genre ?: "" else null,
            year = if (searchWithDeezer) deezerMetadata?.releaseDate ?: "" else null,
            playlists = music.playlists,
            customTags = music.tags
        )
        // Apply in database
        val currentMusic = musicDAO.music(music.fileName) ?: return false
        return musicDAO.editMusic(
            music.fileName,
            music.fileExtension,
            music.title,
            music.artist,
            currentMusic.platformId,
            currentMusic.uploaderId,
            currentMusic.uploadDate,
            music.isNew
        )
    }

    suspend fun getNewMusic(): List<Music> {
        return musicDAO.newMusic()
    }
}