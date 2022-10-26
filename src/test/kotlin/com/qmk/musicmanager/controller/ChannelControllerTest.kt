package com.qmk.musicmanager.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.qmk.musicmanager.model.Channel
import com.qmk.musicmanager.model.NamingFormat
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
internal class ChannelControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper
) {
    @Test
    fun crudTest() {
        val channel = Channel(
            name = "MyChannel",
            namingFormat = NamingFormat()
        )
        // Create channel
        mockMvc.post("/channels") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(channel)
        }
            .andExpect {
                status { isCreated() }
            }
        // Get all channels
        val jsonStringChannels = mockMvc.get("/channels")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$[0].name") { value("MyChannel") }
                jsonPath("$[0].namingFormat.separator") { value(" - ") }
                jsonPath("$[0].namingFormat.artist_before_title") { value(true) }
            }
            .andReturn().response.contentAsString
        val jsonObjectChannels = JSONArray(jsonStringChannels)
        val jsonChannel = JSONObject(jsonObjectChannels[0].toString())
        val id = jsonChannel.getString("id")
        // Get channel by id
        mockMvc.get("/channels/$id")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.name") { value("MyChannel") }
                jsonPath("$.namingFormat.separator") { value(" - ") }
                jsonPath("$.namingFormat.artist_before_title") { value(true) }
            }
        // Edit channel
        mockMvc.post("/channels/$id") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(channel.copy(id = id, name = "New channel name"))
        }
            .andExpect {
                status { isOk() }
            }
        // Check edition worked
        mockMvc.get("/channels/$id")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.name") { value("New channel name") }
                jsonPath("$.namingFormat.separator") { value(" - ") }
                jsonPath("$.namingFormat.artist_before_title") { value(true) }
            }
        // Delete channel
        mockMvc.delete("/channels/$id")
            .andExpect {
                status { isOk() }
            }
        // Check it was deleted and that not found exception works
        mockMvc.get("/channels/$id")
            .andExpect {
                status { isNotFound() }
            }
    }
}