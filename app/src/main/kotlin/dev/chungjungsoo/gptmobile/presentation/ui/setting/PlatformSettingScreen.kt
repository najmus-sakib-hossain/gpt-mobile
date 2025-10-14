package dev.chungjungsoo.gptmobile.presentation.ui.setting

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dev.chungjungsoo.gptmobile.R
import dev.chungjungsoo.gptmobile.data.ModelConstants
import dev.chungjungsoo.gptmobile.data.model.ApiType
import dev.chungjungsoo.gptmobile.presentation.common.SettingItem
import dev.chungjungsoo.gptmobile.util.getPlatformSettingTitle
import dev.chungjungsoo.gptmobile.util.pinnedExitUntilCollapsedScrollBehavior

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformSettingScreen(
    modifier: Modifier = Modifier,
    apiType: ApiType,
    settingViewModel: SettingViewModel = hiltViewModel(),
    onNavigationClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scrollBehavior = pinnedExitUntilCollapsedScrollBehavior(
        canScroll = { scrollState.canScrollForward || scrollState.canScrollBackward }
    )
    val title = getPlatformSettingTitle(apiType)
    val platformState by settingViewModel.platformState.collectAsStateWithLifecycle()
    val dialogState by settingViewModel.dialogState.collectAsStateWithLifecycle()
    
    // File picker launcher for Offline AI model selection
    val modelFilePicker = rememberLauncherForActivityResult(
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
                android.widget.Toast.makeText(
                    context,
                    context.getString(R.string.copying_model_file),
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                
                // Copy file to internal storage in background
                CoroutineScope(Dispatchers.IO).launch {
                    android.util.Log.d("PlatformSettingScreen", "Starting to copy model file from URI: $it")
                    val fileName = dev.chungjungsoo.gptmobile.util.FileUtil.getFileNameFromUri(context, it)
                    android.util.Log.d("PlatformSettingScreen", "Extracted file name: $fileName")
                    val filePath = dev.chungjungsoo.gptmobile.util.FileUtil.copyUriToInternalStorage(context, it, fileName)
                    
                    withContext(Dispatchers.Main) {
                        if (filePath != null) {
                            android.util.Log.d("PlatformSettingScreen", "File copied successfully to: $filePath")
                            // Update the model path with the actual file path
                            settingViewModel.updateModel(apiType, filePath)
                            settingViewModel.savePlatformSettings()
                            
                            android.widget.Toast.makeText(
                                context,
                                context.getString(R.string.model_file_selected),
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            android.util.Log.e("PlatformSettingScreen", "Failed to copy file from URI: $it")
                            android.widget.Toast.makeText(
                                context,
                                context.getString(R.string.error_copying_model_file),
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PlatformSettingScreen", "Exception in file picker callback", e)
                android.widget.Toast.makeText(
                    context,
                    "Error: ${e.message}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            PlatformTopAppBar(
                title = title,
                onNavigationClick = onNavigationClick,
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            val platform = platformState.firstOrNull { it.name == apiType }
            val url = platform?.apiUrl ?: ModelConstants.getDefaultAPIUrl(apiType)
            val enabled = platform?.enabled == true
            val model = platform?.model
            val token = platform?.token
            val temperature = platform?.temperature ?: 1F
            val topP = platform?.topP
            val systemPrompt = platform?.systemPrompt ?: when (apiType) {
                ApiType.OPENAI -> ModelConstants.OPENAI_PROMPT
                ApiType.ANTHROPIC -> ModelConstants.DEFAULT_PROMPT
                ApiType.GOOGLE -> ModelConstants.DEFAULT_PROMPT
                ApiType.GROQ -> ModelConstants.DEFAULT_PROMPT
                ApiType.OLLAMA -> ModelConstants.DEFAULT_PROMPT
                ApiType.OFFLINE_AI -> ModelConstants.DEFAULT_PROMPT
            }

            PreferenceSwitchWithContainer(
                title = stringResource(R.string.enable_api),
                isChecked = enabled
            ) { settingViewModel.toggleAPI(apiType) }
            
            // Hide API URL for Offline AI (not needed)
            if (apiType != ApiType.OFFLINE_AI) {
                SettingItem(
                    modifier = Modifier.height(64.dp),
                    title = stringResource(R.string.api_url),
                    description = url,
                    enabled = enabled && platform.name != ApiType.GOOGLE,
                    onItemClick = settingViewModel::openApiUrlDialog,
                    showTrailingIcon = false,
                    showLeadingIcon = true,
                    leadingIcon = {
                        Icon(
                            ImageVector.vectorResource(id = R.drawable.ic_link),
                            contentDescription = stringResource(R.string.url_icon)
                        )
                    }
                )
            }
            
            // Hide API Key for Offline AI (not needed)
            if (apiType != ApiType.OFFLINE_AI) {
                SettingItem(
                    modifier = Modifier.height(64.dp),
                    title = stringResource(R.string.api_key),
                    description = token?.let { stringResource(R.string.token_set, it[0]) } ?: stringResource(R.string.token_not_set),
                    enabled = enabled,
                    onItemClick = settingViewModel::openApiTokenDialog,
                    showTrailingIcon = false,
                    showLeadingIcon = true,
                    leadingIcon = {
                        Icon(
                            ImageVector.vectorResource(id = R.drawable.ic_key),
                            contentDescription = stringResource(R.string.key_icon)
                        )
                    }
                )
            }
            SettingItem(
                modifier = Modifier.height(64.dp),
                title = stringResource(R.string.api_model),
                description = model ?: stringResource(R.string.select_model_file),
                enabled = enabled,
                onItemClick = {
                    if (apiType == ApiType.OFFLINE_AI) {
                        // Launch file picker for GGUF files
                        modelFilePicker.launch(arrayOf("*/*"))
                    } else {
                        settingViewModel.openApiModelDialog()
                    }
                },
                showTrailingIcon = false,
                showLeadingIcon = true,
                leadingIcon = {
                    Icon(
                        ImageVector.vectorResource(id = R.drawable.ic_model),
                        contentDescription = stringResource(R.string.model_icon)
                    )
                }
            )
            SettingItem(
                modifier = Modifier.height(64.dp),
                title = stringResource(R.string.temperature),
                description = temperature.toString(),
                enabled = enabled,
                onItemClick = settingViewModel::openTemperatureDialog,
                showTrailingIcon = false,
                showLeadingIcon = true,
                leadingIcon = {
                    Icon(
                        ImageVector.vectorResource(id = R.drawable.ic_temperature),
                        contentDescription = stringResource(R.string.temperature_icon)
                    )
                }
            )
            SettingItem(
                modifier = Modifier.height(64.dp),
                title = stringResource(R.string.top_p),
                description = topP?.toString(),
                enabled = enabled,
                onItemClick = settingViewModel::openTopPDialog,
                showTrailingIcon = false,
                showLeadingIcon = true,
                leadingIcon = {
                    Icon(
                        ImageVector.vectorResource(id = R.drawable.ic_chart),
                        contentDescription = stringResource(R.string.top_p_icon)
                    )
                }
            )
            SettingItem(
                modifier = Modifier.height(64.dp),
                title = stringResource(R.string.system_prompt),
                description = systemPrompt,
                enabled = enabled,
                onItemClick = settingViewModel::openSystemPromptDialog,
                showTrailingIcon = false,
                showLeadingIcon = true,
                leadingIcon = {
                    Icon(
                        ImageVector.vectorResource(id = R.drawable.ic_instructions),
                        contentDescription = stringResource(R.string.system_prompt_icon)
                    )
                }
            )

            APIUrlDialog(dialogState, apiType, url, settingViewModel)
            APIKeyDialog(dialogState, apiType, settingViewModel)
            ModelDialog(dialogState, apiType, model, settingViewModel)
            TemperatureDialog(dialogState, apiType, temperature, settingViewModel)
            TopPDialog(dialogState, apiType, topP, settingViewModel)
            SystemPromptDialog(dialogState, apiType, systemPrompt, settingViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformTopAppBar(
    title: String,
    onNavigationClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    LargeTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        ),
        title = {
            Text(
                modifier = Modifier.padding(4.dp),
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(
                modifier = Modifier.padding(4.dp),
                onClick = onNavigationClick
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.go_back))
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun PreferenceSwitchWithContainer(
    title: String,
    icon: ImageVector? = null,
    isChecked: Boolean,
    onClick: () -> Unit
) {
    val thumbContent: (@Composable () -> Unit)? = remember(isChecked) {
        if (isChecked) {
            {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                )
            }
        } else {
            null
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(
                MaterialTheme.colorScheme.primaryContainer
            )
            .toggleable(
                value = isChecked,
                onValueChange = { onClick() },
                interactionSource = interactionSource,
                indication = LocalIndication.current
            )
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 8.dp, end = 16.dp)
                    .size(24.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = if (icon == null) 12.dp else 0.dp, end = 12.dp)
        ) {
            Text(
                text = title,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Switch(
            checked = isChecked,
            interactionSource = interactionSource,
            onCheckedChange = null,
            modifier = Modifier.padding(start = 12.dp, end = 6.dp),
            thumbContent = thumbContent
        )
    }
}
