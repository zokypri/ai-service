package com.z.ai_service.exception

open class ClaudeApiException(message: String) : RuntimeException(message)

class RateLimitException(message: String) : ClaudeApiException(message)

class OverloadedException(message: String) : ClaudeApiException(message)
