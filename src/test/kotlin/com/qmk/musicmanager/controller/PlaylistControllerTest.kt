package com.qmk.musicmanager.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

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