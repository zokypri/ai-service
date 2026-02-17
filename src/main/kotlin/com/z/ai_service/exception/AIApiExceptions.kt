package com.z.ai_service.exception

import org.springframework.http.HttpStatus

// Base exception for all AI API errors
abstract class AIApiException(
  message: String,
  open val httpStatus: HttpStatus = HttpStatus.BAD_GATEWAY
) : RuntimeException(message)

// Shared specific exceptions
class RateLimitException(
  message: String,
  override val httpStatus: HttpStatus = HttpStatus.TOO_MANY_REQUESTS
) : AIApiException(message, httpStatus)

class OverloadedException(
  message: String,
  override val httpStatus: HttpStatus = HttpStatus.SERVICE_UNAVAILABLE
) : AIApiException(message, httpStatus)

// Provider-specific base exceptions (if you need provider-specific handling)
class ClaudeApiException(
  message: String,
  httpStatus: HttpStatus = HttpStatus.BAD_GATEWAY
) : AIApiException(message, httpStatus)

class OpenAIApiException(
  message: String,
  httpStatus: HttpStatus = HttpStatus.BAD_GATEWAY
) : AIApiException(message, httpStatus)
