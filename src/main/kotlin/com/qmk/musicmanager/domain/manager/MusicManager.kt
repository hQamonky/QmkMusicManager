package com.qmk.musicmanager.domain.manager

import com.qmk.musicmanager.database.dao.MusicDAOImpl
import com.qmk.musicmanager.domain.model.Music
import java.io.File


class MusicManager(
    private val musicDAO: MusicDAOImpl,
    private val configurationManager: ConfigurationManager = ConfigurationManager(),
    private val id3Manager: Id3Manager = Id3Manager(),
    private val playlistManager: PlaylistManager
) {
    suspend fun editMusic(music: Music) : Boolean {
        val musicFolder = configurationManager.getConfiguration().musicFolder
        val oldMusic = musicDAO.music(music.fileName) ?: return false
        // Handle playlists
        val oldPlaylists = oldMusic.playlists
        val newPlaylists = music.playlists
        if (oldPlaylists != newPlaylists) {
            val playlistsToRemove = oldPlaylists.filter { newPlaylists.contains(it) }
            playlistsToRemove.forEach {
                playlistManager.removeMusicFromPlaylist(music, it)
            }
            val playlistsToAdd = newPlaylists.filter { oldPlaylists.contains(it) }
            playlistsToAdd.forEach {
                playlistManager.addMusicToPlaylist(music, it)
            }
        }
        // Handle tags
        val oldTags = oldMusic.tags
        val newTags = music.tags
        if (oldTags != newTags) {
            val tagsToRemove = oldTags.filter { newTags.contains(it) }
            tagsToRemove.forEach {
                musicDAO.removeTagFromMusic(it, music.fileName)
            }
            val tagsToAdd = newTags.filter { oldTags.contains(it) }
            tagsToAdd.forEach {
                musicDAO.addTagToMusic(it, music.fileName)
            }
        }
        // Apply metadata in file
        id3Manager.updateMetadata(
            file = File("${musicFolder}/${music.fileName}.${music.fileExtension}"),
            title = music.title,
            artist = music.artist,
            playlists = music.playlists,
            customTags = music.tags
        )
        // Apply in database
        return musicDAO.editMusic(
            music.fileName,
            music.fileExtension,
            music.title,
            music.artist,
            music.platformId,
            music.uploaderId,
            music.uploadDate,
            music.isNew
        )
    }

    suspend fun getNewMusic() : List<Music> {
        return musicDAO.newMusic()
    }
}