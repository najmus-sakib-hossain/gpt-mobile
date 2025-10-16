package dev.chungjungsoo.gptmobile.presentation.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.KeyboardVoice
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import dev.chungjungsoo.gptmobile.R
import dev.chungjungsoo.gptmobile.data.model.ApiType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Full input bar that appears at the bottom of the screen
 * Transparent background with collapsible left icons when input is focused
 * Auto-unfocuses after 5 seconds of inactivity
 * Moves to top with full width when text is long, grows up to 500px max height
 */
@Composable
fun FullInputBar(
    modifier: Modifier = Modifier,
    currentProvider: ApiType = ApiType.OPENAI,
    textInput: String = "",
    onTextChange: (String) -> Unit = {},
    onProviderClick: () -> Unit = {},
    onSearchTypeClick: () -> Unit = {},
    onContextClick: () -> Unit = {},
    onMentionClick: () -> Unit = {},
    onSendClick: () -> Unit = {},
    onVoiceClick: () -> Unit = {},
    onLiveAIClick: () -> Unit = {},
    selectedSearchType: String = "Search",
    selectedContext: String = "Chat"
) {
    var showSearchTypeMenu by remember { mutableStateOf(false) }
    var showContextMenu by remember { mutableStateOf(false) }
    var showMentionMenu by remember { mutableStateOf(false) }
    var isInputFocused by remember { mutableStateOf(false) }
    var showExpandedIcons by remember { mutableStateOf(false) }
    var actionMode by remember { mutableIntStateOf(0) } // 0: Live AI, 1: Send, 2: Voice
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    
    // Determine if text is long (more than 2 lines worth of text ~60 chars)
    val isTextLong = textInput.length > 60
    val showTopInput = isTextLong && isInputFocused
    
    // Auto-unfocus after 5 seconds of inactivity
    LaunchedEffect(isInputFocused, lastInteractionTime) {
        if (isInputFocused) {
            coroutineScope.launch {
                delay(5000) // 5 seconds
                val timeSinceLastInteraction = System.currentTimeMillis() - lastInteractionTime
                if (timeSinceLastInteraction >= 5000) {
                    focusManager.clearFocus()
                }
            }
        }
    }
    
    Column(modifier = modifier.fillMaxWidth()) {
        // Top expanded input when text is long
        AnimatedVisibility(
            visible = showTopInput,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(200))
        ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Cancel button
                IconButton(
                    onClick = {
                        focusManager.clearFocus()
                        onTextChange("")
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Expanded text input with max height
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { 
                        lastInteractionTime = System.currentTimeMillis()
                        onTextChange(it)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(max = 500.dp)
                        .onFocusEvent { focusState ->
                            isInputFocused = focusState.isFocused
                            if (focusState.isFocused) {
                                lastInteractionTime = System.currentTimeMillis()
                            }
                            if (!focusState.isFocused) {
                                showExpandedIcons = false
                            }
                        },
                    placeholder = { Text("Type your message...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(16.dp),
                    maxLines = Int.MAX_VALUE,
                    trailingIcon = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(0.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 2.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    lastInteractionTime = System.currentTimeMillis()
                                    onVoiceClick()
                                },
                                modifier = Modifier
                                    .size(28.dp)
                                    .padding(0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardVoice,
                                    contentDescription = "Voice",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    lastInteractionTime = System.currentTimeMillis()
                                    /* Add attachment */
                                },
                                modifier = Modifier
                                    .size(28.dp)
                                    .padding(0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Add",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                )
            }
        }
    }
    
    // Bottom input bar (shown when text is short or unfocused)
    AnimatedVisibility(
        visible = !showTopInput,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(200))
    ) {
        BottomInputBar(
            currentProvider = currentProvider,
            textInput = textInput,
            onTextChange = onTextChange,
            onProviderClick = onProviderClick,
            onSearchTypeClick = onSearchTypeClick,
            onContextClick = onContextClick,
            onMentionClick = onMentionClick,
            onSendClick = onSendClick,
            onVoiceClick = onVoiceClick,
            onLiveAIClick = onLiveAIClick,
            isInputFocused = isInputFocused,
            onFocusChanged = { focused ->
                isInputFocused = focused
                if (focused) {
                    lastInteractionTime = System.currentTimeMillis()
                }
                if (!focused) {
                    showExpandedIcons = false
                }
            },
            showExpandedIcons = showExpandedIcons,
            onExpandedIconsChanged = { showExpandedIcons = it },
            actionMode = actionMode,
            onActionModeChanged = { actionMode = it },
            lastInteractionTime = lastInteractionTime,
            onInteraction = { lastInteractionTime = System.currentTimeMillis() }
        )
    }
    }
}

/**
 * Bottom input bar component - compact view with truncated text
 */
@Composable
private fun BottomInputBar(
    modifier: Modifier = Modifier,
    currentProvider: ApiType,
    textInput: String,
    onTextChange: (String) -> Unit,
    onProviderClick: () -> Unit,
    onSearchTypeClick: () -> Unit,
    onContextClick: () -> Unit,
    onMentionClick: () -> Unit,
    onSendClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onLiveAIClick: () -> Unit,
    isInputFocused: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    showExpandedIcons: Boolean,
    onExpandedIconsChanged: (Boolean) -> Unit,
    actionMode: Int,
    onActionModeChanged: (Int) -> Unit,
    lastInteractionTime: Long,
    onInteraction: () -> Unit
) {
    var showSearchTypeMenu by remember { mutableStateOf(false) }
    var showContextMenu by remember { mutableStateOf(false) }
    var showMentionMenu by remember { mutableStateOf(false) }
    
    // Transparent background, no elevation
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Collapsible left icons with animations
            AnimatedVisibility(
                visible = isInputFocused && !showExpandedIcons,
                enter = fadeIn(animationSpec = tween(300)) + 
                        expandVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
                exit = fadeOut(animationSpec = tween(200)) + 
                       shrinkVertically(animationSpec = tween(200))
            ) {
                // Single expandable menu icon when focused
                IconButton(
                    onClick = { onExpandedIconsChanged(!showExpandedIcons) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            // All 4 icons when not focused or when expanded
            AnimatedVisibility(
                visible = !isInputFocused || showExpandedIcons,
                enter = fadeIn(animationSpec = tween(300)) + 
                        expandVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
                exit = fadeOut(animationSpec = tween(200)) + 
                       shrinkVertically(animationSpec = tween(200))
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Provider Logo
                    ProviderLogoButton(
                        provider = currentProvider,
                        onClick = onProviderClick
                    )
                    
                    // 2. Search Type
                    Box {
                        IconButton(
                            onClick = { showSearchTypeMenu = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Type",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showSearchTypeMenu,
                            onDismissRequest = { showSearchTypeMenu = false }
                        ) {
                            listOf("Search", "Deep Search", "Fast", "Image", "Video").forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        onSearchTypeClick()
                                        showSearchTypeMenu = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // 3. Context Selector
                    Box {
                        IconButton(
                            onClick = { showContextMenu = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.List,
                                contentDescription = "Context",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showContextMenu,
                            onDismissRequest = { showContextMenu = false }
                        ) {
                            listOf("Chat", "Project", "Workspace").forEach { context ->
                                DropdownMenuItem(
                                    text = { Text(context) },
                                    onClick = {
                                        onContextClick()
                                        showContextMenu = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // 4. Person/Mention Icon
                    Box {
                        IconButton(
                            onClick = { showMentionMenu = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Mention",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showMentionMenu,
                            onDismissRequest = { showMentionMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Add Person") },
                                onClick = {
                                    onMentionClick()
                                    showMentionMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Remove Person") },
                                onClick = {
                                    onMentionClick()
                                    showMentionMenu = false
                                }
                            )
                        }
                    }
                }
            }
            
            // Text Input - expands to fill available space, truncates when unfocused
            OutlinedTextField(
                value = textInput,
                onValueChange = { 
                    onInteraction()
                    onTextChange(it)
                },
                modifier = Modifier
                    .weight(1f)
                    .onFocusEvent { focusState ->
                        onFocusChanged(focusState.isFocused)
                    },
                placeholder = { 
                    Text(
                        "Chat",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp),
                maxLines = if (isInputFocused) 4 else 1,
                singleLine = !isInputFocused,
                trailingIcon = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 2.dp)
                    ) {
                        IconButton(
                            onClick = {
                                onInteraction()
                                onVoiceClick()
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .padding(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.KeyboardVoice,
                                contentDescription = "Voice",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                onInteraction()
                                /* Add attachment */
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .padding(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Add",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            )
            
            // Swipeable Action Button - Now on the right side
            SwipeableActionButton(
                currentMode = actionMode,
                onModeChange = onActionModeChanged,
                onAction = {
                    onInteraction()
                    when (actionMode) {
                        0 -> onLiveAIClick()
                        1 -> onSendClick()
                        2 -> onVoiceClick()
                    }
                }
            )
        }
    }
}

/**
 * Provider logo button (Google, OpenAI, Anthropic, etc.)
 */
@Composable
fun ProviderLogoButton(
    provider: ApiType,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp)
    ) {
        // You can add actual logos here
        Icon(
            imageVector = when (provider) {
                ApiType.GOOGLE -> Icons.Default.Search
                ApiType.OPENAI -> Icons.Default.Face
                ApiType.ANTHROPIC -> Icons.Default.Person
                ApiType.OFFLINE_AI -> Icons.Default.Face
                else -> Icons.Default.Face
            },
            contentDescription = "Provider: ${provider.name}",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
    }
}

/**
 * Avatar button with Unsplash image
 */
@Composable
fun AvatarButton(
    onClick: () -> Unit,
    imageUrl: String = "https://source.unsplash.com/random/100x100/?portrait"
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier.size(40.dp)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Add People",
            modifier = Modifier.size(40.dp),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_hamburger)
        )
    }
}

/**
 * Swipeable action button that changes modes
 */
@Composable
fun SwipeableActionButton(
    currentMode: Int,
    onModeChange: (Int) -> Unit,
    onAction: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    
    val icons = listOf(
        Icons.Outlined.PlayArrow, // Live AI
        Icons.Default.Send,        // Send
        Icons.Default.KeyboardVoice // Voice
    )
    
    val colors = listOf(
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary
    )
    
    Surface(
        onClick = onAction,
        shape = RoundedCornerShape(12.dp),
        color = colors[currentMode],
        modifier = Modifier
            .size(48.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX > 50) {
                            // Swipe right - next mode
                            onModeChange((currentMode + 1) % 3)
                        } else if (offsetX < -50) {
                            // Swipe left - previous mode
                            onModeChange((currentMode + 2) % 3)
                        }
                        offsetX = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount
                    }
                )
            }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = icons[currentMode],
                contentDescription = when (currentMode) {
                    0 -> "Live AI"
                    1 -> "Send"
                    else -> "Voice"
                },
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

/**
 * Model selector button that shows current model
 */
@Composable
fun ModelSelectorButton(
    modelName: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.padding(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = modelName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Action button that switches between Send, Voice Memo, and Live AI
 */
@Composable
fun GesturalActionButton(
    hasText: Boolean,
    isLiveAIMode: Boolean,
    onSendClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onLiveAIClick: () -> Unit
) {
    Surface(
        onClick = {
            when {
                hasText -> onSendClick()
                isLiveAIMode -> onLiveAIClick()
                else -> onVoiceClick()
            }
        },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(48.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = when {
                    hasText -> Icons.Default.Send
                    isLiveAIMode -> Icons.Default.Face
                    else -> Icons.Default.KeyboardVoice
                },
                contentDescription = when {
                    hasText -> "Send"
                    isLiveAIMode -> "Live AI"
                    else -> "Voice Memo"
                },
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

/**
 * Speed dial menu item
 */
@Composable
fun SpeedDialMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Speed dial menu that expands from the FAB
 */
@Composable
fun SpeedDialMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onLiveAIClick: () -> Unit,
    onVoiceMemoClick: () -> Unit,
    onNewTextClick: () -> Unit,
    onAddMediaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Scrim overlay
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            Surface(
                modifier = Modifier
                    .matchParentSize()
                    .zIndex(1f),
                color = Color.Black.copy(alpha = 0.4f),
                onClick = onDismiss
            ) {}
        }
        
        // Speed dial items
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 88.dp)
                .zIndex(2f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    expandFrom = Alignment.Bottom
                ) + fadeIn(),
                exit = shrinkVertically(
                    animationSpec = tween(200),
                    shrinkTowards = Alignment.Bottom
                ) + fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    SpeedDialMenuItem(
                        icon = Icons.Default.Face,
                        label = "Start Live AI",
                        onClick = {
                            onLiveAIClick()
                            onDismiss()
                        }
                    )
                    
                    SpeedDialMenuItem(
                        icon = Icons.Default.KeyboardVoice,
                        label = "Send Voice Memo",
                        onClick = {
                            onVoiceMemoClick()
                            onDismiss()
                        }
                    )
                    
                    SpeedDialMenuItem(
                        icon = Icons.Default.Create,
                        label = "New Text Prompt",
                        onClick = {
                            onNewTextClick()
                            onDismiss()
                        }
                    )
                    
                    SpeedDialMenuItem(
                        icon = Icons.Default.Add,
                        label = "Add Media",
                        onClick = {
                            onAddMediaClick()
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}
