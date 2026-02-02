package com.z.ai_service.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class JacksonConfig {

  @Bean
  @Primary
  fun objectMapper(): ObjectMapper {
    return ObjectMapper().apply {
      registerKotlinModule()
    }
  }
}
