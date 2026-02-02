package com.z.ai_service.client

import com.z.ai_service.ClaudeRequest
import com.z.ai_service.ClaudeResponse
import com.z.ai_service.exception.ClaudeApiException
import com.z.ai_service.exception.OverloadedException
import com.z.ai_service.exception.RateLimitException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class ClaudeClient(
  private val anthropicRestClient: RestClient,
  @Value("\${client.anthropic.model}") private val model: String
)  {

  fun sendMessage(messages: List<ClaudeRequest.Message>, maxTokens: Int = 1024): ClaudeResponse {
    val request = ClaudeRequest(
      model = model,
      maxTokens = maxTokens,
      messages = messages
    )

    return anthropicRestClient.post()
      .uri("/messages")
      .body(request)
      .retrieve()
      .onStatus({ it.value() == 429 }) { _, _ ->
        throw RateLimitException("Rate limit exceeded, consider retrying later")
      }
      .onStatus({ it.value() == 529 }) { _, _ ->
        throw OverloadedException("Anthropic API is overloaded, consider retrying later")
      }
      .body(ClaudeResponse::class.java)
      ?: throw ClaudeApiException("Empty response from Claude API")
  }


}
