package com.z.ai_service.component

import java.nio.file.Files
import java.nio.file.Paths
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SplunkWebhookTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `should accept Splunk alert webhook and return 200`() {
        val requestJson = Files.readString(Paths.get("src/test/resources/__files/splunk-webhook-request.json"))

        mockMvc.perform(
            post("/api/internal/poc/error/prompt/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        )
            .andExpect(status().isOk)
    }

}

