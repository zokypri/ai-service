package com.z.ai_service.model

import com.fasterxml.jackson.annotation.JsonProperty

data class OpenAIRequest(
  val model: String,
  val messages: List<Message>,
  @JsonProperty("max_tokens")
  val maxTokens: Int? = null,
  val temperature: Double? = null,
  val stream: Boolean? = null
) {
  data class Message(
    val role: String,  // "user", "assistant", or "system"
    val content: String
  )
}

enum class OpenAIRole(val value: String) {
  USER("user"),
  ASSISTANT("assistant"),
  SYSTEM("system")
}

data class OpenAIResponse(
  val id: String,
  val `object`: String,  // "chat.completion"
  val created: Long,
  val model: String,
  val choices: List<Choice>,
  val usage: Usage? = null,
  @JsonProperty("system_fingerprint")
  val systemFingerprint: String? = null
) {
  data class Choice(
    val index: Int,
    val message: OpenAIRequest.Message,
    @JsonProperty("finish_reason")
    val finishReason: String?,  // "stop", "length", "content_filter", etc.
    val logprobs: Any? = null
  )

  fun getText(): String = choices.firstOrNull()?.message?.content ?: ""
}

data class OpenAIEmbeddingRequest(
  val model: String,  // e.g., "text-embedding-3-small"
  val input: String,  // or List<String> for batch
  @JsonProperty("encoding_format")
  val encodingFormat: String? = null,  // "float" or "base64"
  val dimensions: Int? = null  // Optional, to reduce dimensions
)

data class OpenAIEmbeddingResponse(
  val `object`: String,  // "list"
  val data: List<EmbeddingData>,
  val model: String,
  val usage: Usage
) {
  data class EmbeddingData(
    val `object`: String,  // "embedding"
    val index: Int,
    val embedding: List<Double>
  )

  fun getEmbedding(): List<Double> = data.firstOrNull()?.embedding ?: emptyList()
}

data class Usage(
  @JsonProperty("prompt_tokens")
  val promptTokens: Int,
  @JsonProperty("completion_tokens")
  val completionTokens: Int? = null,
  @JsonProperty("total_tokens")
  val totalTokens: Int
)

