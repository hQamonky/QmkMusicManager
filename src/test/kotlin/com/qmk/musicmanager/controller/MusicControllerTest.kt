package com.qmk.musicmanager.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.qmk.musicmanager.model.Music
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
internal class MusicControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper
) {
    @Test
    fun crudTest() {
        // Add playlist
        // Download playlist
        // Get new music
        // Edit music
        // Check music was changed
    }
}