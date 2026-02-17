package com.z.ai_service.client

import com.z.ai_service.model.OpenAIRequest
import com.z.ai_service.model.OpenAIResponse
import com.z.ai_service.exception.OpenAIApiException
import kotlin.jvm.java
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class OpenAIClient(
  private val openaiRestClient: RestClient,
  @Value("\${client.openai.model}") private val model: String
) {

  val logger: Logger = LoggerFactory.getLogger(javaClass)

  companion object {
    const val OPENAI_CHAT_COMPLETIONS_URI = "/chat/completions"
  }

  fun sendChat(messages: List<OpenAIRequest.Message>, maxTokens: Int = 1024): OpenAIResponse {
    logger.info("Sending message to OpenAI model $model")
    val request = OpenAIRequest(
      model = model,
      messages = messages,
      maxTokens = maxTokens
    )

    return openaiRestClient.post()
      .uri(OPENAI_CHAT_COMPLETIONS_URI)
      .body(request)
      .retrieve()
      .body(OpenAIResponse::class.java)
      ?: throw OpenAIApiException("Empty response from OpenAI API")
  }
}
