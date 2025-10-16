package dev.chungjungsoo.gptmobile.presentation.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch

enum class InputBarState {
    FULL_BAR,      // Full input bar at bottom
    FAB,           // Floating action button
    SPEED_DIAL     // Speed dial menu expanded
}

/**
 * Animated container that morphs between full input bar and FAB
 */
@Composable
fun AnimatedInputFab(
    isAtBottom: Boolean,
    textInput: String,
    onTextChange: (String) -> Unit,
    selectedModel: String,
    onModelClick: () -> Unit,
    onSendMessage: () -> Unit,
    onVoiceMemo: () -> Unit,
    onLiveAI: () -> Unit,
    onNewTextPrompt: () -> Unit,
    onAddMedia: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentState by remember { mutableStateOf(InputBarState.FAB) }
    var showSpeedDial by remember { mutableStateOf(false) }
    
    // Determine state based on scroll position
    LaunchedEffect(isAtBottom) {
        currentState = if (isAtBottom) InputBarState.FULL_BAR else InputBarState.FAB
        if (!isAtBottom) {
            showSpeedDial = false
        }
    }
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        // Speed dial menu overlay
        if (showSpeedDial) {
            SpeedDialMenu(
                expanded = showSpeedDial,
                onDismiss = { showSpeedDial = false },
                onLiveAIClick = {
                    onLiveAI()
                    showSpeedDial = false
                },
                onVoiceMemoClick = {
                    onVoiceMemo()
                    showSpeedDial = false
                },
                onNewTextClick = {
                    onNewTextPrompt()
                    showSpeedDial = false
                },
                onAddMediaClick = {
                    onAddMedia()
                    showSpeedDial = false
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Animated transition between Full Input Bar and FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(3f)
        ) {
            // Full Input Bar (visible at bottom)
            AnimatedVisibility(
                visible = currentState == InputBarState.FULL_BAR,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(200))
            ) {
                FullInputBar(
                    selectedModel = selectedModel,
                    textInput = textInput,
                    onTextChange = onTextChange,
                    onModelClick = onModelClick,
                    onSendClick = onSendMessage,
                    onVoiceClick = onVoiceMemo,
                    onLiveAIClick = onLiveAI
                )
            }
            
            // Floating Action Button (visible when scrolled)
            AnimatedVisibility(
                visible = currentState == InputBarState.FAB,
                enter = fadeIn(animationSpec = tween(400)) + androidx.compose.animation.scaleIn(
                    initialScale = 0.3f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
                exit = fadeOut(animationSpec = tween(200)) + androidx.compose.animation.scaleOut(
                    targetScale = 0.3f,
                    animationSpec = tween(200)
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                FloatingActionButton(
                    onClick = { showSpeedDial = true },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(64.dp)
                        .graphicsLayer {
                            // Subtle pulse effect when tapped
                            if (showSpeedDial) {
                                scaleX = 1.05f
                                scaleY = 1.05f
                            }
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "New Chat",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

/**
 * Helper to track if LazyColumn is at bottom
 */
@Composable
fun rememberIsAtBottom(
    listState: androidx.compose.foundation.lazy.LazyListState
): Boolean {
    val isAtBottom = remember {
        androidx.compose.runtime.derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem?.index == listState.layoutInfo.totalItemsCount - 1 &&
                    lastVisibleItem.offset + lastVisibleItem.size <= listState.layoutInfo.viewportEndOffset
        }
    }
    return isAtBottom.value
}
