package com.qmk.musicmanager.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.qmk.musicmanager.manager.ConfigurationManager
import com.qmk.musicmanager.manager.MopidyManager
import com.qmk.musicmanager.model.Music
import com.qmk.musicmanager.model.Playlist
import com.qmk.musicmanager.model.PlaylistEntry
import com.qmk.musicmanager.model.Settings
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.io.File
import java.lang.reflect.Type

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
internal class PlaylistControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper
) {
    @Test
    fun fullIntegrationTest() {
        val playlistEntry = PlaylistEntry(
            "MyPlaylist",
            "https://www.youtube.com/playlist?list=PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl"
        )
        // Change music folder
        mockMvc.post("/settings") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                Settings(
                    musicFolder = "src/test/MusicTestDir",
                    downloadOccurrence = 1
                )
            )
        }
            .andExpect {
                status { isOk() }
            }
        // Create playlist
        mockMvc.post("/playlists") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(playlistEntry)
        }
            .andExpect {
                status { isCreated() }
                jsonPath("$.id") { value("PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl") }
                jsonPath("$.name") { value("MyPlaylist") }
            }
        // Get all playlists
        mockMvc.get("/playlists")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$[0].id") { value("PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl") }
                jsonPath("$[0].name") { value("MyPlaylist") }
            }
        // Edit playlist
        mockMvc.post("/playlists/PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(Playlist(
                id = "PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl",
                name = "New playlist name"
            ))
        }
            .andExpect {
                status { isOk() }
            }
        // Check it was edited and that get playlist by id works
        mockMvc.get("/playlists/PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.id") { value("PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl") }
                jsonPath("$.name") { value("New playlist name") }
            }
        // Download playlist
        mockMvc.post("/playlists/PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl/download")
            .andExpect {
                status { isOk() }
            }
        // Download playlists (already downloaded music should not download again)
        mockMvc.post("/playlists/download")
            .andExpect {
                status { isOk() }
            }
        // Get all uploaders
        mockMvc.get("/uploaders")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$[0].id") { value("UCT8Y-bugDyR4ADHoQ-FOluw") }
                jsonPath("$[0].name") { value("William Herlicq") }
                jsonPath("$[0].namingFormat.separator") { value(" - ") }
                jsonPath("$[0].namingFormat.artist_before_title") { value(true) }
            }
        // Get uploader by id
        mockMvc.get("/uploaders/UCT8Y-bugDyR4ADHoQ-FOluw")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.id") { value("UCT8Y-bugDyR4ADHoQ-FOluw") }
                jsonPath("$.name") { value("William Herlicq") }
                jsonPath("$.namingFormat.separator") { value(" - ") }
                jsonPath("$.namingFormat.artist_before_title") { value(true) }
            }
        // Get new music
        val jsonMusic = mockMvc.get("/music/new")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$[0].id") { value("VFWJd69f9F0") }
                jsonPath("$[0].isNew") { value(true) }
            }.andReturn().response.contentAsString
        // Edit music
        val musicArray: Array<Music> = Gson().fromJson(jsonMusic, Array<Music>::class.java)
        val music = musicArray[0]
        mockMvc.post("/music/VFWJd69f9F0") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                music.copy(
                    title = "new title",
                    artist = "new artist",
                    isNew = false
                )
            )
        }
            .andExpect {
                status { isOk() }
            }
        // Check that music was edited
        val jsonMusicEmpty = mockMvc.get("/music/new")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }.andReturn().response.contentAsString
        val musicEmptyArray: Array<Music> = Gson().fromJson(jsonMusicEmpty, Array<Music>::class.java)
        assert(musicEmptyArray.isEmpty())
        // Archive music
        val mopidyManager = MopidyManager()
        mopidyManager.addMusicToPlaylist(music, mopidyManager.archivePlaylistName)
        mockMvc.post("/playlists/archive-music")
            .andExpect {
                status { isOk() }
            }
        val musicFolder = ConfigurationManager().getConfiguration().musicFolder
        val musicFile = "${music.fileName}.${music.fileExtension}"
        assert(!File("$musicFolder/$musicFile").exists())
        assert(File("$musicFolder/${mopidyManager.archivePlaylistName}/$musicFile").exists())
        // Delete playlist
        mockMvc.delete("/playlists/PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl")
            .andExpect {
                status { isOk() }
            }
        // Check it was deleted and that not found exception works
        mockMvc.get("/playlists/PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl")
            .andExpect {
                status { isNotFound() }
            }
    }
}