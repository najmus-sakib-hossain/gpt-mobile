package dev.chungjungsoo.gptmobile.data.service

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import io.shubham0204.smollm.SmolLM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LLMServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : LLMService {
    
    private var smolLM: SmolLM? = null
    private var currentModelPath: String? = null
    
    companion object {
        private const val TAG = "LLMServiceImpl"
        
        // SmolChat default inference parameters from SmolLM.InferenceParams:
        // minP: 0.1f, temperature: 0.8f, storeChats: true, numThreads: 4
        // useMmap: true, useMlock: false
    }
    
    override suspend fun loadModel(modelPath: String, contextSize: Int, systemPrompt: String?): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Loading model from: $modelPath")
                
                // Check if file exists
                val file = File(modelPath)
                if (!file.exists()) {
                    Log.e(TAG, "Model file does not exist: $modelPath")
                    return@withContext false
                }
                
                // Unload existing model if any
                unloadModel()
                
                // Load new model with SmolChat's default inference parameters
                val model = SmolLM()
                model.load(
                    modelPath,
                    SmolLM.InferenceParams(
                        minP = 0.1f,                     // SmolChat default
                        temperature = 0.8f,              // SmolChat default
                        storeChats = true,               // Enable chat history
                        contextSize = contextSize.toLong(),
                        chatTemplate = null,             // Auto-detect from GGUF
                        numThreads = 4,                  // SmolChat default
                        useMmap = true,                  // SmolChat default
                        useMlock = false                 // SmolChat default
                    )
                )
                
                // Add system prompt if provided (SmolChat pattern)
                if (!systemPrompt.isNullOrBlank()) {
                    model.addSystemPrompt(systemPrompt)
                    Log.d(TAG, "System prompt added")
                }
                
                smolLM = model
                
                currentModelPath = modelPath
                Log.d(TAG, "Model loaded successfully")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load model", e)
                smolLM = null
                currentModelPath = null
                false
            }
        }
    }
    
    override suspend fun unloadModel() {
        withContext(Dispatchers.IO) {
            try {
                smolLM?.close()
                smolLM = null
                currentModelPath = null
                Log.d(TAG, "Model unloaded")
            } catch (e: Exception) {
                Log.e(TAG, "Error unloading model", e)
            }
        }
    }
    
    override fun addSystemPrompt(prompt: String) {
        val model = smolLM
        if (model == null) {
            Log.e(TAG, "Cannot add system prompt: model not loaded")
            return
        }
        model.addSystemPrompt(prompt)
        Log.d(TAG, "System prompt added: ${prompt.take(50)}...")
    }
    
    override fun addUserMessage(message: String) {
        val model = smolLM
        if (model == null) {
            Log.e(TAG, "Cannot add user message: model not loaded")
            return
        }
        model.addUserMessage(message)
        Log.d(TAG, "User message added: ${message.take(50)}...")
    }
    
    override fun addAssistantMessage(message: String) {
        val model = smolLM
        if (model == null) {
            Log.e(TAG, "Cannot add assistant message: model not loaded")
            return
        }
        model.addAssistantMessage(message)
        Log.d(TAG, "Assistant message added: ${message.take(50)}...")
    }
    
    override fun getResponse(query: String): Flow<String> = flow {
        val model = smolLM
        if (model == null) {
            Log.e(TAG, "Cannot generate response: model not loaded")
            emit("[Error: Model not loaded]")
            return@flow
        }
        
        try {
            Log.d(TAG, "Generating response for: ${query.take(50)}...")
            
            // SmolLM applies chat template automatically
            model.getResponseAsFlow(query).collect { token ->
                if (token != "[EOG]") {
                    emit(token)
                }
            }
            
            Log.d(TAG, "Response generation complete")
        } catch (e: Exception) {
            Log.e(TAG, "Error generating response", e)
            emit("[Error: ${e.message}]")
        }
    }
    
    override fun getResponseGenerationSpeed(): Float {
        return smolLM?.getResponseGenerationSpeed() ?: 0f
    }
    
    override fun getContextLengthUsed(): Int {
        return smolLM?.getContextLengthUsed() ?: 0
    }
    
    override fun isModelLoaded(): Boolean = smolLM != null
    
    override fun getLoadedModelPath(): String? = currentModelPath
}
