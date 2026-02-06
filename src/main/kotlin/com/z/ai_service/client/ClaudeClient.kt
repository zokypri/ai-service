package com.z.ai_service.client

import com.z.ai_service.model.ClaudeRequest
import com.z.ai_service.model.ClaudeResponse
import com.z.ai_service.exception.ClaudeApiException
import com.z.ai_service.exception.OverloadedException
import com.z.ai_service.exception.RateLimitException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.retry.annotation.Retryable
import org.springframework.retry.annotation.Backoff

@Component
class ClaudeClient(
  private val anthropicRestClient: RestClient,
  @Value("\${client.anthropic.model}") private val model: String
)  {

  val logger: Logger = LoggerFactory.getLogger(javaClass)

  companion object {
    const val CLAUDE_MESSAGE_URI = "/messages"
  }

  @Retryable(
    value = [OverloadedException::class, RateLimitException::class],
    maxAttempts = 3,
    backoff = Backoff(delay = 1000)
  )
  fun sendMessage(messages: List<ClaudeRequest.Message>, maxTokens: Int = 1024): ClaudeResponse {
    logger.info("Sending message to $model")
    val request = ClaudeRequest(
      model = model,
      maxTokens = maxTokens,
      messages = messages
    )

    return anthropicRestClient.post()
      .uri(CLAUDE_MESSAGE_URI)
      .body(request)
      .retrieve()
      .onStatus({ it.value() == 429 }) { _, _ ->
        logger.warn("Anthropic rate limit exceeded, retrying later")
        throw RateLimitException("Anthropic rate limit exceeded, consider retrying later")
      }
      .onStatus({ it.value() == 529 }) { _, _ ->
        logger.warn("Anthropic API is overloaded, retrying later")
        throw OverloadedException("Anthropic API is overloaded, consider retrying later")
      }
      .body(ClaudeResponse::class.java)
      ?: throw ClaudeApiException("Empty response from Claude API")
  }


}
