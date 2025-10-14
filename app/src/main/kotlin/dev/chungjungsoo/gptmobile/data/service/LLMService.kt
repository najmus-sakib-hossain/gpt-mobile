package dev.chungjungsoo.gptmobile.data.service

import kotlinx.coroutines.flow.Flow

/**
 * Service interface for offline LLM inference using GGUF models
 * Based on SmolChat's SmolLMManager pattern
 */
interface LLMService {
    /**
     * Load a GGUF model from the given file path and initialize chat history
     * @param modelPath Absolute path to the GGUF model file
     * @param contextSize Context window size (default 2048)
     * @param systemPrompt Optional system prompt to add during initialization
     * @return True if model loaded successfully
     */
    suspend fun loadModel(modelPath: String, contextSize: Int = 2048, systemPrompt: String? = null): Boolean
    
    /**
     * Unload the currently loaded model and free memory
     */
    suspend fun unloadModel()
    
    /**
     * Check if a model is currently loaded
     */
    fun isModelLoaded(): Boolean
    
    /**
     * Get the path of the currently loaded model
     */
    fun getLoadedModelPath(): String?
    
    /**
     * Add a system prompt to the conversation
     * Must be called after loadModel() and before any messages
     */
    fun addSystemPrompt(prompt: String)
    
    /**
     * Add a user message to the conversation history
     */
    fun addUserMessage(message: String)
    
    /**
     * Add an assistant message to the conversation history
     */
    fun addAssistantMessage(message: String)
    
    /**
     * Generate a response for the given query
     * @param query The user's question/prompt
     * @return Flow of generated text chunks
     */
    fun getResponse(query: String): Flow<String>
    
    /**
     * Get the generation speed of the last response (tokens/sec)
     */
    fun getResponseGenerationSpeed(): Float
    
    /**
     * Get the current context length used
     */
    fun getContextLengthUsed(): Int
}

