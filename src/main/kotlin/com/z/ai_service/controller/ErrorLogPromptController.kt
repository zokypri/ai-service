package com.z.ai_service.controller

import com.z.ai_service.ChatResponse
import com.z.ai_service.ErrorAnalysis
import com.z.ai_service.service.ErrorLogPromptService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/error/prompt")
@Tag(name = "Prompt", description = "Prompt related endpoints")
class ErrorLogPromptController(private val errorLogPromptService: ErrorLogPromptService) {

  @PostMapping("/chat", consumes = ["text/plain"])
  @Operation(summary = "Chat with the prompt service", description = "Sends error log as plain text")
  fun chat(@RequestBody message: String): ResponseEntity<ErrorAnalysis> {
    val errorAnalysis = errorLogPromptService.promptErrorLog(message)
    return ResponseEntity.ok(errorAnalysis)
  }

  @DeleteMapping("/chat")
  @Operation(summary = "Clear conversation history", description = "Delete all messages in the current conversation")
  fun clearConversation(): ResponseEntity<Unit> {
    errorLogPromptService.clearConversation()
    return ResponseEntity.noContent().build()
  }
}
