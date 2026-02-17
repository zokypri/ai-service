package com.z.ai_service.model

data class ChromaAddRequest(
  val ids: List<String>,
  val embeddings: List<List<Float>>,
  val documents: List<String>,
  val metadatas: List<Map<String, Any>>? = null
)
