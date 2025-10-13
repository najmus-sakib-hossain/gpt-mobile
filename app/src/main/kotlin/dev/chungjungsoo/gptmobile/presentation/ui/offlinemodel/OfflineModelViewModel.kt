package dev.chungjungsoo.gptmobile.presentation.ui.offlinemodel

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.chungjungsoo.gptmobile.data.database.entity.OfflineModel
import dev.chungjungsoo.gptmobile.data.dto.HFModelSearchResult
import dev.chungjungsoo.gptmobile.data.dto.HFModelInfo
import dev.chungjungsoo.gptmobile.data.dto.HFModelFile
import dev.chungjungsoo.gptmobile.data.repository.OfflineModelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ModelDownloadState(
    val isLoading: Boolean = false,
    val searchResults: List<HFModelSearchResult> = emptyList(),
    val selectedModel: HFModelInfo? = null,
    val selectedModelFiles: List<HFModelFile> = emptyList(),
    val downloadedModels: List<OfflineModel> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class OfflineModelViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: OfflineModelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModelDownloadState())
    val uiState: StateFlow<ModelDownloadState> = _uiState.asStateFlow()

    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    init {
        loadDownloadedModels()
        loadTrendingModels()
    }

    fun loadDownloadedModels() {
        viewModelScope.launch {
            repository.getAllModels().collect { models ->
                _uiState.value = _uiState.value.copy(downloadedModels = models)
            }
        }
    }

    fun loadTrendingModels() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Load trending small GGUF models optimized for mobile
                val results = repository.searchModels(
                    query = "chat",
                    filter = "gguf",
                    limit = 30,
                    sort = "downloads"
                )
                _uiState.value = _uiState.value.copy(
                    searchResults = results,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load trending models"
                )
            }
        }
    }

    fun searchModels(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val results = repository.searchModels(query)
                _uiState.value = _uiState.value.copy(
                    searchResults = results,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to search models"
                )
            }
        }
    }

    fun loadModelDetails(modelId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val modelInfo = repository.getModelInfo(modelId)
                val modelFiles = repository.getModelFiles(modelId)
                _uiState.value = _uiState.value.copy(
                    selectedModel = modelInfo,
                    selectedModelFiles = modelFiles,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load model details"
                )
            }
        }
    }

    fun downloadModel(modelId: String, fileName: String, downloadUrl: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(downloadUrl))
                .setTitle(fileName)
                .setDescription("Downloading offline AI model for Friday")
                .setMimeType("application/octet-stream")
                .setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
                )
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            downloadManager.enqueue(request)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = e.message ?: "Failed to start download"
            )
        }
    }

    fun addDownloadedModel(
        modelName: String,
        modelId: String,
        filePath: String,
        fileSize: Long
    ) {
        viewModelScope.launch {
            try {
                val model = OfflineModel(
                    modelName = modelName,
                    modelId = modelId,
                    filePath = filePath,
                    fileSize = fileSize,
                    contextSize = 2048,
                    chatTemplate = "",
                    downloadDate = System.currentTimeMillis(),
                    isDownloaded = true
                )
                repository.insertModel(model)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to add model"
                )
            }
        }
    }

    fun deleteModel(model: OfflineModel) {
        viewModelScope.launch {
            try {
                repository.deleteModel(model)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete model"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSelectedModel() {
        _uiState.value = _uiState.value.copy(
            selectedModel = null,
            selectedModelFiles = emptyList()
        )
    }
}
