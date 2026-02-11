package com.z.ai_service.component

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.reset
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.verify
import com.github.tomakehurst.wiremock.stubbing.Scenario
import java.nio.file.Files
import java.nio.file.Paths
import org.junit.jupiter.api.BeforeEach
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

  @BeforeEach
  fun resetWireMock() {
    reset()
  }

  @Test
  fun `should fetch error log analysis from Claude`() {

    val claudeErrorLogAnalysisResponse = Files.readString(Paths.get("src/test/resources/__files/ClaudeErrorLogAnalysisResponse.json"))
    stubFor(
      WireMock.post(WireMock.urlEqualTo("/v1/messages"))
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(claudeErrorLogAnalysisResponse)
        )
    )

    val requestText = Files.readString(Paths.get("src/test/resources/__files/request-error-log-sample.txt"))
    val responseJson = Files.readString(Paths.get("src/test/resources/__files/response-error-log-sample.json"))

    mockMvc.perform(
      post("/api/error/prompt/chat")
        .contentType("text/plain")
        .content(requestText)
    )
      .andExpect(status().isOk)
      .andExpect(content().json(responseJson))
  }

  @Test
  fun `should return error when Claude is overloaded`() {
    stubFor(
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
    stubFor(
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
    stubFor(
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

  @Test
  fun `should retry and succeed after Claude is overloaded twice`() {
    val claudeErrorLogAnalysisResponse = Files.readString(Paths.get("src/test/resources/__files/ClaudeErrorLogAnalysisResponse.json"))
    stubFor(
      WireMock.post(WireMock.urlEqualTo("/v1/messages"))
        .inScenario("Retry Scenario")
        .whenScenarioStateIs(Scenario.STARTED)
        .willReturn(WireMock.aResponse().withStatus(529))
        .willSetStateTo("SecondAttempt")
    )
    stubFor(
      WireMock.post(WireMock.urlEqualTo("/v1/messages"))
        .inScenario("Retry Scenario")
        .whenScenarioStateIs("SecondAttempt")
        .willReturn(WireMock.aResponse().withStatus(529))
        .willSetStateTo("Success")
    )
    stubFor(
      WireMock.post(WireMock.urlEqualTo("/v1/messages"))
        .inScenario("Retry Scenario")
        .whenScenarioStateIs("Success")
        .willReturn(WireMock.aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(claudeErrorLogAnalysisResponse))
    )

    val requestText = Files.readString(Paths.get("src/test/resources/__files/request-error-log-sample.txt"))
    val responseJson = Files.readString(Paths.get("src/test/resources/__files/response-error-log-sample.json"))


    mockMvc.perform(
      post("/api/error/prompt/chat")
        .contentType("text/plain")
        .content(requestText)
    )
      .andExpect(status().isOk)
      .andExpect(content().json(responseJson))

    verify(
      3,
      postRequestedFor(WireMock.urlEqualTo("/v1/messages"))
    )
  }

}
