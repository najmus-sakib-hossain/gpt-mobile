package dev.chungjungsoo.gptmobile.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HFModelSearchResult(
    val id: String,
    @SerialName("modelId")
    val modelId: String? = null,
    val author: String? = null,
    @SerialName("sha")
    val sha: String? = null,
    @SerialName("lastModified")
    val lastModified: String? = null,
    @SerialName("private")
    val private: Boolean? = null,
    @SerialName("gated")
    val gated: Boolean? = null,
    val downloads: Int? = null,
    val likes: Int? = null,
    val tags: List<String> = emptyList(),
    @SerialName("pipeline_tag")
    val pipelineTag: String? = null,
    val library: String? = null
)

@Serializable
data class HFModelInfo(
    val id: String,
    @SerialName("modelId")
    val modelId: String,
    val author: String? = null,
    @SerialName("sha")
    val sha: String,
    @SerialName("lastModified")
    val lastModified: String,
    @SerialName("private")
    val private: Boolean = false,
    val downloads: Int = 0,
    val likes: Int = 0,
    val tags: List<String> = emptyList(),
    @SerialName("pipeline_tag")
    val pipelineTag: String? = null,
    val library: String? = null,
    @SerialName("card")
    val card: String? = null
)

@Serializable
data class HFModelFile(
    val path: String,
    val size: Long,
    val lfs: HFLfsInfo? = null,
    val type: String
)

@Serializable
data class HFLfsInfo(
    val oid: String,
    val size: Long,
    @SerialName("pointerSize")
    val pointerSize: Int
)

data class ExampleModel(
    val name: String,
    val modelId: String,
    val fileName: String,
    val url: String,
    val description: String,
    val size: String
)

// Example models list for quick access - using SmolChat recommended models
val exampleModelsList = listOf(
    ExampleModel(
        name = "SmolLM2-360M-Instruct",
        modelId = "HuggingFaceTB/SmolLM2-360M-Instruct-GGUF",
        fileName = "smollm2-360m-instruct-q8_0.gguf",
        url = "https://huggingface.co/HuggingFaceTB/SmolLM2-360M-Instruct-GGUF/resolve/main/smollm2-360m-instruct-q8_0.gguf",
        description = "Tiny 360M model, perfect for mobile devices",
        size = "~380 MB"
    ),
    ExampleModel(
        name = "SmolLM2-1.7B-Instruct",
        modelId = "HuggingFaceTB/SmolLM2-1.7B-Instruct-GGUF",
        fileName = "smollm2-1.7b-instruct-q4_k_m.gguf",
        url = "https://huggingface.co/HuggingFaceTB/SmolLM2-1.7B-Instruct-GGUF/resolve/main/smollm2-1.7b-instruct-q4_k_m.gguf",
        description = "Small but powerful 1.7B model optimized for mobile",
        size = "~1.1 GB"
    ),
    ExampleModel(
        name = "Qwen2.5-1.5B-Instruct",
        modelId = "Qwen/Qwen2.5-1.5B-Instruct-GGUF",
        fileName = "qwen2.5-1.5b-instruct-q8_0.gguf",
        url = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q8_0.gguf",
        description = "Qwen 1.5B model with excellent instruction following",
        size = "~1.6 GB"
    ),
    ExampleModel(
        name = "Qwen2.5-Coder-3B-Instruct",
        modelId = "Qwen/Qwen2.5-Coder-3B-Instruct-GGUF",
        fileName = "qwen2.5-coder-3b-instruct-q5_0.gguf",
        url = "https://huggingface.co/Qwen/Qwen2.5-Coder-3B-Instruct-GGUF/resolve/main/qwen2.5-coder-3b-instruct-q5_0.gguf",
        description = "Specialized coding model with 3B parameters",
        size = "~2.3 GB"
    )
)
