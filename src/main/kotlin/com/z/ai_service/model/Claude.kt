package com.z.ai_service.model

import com.fasterxml.jackson.annotation.JsonProperty

data class ClaudeRequest(
  val model: String,
  @JsonProperty("max_tokens")
  val maxTokens: Int,
  val messages: List<Message>
) {
  data class Message(
    val role: String,  // "user" or "assistant"
    val content: String
  )
}

enum class Role(val value: String) {
  USER("user"),
  ASSISTANT("assistant")
}

data class ClaudeResponse(
  val id: String,
  val type: String,  // Always "message"
  val role: String,  // Always "assistant"
  val content: List<Content>,
  val model: String,
  @JsonProperty("stop_reason")
  val stopReason: String?,  // "end_turn", "max_tokens", "stop_sequence", etc.
  val usage: Usage? = null
) {
  data class Content(
    val type: String,  // "text", "tool_use", etc.
    val text: String?
  )

  data class Usage(
    @JsonProperty("input_tokens")
    val inputTokens: Int,
    @JsonProperty("output_tokens")
    val outputTokens: Int
  )

  fun getText(): String = content.firstOrNull()?.text ?: ""
}


data class ErrorAnalysis(
  val rootCause: String,
  val missingValue: String? = null,
  val allowedValues: List<String>? = null,
  val suggestion: String
)



