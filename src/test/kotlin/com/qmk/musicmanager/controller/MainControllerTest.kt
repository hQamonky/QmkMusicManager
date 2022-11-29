package com.qmk.musicmanager.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.qmk.musicmanager.model.Music
import com.qmk.musicmanager.model.NamingRule
import com.qmk.musicmanager.model.PlaylistEntry
import com.qmk.musicmanager.model.Settings
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
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
internal class MainControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper
) {

    @Test
    fun factoryResetTest() {
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
        // Download playlist
        mockMvc.get("/playlists/PLCVGGn6GhhDtYoqlNGqGFdg3ODeofpkLl/download")
            .andExpect {
                status { isOk() }
            }
        // Factory reset
        mockMvc.post("/factory-reset")
            .andExpect {
                status { isOk() }
            }
        // Check database
        val jsonNamingRules = mockMvc.get("/naming-rules")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$[0].replace") { value(" / ") }
                jsonPath("$[0].replaceBy") { value(" ") }
                jsonPath("$[0].priority") { value(1) }
            }.andReturn().response.contentAsString
        val namingRules: Array<NamingRule> = Gson().fromJson(jsonNamingRules, Array<NamingRule>::class.java)
        assert(namingRules.size == 30)

        val jsonMusic = mockMvc.get("/music/new")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }.andReturn().response.contentAsString
        val musicArray: Array<Music> = Gson().fromJson(jsonMusic, Array<Music>::class.java)
        assert(musicArray.isEmpty())
    }
}