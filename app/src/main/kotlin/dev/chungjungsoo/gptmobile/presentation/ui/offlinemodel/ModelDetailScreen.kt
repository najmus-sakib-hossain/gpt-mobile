package dev.chungjungsoo.gptmobile.presentation.ui.offlinemodel

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chungjungsoo.gptmobile.data.dto.HFModelFile
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelDetailScreen(
    modelId: String,
    onBackClick: () -> Unit,
    viewModel: OfflineModelViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDownloadDialog by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<HFModelFile?>(null) }

    LaunchedEffect(modelId) {
        viewModel.loadModelDetails(modelId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Model Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = "https://huggingface.co/$modelId".toUri()
                        }
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.OpenInBrowser, "Open in Browser")
                    }
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "https://huggingface.co/$modelId")
                        }
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.Share, "Share")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    uiState.selectedModel?.let { model ->
                        // Model info card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = model.modelId,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Download,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${model.downloads}")
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Favorite,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${model.likes}")
                                    }
                                }
                            }
                        }

                        // Files section
                        Text(
                            text = "Available GGUF Files",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        LazyColumn {
                            items(uiState.selectedModelFiles) { file ->
                                ModelFileItem(
                                    file = file,
                                    onClick = {
                                        selectedFile = file
                                        showDownloadDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (showDownloadDialog && selectedFile != null) {
                AlertDialog(
                    onDismissRequest = { showDownloadDialog = false },
                    title = { Text("Download Model") },
                    text = {
                        Text(
                            "The model will start downloading and will be stored in the Downloads folder. " +
                            "You can then load it in the app.\n\n" +
                            "File: ${selectedFile?.path}\n" +
                            "Size: ${formatFileSize(selectedFile?.size ?: 0)}"
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                selectedFile?.let { file ->
                                    val downloadUrl = "https://huggingface.co/$modelId/resolve/main/${file.path}"
                                    viewModel.downloadModel(modelId, file.path, downloadUrl)
                                }
                                showDownloadDialog = false
                                onBackClick()
                            }
                        ) {
                            Text("Download")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDownloadDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ModelFileItem(
    file: HFModelFile,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.path,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatFileSize(file.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Icon(
                Icons.Default.Download,
                contentDescription = "Download",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
    }
}

private fun formatFileSize(bytes: Long): String {
    val df = DecimalFormat("#.##")
    return when {
        bytes >= 1_073_741_824 -> "${df.format(bytes / 1_073_741_824.0)} GB"
        bytes >= 1_048_576 -> "${df.format(bytes / 1_048_576.0)} MB"
        bytes >= 1_024 -> "${df.format(bytes / 1_024.0)} KB"
        else -> "$bytes B"
    }
}
