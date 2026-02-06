package com.z.ai_service.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.z.ai_service.model.ClaudeRequest
import com.z.ai_service.model.ErrorAnalysis
import com.z.ai_service.model.Role
import com.z.ai_service.client.ClaudeClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ErrorLogPromptService(
  private val claudeClient: ClaudeClient,
  private val objectMapper: ObjectMapper
) {

  val logger: Logger = LoggerFactory.getLogger(javaClass)

  private val conversationHistory = mutableListOf(
    ClaudeRequest.Message(
      role = Role.USER.value,
      content = """
    You are a JSON-only API. You must respond ONLY with raw JSON nothing else.
    
    Priority: 
    1. Valid JSON only (no markdown, code blocks, backticks). 
    2. Exact schema match. 
    3. Field rules.
    
    CRITICAL: Do NOT use markdown formatting. Do NOT use code blocks. Do NOT use backticks.
    Your response must start with { and end with }. Nothing else.
  
    For every error log you receive, respond with this exact JSON structure:
    Schema for errors:
    {
      "rootCause": "Brief description of what caused the error",
      "missingValue": "The specific missing value or null (or null if not applicable)",
      "allowedValues": ["list", "of", "valid", "values"] or null if there's no enumerated set of valid values,
      "suggestion": "How to fix it"
    }
    
    Rules: 
    - Do NOT assume whether it's a database, service call, cache, or file system issue
    - If the log level is not ERROR then respond with rootCause: "Log level is not ERROR", and respond with a suggestion in the suggestion field and set other fields to null
    - Use "allowedValues": null when the error is about missing data, null references, or configuration issues
    - Only populate "allowedValues" when there's a specific set of valid enum/constant values (e.g., HTTP methods, status codes, predefined options)
    - For missing database records, null fields, or service failures, set "allowedValues": null
     - For "suggestion": Provide generic troubleshooting (verify the ID exists, check logs, etc.) without implementation assumptions
    
    Bad response: ```json{"rootCause":"..."}```
    Good response: {"rootCause":"..."}
  """.trimIndent().replace("\n", " ").replace(Regex("\\s+"), " ").trim()
    )
  )

  fun promptErrorLog(userMessage: String): ErrorAnalysis {
    logger.info("Prompting Claude with user message")
    conversationHistory.add(ClaudeRequest.Message(role = Role.USER.value, content = userMessage))

    val response = claudeClient.sendMessage(conversationHistory)
    val assistantMessage = response.getText().trim()
      .removePrefix("```json")
      .removePrefix("```")
      .removeSuffix("```")

    conversationHistory.add(ClaudeRequest.Message(role = Role.ASSISTANT.value, content = assistantMessage))

    return objectMapper.readValue(assistantMessage, ErrorAnalysis::class.java)
  }

  fun clearConversation() {
    conversationHistory.clear()
  }

  fun getConversationHistory(): MutableList<ClaudeRequest.Message> {
    return conversationHistory
  }
}
