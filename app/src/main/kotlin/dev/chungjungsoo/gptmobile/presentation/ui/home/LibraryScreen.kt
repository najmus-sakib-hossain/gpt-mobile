package dev.chungjungsoo.gptmobile.presentation.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.chungjungsoo.gptmobile.R
import dev.chungjungsoo.gptmobile.data.database.entity.ChatRoom
import dev.chungjungsoo.gptmobile.data.model.ApiType
import dev.chungjungsoo.gptmobile.util.getPlatformTitleResources

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    chatListState: HomeViewModel.ChatListState,
    onExistingChatClick: (ChatRoom) -> Unit,
    homeViewModel: HomeViewModel
) {
    val platformTitles = getPlatformTitleResources()
    val listState = rememberLazyListState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(lifecycleState) {
        if (lifecycleState == Lifecycle.State.RESUMED && !chatListState.isSelectionMode) {
            homeViewModel.fetchChats()
            homeViewModel.fetchPlatformStatus()
        }
    }

    LazyColumn(state = listState) {
        itemsIndexed(chatListState.chats, key = { _, it -> it.id }) { idx, chatRoom ->
            val usingPlatform = chatRoom.enabledPlatform.joinToString(", ") { platformTitles[it] ?: "" }
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onLongClick = {
                            homeViewModel.enableSelectionMode()
                            homeViewModel.selectChat(idx)
                        },
                        onClick = {
                            if (chatListState.isSelectionMode) {
                                homeViewModel.selectChat(idx)
                            } else {
                                onExistingChatClick(chatRoom)
                            }
                        }
                    )
                    .padding(start = 8.dp, end = 8.dp)
                    .animateItem(),
                headlineContent = {
                    Text(
                        text = chatRoom.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = {
                    if (chatListState.isSelectionMode) {
                        Checkbox(
                            checked = chatListState.selected[idx],
                            onCheckedChange = { homeViewModel.selectChat(idx) }
                        )
                    } else {
                        if (chatRoom.enabledPlatform.contains(ApiType.GOOGLE)) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_google),
                                contentDescription = "Google Logo",
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                ImageVector.vectorResource(id = R.drawable.ic_rounded_chat),
                                contentDescription = stringResource(R.string.chat_icon)
                            )
                        }
                    }
                },
                supportingContent = {
                    if (!chatRoom.enabledPlatform.contains(ApiType.GOOGLE)) {
                        Text(text = stringResource(R.string.using_certain_platform, usingPlatform))
                    }
                }
            )
        }
    }
}