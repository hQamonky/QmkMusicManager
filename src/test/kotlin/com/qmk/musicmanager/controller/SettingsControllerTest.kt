package com.qmk.musicmanager.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.qmk.musicmanager.model.Settings
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
internal class SettingsControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper
) {
    @Test
    fun getAndSetTest() {
        val gson = Gson()
        val settingsJson = mockMvc.get("/settings")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.musicFolder") { value("~/Music/") }
                jsonPath("$.downloadOccurrence") { value(1) }
            }.andReturn().response.contentAsString
        val settings = gson.fromJson(settingsJson, Settings::class.java)
        mockMvc.post("/settings") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                settings.copy(
                    musicFolder = "/mnt/seagate14/n_u/Music/",
                    downloadOccurrence = 2
                )
            )
        }
            .andExpect {
                status { isOk() }
            }
        mockMvc.get("/settings")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.musicFolder") { value("/mnt/seagate14/n_u/Music/") }
                jsonPath("$.downloadOccurrence") { value(2) }
            }
    }
}