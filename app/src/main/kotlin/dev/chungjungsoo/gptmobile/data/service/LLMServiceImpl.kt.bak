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
    }
    
    override suspend fun loadModel(modelPath: String, contextSize: Int): Boolean {
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
                
                // Load new model with InferenceParams
                val model = SmolLM()
                model.load(
                    modelPath, 
                    SmolLM.InferenceParams(contextSize = contextSize.toLong())
                )
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
    
    override fun generateText(
        prompt: String,
        maxTokens: Int,
        temperature: Float,
        topP: Float
    ): Flow<String> = flow {
        val model = smolLM
        if (model == null) {
            Log.e(TAG, "Cannot generate text: model not loaded")
            emit("[Error: Model not loaded]")
            return@flow
        }
        
        try {
            Log.d(TAG, "Generating text with prompt length: ${prompt.length}")
            
            // Generate tokens using SmolLM's getResponseAsFlow
            model.getResponseAsFlow(prompt).collect { token ->
                if (token != "[EOG]") {  // Filter out end-of-generation marker
                    emit(token)
                }
            }
            
            Log.d(TAG, "Generation complete")
        } catch (e: Exception) {
            Log.e(TAG, "Error during text generation", e)
            emit("\n[Error: ${e.message}]")
        }
    }
    
    override fun isModelLoaded(): Boolean {
        return smolLM != null && currentModelPath != null
    }
    
    override fun getLoadedModelPath(): String? {
        return currentModelPath
    }
}
