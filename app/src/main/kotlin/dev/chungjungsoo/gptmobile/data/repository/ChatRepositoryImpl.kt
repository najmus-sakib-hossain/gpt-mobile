package dev.chungjungsoo.gptmobile.data.repository

import android.content.Context
import android.util.Log
import com.aallam.openai.api.chat.ChatCompletionChunk
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import dev.chungjungsoo.gptmobile.data.ModelConstants
import dev.chungjungsoo.gptmobile.data.database.dao.ChatRoomDao
import dev.chungjungsoo.gptmobile.data.database.dao.MessageDao
import dev.chungjungsoo.gptmobile.data.database.entity.ChatRoom
import dev.chungjungsoo.gptmobile.data.database.entity.Message
import dev.chungjungsoo.gptmobile.data.dto.ApiState
import dev.chungjungsoo.gptmobile.data.dto.anthropic.common.MessageRole
import dev.chungjungsoo.gptmobile.data.dto.anthropic.common.TextContent
import dev.chungjungsoo.gptmobile.data.dto.anthropic.request.InputMessage
import dev.chungjungsoo.gptmobile.data.dto.anthropic.request.MessageRequest
import dev.chungjungsoo.gptmobile.data.dto.anthropic.response.ContentDeltaResponseChunk
import dev.chungjungsoo.gptmobile.data.dto.anthropic.response.ErrorResponseChunk
import dev.chungjungsoo.gptmobile.data.dto.anthropic.response.MessageResponseChunk
import dev.chungjungsoo.gptmobile.data.model.ApiType
import dev.chungjungsoo.gptmobile.data.network.AnthropicAPI
import dev.chungjungsoo.gptmobile.data.service.LLMService
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

class ChatRepositoryImpl
@Inject
constructor(
        private val appContext: Context,
        private val chatRoomDao: ChatRoomDao,
        private val messageDao: MessageDao,
        private val settingRepository: SettingRepository,
        private val anthropic: AnthropicAPI,
        private val llmService: LLMService
) : ChatRepository {

    private lateinit var openAI: OpenAI
    private lateinit var google: GenerativeModel
    private lateinit var ollama: OpenAI
    private lateinit var groq: OpenAI

    override suspend fun completeOpenAIChat(
            question: Message,
            history: List<Message>
    ): Flow<ApiState> {
        val platform =
                checkNotNull(
                        settingRepository.fetchPlatforms().firstOrNull { it.name == ApiType.OPENAI }
                )
        openAI = OpenAI(platform.token ?: "", host = OpenAIHost(baseUrl = platform.apiUrl))

        val generatedMessages =
                messageToOpenAICompatibleMessage(ApiType.OPENAI, history + listOf(question))
        val generatedMessageWithPrompt =
                listOf(
                        ChatMessage(
                                role = ChatRole.System,
                                content = platform.systemPrompt ?: ModelConstants.OPENAI_PROMPT
                        )
                ) + generatedMessages
        val chatCompletionRequest =
                ChatCompletionRequest(
                        model = ModelId(platform.model ?: ""),
                        messages = generatedMessageWithPrompt,
                        temperature = platform.temperature?.toDouble(),
                        topP = platform.topP?.toDouble()
                )

        return openAI.chatCompletions(chatCompletionRequest)
                .map<ChatCompletionChunk, ApiState> { chunk ->
                    ApiState.Success(chunk.choices.getOrNull(0)?.delta?.content ?: "")
                }
                .catch { throwable -> emit(ApiState.Error(throwable.message ?: "Unknown error")) }
                .onStart { emit(ApiState.Loading) }
                .onCompletion { emit(ApiState.Done) }
    }

    override suspend fun completeAnthropicChat(
            question: Message,
            history: List<Message>
    ): Flow<ApiState> {
        val platform =
                checkNotNull(
                        settingRepository.fetchPlatforms().firstOrNull {
                            it.name == ApiType.ANTHROPIC
                        }
                )
        anthropic.setToken(platform.token)
        anthropic.setAPIUrl(platform.apiUrl)

        val generatedMessages = messageToAnthropicMessage(history + listOf(question))
        val messageRequest =
                MessageRequest(
                        model = platform.model ?: "",
                        messages = generatedMessages,
                        maxTokens = ModelConstants.ANTHROPIC_MAXIMUM_TOKEN,
                        systemPrompt = platform.systemPrompt ?: ModelConstants.DEFAULT_PROMPT,
                        stream = true,
                        temperature = platform.temperature,
                        topP = platform.topP
                )

        return anthropic
                .streamChatMessage(messageRequest)
                .map<MessageResponseChunk, ApiState> { chunk ->
                    when (chunk) {
                        is ContentDeltaResponseChunk -> ApiState.Success(chunk.delta.text)
                        is ErrorResponseChunk -> throw Error(chunk.error.message)
                        else -> ApiState.Success("")
                    }
                }
                .catch { throwable -> emit(ApiState.Error(throwable.message ?: "Unknown error")) }
                .onStart { emit(ApiState.Loading) }
                .onCompletion { emit(ApiState.Done) }
    }

    override suspend fun completeGoogleChat(
            question: Message,
            history: List<Message>
    ): Flow<ApiState> {
        val platform =
                checkNotNull(
                        settingRepository.fetchPlatforms().firstOrNull { it.name == ApiType.GOOGLE }
                )
        val config = generationConfig {
            temperature = platform.temperature
            topP = platform.topP
        }
        // Only Gemini models support system instruction, not Gemma models
        val supportsSystemInstruction =
                platform.model?.startsWith("gemini", ignoreCase = true) == true
        val systemInstruction =
                if (supportsSystemInstruction)
                        content { text(platform.systemPrompt ?: ModelConstants.DEFAULT_PROMPT) }
                else null
        google =
                GenerativeModel(
                        modelName = platform.model ?: "",
                        apiKey = platform.token ?: "",
                        systemInstruction = systemInstruction,
                        generationConfig = config,
                        safetySettings =
                                listOf(
                                        SafetySetting(
                                                HarmCategory.DANGEROUS_CONTENT,
                                                BlockThreshold.ONLY_HIGH
                                        ),
                                        SafetySetting(
                                                HarmCategory.SEXUALLY_EXPLICIT,
                                                BlockThreshold.NONE
                                        )
                                )
                )

        val inputContent = messageToGoogleMessage(history)
        val chat = google.startChat(history = inputContent)

        return chat.sendMessageStream(question.content)
            .map<GenerateContentResponse, ApiState> { response ->
                try {
                    ApiState.Success(response.text ?: "")
                } catch (e: Exception) {
                    // Silently skip chunks that can't be processed
                    ApiState.Success("")
                }
            }
            .catch { throwable ->
                // Silently log errors but don't emit error state
                Log.d("ChatRepository", "Stream error (ignored): ${throwable.message}")
                // Don't emit error - just continue
            }
            .onStart { emit(ApiState.Loading) }
            .onCompletion { 
                // Always emit Done regardless of any errors
                emit(ApiState.Done)
            }
    }

    override suspend fun completeGroqChat(
            question: Message,
            history: List<Message>
    ): Flow<ApiState> {
        val platform =
                checkNotNull(
                        settingRepository.fetchPlatforms().firstOrNull { it.name == ApiType.GROQ }
                )
        groq = OpenAI(platform.token ?: "", host = OpenAIHost(baseUrl = platform.apiUrl))

        val generatedMessages =
                messageToOpenAICompatibleMessage(ApiType.GROQ, history + listOf(question))
        val generatedMessageWithPrompt =
                listOf(
                        ChatMessage(
                                role = ChatRole.System,
                                content = platform.systemPrompt ?: ModelConstants.DEFAULT_PROMPT
                        )
                ) + generatedMessages
        val chatCompletionRequest =
                ChatCompletionRequest(
                        model = ModelId(platform.model ?: ""),
                        messages = generatedMessageWithPrompt,
                        temperature = platform.temperature?.toDouble(),
                        topP = platform.topP?.toDouble()
                )

        return groq.chatCompletions(chatCompletionRequest)
                .map<ChatCompletionChunk, ApiState> { chunk ->
                    ApiState.Success(chunk.choices.getOrNull(0)?.delta?.content ?: "")
                }
                .catch { throwable -> emit(ApiState.Error(throwable.message ?: "Unknown error")) }
                .onStart { emit(ApiState.Loading) }
                .onCompletion { emit(ApiState.Done) }
    }

    override suspend fun completeOllamaChat(
            question: Message,
            history: List<Message>
    ): Flow<ApiState> {
        val platform =
                checkNotNull(
                        settingRepository.fetchPlatforms().firstOrNull { it.name == ApiType.OLLAMA }
                )
        ollama = OpenAI(platform.token ?: "", host = OpenAIHost(baseUrl = "${platform.apiUrl}v1/"))

        val generatedMessages =
                messageToOpenAICompatibleMessage(ApiType.OLLAMA, history + listOf(question))
        val generatedMessageWithPrompt =
                listOf(
                        ChatMessage(
                                role = ChatRole.System,
                                content = platform.systemPrompt ?: ModelConstants.DEFAULT_PROMPT
                        )
                ) + generatedMessages
        val chatCompletionRequest =
                ChatCompletionRequest(
                        model = ModelId(platform.model ?: ""),
                        messages = generatedMessageWithPrompt,
                        temperature = platform.temperature?.toDouble(),
                        topP = platform.topP?.toDouble()
                )

        return ollama.chatCompletions(chatCompletionRequest)
                .map<ChatCompletionChunk, ApiState> { chunk ->
                    ApiState.Success(chunk.choices.getOrNull(0)?.delta?.content ?: "")
                }
                .catch { throwable -> emit(ApiState.Error(throwable.message ?: "Unknown error")) }
                .onStart { emit(ApiState.Loading) }
                .onCompletion { emit(ApiState.Done) }
    }

    override suspend fun completeOfflineAIChat(
            question: Message,
            history: List<Message>
    ): Flow<ApiState> {
        val platform =
                checkNotNull(
                        settingRepository.fetchPlatforms().firstOrNull { it.name == ApiType.OFFLINE_AI }
                )
        
        // Get model path from platform.model (should be the downloaded file path)
        val modelPath = platform.model ?: throw IllegalStateException("No offline model selected")
        
        Log.d("ChatRepositoryImpl", "Attempting to use offline model at: $modelPath")
        
        // Convert content URI to file path if needed (cached, only copies once)
        val actualFilePath = dev.chungjungsoo.gptmobile.util.FileUtil.ensureModelFileExists(appContext, modelPath)
        if (actualFilePath == null) {
            throw IllegalStateException("Failed to access offline model from: $modelPath. The file may not exist or cannot be read.")
        }
        
        Log.d("ChatRepositoryImpl", "Using model file at: $actualFilePath")
        
        // Load model if not already loaded - includes system prompt
        if (!llmService.isModelLoaded() || llmService.getLoadedModelPath() != actualFilePath) {
            val systemPrompt = platform.systemPrompt ?: ModelConstants.DEFAULT_PROMPT
            Log.d("ChatRepositoryImpl", "Loading model from: $actualFilePath")
            val loadSuccess = llmService.loadModel(
                modelPath = actualFilePath, 
                contextSize = 4096,
                systemPrompt = systemPrompt
            )
            if (!loadSuccess) {
                throw IllegalStateException("Failed to load offline model from: $actualFilePath. Please verify the file is a valid GGUF model.")
            }
            Log.d("ChatRepositoryImpl", "Model loaded successfully")
        } else {
            Log.d("ChatRepositoryImpl", "Model already loaded, reusing")
        }
        
        // Add chat history before generating (SmolLM maintains context)
        // Only add the NEW history that hasn't been added yet
        Log.d("ChatRepositoryImpl", "Adding chat history (${history.size} messages)")
        history.forEach { message ->
            when (message.platformType) {
                null -> {
                    // User message
                    llmService.addUserMessage(message.content)
                }
                ApiType.OFFLINE_AI -> {
                    // Assistant message (previous responses)
                    llmService.addAssistantMessage(message.content)
                }
                else -> {
                    // Ignore messages from other platforms
                }
            }
        }
        
        // Generate response using SmolLM's chat API
        Log.d("ChatRepositoryImpl", "Generating response for new question")
        return llmService.getResponse(question.content)
                .map<String, ApiState> { token -> ApiState.Success(token) }
                .catch { throwable -> emit(ApiState.Error(throwable.message ?: "Unknown error")) }
                .onStart { emit(ApiState.Loading) }
                .onCompletion { emit(ApiState.Done) }
    }
    
    override suspend fun preloadOfflineAIModel() {
        try {
            val platform = settingRepository.fetchPlatforms().firstOrNull { it.name == ApiType.OFFLINE_AI }
            if (platform == null) {
                Log.d("ChatRepositoryImpl", "Offline AI platform not configured")
                return
            }
            
            val modelPath = platform.model
            if (modelPath.isNullOrBlank()) {
                Log.d("ChatRepositoryImpl", "No offline model selected")
                return
            }
            
            Log.d("ChatRepositoryImpl", "Pre-loading offline model from: $modelPath")
            
            // Convert content URI to file path if needed (this will be cached)
            val actualFilePath = dev.chungjungsoo.gptmobile.util.FileUtil.ensureModelFileExists(appContext, modelPath)
            if (actualFilePath == null) {
                Log.e("ChatRepositoryImpl", "Failed to access offline model for preloading")
                return
            }
            
            // Load model if not already loaded
            if (!llmService.isModelLoaded() || llmService.getLoadedModelPath() != actualFilePath) {
                val systemPrompt = platform.systemPrompt ?: ModelConstants.DEFAULT_PROMPT
                Log.d("ChatRepositoryImpl", "Pre-loading model...")
                val loadSuccess = llmService.loadModel(
                    modelPath = actualFilePath,
                    contextSize = 4096,
                    systemPrompt = systemPrompt
                )
                if (loadSuccess) {
                    Log.d("ChatRepositoryImpl", "Model pre-loaded successfully! Ready for instant inference.")
                } else {
                    Log.e("ChatRepositoryImpl", "Failed to pre-load model")
                }
            } else {
                Log.d("ChatRepositoryImpl", "Model already loaded")
            }
        } catch (e: Exception) {
            Log.e("ChatRepositoryImpl", "Error pre-loading model", e)
        }
    }

    override suspend fun fetchChatList(): List<ChatRoom> = chatRoomDao.getChatRooms()

    override suspend fun fetchMessages(chatId: Int): List<Message> = messageDao.loadMessages(chatId)

    override fun generateDefaultChatTitle(messages: List<Message>): String? =
            messages
                    .sortedBy { it.createdAt }
                    .firstOrNull { it.platformType == null }
                    ?.content
                    ?.replace('\n', ' ')
                    ?.take(50)

    override suspend fun updateChatTitle(chatRoom: ChatRoom, title: String) {
        chatRoomDao.editChatRoom(chatRoom.copy(title = title.replace('\n', ' ').take(50)))
    }

    override suspend fun saveChat(chatRoom: ChatRoom, messages: List<Message>): ChatRoom {
        if (chatRoom.id == 0) {
            // New Chat
            val chatId = chatRoomDao.addChatRoom(chatRoom)
            val updatedMessages = messages.map { it.copy(chatId = chatId.toInt()) }
            messageDao.addMessages(*updatedMessages.toTypedArray())

            val savedChatRoom = chatRoom.copy(id = chatId.toInt())
            updateChatTitle(savedChatRoom, updatedMessages[0].content)

            return savedChatRoom.copy(
                    title = updatedMessages[0].content.replace('\n', ' ').take(50)
            )
        }

        val savedMessages = fetchMessages(chatRoom.id)
        val updatedMessages = messages.map { it.copy(chatId = chatRoom.id) }

        val shouldBeDeleted =
                savedMessages.filter { m -> updatedMessages.firstOrNull { it.id == m.id } == null }
        val shouldBeUpdated =
                updatedMessages.filter { m ->
                    savedMessages.firstOrNull { it.id == m.id && it != m } != null
                }
        val shouldBeAdded =
                updatedMessages.filter { m -> savedMessages.firstOrNull { it.id == m.id } == null }

        chatRoomDao.editChatRoom(chatRoom)
        messageDao.deleteMessages(*shouldBeDeleted.toTypedArray())
        messageDao.editMessages(*shouldBeUpdated.toTypedArray())
        messageDao.addMessages(*shouldBeAdded.toTypedArray())

        return chatRoom
    }

    override suspend fun deleteChats(chatRooms: List<ChatRoom>) {
        chatRoomDao.deleteChatRooms(*chatRooms.toTypedArray())
    }

    private fun messageToOpenAICompatibleMessage(
            apiType: ApiType,
            messages: List<Message>
    ): List<ChatMessage> {
        val result = mutableListOf<ChatMessage>()

        messages.forEach { message ->
            when (message.platformType) {
                null -> {
                    result.add(ChatMessage(role = ChatRole.User, content = message.content))
                }
                apiType -> {
                    result.add(ChatMessage(role = ChatRole.Assistant, content = message.content))
                }
                else -> {}
            }
        }

        return result
    }

    private fun messageToAnthropicMessage(messages: List<Message>): List<InputMessage> {
        val result = mutableListOf<InputMessage>()

        messages.forEach { message ->
            when (message.platformType) {
                null ->
                        result.add(
                                InputMessage(
                                        role = MessageRole.USER,
                                        content = listOf(TextContent(text = message.content))
                                )
                        )
                ApiType.ANTHROPIC ->
                        result.add(
                                InputMessage(
                                        role = MessageRole.ASSISTANT,
                                        content = listOf(TextContent(text = message.content))
                                )
                        )
                else -> {}
            }
        }

        return result
    }

    private fun messageToGoogleMessage(messages: List<Message>): List<Content> {
        val result = mutableListOf<Content>()

        messages.forEach { message ->
            when (message.platformType) {
                null -> result.add(content(role = "user") { text(message.content) })
                ApiType.GOOGLE -> result.add(content(role = "model") { text(message.content) })
                else -> {}
            }
        }

        return result
    }
}
