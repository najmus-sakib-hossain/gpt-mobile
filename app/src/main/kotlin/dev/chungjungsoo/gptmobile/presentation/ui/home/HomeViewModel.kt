package dev.chungjungsoo.gptmobile.presentation.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.chungjungsoo.gptmobile.data.database.entity.ChatRoom
import dev.chungjungsoo.gptmobile.data.dto.Platform
import dev.chungjungsoo.gptmobile.data.model.ApiType
import dev.chungjungsoo.gptmobile.data.repository.ChatRepository
import dev.chungjungsoo.gptmobile.data.repository.SettingRepository
import dev.chungjungsoo.gptmobile.util.ConnectivityUtil
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dev.chungjungsoo.gptmobile.data.dto.RainbowAnimationStyle

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val application: Application,
    private val chatRepository: ChatRepository,
    private val settingRepository: SettingRepository
) : ViewModel() {

    data class ChatListState(
        val chats: List<ChatRoom> = listOf(),
        val isSelectionMode: Boolean = false,
        val selected: List<Boolean> = listOf()
    )

    private val _chatListState = MutableStateFlow(ChatListState())
    val chatListState: StateFlow<ChatListState> = _chatListState.asStateFlow()

    private val _platformState = MutableStateFlow(listOf<Platform>())
    val platformState: StateFlow<List<Platform>> = _platformState.asStateFlow()

    private val _showSelectModelDialog = MutableStateFlow(false)
    val showSelectModelDialog: StateFlow<Boolean> = _showSelectModelDialog.asStateFlow()

    private val _showDeleteWarningDialog = MutableStateFlow(false)
    val showDeleteWarningDialog: StateFlow<Boolean> = _showDeleteWarningDialog.asStateFlow()

    private val _showBorderSettingsDialog = MutableStateFlow(false)
    val showBorderSettingsDialog: StateFlow<Boolean> = _showBorderSettingsDialog.asStateFlow()

    private val _borderSettings = MutableStateFlow(dev.chungjungsoo.gptmobile.data.dto.BorderSetting())
    val borderSettings: StateFlow<dev.chungjungsoo.gptmobile.data.dto.BorderSetting> = _borderSettings.asStateFlow()

    init {
        fetchBorderSettings()
    }

    fun updateCheckedState(platform: Platform) {
        val index = _platformState.value.indexOf(platform)

        if (index >= 0) {
            _platformState.update {
                it.mapIndexed { i, p ->
                    if (index == i) {
                        p.copy(selected = p.selected.not())
                    } else {
                        p
                    }
                }
            }
        }
    }

    fun openDeleteWarningDialog() {
        closeSelectModelDialog()
        _showDeleteWarningDialog.update { true }
    }

    fun closeDeleteWarningDialog() {
        _showDeleteWarningDialog.update { false }
    }

    fun openSelectModelDialog() {
        _showSelectModelDialog.update { true }
        disableSelectionMode()
    }

    fun closeSelectModelDialog() {
        _showSelectModelDialog.update { false }
    }

    fun deleteSelectedChats() {
        viewModelScope.launch {
            val selectedChats = _chatListState.value.chats.filterIndexed { index, _ ->
                _chatListState.value.selected[index]
            }

            chatRepository.deleteChats(selectedChats)
            _chatListState.update { it.copy(chats = chatRepository.fetchChatList()) }
            disableSelectionMode()
        }
    }

    fun disableSelectionMode() {
        _chatListState.update {
            it.copy(
                selected = List(it.chats.size) { false },
                isSelectionMode = false
            )
        }
    }

    fun enableSelectionMode() {
        _chatListState.update { it.copy(isSelectionMode = true) }
    }

    fun fetchChats() {
        viewModelScope.launch {
            val chats = chatRepository.fetchChatList()

            _chatListState.update {
                it.copy(
                    chats = chats,
                    selected = List(chats.size) { false },
                    isSelectionMode = false
                )
            }

            Log.d("chats", "${_chatListState.value.chats}")
        }
    }

    fun fetchPlatformStatus() {
        viewModelScope.launch {
            val platforms = settingRepository.fetchPlatforms()
            
            // Auto-select Offline AI if no internet connection
            val hasInternet = ConnectivityUtil.isNetworkAvailable(application)
            val updatedPlatforms = if (!hasInternet) {
                platforms.map { platform ->
                    if (platform.name == ApiType.OFFLINE_AI && platform.enabled) {
                        platform.copy(selected = true)
                    } else {
                        platform.copy(selected = false)
                    }
                }
            } else {
                platforms
            }
            
            _platformState.update { updatedPlatforms }
        }
    }
    
    fun updateOfflineModelPath(modelPath: String) {
        android.util.Log.d("HomeViewModel", "Updating offline model path to: $modelPath")
        viewModelScope.launch {
            // Update the model path for Offline AI
            val updatedPlatforms = _platformState.value.map { platform ->
                if (platform.name == ApiType.OFFLINE_AI) {
                    platform.copy(model = modelPath)
                } else {
                    platform
                }
            }
            _platformState.update { updatedPlatforms }
            
            // Save to repository
            settingRepository.updatePlatforms(updatedPlatforms)
            android.util.Log.d("HomeViewModel", "Platform settings saved with model path: $modelPath")
        }
    }

    fun selectChat(chatRoomIdx: Int) {
        if (chatRoomIdx < 0 || chatRoomIdx > _chatListState.value.chats.size) return

        _chatListState.update {
            it.copy(
                selected = it.selected.mapIndexed { index, b ->
                    if (index == chatRoomIdx) {
                        !b
                    } else {
                        b
                    }
                }
            )
        }

        if (_chatListState.value.selected.count { it } == 0) {
            disableSelectionMode()
        }
    }

    fun openBorderSettingsDialog() = _showBorderSettingsDialog.update { true }

    fun closeBorderSettingsDialog() = _showBorderSettingsDialog.update { false }

    fun updateBorderEnabled(enabled: Boolean) {
        _borderSettings.update { it.copy(enabled = enabled) }
    }

    fun updateBorderRadius(radius: Float) {
        _borderSettings.update { it.copy(borderRadius = radius) }
    }

    fun updateBorderWidth(width: Float) {
        _borderSettings.update { it.copy(borderWidth = width) }
    }

    fun updateBorderAnimationStyle(style: RainbowAnimationStyle) {
        _borderSettings.update { it.copy(animationStyle = style) }
    }

    fun saveBorderSettings() {
        viewModelScope.launch {
            settingRepository.updateBorderSettings(_borderSettings.value)
        }
    }

    private fun fetchBorderSettings() {
        viewModelScope.launch {
            val settings = settingRepository.fetchBorderSettings()
            _borderSettings.update { settings }
        }
    }
}
