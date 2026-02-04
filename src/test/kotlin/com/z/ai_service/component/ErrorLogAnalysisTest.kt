package com.z.ai_service.component

import com.github.tomakehurst.wiremock.client.WireMock
import java.nio.file.Files
import java.nio.file.Paths
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.junit.jupiter.api.Test
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
class ErrorLogAnalysisTest {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @Test
  fun `should fetch error log analysis from Claude`() {

    val responseJson = Files.readString(Paths.get("src/test/resources/__files/ClaudeErrorLogAnalysisResponse.json"))
    WireMock.stubFor(
      WireMock.post(WireMock.urlEqualTo("/v1/messages"))
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(responseJson)
        )
    )

    val requestText = Files.readString(Paths.get("src/test/resources/__files/request-error-log-sample.txt"))

    mockMvc.perform(
      post("/api/error/prompt/chat")
        .contentType("text/plain")
        .content(requestText)
    )
      .andExpect(status().isOk)
      .andExpect(content().json(Files.readString(Paths.get("src/test/resources/__files/response-error-log-sample.json"))))
  }

  @Test
  fun `should return error when Claude is overloaded`() {
    WireMock.stubFor(
      WireMock.post(WireMock.urlEqualTo("/v1/messages"))
        .willReturn(
          WireMock.aResponse()
            .withStatus(529)
            .withHeader("Content-Type", "application/json")
        )
    )

    val requestText = Files.readString(Paths.get("src/test/resources/__files/request-error-log-sample.txt"))
    val errorResponse = Files.readString(Paths.get("src/test/resources/__files/ClaudeOverLoadedException.json"))

    mockMvc.perform(
      post("/api/error/prompt/chat")
        .contentType("text/plain")
        .content(requestText)
    )
      .andExpect(status().isServiceUnavailable)
      .andExpect(content().json(errorResponse))
  }

  @Test
  fun `should return error when Claude rate limit is exceeded`() {
    WireMock.stubFor(
      WireMock.post(WireMock.urlEqualTo("/v1/messages"))
        .willReturn(
          WireMock.aResponse()
            .withStatus(429)
            .withHeader("Content-Type", "application/json")
        )
    )

    val requestText = Files.readString(Paths.get("src/test/resources/__files/request-error-log-sample.txt"))
    val errorResponse = Files.readString(Paths.get("src/test/resources/__files/ClaudeRateLimitException.json"))

    mockMvc.perform(
      post("/api/error/prompt/chat")
        .contentType("text/plain")
        .content(requestText)
    )
      .andExpect(status().isTooManyRequests)
      .andExpect(content().json(errorResponse))
  }

  @Test
  fun `should return error when Claude returns 200 with empty body`() {
    WireMock.stubFor(
      WireMock.post(WireMock.urlEqualTo("/v1/messages"))
        .willReturn(
          WireMock.aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("") // Empty body
        )
    )

    val requestText = Files.readString(Paths.get("src/test/resources/__files/request-error-log-sample.txt"))

    mockMvc.perform(
      post("/api/error/prompt/chat")
        .contentType("text/plain")
        .content(requestText)
    )
      .andExpect(status().isBadGateway)
      .andExpect(content().json("""{"error":"Empty response from Claude API"}"""))
  }

}
