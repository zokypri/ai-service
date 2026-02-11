package com.z.ai_service.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.z.ai_service.model.ClaudeRequest
import com.z.ai_service.model.ErrorAnalysis
import com.z.ai_service.model.Role
import com.z.ai_service.client.ClaudeClient
import com.z.ai_service.constants.PromptConstants.DEFAULT_PROMPT
import com.z.ai_service.constants.PromptConstants.ERROR_SPECIFIC_PROMPT
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ErrorLogPromptService(
  private val claudeClient: ClaudeClient,
  private val objectMapper: ObjectMapper
) {

  val logger: Logger = LoggerFactory.getLogger(javaClass)

  private val conversationHistory = mutableListOf<ClaudeRequest.Message>()

  fun promptErrorLog(userMessage: String): ErrorAnalysis {
    logger.info("Prompting Claude with user message")
    conversationHistory.add(ClaudeRequest.Message(role = Role.USER.value, content = userMessage))

    val response = claudeClient.sendMessage(buildRequestMessages())
    val assistantMessage = response.getText().trim()
      .removePrefix("```json")
      .removePrefix("```")
      .removeSuffix("```")
    conversationHistory.add(ClaudeRequest.Message(role = Role.ASSISTANT.value, content = assistantMessage))
    val errorAnalysis = objectMapper.readValue(assistantMessage, ErrorAnalysis::class.java)
    logger.info("Claude responded with root cause to the error '${errorAnalysis.rootCause}'.")
    return errorAnalysis
  }

  fun clearConversation() {
    conversationHistory.clear()
  }

  fun getConversationHistory(): List<ClaudeRequest.Message> {
    return conversationHistory.toList()
  }

  private fun buildErrorSpecificDefaultPrompt(): String {

    return (DEFAULT_PROMPT + "\n\n" + ERROR_SPECIFIC_PROMPT)
      .trimIndent()
      .replace("\n", " ")
      .replace(Regex("\\s+"), " ")
      .trim()
  }

  private fun buildRequestMessages(): List<ClaudeRequest.Message> =
    listOf(
      ClaudeRequest.Message(
        role = Role.USER.value,
        content = buildErrorSpecificDefaultPrompt()
      )
    ) + conversationHistory
}
