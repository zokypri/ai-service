package com.z.ai_service.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig {

  @Value("\${client.anthropic.api.key}")
  private lateinit var anthropicApiKey: String

  @Value("\${client.anthropic.api.version}")
  private lateinit var anthropicApiVersion: String

  @Value("\${client.anthropic.api.base-url}")
  private lateinit var anthropicBaseUrl: String

  @Value("\${client.openai.api.key}")
  private lateinit var openaiApiKey: String

  @Value("\${client.openai.api.base-url}")
  private lateinit var openaiBaseUrl: String

  @Value("\${client.chroma.api.base-url}")
  private lateinit var chromaBaseUrl: String


  @Bean
  fun anthropicRestClient(): RestClient {
    return RestClient.builder()
      .baseUrl(anthropicBaseUrl)
      .defaultHeader("x-api-key", anthropicApiKey)
      .defaultHeader("anthropic-version", anthropicApiVersion)
      .defaultHeader("content-type", "application/json")
      .build()
  }

  @Bean
  fun openaiRestClient(): RestClient {
    return RestClient.builder()
      .baseUrl(openaiBaseUrl)
      .defaultHeader("Authorization", "Bearer $openaiApiKey")
      .defaultHeader("Content-Type", "application/json")
      .build()
  }

  @Bean
  fun chromaRestClient(
  ): RestClient {
    return RestClient.builder()
      .baseUrl(chromaBaseUrl)
      .defaultHeader("Content-Type", "application/json")
      .build()
  }
}
