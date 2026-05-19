package com.z.ai_service.model

import com.fasterxml.jackson.annotation.JsonProperty

data class SplunkAlertRequest(
    val sid: String,
    @JsonProperty("search_name")
    val searchName: String,
    val app: String,
    val owner: String,
    @JsonProperty("results_link")
    val resultsLink: String,
    val result: SplunkResult
) {
    fun toErrorLogPrompt(): String = result.toErrorLogPrompt()
}

data class SplunkResult(
    val sourcetype: String,
    val source: String,
    val host: String,
    val index: String,
    @JsonProperty("_time")
    val time: String,
    @JsonProperty("log_level")
    val logLevel: String,
    @JsonProperty("_raw")
    val raw: String
) {
    fun toErrorLogPrompt(): String {
        val lines = raw.lines()
        val errorLog = lines.firstOrNull() ?: ""
        val stackTrace = lines.drop(1).joinToString("\n")
        return """
            Error Log: $errorLog
            Stack Trace:
            $stackTrace
        """.trimIndent()
    }
}

