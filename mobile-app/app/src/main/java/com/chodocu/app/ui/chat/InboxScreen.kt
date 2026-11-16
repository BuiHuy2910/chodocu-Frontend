package com.chodocu.app.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chodocu.app.core.formatRelativeTime
import com.chodocu.app.data.model.Conversation
import com.chodocu.app.data.model.MessageType
import com.chodocu.app.di.appViewModel
import com.chodocu.app.ui.components.AppTopBar
import com.chodocu.app.ui.components.CountBadge
import com.chodocu.app.ui.components.EmptyView
import com.chodocu.app.ui.components.ErrorView
import com.chodocu.app.ui.components.LoadingView
import com.chodocu.app.ui.components.PollWhileResumed
import com.chodocu.app.ui.components.RefreshOnResume
import com.chodocu.app.ui.components.RemoteImage
import com.chodocu.app.ui.components.UserAvatar
import com.chodocu.app.ui.theme.Radius
import com.chodocu.app.ui.theme.Space

private const val INBOX_POLL_MILLIS = 10_000L

/** Hộp thư: mỗi người từng nhắn tin với mình là một dòng. */
@Composable
fun InboxScreen(onOpenThread: (partnerId: Long, productId: Long?, partnerName: String) -> Unit) {
    val viewModel = appViewModel { _ -> InboxViewModel(chat) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    RefreshOnResume { viewModel.refreshSilently() }
    PollWhileResumed(INBOX_POLL_MILLIS) { viewModel.refreshSilently() }

    InboxContent(
        state = state,
        onRetry = viewModel::retry,
        onRefresh = viewModel::refresh,
        onUserMessageShown = viewModel::userMessageShown,
        onOpen = { conversation ->
            onOpenThread(conversation.partner.id, conversation.productId, conversation.partner.fullName)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InboxContent(
    state: InboxUiState,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onUserMessageShown: () -> Unit,
    onOpen: (Conversation) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.userMessage) {
        state.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            onUserMessageShown()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AppTopBar(title = "Tin nhắn") },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding),
        ) {
            when {
                state.isLoading -> LoadingView()
                state.errorMessage != null -> ErrorView(message = state.errorMessage, onRetry = onRetry)
                state.conversations.isEmpty() -> Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    EmptyView(
                        icon = Icons.Outlined.ChatBubbleOutline,
                        title = "Chưa có tin nhắn",
                        message = "Nhắn tin cho người bán từ trang chi tiết sản phẩm để hỏi thêm hoặc hẹn gặp.",
                        modifier = Modifier.padding(top = Space.xxl),
                    )
                }
                else -> LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = Space.md)) {
                    items(state.conversations, key = { it.partner.id }) { conversation ->
                        ConversationRow(conversation = conversation, onClick = { onOpen(conversation) })
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.padding(start = Space.screen + 48.dp + Space.sm),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationRow(conversation: Conversation, onClick: () -> Unit) {
    val hasUnread = conversation.unreadCount > 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Space.screen, vertical = Space.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.sm),
    ) {
        UserAvatar(name = conversation.partner.fullName, imageUrl = conversation.partner.avatarUrl, size = 48.dp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = conversation.partner.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = formatRelativeTime(conversation.lastMessageAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (hasUnread) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            conversation.productTitle?.let { title ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                Text(
                    text = conversation.previewText(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (hasUnread) FontWeight.Medium else FontWeight.Normal,
                    color = if (hasUnread) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                CountBadge(count = conversation.unreadCount)
            }
        }
        if (conversation.productImage != null) {
            RemoteImage(
                url = conversation.productImage,
                contentDescription = conversation.productTitle,
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(Radius.chip)),
            )
        }
    }
}

/** Dòng xem trước tin nhắn cuối cùng ("Bạn: ..." nếu là tin mình gửi). */
private fun Conversation.previewText(): String {
    val body = when (lastMessageType) {
        MessageType.IMAGE -> "Đã gửi một ảnh"
        MessageType.MEETUP -> "Đề xuất lịch hẹn"
        MessageType.OFFER -> lastMessage.ifBlank { "Đề nghị giá" }
        MessageType.TEXT, MessageType.SYSTEM -> lastMessage
    }
    return if (lastMessageMine && lastMessageType != MessageType.SYSTEM) "Bạn: $body" else body
}
