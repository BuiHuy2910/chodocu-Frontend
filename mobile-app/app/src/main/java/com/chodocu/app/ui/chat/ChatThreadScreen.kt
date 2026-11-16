package com.chodocu.app.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chodocu.app.data.model.MessageType
import com.chodocu.app.data.model.OrderRole
import com.chodocu.app.di.appViewModel
import com.chodocu.app.ui.components.AppTopBar
import com.chodocu.app.ui.components.ErrorView
import com.chodocu.app.ui.components.FullScreenImageViewer
import com.chodocu.app.ui.components.LoadingView
import com.chodocu.app.ui.components.OrderAction
import com.chodocu.app.ui.components.OrderActionDialog
import com.chodocu.app.ui.components.PollWhileResumed
import com.chodocu.app.ui.components.RefreshOnResume
import com.chodocu.app.ui.components.UserAvatar
import com.chodocu.app.ui.components.openDialer
import com.chodocu.app.ui.components.rememberSingleImagePicker
import com.chodocu.app.ui.theme.Sizes
import com.chodocu.app.ui.theme.Space
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/** Hỏi tin mới mỗi 3 giây khi đang mở cuộc trò chuyện. */
private const val CHAT_POLL_MILLIS = 3_000L

/** Màn trò chuyện giữa người mua và người bán. */
@Composable
fun ChatThreadScreen(onBack: () -> Unit, onOpenProduct: (Long) -> Unit, onOpenOrders: (OrderRole) -> Unit) {
    val viewModel = appViewModel { handle ->
        ChatThreadViewModel(chat, products, orders, catalog, uploads, session, handle)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    RefreshOnResume { viewModel.onResume() }
    PollWhileResumed(CHAT_POLL_MILLIS) { viewModel.poll() }

    val pickImage = rememberSingleImagePicker(onPicked = viewModel::sendImage)
    ChatThreadContent(
        state = state,
        actions = ChatThreadActions(
            onBack = onBack,
            onRetry = viewModel::retry,
            onOpenProduct = onOpenProduct,
            onOpenOrders = onOpenOrders,
            onDraftChange = viewModel::onDraftChange,
            onSend = viewModel::sendDraft,
            onQuickReply = viewModel::sendQuickReply,
            onPickImage = pickImage,
            onSendOffer = viewModel::sendOffer,
            onSendMeetup = viewModel::sendMeetup,
            onRetryPending = viewModel::retryPending,
            onDiscardPending = viewModel::discardPending,
            onOrderAction = viewModel::runOrderAction,
            onUserMessageShown = viewModel::userMessageShown,
        ),
    )
}

/** Các hành động của màn chat, gom lại cho gọn tham số. */
private data class ChatThreadActions(
    val onBack: () -> Unit,
    val onRetry: () -> Unit,
    val onOpenProduct: (Long) -> Unit,
    val onOpenOrders: (OrderRole) -> Unit,
    val onDraftChange: (String) -> Unit,
    val onSend: () -> Unit,
    val onQuickReply: (String) -> Unit,
    val onPickImage: () -> Unit,
    val onSendOffer: (price: Long, note: String) -> Unit,
    val onSendMeetup: (location: String, time: LocalDateTime, note: String) -> Unit,
    val onRetryPending: (localId: Long) -> Unit,
    val onDiscardPending: (localId: Long) -> Unit,
    val onOrderAction: (OrderAction, reason: String) -> Unit,
    val onUserMessageShown: () -> Unit,
)

/** Bảng / hộp thoại đang mở trên màn chat (mỗi lúc chỉ một cái). */
private sealed interface ChatOverlay {
    data object Attach : ChatOverlay
    data object Offer : ChatOverlay
    data object Meetup : ChatOverlay
    data class ConfirmOrderAction(val action: OrderAction) : ChatOverlay
    data class ImageViewer(val index: Int) : ChatOverlay
}

@Composable
private fun ChatThreadContent(state: ChatThreadUiState, actions: ChatThreadActions) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var overlay by remember { mutableStateOf<ChatOverlay?>(null) }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            actions.onUserMessageShown()
        }
    }

    // Toàn bộ ảnh trong cuộc trò chuyện, để vuốt qua lại khi xem ảnh lớn
    val imageUrls = remember(state.messages) {
        state.messages.filter { it.type == MessageType.IMAGE }.mapNotNull { it.imageUrl }
    }
    val partnerPhone = state.partnerPhone

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ChatTopBar(
                state = state,
                onBack = actions.onBack,
                onCall = partnerPhone?.let { phone ->
                    {
                        if (!context.openDialer(phone)) {
                            scope.launch { snackbarHostState.showSnackbar("Không mở được ứng dụng gọi điện") }
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (!state.isLoading && state.errorMessage == null) {
                ChatInputBar(
                    draft = state.draft,
                    onDraftChange = actions.onDraftChange,
                    onSend = actions.onSend,
                    onAttach = { overlay = ChatOverlay.Attach },
                    quickReplies = if (state.showQuickReplies) BUYER_QUICK_REPLIES else emptyList(),
                    onQuickReply = actions.onQuickReply,
                )
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
            state.product?.let { product ->
                ChatProductStrip(product = product, onClick = { actions.onOpenProduct(product.id) })
            }
            state.activeOrder?.let { order ->
                ChatOrderBanner(
                    order = order,
                    myUserId = state.myUserId,
                    isBusy = state.isOrderActionRunning,
                    onAction = { action -> overlay = ChatOverlay.ConfirmOrderAction(action) },
                    onOpenOrders = actions.onOpenOrders,
                )
            }
            val contentModifier = Modifier.weight(1f)
            when {
                state.isLoading -> LoadingView(modifier = contentModifier)
                state.errorMessage != null -> ErrorView(
                    message = state.errorMessage,
                    onRetry = actions.onRetry,
                    modifier = contentModifier,
                )
                state.messages.isEmpty() && state.pending.isEmpty() -> EmptyThread(
                    partnerName = state.partnerName,
                    avatarUrl = state.partner?.avatarUrl,
                    modifier = contentModifier,
                )
                else -> MessageList(
                    state = state,
                    onImageClick = { url ->
                        val index = imageUrls.indexOf(url)
                        if (index >= 0) overlay = ChatOverlay.ImageViewer(index)
                    },
                    onProductClick = actions.onOpenProduct,
                    onRetryPending = actions.onRetryPending,
                    onDiscardPending = actions.onDiscardPending,
                    modifier = contentModifier,
                )
            }
        }
    }

    ChatOverlays(
        overlay = overlay,
        state = state,
        imageUrls = imageUrls,
        actions = actions,
        onOverlayChange = { overlay = it },
    )
}

/** Thanh tiêu đề: ảnh đại diện + tên người kia, vai trò trong giao dịch, nút gọi khi đã chốt hẹn. */
@Composable
private fun ChatTopBar(state: ChatThreadUiState, onBack: () -> Unit, onCall: (() -> Unit)?) {
    val role = when {
        state.product == null -> null
        state.isSeller -> "Người mua"
        else -> "Người bán"
    }
    val subtitle = listOfNotNull(role, state.partner?.universityShortName?.takeIf { it.isNotBlank() })
        .joinToString(" · ")
    AppTopBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                UserAvatar(name = state.partnerName, imageUrl = state.partner?.avatarUrl, size = 36.dp)
                Column {
                    Text(
                        text = state.partnerName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitle.isNotEmpty()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        },
        onBack = onBack,
        actions = {
            if (onCall != null) {
                IconButton(onClick = onCall) {
                    Icon(Icons.Outlined.Phone, contentDescription = "Gọi cho ${state.partnerName}")
                }
            }
        },
    )
}

/** Danh sách tin nhắn: tin mới nhất ở dưới cùng, sát ô nhập. */
@Composable
private fun MessageList(
    state: ChatThreadUiState,
    onImageClick: (String) -> Unit,
    onProductClick: (Long) -> Unit,
    onRetryPending: (Long) -> Unit,
    onDiscardPending: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = remember(state.messages, state.pending, state.myUserId) {
        buildChatItems(messages = state.messages, pending = state.pending, myUserId = state.myUserId)
    }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Có tin mới: tin của mình thì luôn cuộn xuống; tin người kia thì chỉ cuộn khi đang xem cuối danh sách
    val newest = items.firstOrNull()
    LaunchedEffect(newest?.key) {
        val isMine = newest is ChatItem.Pending || (newest is ChatItem.Message && newest.isMine)
        if (isMine || listState.firstVisibleItemIndex <= 1) listState.animateScrollToItem(0)
    }
    val showJumpToLatest by remember { derivedStateOf { listState.firstVisibleItemIndex > 2 } }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            reverseLayout = true,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = Space.screen, vertical = Space.sm),
        ) {
            items(items, key = { it.key }, contentType = { it::class }) { item ->
                when (item) {
                    is ChatItem.DayHeader -> DayHeaderRow(label = item.label)
                    is ChatItem.Message -> MessageRow(
                        item = item,
                        partnerName = state.partnerName,
                        partnerAvatarUrl = state.partner?.avatarUrl,
                        onImageClick = onImageClick,
                        onProductClick = onProductClick,
                    )
                    is ChatItem.Pending -> PendingMessageRow(
                        pending = item.pending,
                        productTitle = state.product?.title,
                        onRetry = { onRetryPending(item.pending.localId) },
                        onDiscard = { onDiscardPending(item.pending.localId) },
                    )
                }
            }
            // reverseLayout: phần tử cuối cùng nằm trên đầu cuộc trò chuyện
            item(key = "safety-notice") { SafetyNoticeRow() }
        }
        if (showJumpToLatest) {
            SmallFloatingActionButton(
                onClick = { scope.launch { listState.animateScrollToItem(0) } },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.BottomEnd).padding(Space.md),
            ) {
                Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = "Xuống tin mới nhất")
            }
        }
    }
}

/** Cuộc trò chuyện chưa có tin nào. */
@Composable
private fun EmptyThread(partnerName: String, avatarUrl: String?, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(Space.xl), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            UserAvatar(name = partnerName, imageUrl = avatarUrl, size = Sizes.avatarLarge)
            Spacer(Modifier.height(Space.md))
            Text(
                text = "Bắt đầu trò chuyện với $partnerName",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Space.xxs))
            Text(
                text = "Hỏi thêm về tình trạng món đồ, trả giá hoặc hẹn thời gian xem hàng.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Space.md))
            SafetyNoticeRow()
        }
    }
}

@Composable
private fun ChatOverlays(
    overlay: ChatOverlay?,
    state: ChatThreadUiState,
    imageUrls: List<String>,
    actions: ChatThreadActions,
    onOverlayChange: (ChatOverlay?) -> Unit,
) {
    val close = { onOverlayChange(null) }
    when (overlay) {
        ChatOverlay.Attach -> AttachSheet(
            canSendOffer = state.canSendOffer,
            canProposeMeetup = state.canProposeMeetup,
            onPickImage = actions.onPickImage,
            onOffer = { onOverlayChange(ChatOverlay.Offer) },
            onMeetup = { onOverlayChange(ChatOverlay.Meetup) },
            onDismiss = close,
        )
        ChatOverlay.Offer -> state.product?.let { product ->
            OfferDialog(
                listedPrice = product.price,
                onSend = { price, note ->
                    close()
                    actions.onSendOffer(price, note)
                },
                onDismiss = close,
            )
        }
        ChatOverlay.Meetup -> state.product?.let { product ->
            MeetupSheet(
                universityName = product.universityName,
                meetupSpots = state.meetupSpots,
                onSend = { location, time, note ->
                    close()
                    actions.onSendMeetup(location, time, note)
                },
                onDismiss = close,
            )
        }
        is ChatOverlay.ConfirmOrderAction -> state.activeOrder?.let { order ->
            OrderActionDialog(
                action = overlay.action,
                order = order,
                myUserId = state.myUserId,
                onConfirm = { reason ->
                    close()
                    actions.onOrderAction(overlay.action, reason)
                },
                onDismiss = close,
            )
        }
        is ChatOverlay.ImageViewer -> if (imageUrls.isNotEmpty()) {
            FullScreenImageViewer(urls = imageUrls, initialPage = overlay.index, onDismiss = close)
        }
        null -> Unit
    }
}
