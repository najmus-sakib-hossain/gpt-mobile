package dev.chungjungsoo.gptmobile.presentation.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

/**
 * Full input bar that appears at the bottom of the screen
 * Components: [Model Selector] [Tools] [@Mention] [Text Input] [Action Button]
 */
@Composable
fun FullInputBar(
    modifier: Modifier = Modifier,
    selectedModel: String = "SmolLM",
    textInput: String = "",
    onTextChange: (String) -> Unit = {},
    onModelClick: () -> Unit = {},
    onToolsClick: () -> Unit = {},
    onMentionClick: () -> Unit = {},
    onSendClick: () -> Unit = {},
    onVoiceClick: () -> Unit = {},
    onLiveAIClick: () -> Unit = {},
    isLiveAIMode: Boolean = false
) {
    var showToolsMenu by remember { mutableStateOf(false) }
    
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Model Selector
                ModelSelectorButton(
                    modelName = selectedModel,
                    onClick = onModelClick
                )
                
                // Tools & Options
                Box {
                    IconButton(
                        onClick = { showToolsMenu = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Tools & Options",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showToolsMenu,
                        onDismissRequest = { showToolsMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = {
                                onToolsClick()
                                showToolsMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Live AI") },
                            onClick = {
                                onLiveAIClick()
                                showToolsMenu = false
                            }
                        )
                    }
                }
                
                // @Mention/Collaborator
                IconButton(
                    onClick = onMentionClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Mention",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                
                // Smart Text Input Box
                OutlinedTextField(
                    value = textInput,
                    onValueChange = onTextChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    leadingIcon = {
                        IconButton(onClick = { /* Add attachment */ }) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Add",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    trailingIcon = {
                        IconButton(onClick = onVoiceClick) {
                            Icon(
                                imageVector = Icons.Rounded.KeyboardVoice,
                                contentDescription = "Voice",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )
                
                // Gestural Action Button (Send/Voice/Live AI)
                GesturalActionButton(
                    hasText = textInput.isNotEmpty(),
                    isLiveAIMode = isLiveAIMode,
                    onSendClick = onSendClick,
                    onVoiceClick = onVoiceClick,
                    onLiveAIClick = onLiveAIClick
                )
            }
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
