package com.z.ai_service.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.z.ai_service.model.SplunkAlertRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/internal/poc/error/prompt")
@Tag(name = "POC Internal Prompt", description = "POC internal prompt related endpoints")
class PocInternalErrorLogPromptController(private val objectMapper: ObjectMapper) {

    private val logger = LoggerFactory.getLogger(PocInternalErrorLogPromptController::class.java)

    @PostMapping("/chat", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Log incoming Splunk alert (JSON)", description = "Receives a Splunk alert as JSON and logs the error log and stack trace")
    fun chat(@RequestBody alert: SplunkAlertRequest): ResponseEntity<Unit> {
        logAlert(alert)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/chat", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    @Operation(summary = "Log incoming Splunk alert (form)", description = "Receives a Splunk alert as form-encoded payload and logs the error log and stack trace")
    fun chatFormEncoded(@RequestParam("payload") payload: String): ResponseEntity<Unit> {
        val alert = objectMapper.readValue(payload, SplunkAlertRequest::class.java)
        logAlert(alert)
        return ResponseEntity.ok().build()
    }

    private fun logAlert(alert: SplunkAlertRequest) {
        val errorLogPrompt = alert.toErrorLogPrompt()
        logger.info("Received Splunk alert '{}' from host '{}': {}", alert.searchName, alert.result.host, errorLogPrompt)
    }
}

