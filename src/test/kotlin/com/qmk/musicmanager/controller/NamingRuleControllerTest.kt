package com.qmk.musicmanager.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.qmk.musicmanager.model.NamingRule
import com.qmk.musicmanager.model.NamingFormat
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
internal class NamingRuleControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper
) {
    @Test
    fun crudTest() {
        val namingRule = NamingRule(
            replace = " unwanted text"
        )
        // Create naming rule
        mockMvc.post("/naming-rules") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(namingRule)
        }
            .andExpect {
                status { isCreated() }
            }
        // Get all naming rules
        val jsonStringNamingRules = mockMvc.get("/naming-rules")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$[0].replace") { value(" unwanted text") }
                jsonPath("$[0].replaceBy") { value("") }
                jsonPath("$[0].priority") { value(2) }
            }
            .andReturn().response.contentAsString
        val jsonObjectNamingRules = JSONArray(jsonStringNamingRules)
        val jsonNamingRule = JSONObject(jsonObjectNamingRules[0].toString())
        val id = jsonNamingRule.getString("id")
        // Get naming rule by id
        mockMvc.get("/naming-rules/$id")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.replace") { value(" unwanted text") }
                jsonPath("$.replaceBy") { value("") }
                jsonPath("$.priority") { value(2) }
            }
        // Edit naming rule
        mockMvc.post("/naming-rules/$id") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                namingRule.copy(id = id, replace = "new unwanted text", priority = 3)
            )
        }
            .andExpect {
                status { isOk() }
            }
        // Check edition worked
        mockMvc.get("/naming-rules/$id")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.replace") { value("new unwanted text") }
                jsonPath("$.replaceBy") { value("") }
                jsonPath("$.priority") { value(3) }
            }
        // Delete naming rule
        mockMvc.delete("/naming-rules/$id")
            .andExpect {
                status { isOk() }
            }
        // Check it was deleted and that not found exception works
        mockMvc.get("/naming-rules/$id")
            .andExpect {
                status { isNotFound() }
            }
    }
}