package dev.chungjungsoo.gptmobile.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import dev.chungjungsoo.gptmobile.data.dto.HFModelSearchResult
import dev.chungjungsoo.gptmobile.data.dto.HFModelInfo
import dev.chungjungsoo.gptmobile.data.dto.HFModelFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HuggingFaceApi(private val client: HttpClient) {
    private val baseUrl = "https://huggingface.co/api"

    suspend fun searchModels(
        query: String,
        filter: String = "gguf",
        limit: Int = 20,
        sort: String = "downloads"
    ): List<HFModelSearchResult> = withContext(Dispatchers.IO) {
        try {
            client.get("$baseUrl/models") {
                parameter("search", query)
                parameter("filter", filter)
                parameter("limit", limit)
                parameter("sort", sort)
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getModelInfo(modelId: String): HFModelInfo? = withContext(Dispatchers.IO) {
        try {
            client.get("$baseUrl/models/$modelId").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getModelFiles(modelId: String): List<HFModelFile> = withContext(Dispatchers.IO) {
        try {
            val response: List<HFModelFile> = client.get("$baseUrl/models/$modelId/tree/main").body()
            // Filter for GGUF files only
            response.filter { it.path.endsWith(".gguf", ignoreCase = true) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
