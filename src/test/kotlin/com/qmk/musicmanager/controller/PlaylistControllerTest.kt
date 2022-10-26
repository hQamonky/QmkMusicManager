package com.qmk.musicmanager.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.qmk.musicmanager.model.Playlist
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
internal class PlaylistControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper
) {
    @Test
    fun crudTest() {
        val playlist = Playlist(
            youtubeId = "youtubeId",
            name = "MyPlaylist",
            channelId = "channelId"
        )
        // Create playlist
        mockMvc.post("/playlists") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(playlist)
        }
            .andExpect {
                status { isCreated() }
            }
        // Get all playlists
        val jsonStringPlaylists = mockMvc.get("/playlists")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$[0].youtubeId") { value("youtubeId") }
                jsonPath("$[0].name") { value("MyPlaylist") }
                jsonPath("$[0].channelId") { value("channelId") }
            }
            .andReturn().response.contentAsString
        val jsonObjectPlaylists = JSONArray(jsonStringPlaylists)
        val jsonPlaylist = JSONObject(jsonObjectPlaylists[0].toString())
        val id = jsonPlaylist.getString("id")
        // Get playlist by id
        mockMvc.get("/playlists/$id")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.youtubeId") { value("youtubeId") }
                jsonPath("$.name") { value("MyPlaylist") }
                jsonPath("$.channelId") { value("channelId") }
            }
        // Edit playlist
        mockMvc.post("/playlists/$id") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(playlist.copy(id = id, name = "New playlist name"))
        }
            .andExpect {
                status { isOk() }
            }
        // Check edition worked
        mockMvc.get("/playlists/$id")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.youtubeId") { value("youtubeId") }
                jsonPath("$.name") { value("New playlist name") }
                jsonPath("$.channelId") { value("channelId") }
            }
        // Delete playlist
        mockMvc.delete("/playlists/$id")
            .andExpect {
                status { isOk() }
            }
        // Check it was deleted and that not found exception works
        mockMvc.get("/playlists/$id")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun downloadPlaylist() {
    }
}