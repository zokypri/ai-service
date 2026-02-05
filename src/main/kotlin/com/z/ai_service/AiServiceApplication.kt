package com.z.ai_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
@EnableRetry
class AiServiceApplication

fun main(args: Array<String>) {
	runApplication<AiServiceApplication>(*args)
}
