# Offline AI Model Inference Implementation Plan

## Overview

This document outlines the steps needed to implement actual offline AI inference using downloaded GGUF models, inspired by SmolChat-Android's implementation.

---

## Current Status

### ✅ Completed

- Model discovery and browsing from HuggingFace
- Model download functionality using Android DownloadManager
- Model metadata storage in Room database
- UI for model browser and model details
- Example models from SmolChat recommendations
- Navigation and routing

### ❌ Not Implemented Yet

- **llama.cpp integration** (C++ native library)
- **Model loading** from downloaded GGUF files
- **Inference engine** for generating text
- **Chat completion** for OFFLINE_AI ApiType
- **Model selection** in chat interface
- **Memory management** for loaded models

---

## Implementation Approach

### Option 1: Use Existing llama.cpp Android Library (Recommended)

**Advantages:**

- Proven, tested solution
- Active community support
- Optimized for mobile devices
- Regular updates

**Libraries to Consider:**

1. **llama.cpp Android** (Official)
   - Repository: <https://github.com/ggerganov/llama.cpp>
   - Has Android example in `examples/llama.android`
   - Requires building native library

2. **SmolChat's smollm module**
   - Already integrated llama.cpp
   - Can be used as reference
   - Located at `SmolChat-Android/smollm/`

3. **llama-android** (Third-party wrapper)
   - Repository: <https://github.com/alexzhirkevich/llama-android>
   - Kotlin-friendly API
   - Pre-built AAR available

### Option 2: Integrate SmolChat's Module Directly

**Steps:**

1. Copy `SmolChat-Android/smollm/` module to gpt-mobile
2. Add as Gradle module dependency
3. Adapt API to work with existing architecture
4. Handle licensing (Apache 2.0 compatible)

---

## Detailed Implementation Plan

### Phase 1: Native Library Integration (Estimated: 2-3 days)

#### Step 1.1: Add llama.cpp Module

```kotlin
// settings.gradle.kts
include(":smollm")  // or ":llama-cpp-android"
```

#### Step 1.2: Add Module Dependency

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(project(":smollm"))
    // OR use third-party library
    implementation("io.github.alexzhirkevich:llama-android:0.x.x")
}
```

#### Step 1.3: Configure Native Build

```kotlin
// app/build.gradle.kts
android {
    ndkVersion = "27.2.12479018"  // Match SmolChat version
    
    defaultConfig {
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        }
    }
    
    externalNativeBuild {
        cmake {
            path = file("../smollm/src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}
```

### Phase 2: LLM Service Layer (Estimated: 2-3 days)

#### Step 2.1: Create LLM Service Interface

```kotlin
// File: data/service/LLMService.kt
package dev.chungjungsoo.gptmobile.data.service

import kotlinx.coroutines.flow.Flow

interface LLMService {
    suspend fun loadModel(modelPath: String, contextSize: Int = 2048): Boolean
    suspend fun unloadModel()
    fun generateText(
        prompt: String,
        maxTokens: Int = 512,
        temperature: Float = 0.7f,
        topP: Float = 0.9f
    ): Flow<String>
    fun isModelLoaded(): Boolean
    fun getModelInfo(): ModelInfo?
}

data class ModelInfo(
    val name: String,
    val contextSize: Int,
    val parameterCount: String,
    val quantization: String
)
```

#### Step 2.2: Implement LLM Service

```kotlin
// File: data/service/LLMServiceImpl.kt
package dev.chungjungsoo.gptmobile.data.service

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.shubham0204.smollm.LlamaAndroid  // From smollm module
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LLMServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : LLMService {
    
    private var llama: LlamaAndroid? = null
    private var currentModelPath: String? = null
    
    override suspend fun loadModel(modelPath: String, contextSize: Int): Boolean {
        return try {
            // Unload existing model if any
            unloadModel()
            
            // Load new model
            llama = LlamaAndroid(
                modelPath = modelPath,
                contextLength = contextSize,
                numThreads = Runtime.getRuntime().availableProcessors()
            )
            currentModelPath = modelPath
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    override suspend fun unloadModel() {
        llama?.close()
        llama = null
        currentModelPath = null
    }
    
    override fun generateText(
        prompt: String,
        maxTokens: Int,
        temperature: Float,
        topP: Float
    ): Flow<String> = flow {
        val model = llama ?: throw IllegalStateException("Model not loaded")
        
        // Generate text token by token
        val tokens = model.generate(
            prompt = prompt,
            maxTokens = maxTokens,
            temperature = temperature,
            topP = topP
        )
        
        tokens.collect { token ->
            emit(token)
        }
    }
    
    override fun isModelLoaded(): Boolean = llama != null
    
    override fun getModelInfo(): ModelInfo? {
        val model = llama ?: return null
        val path = currentModelPath ?: return null
        
        return ModelInfo(
            name = path.substringAfterLast("/"),
            contextSize = model.contextLength,
            parameterCount = extractParameterCount(path),
            quantization = extractQuantization(path)
        )
    }
    
    private fun extractParameterCount(fileName: String): String {
        // Extract from filename like "smollm2-360m-instruct-q8_0.gguf"
        val regex = Regex("(\\d+\\.?\\d*[mb])", RegexOption.IGNORE_CASE)
        return regex.find(fileName)?.value?.uppercase() ?: "Unknown"
    }
    
    private fun extractQuantization(fileName: String): String {
        // Extract from filename like "q8_0" or "Q4_K_M"
        val regex = Regex("[qQ]\\d+[_\\d]*[KkMm]?")
        return regex.find(fileName)?.value?.uppercase() ?: "Unknown"
    }
}
```

#### Step 2.3: Provide Service in Hilt Module

```kotlin
// File: di/ServiceModule.kt
package dev.chungjungsoo.gptmobile.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.chungjungsoo.gptmobile.data.service.LLMService
import dev.chungjungsoo.gptmobile.data.service.LLMServiceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {
    
    @Binds
    @Singleton
    abstract fun bindLLMService(
        llmServiceImpl: LLMServiceImpl
    ): LLMService
}
```

### Phase 3: Repository Integration (Estimated: 1-2 days)

#### Step 3.1: Update ChatRepository

```kotlin
// File: data/repository/ChatRepository.kt

// Add offline AI completion method
suspend fun completeOfflineAIChat(
    question: String,
    history: List<Message>,
    modelPath: String? = null
): Flow<String> = flow {
    try {
        // Load model if not already loaded or if different model requested
        val model = modelPath ?: offlineModelRepository.getSelectedModel()?.filePath
        ?: throw IllegalStateException("No offline model selected")
        
        if (!llmService.isModelLoaded() || currentModel != model) {
            val loaded = llmService.loadModel(model)
            if (!loaded) {
                throw IllegalStateException("Failed to load model")
            }
            currentModel = model
        }
        
        // Format prompt with history
        val prompt = formatPromptWithHistory(question, history)
        
        // Generate response
        llmService.generateText(
            prompt = prompt,
            maxTokens = 512,
            temperature = 0.7f,
            topP = 0.9f
        ).collect { token ->
            emit(token)
        }
    } catch (e: Exception) {
        emit("Error: ${e.message}")
    }
}

private fun formatPromptWithHistory(question: String, history: List<Message>): String {
    // Format based on chat template (e.g., ChatML, Llama-2 format, etc.)
    val builder = StringBuilder()
    
    // Add system prompt
    builder.append("<|system|>\nYou are a helpful AI assistant.</s>\n")
    
    // Add history
    history.takeLast(10).forEach { message ->
        when (message.sender) {
            "user" -> builder.append("<|user|>\n${message.content}</s>\n")
            else -> builder.append("<|assistant|>\n${message.content}</s>\n")
        }
    }
    
    // Add current question
    builder.append("<|user|>\n$question</s>\n")
    builder.append("<|assistant|>\n")
    
    return builder.toString()
}
```

### Phase 4: ViewModel Updates (Estimated: 1 day)

#### Step 4.1: Add Offline AI State to ChatViewModel

```kotlin
// File: presentation/ui/chat/ChatViewModel.kt

// Add offline AI flow and states
private val offlineAIFlow = MutableSharedFlow<String>()
private val _offlineAILoadingState = MutableStateFlow(LoadingState.Idle)
val offlineAILoadingState = _offlineAILoadingState.asStateFlow()

private val _offlineAIMessage = MutableStateFlow("")
val offlineAIMessage = _offlineAIMessage.asStateFlow()

// Add to sendQuestion()
if (ApiType.OFFLINE_AI in enabledPlatforms) {
    completeOfflineAIChat()
}

private fun completeOfflineAIChat() {
    viewModelScope.launch {
        val modelPath = settingRepository.getOfflineModelPath(ApiType.OFFLINE_AI)
        val chatFlow = chatRepository.completeOfflineAIChat(
            question = _userMessage.value,
            history = _messages.value,
            modelPath = modelPath
        )
        chatFlow.collect { chunk -> offlineAIFlow.emit(chunk) }
    }
}

// Add to handleStates()
viewModelScope.launch {
    offlineAIFlow.collect {
        _offlineAIMessage.value += it
        _offlineAILoadingState.value = LoadingState.Loading
    }
}
```

### Phase 5: UI Updates (Estimated: 1 day)

#### Step 5.1: Add Offline AI Loading State to ChatScreen

```kotlin
// File: presentation/ui/chat/ChatScreen.kt

val offlineAILoadingState by chatViewModel.offlineAILoadingState.collectAsStateWithLifecycle()
val offlineAIMessage by chatViewModel.offlineAIMessage.collectAsStateWithLifecycle()

// Update loading state check
ApiType.OFFLINE_AI -> offlineAILoadingState
```

#### Step 5.2: Add Model Selection in Settings

```kotlin
// Create new screen: OfflineModelSelectionScreen.kt
@Composable
fun OfflineModelSelectionScreen(
    viewModel: OfflineModelViewModel = hiltViewModel(),
    onModelSelected: (OfflineModel) -> Unit,
    onBackClick: () -> Unit
) {
    val downloadedModels by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Offline Model") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(downloadedModels.downloadedModels) { model ->
                OfflineModelSelectionCard(
                    model = model,
                    onClick = { onModelSelected(model) }
                )
            }
        }
    }
}
```

---

## Memory Management Considerations

### Model Size vs Device RAM

- **360M models**: Require ~500MB RAM
- **1.7B models**: Require ~2GB RAM  
- **3B models**: Require ~4GB RAM

### Best Practices

1. **Check available memory** before loading model
2. **Unload models** when switching platforms
3. **Show memory warnings** for large models on low-RAM devices
4. **Implement model caching** (keep last used model loaded)

```kotlin
fun checkAvailableMemory(context: Context, requiredMB: Int): Boolean {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)
    
    val availableMB = memoryInfo.availMem / (1024 * 1024)
    return availableMB > requiredMB * 1.5  // 1.5x safety margin
}
```

---

## Chat Template Support

Different models use different chat templates. Need to support:

### Common Templates

1. **ChatML** (Used by SmolLM2, Qwen)

```
<|im_start|>system
You are a helpful assistant<|im_end|>
<|im_start|>user
Hello!<|im_end|>
<|im_start|>assistant
```

2. **Llama-2/3**

```
[INST] <<SYS>>
You are a helpful assistant
<</SYS>>

Hello! [/INST]
```

3. **Alpaca**

```
Below is an instruction...

### Instruction:
Hello!

### Response:
```

### Implementation

Store chat template in OfflineModel entity:

```kotlin
@Entity(tableName = "offline_models")
data class OfflineModel(
    // ... existing fields
    @ColumnInfo(name = "chat_template")
    val chatTemplate: String = "chatml"  // Default to ChatML
)
```

---

## Testing Plan

### Unit Tests

- [ ] LLMService model loading/unloading
- [ ] Prompt formatting with different templates
- [ ] Token generation flow
- [ ] Memory checks

### Integration Tests

- [ ] End-to-end chat with offline model
- [ ] Model switching
- [ ] Error handling (model not found, out of memory)

### Manual Testing

- [ ] Download and use SmolLM2-360M
- [ ] Test with different models
- [ ] Test on devices with different RAM
- [ ] Test interruption (app backgrounded during generation)
- [ ] Test model switching mid-conversation

---

## Performance Optimizations

### Initial Load Time

- Pre-warm model on app start (if enabled in settings)
- Show progress during model loading
- Cache loaded models

### Generation Speed

- Use optimal thread count (CPU cores)
- Enable GPU acceleration if available (Vulkan/OpenCL)
- Implement batch processing for multiple requests

### Battery Impact

- Monitor CPU usage
- Provide "power saver" mode (reduces threads)
- Allow user to set generation timeout

---

## User Experience Considerations

### Settings Screen

Add offline AI settings:

```
Settings > Offline AI
├── Selected Model: SmolLM2-360M
├── Context Size: 2048 tokens
├── Temperature: 0.7
├── Top-P: 0.9
├── Max Response Length: 512 tokens
├── Auto-load Model: [Toggle]
└── Advanced Settings...
```

### Model Selection Flow

```
Home Screen
  └── Click "New Chat"
      └── Select Platforms
          ├── [✓] OpenAI
          ├── [✓] Anthropic
          └── [✓] Offline AI
              └── Select Model: SmolLM2-360M ✓
```

### First-Time Experience

1. User downloads model
2. Prompt: "Model ready! Start chatting offline"
3. Show tutorial: "Offline AI works without internet"
4. Explain limitations: "Responses may be slower/shorter"

---

## Limitations & Known Issues

### Technical Limitations

- Generation is slower than API-based models
- Limited context window (2048-4096 tokens)
- Model quality varies by size
- Requires significant storage and RAM

### Feature Limitations

- No streaming from model (simulated streaming possible)
- No function calling support
- No vision/image understanding
- No voice generation

---

## Alternative: Use Remote Inference Initially

If llama.cpp integration is too complex initially, consider:

### Use Ollama API

- Run Ollama on user's computer
- Connect from mobile app
- Similar to current API approach
- Requires local network or exposed endpoint

### Use HuggingFace Inference API

- Use smaller models via API
- Pay per token but very cheap
- Easier to implement
- Falls back to online mode

---

## Estimated Timeline

| Phase | Task | Duration | Priority |
|-------|------|----------|----------|
| 1 | Research & decide on library | 1-2 days | High |
| 2 | Integrate native llama.cpp | 2-3 days | High |
| 3 | Implement LLMService | 2-3 days | High |
| 4 | Repository integration | 1-2 days | High |
| 5 | ViewModel updates | 1 day | High |
| 6 | UI updates | 1 day | Medium |
| 7 | Settings & model selection | 1-2 days | Medium |
| 8 | Testing & bug fixes | 2-3 days | High |
| 9 | Documentation | 1 day | Low |
| **Total** | | **12-18 days** | |

---

## Recommended Next Steps

1. **Decision Point**: Choose between:
   - Integrating llama.cpp from scratch
   - Using SmolChat's smollm module as-is
   - Using third-party wrapper library

2. **Prototype**: Build minimal working version with one model

3. **Test**: Verify performance on target devices

4. **Iterate**: Add features incrementally

5. **Document**: Create user guide for offline AI

---

## Resources & References

### Documentation

- llama.cpp: <https://github.com/ggerganov/llama.cpp>
- llama-android example: <https://github.com/ggerganov/llama.cpp/tree/master/examples/llama.android>
- SmolChat source: <https://github.com/shubham0204/SmolChat-Android>
- GGUF format: <https://github.com/ggerganov/ggml/blob/master/docs/gguf.md>

### Libraries

- llama-android wrapper: <https://github.com/alexzhirkevich/llama-android>
- Android NDK: <https://developer.android.com/ndk>
- CMake for Android: <https://developer.android.com/ndk/guides/cmake>

### Community

- llama.cpp Discord: <https://discord.gg/llamacpp>
- HuggingFace forums: <https://discuss.huggingface.co/>
- r/LocalLLaMA: <https://reddit.com/r/LocalLLaMA>

---

## Conclusion

Implementing offline AI inference is a significant undertaking requiring native C++ integration, memory management, and careful UX design. However, it would provide a unique feature that sets the app apart and enables true offline functionality.

The recommended approach is to start with SmolChat's smollm module as a reference, build a minimal working prototype, and iterate based on user feedback and performance testing.

**Status**: 🟡 **Planning Complete - Ready for Implementation**
