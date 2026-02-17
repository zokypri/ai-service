package com.z.ai_service.client

import com.z.ai_service.model.ChromaAddRequest
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class ChromaClient(
  private val chromaRestClient: RestClient
) {
  fun addVectors(collection: String, requestBody: ChromaAddRequest): String? {
    return chromaRestClient.post()
      .uri("/api/v1/collections/$collection/add")
      .body(requestBody)
      .retrieve()
      .body(String::class.java)
  }
}
