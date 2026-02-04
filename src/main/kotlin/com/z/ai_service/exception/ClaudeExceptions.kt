package com.z.ai_service.exception

import org.springframework.http.HttpStatus

open class ClaudeApiException(
  message: String,
  open val httpStatus: HttpStatus = HttpStatus.BAD_GATEWAY
) : RuntimeException(message)

class RateLimitException(
  message: String,
  override val httpStatus: HttpStatus = HttpStatus.TOO_MANY_REQUESTS
) : ClaudeApiException(message, httpStatus)

class OverloadedException(
  message: String,
  override val httpStatus: HttpStatus = HttpStatus.SERVICE_UNAVAILABLE
) : ClaudeApiException(message, httpStatus)
