package com.qmk.musicmanager.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.qmk.musicmanager.model.Playlist
import com.qmk.musicmanager.model.PlaylistEntry
import com.qmk.musicmanager.youtube.YoutubeController
import org.json.JSONArray
import org.json.JSONObject
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

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
internal class PlaylistControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper
) {
    @Test
    fun crudTest() {
        val playlistEntry = PlaylistEntry(
            "MyPlaylist",
            "https://www.youtube.com/playlist?list=PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl"
        )
        // Create playlist
        mockMvc.post("/playlists") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(playlistEntry)
        }
            .andExpect {
                status { isCreated() }
                jsonPath("$.id") { value("PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl") }
                jsonPath("$.name") { value("MyPlaylist") }
                jsonPath("$.uploaderId") { value("UCT8Y-bugDyR4ADHoQ-FOluw") }
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
        // Get all playlists
        mockMvc.get("/playlists")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$[0].id") { value("PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl") }
                jsonPath("$[0].name") { value("MyPlaylist") }
                jsonPath("$[0].uploaderId") { value("UCT8Y-bugDyR4ADHoQ-FOluw") }
            }
        // Edit playlist
        mockMvc.post("/playlists/PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(Playlist(
                id = "PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl",
                name = "New playlist name",
                uploaderId = "UCT8Y-bugDyR4ADHoQ-FOluw"
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
                jsonPath("$.uploaderId") { value("UCT8Y-bugDyR4ADHoQ-FOluw") }
            }
        // Download playlist

        // Get new music
        // Edit music
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

    @Test
    fun downloadPlaylist() {
        val youtubeDl = YoutubeController()
        val url = "https://www.youtube.com/playlist?list=PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl"
        val playlistInfo = youtubeDl.getPlaylistInfo(url)
        println(playlistInfo)
//        val playlist = Playlist(
//            youtubeId = playlistId,
//            name = "test playlist 1",
//            uploaderId = "uploaderId"
//        )
//        // Create playlist
//        mockMvc.post("/playlists") {
//            contentType = MediaType.APPLICATION_JSON
//            content = objectMapper.writeValueAsString(playlist)
//        }
//            .andExpect {
//                status { isCreated() }
//            }
    }
}