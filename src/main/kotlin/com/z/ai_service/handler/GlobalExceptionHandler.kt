package com.z.ai_service.handler

import com.z.ai_service.exception.OverloadedException
import com.z.ai_service.exception.RateLimitException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.RestClientException

@RestControllerAdvice
class GlobalExceptionHandler {

  @ExceptionHandler(RateLimitException::class)
  fun handleRateLimit(e: RateLimitException) =
    ResponseEntity
      .status(HttpStatus.TOO_MANY_REQUESTS)
      .body(ErrorResponse(e.message ?: "Rate limit exceeded"))

  @ExceptionHandler(OverloadedException::class)
  fun handleOverloaded(e: OverloadedException) =
    ResponseEntity
      .status(HttpStatus.SERVICE_UNAVAILABLE)
      .body(ErrorResponse(e.message ?: "API overloaded"))

  @ExceptionHandler(RestClientException::class)
  fun handleRestClientException(e: RestClientException) =
    ResponseEntity
      .status(HttpStatus.BAD_GATEWAY)
      .body(ErrorResponse(e.message ?: "External API error"))

  // Catch-all for anything unexpected
  @ExceptionHandler(Exception::class)
  fun handleException(e: Exception) =
    ResponseEntity
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(ErrorResponse(e.message ?: "Something went wrong"))
}

data class ErrorResponse(val error: String)

