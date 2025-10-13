package dev.chungjungsoo.gptmobile.data.service

import kotlinx.coroutines.flow.Flow

/**
 * Service interface for offline LLM inference using GGUF models
 */
interface LLMService {
    /**
     * Load a GGUF model from the given file path
     * @param modelPath Absolute path to the GGUF model file
     * @param contextSize Context window size (default 2048)
     * @return True if model loaded successfully
     */
    suspend fun loadModel(modelPath: String, contextSize: Int = 2048): Boolean
    
    /**
     * Unload the currently loaded model and free memory
     */
    suspend fun unloadModel()
    
    /**
     * Generate text response from the loaded model
     * @param prompt The input prompt (including chat history if needed)
     * @param maxTokens Maximum number of tokens to generate
     * @param temperature Sampling temperature (0.0-2.0)
     * @param topP Nucleus sampling parameter
     * @return Flow of generated text chunks
     */
    fun generateText(
        prompt: String,
        maxTokens: Int = 512,
        temperature: Float = 0.7f,
        topP: Float = 0.9f
    ): Flow<String>
    
    /**
     * Check if a model is currently loaded
     */
    fun isModelLoaded(): Boolean
    
    /**
     * Get information about the currently loaded model
     */
    fun getLoadedModelPath(): String?
}
