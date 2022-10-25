package com.qmk.musicmanager.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.qmk.musicmanager.model.Playlist
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
internal class PlaylistControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper
) {

    @Test
    fun getPlaylists() {
        mockMvc.get("/playlists")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
    }

    @Test
    fun downloadPlaylists() {
    }

    @Test
    fun postPlaylists() {
        mockMvc.get("/playlists/1")
            .andExpect {
                status { isNotFound() }
            }
        mockMvc.get("/playlists")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
        val playlist = Playlist(
            youtubeId = "youtubeId",
            name = "MyPlaylist",
            channelId = 1
        )
        mockMvc.post("/playlists") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(playlist)
        }
            .andExpect {
                status { isCreated() }
            }
        mockMvc.get("/playlists")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$[0].youtubeId") { value("youtubeId") }
                jsonPath("$[0].name") { value("MyPlaylist") }
                jsonPath("$[0].channelId") { value(1) }
            }
    }

    @Test
    fun getPlaylist() {
    }

    @Test
    fun postPlaylist() {
    }

    @Test
    fun deletePlaylist() {
    }

    @Test
    fun downloadPlaylist() {
    }
}