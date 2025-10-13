package dev.chungjungsoo.gptmobile.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_models")
data class OfflineModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val modelName: String,
    val modelId: String, // HuggingFace model ID (e.g., "TheBloke/Llama-2-7B-GGUF")
    val filePath: String,
    val fileSize: Long, // Size in bytes
    val contextSize: Int = 2048,
    val chatTemplate: String = "",
    val downloadDate: Long = System.currentTimeMillis(),
    val isDownloaded: Boolean = true
)
