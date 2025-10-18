package dev.chungjungsoo.gptmobile.presentation.ui.home

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.chungjungsoo.gptmobile.R
import dev.chungjungsoo.gptmobile.data.database.entity.ChatRoom
import dev.chungjungsoo.gptmobile.data.dto.Platform
import dev.chungjungsoo.gptmobile.data.dto.exampleModelsList
import dev.chungjungsoo.gptmobile.data.model.ApiType
import dev.chungjungsoo.gptmobile.presentation.common.GeneratingSkeleton
import dev.chungjungsoo.gptmobile.presentation.common.PlatformCheckBoxItem
import dev.chungjungsoo.gptmobile.presentation.icons.SolarIcons
import dev.chungjungsoo.gptmobile.presentation.ui.offlinemodel.OfflineModelViewModel
import dev.chungjungsoo.gptmobile.util.getPlatformTitleResources
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    settingOnClick: () -> Unit,
    onExistingChatClick: (ChatRoom) -> Unit,
    navigateToNewChat: (enabledPlatforms: List<ApiType>) -> Unit,
    drawerState: DrawerState,
    scope: CoroutineScope,
    navController: NavHostController = rememberNavController() // Add navController if not already passed
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val chatListState by homeViewModel.chatListState.collectAsStateWithLifecycle()
    val showSelectModelDialog by homeViewModel.showSelectModelDialog.collectAsStateWithLifecycle()
    val showDeleteWarningDialog by homeViewModel.showDeleteWarningDialog.collectAsStateWithLifecycle()
    val platformState by homeViewModel.platformState.collectAsStateWithLifecycle()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Input bar state
    var chatInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    
    // File picker for Offline AI model selection
    val offlineModelPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                // Get persistent permission for the file
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                
                // Show loading toast
                Toast.makeText(
                    context,
                    context.getString(R.string.copying_model_file),
                    Toast.LENGTH_SHORT
                ).show()
                
                // Copy file to internal storage in background
                CoroutineScope(Dispatchers.IO).launch {
                    android.util.Log.d("HomeScreen", "Starting to copy model file from URI: $it")
                    val fileName = dev.chungjungsoo.gptmobile.util.FileUtil.getFileNameFromUri(context, it)
                    android.util.Log.d("HomeScreen", "Extracted file name: $fileName")
                    val filePath = dev.chungjungsoo.gptmobile.util.FileUtil.copyUriToInternalStorage(context, it, fileName)
                    
                    withContext(Dispatchers.Main) {
                        if (filePath != null) {
                            android.util.Log.d("HomeScreen", "File copied successfully to: $filePath")
                            // Update the model path with the actual file path
                            homeViewModel.updateOfflineModelPath(filePath)
                            
                            Toast.makeText(
                                context,
                                context.getString(R.string.model_file_selected),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            android.util.Log.e("HomeScreen", "Failed to copy file from URI: $it")
                            Toast.makeText(
                                context,
                                context.getString(R.string.error_copying_model_file),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeScreen", "Exception in file picker callback", e)
                Toast.makeText(
                    context,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    LaunchedEffect(lifecycleState) {
        if (lifecycleState == Lifecycle.State.RESUMED && !chatListState.isSelectionMode) {
            homeViewModel.fetchChats()
            homeViewModel.fetchPlatformStatus()
        }
    }

    BackHandler(enabled = chatListState.isSelectionMode) {
        homeViewModel.disableSelectionMode()
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            HomeTopAppBar(
                chatListState.isSelectionMode,
                selectedChats = chatListState.selected.count { it },
                scrollBehavior,
                actionOnClick = {
                    if (chatListState.isSelectionMode) {
                        homeViewModel.openDeleteWarningDialog()
                    } else {
                        settingOnClick()
                    }
                },
                navigationOnClick = {
                    homeViewModel.disableSelectionMode()
                },
                onMenuClick = { scope.launch { drawerState.open() } }
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Main content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    HomeContent(
                        navController = navController,
                        chatListState = chatListState,
                        onExistingChatClick = onExistingChatClick,
                        homeViewModel = homeViewModel,
                        listState = listState
                    )
                }
                
                // Animated Input Bar / FAB
                val isAtBottom = rememberIsAtBottom(listState)
                AnimatedInputFab(
                    isAtBottom = isAtBottom,
                    textInput = chatInput,
                    onTextChange = { chatInput = it },
                    currentProvider = platformState.firstOrNull { it.enabled }?.name ?: ApiType.OPENAI,
                    onProviderClick = { homeViewModel.openSelectModelDialog() },
                    onSendMessage = {
                        if (chatInput.isNotEmpty()) {
                            // Handle send message
                            val enabledApiTypes = platformState.filter { it.enabled }.map { it.name }
                            if (enabledApiTypes.isNotEmpty()) {
                                navigateToNewChat(enabledApiTypes)
                            }
                            chatInput = ""
                        }
                    },
                    onVoiceMemo = {
                        Toast.makeText(context, "Voice memo feature coming soon", Toast.LENGTH_SHORT).show()
                    },
                    onLiveAI = {
                        Toast.makeText(context, "Live AI feature coming soon", Toast.LENGTH_SHORT).show()
                    },
                    onNewTextPrompt = {
                        val enabledApiTypes = platformState.filter { it.enabled }.map { it.name }
                        if (enabledApiTypes.size == 1) {
                            navigateToNewChat(enabledApiTypes)
                        } else {
                            homeViewModel.openSelectModelDialog()
                        }
                    },
                    onAddMedia = {
                        Toast.makeText(context, "Add media feature coming soon", Toast.LENGTH_SHORT).show()
                    },
                    onMentionClick = {
                        Toast.makeText(context, "Mention feature coming soon", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    )

    if (showSelectModelDialog) {
        SelectPlatformDialog(
            platformState,
            onDismissRequest = { homeViewModel.closeSelectModelDialog() },
            onConfirmation = { selectedPlatforms ->
                // Validate Offline AI has model selected
                val offlineAISelected = selectedPlatforms.contains(ApiType.OFFLINE_AI)
                val offlineAIPlatform = platformState.find { it.name == ApiType.OFFLINE_AI }
                
                if (offlineAISelected && offlineAIPlatform?.model.isNullOrBlank()) {
                    // Show error toast - need to select model first
                    Toast.makeText(
                        context,
                        context.getString(R.string.please_select_offline_model),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    homeViewModel.closeSelectModelDialog()
                    navigateToNewChat(selectedPlatforms)
                }
            },
            onPlatformSelect = { homeViewModel.updateCheckedState(it) },
            onOfflineModelPick = {
                // Launch file picker for GGUF files
                offlineModelPicker.launch(arrayOf("*/*"))
            }
        )
    }

    if (showDeleteWarningDialog) {
        DeleteWarningDialog(
            onDismissRequest = homeViewModel::closeDeleteWarningDialog,
            onConfirm = {
                val deletedChatRoomCount = chatListState.selected.count { it }
                homeViewModel.deleteSelectedChats()
                Toast.makeText(context, context.getString(R.string.deleted_chats, deletedChatRoomCount), Toast.LENGTH_SHORT).show()
                homeViewModel.closeDeleteWarningDialog()
            }
        )
    }

    val showBorderSettingsDialog by homeViewModel.showBorderSettingsDialog.collectAsStateWithLifecycle()
    val borderSettings by homeViewModel.borderSettings.collectAsStateWithLifecycle()

    if (showBorderSettingsDialog) {
        BorderSettingsDialog(
            borderSettings = borderSettings,
            onDismissRequest = homeViewModel::closeBorderSettingsDialog,
            onEnabledChange = homeViewModel::updateBorderEnabled,
            onRadiusChange = homeViewModel::updateBorderRadius,
            onWidthChange = homeViewModel::updateBorderWidth,
            onAnimationStyleChange = homeViewModel::updateBorderAnimationStyle,
            onSave = {
                homeViewModel.saveBorderSettings()
                homeViewModel.closeBorderSettingsDialog()
            }
        )
    }
}

@Composable
fun HomeContent(
    navController: NavHostController,
    chatListState: HomeViewModel.ChatListState,
    onExistingChatClick: (ChatRoom) -> Unit,
    homeViewModel: HomeViewModel,
    listState: LazyListState = rememberLazyListState()
) {
    val offlineModelViewModel: OfflineModelViewModel = hiltViewModel()
    val offlineModelsState by offlineModelViewModel.uiState.collectAsStateWithLifecycle()
    val borderSettings by homeViewModel.borderSettings.collectAsStateWithLifecycle()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Generation Rainbow Glow Showcase
        item {
            GenerationRainbowGlowSection()
        }


        // Border Settings Section - Top Priority
        item {
            BorderSettingsCard(
                borderSettings = borderSettings,
                onEnabledChange = homeViewModel::updateBorderEnabled,
                onRadiusChange = homeViewModel::updateBorderRadius,
                onWidthChange = homeViewModel::updateBorderWidth,
                onAnimationStyleChange = homeViewModel::updateBorderAnimationStyle,
                onSave = homeViewModel::saveBorderSettings
            )
        }

        // Offline AI Section
        item {
            OfflineAISection(
                downloadedModels = offlineModelsState.downloadedModels,
                onBrowseModelsClick = {
                    navController.navigate("offlineModelBrowser")
                },
                onModelClick = { model ->
                    // Handle model selection
                }
            )
        }

        // Example Models Section
        item {
            ExampleModelsSection(
                onDownloadClick = { modelId ->
                    navController.navigate("offlineModelBrowser")
                },
                onModelDetailsClick = { modelId ->
                    // Navigate to model detail page
                    val encodedModelId = java.net.URLEncoder.encode(modelId, "UTF-8")
                    navController.navigate("offlineModelDetail/$encodedModelId")
                }
            )
        }

        // Recent Chats Section
        if (chatListState.chats.isNotEmpty()) {
            item {
                Text(
                    text = "Recent Chats",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(chatListState.chats.take(5)) { chatRoom ->
                ChatRoomCard(
                    chatRoom = chatRoom,
                    onClick = { onExistingChatClick(chatRoom) }
                )
            }
        }
    }
}

@Composable
fun BorderSettingsCard(
    borderSettings: dev.chungjungsoo.gptmobile.data.dto.BorderSetting,
    onEnabledChange: (Boolean) -> Unit,
    onRadiusChange: (Float) -> Unit,
    onWidthChange: (Float) -> Unit,
    onAnimationStyleChange: (dev.chungjungsoo.gptmobile.data.dto.RainbowAnimationStyle) -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🌈",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Rainbow Border",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                androidx.compose.material3.Switch(
                    checked = borderSettings.enabled,
                    onCheckedChange = {
                        onEnabledChange(it)
                        onSave()
                    },
                    colors = androidx.compose.material3.SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Corner Radius Slider
            Text(
                text = "Corner Radius: ${borderSettings.borderRadius.toInt()}dp",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            androidx.compose.material3.Slider(
                value = borderSettings.borderRadius,
                onValueChange = onRadiusChange,
                onValueChangeFinished = onSave,
                valueRange = 0f..64f,
                steps = 63,
                enabled = borderSettings.enabled,
                modifier = Modifier.padding(vertical = 4.dp),
                colors = androidx.compose.material3.SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Border Width Slider
            Text(
                text = "Border Width: ${borderSettings.borderWidth.toInt()}dp",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            androidx.compose.material3.Slider(
                value = borderSettings.borderWidth,
                onValueChange = onWidthChange,
                onValueChangeFinished = onSave,
                valueRange = 1f..16f,
                steps = 14,
                enabled = borderSettings.enabled,
                modifier = Modifier.padding(vertical = 4.dp),
                colors = androidx.compose.material3.SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Animation Style",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            val animationOptions = listOf(
                dev.chungjungsoo.gptmobile.data.dto.RainbowAnimationStyle.CONTINUOUS_SWEEP to Pair(
                    "Continuous Orbit",
                    "Classic rainbow sweep circling every edge."
                ),
                dev.chungjungsoo.gptmobile.data.dto.RainbowAnimationStyle.TOP_RIGHT_BOUNCE to Pair(
                    "Volume Button Bounce",
                    "Glow launches from the upper-right, bouncing like a volume press."
                ),
                dev.chungjungsoo.gptmobile.data.dto.RainbowAnimationStyle.BOTTOM_CENTER_REVEAL to Pair(
                    "Bottom Lift Reveal",
                    "Border rises from the bottom center and wraps the screen."
                )
            )

            animationOptions.forEach { (style, info) ->
                val selected = borderSettings.animationStyle == style
                val optionModifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .then(
                        if (borderSettings.enabled) {
                            Modifier.clickable {
                                onAnimationStyleChange(style)
                                onSave()
                            }
                        } else {
                            Modifier
                        }
                    )

                androidx.compose.material3.Surface(
                    modifier = optionModifier,
                    shape = MaterialTheme.shapes.medium,
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.05f)
                    },
                    tonalElevation = if (selected) 4.dp else 0.dp,
                    shadowElevation = if (selected) 2.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = selected,
                            onClick = null,
                            enabled = borderSettings.enabled
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = info.first,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = info.second,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Preview bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val colors = listOf(
                        Color(0xFFFF0000), Color(0xFFFF7F00), Color(0xFFFFFF00),
                        Color(0xFF00FF00), Color(0xFF0000FF), Color(0xFF4B0082),
                        Color(0xFF9400D3), Color(0xFFFF0000)
                    )
                    val brush = Brush.horizontalGradient(colors = colors)
                    drawRoundRect(
                        brush = brush,
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                        alpha = if (borderSettings.enabled) 1f else 0.3f
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "✨ Animated glow effect around the entire screen",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun GenerationRainbowGlowSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "Generation Rainbow Glow",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        GeneratingSkeleton(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            contentPadding = 24.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Generating...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Rainbow glow animation preview for loading states.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun OfflineAISection(
    downloadedModels: List<dev.chungjungsoo.gptmobile.data.database.entity.OfflineModel>,
    onBrowseModelsClick: () -> Unit,
    onModelClick: (dev.chungjungsoo.gptmobile.data.database.entity.OfflineModel) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Face,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Offline AI Models",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (downloadedModels.isEmpty()) 
                            "No models downloaded yet" 
                        else 
                            "${downloadedModels.size} model${if (downloadedModels.size != 1) "s" else ""} ready",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (downloadedModels.isEmpty()) {
                // Empty state with prominent download button
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Chat without internet!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Download small AI models from HuggingFace",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onBrowseModelsClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download AI Models")
                    }
                }
            } else {
                // Show downloaded models
                downloadedModels.take(3).forEach { model ->
                    ModelListCard(
                        model = model,
                        onClick = { onModelClick(model) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Browse more button
                OutlinedButton(
                    onClick = onBrowseModelsClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download More Models")
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Default.Autorenew,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ModelListCard(
    model: dev.chungjungsoo.gptmobile.data.database.entity.OfflineModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Face,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = model.modelName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${formatFileSize(model.fileSize)} • ${model.contextSize} context",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Downloaded",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ExampleModelsSection(
    onDownloadClick: (String) -> Unit,
    onModelDetailsClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Quick Start Models",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Popular small models optimized for mobile",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            exampleModelsList.take(3).forEach { model ->
                ExampleModelCard(
                    model = model,
                    onDownloadClick = { onModelDetailsClick(model.modelId) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ExampleModelCard(
    model: dev.chungjungsoo.gptmobile.data.dto.ExampleModel,
    onDownloadClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = model.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = model.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 2
                )
                Text(
                    text = "Size: ${model.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            FilledTonalButton(
                onClick = onDownloadClick,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = "Download",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Download", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun ChatRoomCard(
    chatRoom: ChatRoom,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chatRoom.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Created ${formatTimestamp(chatRoom.createdAt * 1000)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1_073_741_824 -> String.format("%.1f GB", bytes / 1_073_741_824.0)
        bytes >= 1_048_576 -> String.format("%.1f MB", bytes / 1_048_576.0)
        bytes >= 1_024 -> String.format("%.1f KB", bytes / 1_024.0)
        else -> "$bytes B"
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 604_800_000 -> "${diff / 86_400_000}d ago"
        else -> "${diff / 604_800_000}w ago"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    isSelectionMode: Boolean,
    selectedChats: Int,
    scrollBehavior: TopAppBarScrollBehavior,
    actionOnClick: () -> Unit,
    navigationOnClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            scrolledContainerColor = if (isSelectionMode) MaterialTheme.colorScheme.primaryContainer else Color.Unspecified,
            containerColor = if (isSelectionMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background,
            titleContentColor = if (isSelectionMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onBackground
        ),
        title = {
            if (isSelectionMode) {
                Text(
                    modifier = Modifier.padding(4.dp),
                    text = stringResource(R.string.chats_selected, selectedChats),
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    modifier = Modifier.padding(4.dp),
                    text = "Friday",
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        navigationIcon = {
            if (isSelectionMode) {
                IconButton(
                    modifier = Modifier.padding(4.dp),
                    onClick = navigationOnClick
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        contentDescription = stringResource(R.string.close)
                    )
                }
            } else {
                IconButton(
                    modifier = Modifier.padding(4.dp),
                    onClick = onMenuClick
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_hamburger),
                        contentDescription = "Menu"
                    )
                }
            }
        },
        actions = {
            if (isSelectionMode) {
                IconButton(
                    modifier = Modifier.padding(4.dp),
                    onClick = actionOnClick
                ) {
                    Icon(
                        painter = painterResource(id = SolarIcons.TrashLine),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        contentDescription = stringResource(R.string.delete)
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatsTitle(scrollBehavior: TopAppBarScrollBehavior) {
    Text(
        modifier = Modifier
            .padding(top = 32.dp)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        text = stringResource(R.string.chats),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 1.0F - scrollBehavior.state.overlappedFraction),
        style = MaterialTheme.typography.headlineLarge
    )
}

@Composable
private fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

@Preview
@Composable
fun NewChatButton(
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
    onClick: () -> Unit = { }
) {
    val orientation = LocalConfiguration.current.orientation
    val fabModifier = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        modifier.systemBarsPadding()
    } else {
        modifier
    }
    ExtendedFloatingActionButton(
        modifier = fabModifier,
        onClick = { onClick() },
        expanded = expanded,
        icon = { Icon(Icons.Filled.Add, stringResource(R.string.new_chat)) },
        text = { Text(text = stringResource(R.string.new_chat)) }
    )
}

@Composable
fun SelectPlatformDialog(
    platforms: List<Platform>,
    onDismissRequest: () -> Unit,
    onConfirmation: (enabledPlatforms: List<ApiType>) -> Unit,
    onPlatformSelect: (Platform) -> Unit,
    onOfflineModelPick: () -> Unit = {}
) {
    val titles = getPlatformTitleResources()
    val configuration = LocalConfiguration.current

    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .widthIn(max = configuration.screenWidthDp.dp - 40.dp)
            .heightIn(max = configuration.screenHeightDp.dp - 80.dp),
        onDismissRequest = onDismissRequest,
        title = {
            Column {
                Text(
                    text = stringResource(R.string.select_platform),
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = stringResource(R.string.select_platform_description),
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        text = {
            HorizontalDivider()
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (platforms.any { it.enabled }) {
                    platforms.forEach { platform ->
                        Column {
                            val needsModelSelection = platform.name == ApiType.OFFLINE_AI && platform.model.isNullOrBlank()
                            val description = if (needsModelSelection) {
                                stringResource(R.string.model_not_selected_warning)
                            } else if (platform.name == ApiType.OFFLINE_AI && !platform.model.isNullOrBlank()) {
                                "Model: ${platform.model?.substringAfterLast("/") ?: ""}"
                            } else {
                                null
                            }
                            
                            PlatformCheckBoxItem(
                                platform = platform,
                                title = titles[platform.name]!!,
                                enabled = platform.enabled,
                                description = description,
                                onClickEvent = { onPlatformSelect(platform) }
                            )
                            
                            // Show model picker button for Offline AI
                            if (platform.name == ApiType.OFFLINE_AI && platform.enabled) {
                                androidx.compose.material3.OutlinedButton(
                                    onClick = onOfflineModelPick,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = SolarIcons.ShareLine),
                                        contentDescription = "Select Model",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                                    Text(
                                        text = if (platform.model.isNullOrBlank()) {
                                            stringResource(R.string.select_model_file)
                                        } else {
                                            stringResource(R.string.change_model_file)
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    EnablePlatformWarningText()
                }
                HorizontalDivider(Modifier.padding(top = 8.dp))
            }
        },
        confirmButton = {
            TextButton(
                enabled = platforms.any { it.selected },
                onClick = { onConfirmation(platforms.filter { it.selected }.map { it.name }) }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismissRequest() }
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Preview
@Composable
fun EnablePlatformWarningText() {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .wrapContentHeight(align = Alignment.CenterVertically)
            .padding(16.dp),
        textAlign = TextAlign.Center,
        text = stringResource(R.string.enable_at_leat_one_platform)
    )
}

@Preview
@Composable
private fun SelectPlatformDialogPreview() {
    val platforms = listOf(
        Platform(ApiType.OPENAI, enabled = true),
        Platform(ApiType.ANTHROPIC, enabled = false),
        Platform(ApiType.GOOGLE, enabled = false),
        Platform(ApiType.GROQ, enabled = true),
        Platform(ApiType.OLLAMA, enabled = true)
    )
    SelectPlatformDialog(
        platforms = platforms,
        onDismissRequest = {},
        onConfirmation = {},
        onPlatformSelect = {},
        onOfflineModelPick = {}
    )
}

@Composable
fun BorderSettingsDialog(
    borderSettings: dev.chungjungsoo.gptmobile.data.dto.BorderSetting,
    onDismissRequest: () -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    onRadiusChange: (Float) -> Unit,
    onWidthChange: (Float) -> Unit,
    onAnimationStyleChange: (dev.chungjungsoo.gptmobile.data.dto.RainbowAnimationStyle) -> Unit,
    onSave: () -> Unit
) {
    val configuration = LocalConfiguration.current
    var enabled by remember { mutableStateOf(borderSettings.enabled) }
    var borderRadius by remember { mutableStateOf(borderSettings.borderRadius) }
    var borderWidth by remember { mutableStateOf(borderSettings.borderWidth) }
    var animationStyle by remember { mutableStateOf(borderSettings.animationStyle) }

    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .width(configuration.screenWidthDp.dp - 40.dp)
            .heightIn(max = configuration.screenHeightDp.dp - 80.dp),
        title = {
            Text(
                text = "🌈 Rainbow Border Settings",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Enable/Disable Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Border", style = MaterialTheme.typography.bodyLarge)
                    androidx.compose.material3.Switch(
                        checked = enabled,
                        onCheckedChange = {
                            enabled = it
                            onEnabledChange(it)
                        }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Border Radius Slider
                Text(
                    text = "Corner Radius: ${borderRadius.toInt()}dp",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                androidx.compose.material3.Slider(
                    value = borderRadius,
                    onValueChange = {
                        borderRadius = it
                        onRadiusChange(it)
                    },
                    valueRange = 0f..64f,
                    steps = 63,
                    enabled = enabled,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Border Width Slider
                Text(
                    text = "Border Width: ${borderWidth.toInt()}dp",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                androidx.compose.material3.Slider(
                    value = borderWidth,
                    onValueChange = {
                        borderWidth = it
                        onWidthChange(it)
                    },
                    valueRange = 1f..16f,
                    steps = 14,
                    enabled = enabled,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Animation Style",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val animationOptions = listOf(
                    dev.chungjungsoo.gptmobile.data.dto.RainbowAnimationStyle.CONTINUOUS_SWEEP to Pair(
                        "Continuous Orbit",
                        "Classic rainbow sweep circling every edge."
                    ),
                    dev.chungjungsoo.gptmobile.data.dto.RainbowAnimationStyle.TOP_RIGHT_BOUNCE to Pair(
                        "Volume Button Bounce",
                        "Glow launches from the upper-right, bouncing like a volume press."
                    ),
                    dev.chungjungsoo.gptmobile.data.dto.RainbowAnimationStyle.BOTTOM_CENTER_REVEAL to Pair(
                        "Bottom Lift Reveal",
                        "Border rises from the bottom center and wraps the screen."
                    )
                )

                animationOptions.forEach { (style, info) ->
                    val selected = animationStyle == style
                    val optionModifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .then(
                            if (enabled) {
                                Modifier.clickable {
                                    animationStyle = style
                                    onAnimationStyleChange(style)
                                }
                            } else {
                                Modifier
                            }
                        )

                    androidx.compose.material3.Surface(
                        modifier = optionModifier,
                        shape = MaterialTheme.shapes.medium,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        },
                        tonalElevation = if (selected) 6.dp else 0.dp,
                        shadowElevation = if (selected) 3.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.RadioButton(
                                selected = selected,
                                onClick = null,
                                enabled = enabled
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = info.first,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                                )
                                Text(
                                    text = info.second,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Preview info
                Text(
                    text = "✨ Animated rainbow border with glowing sparkles will appear around the screen!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onSave) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteWarningDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    val configuration = LocalConfiguration.current
    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .width(configuration.screenWidthDp.dp - 40.dp)
            .heightIn(max = configuration.screenHeightDp.dp - 80.dp),
        title = {
            Text(
                text = stringResource(R.string.delete_selected_chats),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(stringResource(R.string.this_operation_can_t_be_undone))
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
