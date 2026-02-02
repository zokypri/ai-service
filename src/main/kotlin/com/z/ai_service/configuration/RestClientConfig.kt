package com.z.ai_service.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig {

  @Value("\${client.anthropic.api.key}")
  private lateinit var apiKey: String

  @Value("\${client.anthropic.api.version}")
  private lateinit var apiVersion: String

  @Bean
  fun anthropicRestClient(): RestClient {
    return RestClient.builder()
      .baseUrl("https://api.anthropic.com/v1")
      .defaultHeader("x-api-key", apiKey)
      .defaultHeader("anthropic-version", apiVersion)
      .defaultHeader("content-type", "application/json")
      .build()
  }
}
