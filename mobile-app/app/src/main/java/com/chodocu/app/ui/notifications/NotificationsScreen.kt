package com.chodocu.app.ui.notifications

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chodocu.app.core.formatRelativeTime
import com.chodocu.app.data.model.AppNotification
import com.chodocu.app.data.model.OrderRole
import com.chodocu.app.di.appViewModel
import com.chodocu.app.ui.components.AppTopBar
import com.chodocu.app.ui.components.EmptyView
import com.chodocu.app.ui.components.ErrorView
import com.chodocu.app.ui.components.LoadingView
import com.chodocu.app.ui.components.StatusTone
import com.chodocu.app.ui.components.toneColors
import com.chodocu.app.ui.theme.BrandOrange
import com.chodocu.app.ui.theme.Space

/** Màn danh sách thông báo (duyệt tin, đơn mua mới, đơn được chấp nhận...). */
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onOpenProduct: (Long) -> Unit,
    onOpenMyPosts: () -> Unit,
    onOpenOrders: (OrderRole) -> Unit,
) {
    val viewModel = appViewModel { _ -> NotificationsViewModel(notifications) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Bấm vào thông báo: mở đúng nơi liên quan
    val openTarget: (AppNotification) -> Unit = { notification ->
        val referenceId = notification.referenceId
        when {
            notification.type == "PRODUCT_APPROVED" && referenceId != null -> onOpenProduct(referenceId)
            notification.isProductRelated -> onOpenMyPosts()
            notification.isOrderRelated -> onOpenOrders(orderTabFor(notification))
        }
    }

    NotificationsContent(
        state = state,
        onBack = onBack,
        onRetry = viewModel::retry,
        onRefresh = viewModel::refresh,
        onUserMessageShown = viewModel::userMessageShown,
        onOpen = openTarget,
    )
}

/** Thông báo đơn hàng thuộc tab nào: người bán nhận đơn mới / đơn bị hủy, người mua nhận phần còn lại. */
private fun orderTabFor(notification: AppNotification): OrderRole = when (notification.type) {
    "ORDER_RECEIVED", "ORDER_CANCELLED" -> OrderRole.SELLER
    else -> OrderRole.BUYER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsContent(
    state: NotificationsUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onUserMessageShown: () -> Unit,
    onOpen: (AppNotification) -> Unit,
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
        topBar = { AppTopBar(title = "Thông báo", onBack = onBack) },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding),
        ) {
            when {
                state.isLoading -> LoadingView()
                state.errorMessage != null -> ErrorView(message = state.errorMessage, onRetry = onRetry)
                state.items.isEmpty() -> Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    EmptyView(
                        icon = Icons.Outlined.NotificationsNone,
                        title = "Chưa có thông báo",
                        message = "Kết quả duyệt tin và cập nhật đơn hàng sẽ hiện ở đây.",
                        modifier = Modifier.padding(top = Space.xxl),
                    )
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = Space.lg),
                ) {
                    items(state.items, key = { it.id }) { notification ->
                        NotificationRow(notification = notification, onClick = { onOpen(notification) })
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(notification: AppNotification, onClick: () -> Unit) {
    val (icon, tone) = notification.visual()
    val tones = tone.toneColors()
    val background = if (notification.isRead) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = Space.screen, vertical = Space.sm),
        horizontalArrangement = Arrangement.spacedBy(Space.sm),
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(tones.container, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tones.content, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = formatRelativeTime(notification.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (!notification.isRead) {
            Box(
                modifier = Modifier
                    .padding(top = Space.xs)
                    .size(8.dp)
                    .background(BrandOrange, CircleShape),
            )
        }
    }
}

/** Biểu tượng và tông màu theo loại thông báo (loại lạ dùng biểu tượng chuông mặc định). */
private fun AppNotification.visual(): Pair<ImageVector, StatusTone> = when (type) {
    "PRODUCT_APPROVED" -> Icons.Outlined.CheckCircle to StatusTone.SUCCESS
    "PRODUCT_REJECTED" -> Icons.Outlined.Block to StatusTone.DANGER
    "PRODUCT_HIDDEN" -> Icons.Outlined.VisibilityOff to StatusTone.WARNING
    "ORDER_RECEIVED" -> Icons.Outlined.ShoppingBag to StatusTone.INFO
    "ORDER_ACCEPTED" -> Icons.Outlined.Handshake to StatusTone.SUCCESS
    "ORDER_REJECTED", "ORDER_CANCELLED" -> Icons.Outlined.Cancel to StatusTone.DANGER
    "ORDER_COMPLETED" -> Icons.Outlined.TaskAlt to StatusTone.SUCCESS
    "ORDER_DISPUTED" -> Icons.Outlined.Gavel to StatusTone.WARNING
    else -> Icons.Outlined.Notifications to StatusTone.NEUTRAL
}
