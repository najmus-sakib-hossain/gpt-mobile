# Offline AI Chat - SmolChat Pattern Implementation Summary

## Critical Issue Identified

The current implementation was sending the **chat template markup** instead of just the conversation content, which is why you're seeing:

```
<|system|>
My task is to answer your questions exactly as you asked them.
<|user|>
Can you tell me what is the capital of France?
<|assistant|>
```

## Root Cause

We were **manually building the prompt with ChatML tags**, but SmolLM automatically applies the chat template from the GGUF file. This caused the template to be echoed back in the response.

## Solution: Use SmolChat's Pattern

SmolChat uses SmolLM's **built-in chat history management** instead of building prompts manually.

### Key SmolLM Methods We Need to Use

```kotlin
// Initialize model with system prompt
smolLM.load(modelPath, InferenceParams(contextSize = 4096))
smolLM.addSystemPrompt("You are a helpful assistant")

// Add conversation history
smolLM.addUserMessage("What is the capital of France?")
smolLM.addAssistantMessage("The capital of France is Paris.")
smolLM.addUserMessage("And what about Germany?")

// Get response (SmolLM applies chat template automatically)
smolLM.getResponseAsFlow("And what about Germany?").collect { token ->
    emit(token)  // Just: "The capital of Germany is Berlin."
}
```

## Required Changes

### 1. Update LLMService Interface

```kotlin
interface LLMService {
    suspend fun loadModel(modelPath: String, contextSize: Int, systemPrompt: String?): Boolean
    suspend fun unloadModel()
    fun isModelLoaded(): Boolean
    fun getLoadedModelPath(): String?
    
    // Chat history management (new!)
    fun addSystemPrompt(prompt: String)
    fun addUserMessage(message: String)
    fun addAssistantMessage(message: String)
    
    // Simplified inference
    fun getResponse(query: String): Flow<String>
    
    // Model info
    fun getResponseGenerationSpeed(): Float
    fun getContextLengthUsed(): Int
}
```

### 2. Update LLMServiceImpl

```kotlin
@Singleton
class LLMServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : LLMService {
    
    private var smolLM: SmolLM? = null
    private var currentModelPath: String? = null
    
    override suspend fun loadModel(modelPath: String, contextSize: Int, systemPrompt: String?): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Loading model from: $modelPath")
                
                val file = File(modelPath)
                if (!file.exists()) {
                    Log.e(TAG, "Model file does not exist: $modelPath")
                    return@withContext false
                }
                
                unloadModel()
                
                val model = SmolLM()
                model.load(modelPath, SmolLM.InferenceParams(contextSize = contextSize.toLong()))
                
                // Add system prompt during initialization
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
    
    override fun addUserMessage(message: String) {
        smolLM?.addUserMessage(message)
        Log.d(TAG, "User message added: ${message.take(50)}...")
    }
    
    override fun addAssistantMessage(message: String) {
        smolLM?.addAssistantMessage(message)
        Log.d(TAG, "Assistant message added: ${message.take(50)}...")
    }
    
    override fun getResponse(query: String): Flow<String> = flow {
        val model = smolLM ?: run {
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
    
    override fun getResponseGenerationSpeed(): Float = smolLM?.getResponseGenerationSpeed() ?: 0f
    override fun getContextLengthUsed(): Int = smolLM?.getContextLengthUsed() ?: 0
}
```

### 3. Update ChatRepositoryImpl.completeOfflineAIChat()

```kotlin
override suspend fun completeOfflineAIChat(
    question: Message,
    history: List<Message>
): Flow<ApiState> {
    val platform = checkNotNull(
        settingRepository.fetchPlatforms().firstOrNull { it.name == ApiType.OFFLINE_AI }
    )
    
    val modelPath = platform.model ?: throw IllegalStateException("No offline model selected")
    
    Log.d("ChatRepositoryImpl", "Attempting to use offline model at: $modelPath")
    
    // Convert content URI to file path if needed
    val actualFilePath = FileUtil.ensureModelFileExists(appContext, modelPath)
        ?: throw IllegalStateException("Failed to access offline model from: $modelPath")
    
    Log.d("ChatRepositoryImpl", "Using model file at: $actualFilePath")
    
    // Load model if not already loaded
    if (!llmService.isModelLoaded() || llmService.getLoadedModelPath() != actualFilePath) {
        val systemPrompt = platform.systemPrompt ?: ModelConstants.DEFAULT_PROMPT
        val loadSuccess = llmService.loadModel(
            modelPath = actualFilePath, 
            contextSize = 4096,
            systemPrompt = systemPrompt
        )
        if (!loadSuccess) {
            throw IllegalStateException("Failed to load offline model from: $actualFilePath")
        }
        Log.d("ChatRepositoryImpl", "Model loaded successfully")
        
        // Add chat history to the model
        history.forEach { message ->
            when (message.platformType) {
                null -> llmService.addUserMessage(message.content)
                ApiType.OFFLINE_AI -> llmService.addAssistantMessage(message.content)
                else -> {} // Ignore other platforms
            }
        }
        Log.d("ChatRepositoryImpl", "Chat history added (${history.size} messages)")
    }
    
    // Generate response (SmolLM applies chat template automatically)
    return llmService.getResponse(question.content)
        .map<String, ApiState> { token -> ApiState.Success(token) }
        .catch { throwable -> emit(ApiState.Error(throwable.message ?: "Unknown error")) }
        .onStart { emit(ApiState.Loading) }
        .onCompletion { emit(ApiState.Done) }
}
```

## How This Fixes The Issue

### Before (Wrong ❌)

```kotlin
// We manually built the prompt
val prompt = """
<|system|>
You are a helpful assistant
<|user|>
What is the capital of France?
<|assistant|>
""".trimIndent()

// SmolLM applies its template on top of our template!
smolLM.getResponseAsFlow(prompt) 
// Result: Echoes back the entire template
```

### After (Correct ✅)

```kotlin
// Load model with system prompt
smolLM.load(modelPath, params)
smolLM.addSystemPrompt("You are a helpful assistant")

// Add history
smolLM.addUserMessage("What is 2+2?")
smolLM.addAssistantMessage("4")

// Get response for new question
smolLM.getResponseAsFlow("What is the capital of France?")
// Result: "The capital of France is Paris." (clean response!)
```

## Why This is Better

1. **No template duplication** - SmolLM handles templates internally
2. **Chat history management** - Model maintains conversation context
3. **Model loads once** - History added at load time, not per-message
4. **Proper token generation** - Clean responses without markup
5. **Matches SmolChat pattern** - Proven implementation

## Next Steps

1. Delete the corrupted `LLMServiceImpl.kt`
2. Create new `LLMServiceImpl.kt` with the code above
3. Update `ChatRepositoryImpl.kt` with the new pattern
4. Test with a small model (SmolLM2-135M)
5. Verify responses are clean without template markup

## Expected Result

When you ask: "What is the capital of France?"

**You'll get:**

```
The capital of France is Paris.
```

**NOT:**

```
<|system|>
...
<|assistant|>
The capital of France is Paris.
```

The template tags will be handled internally by SmolLM and won't appear in the output!
