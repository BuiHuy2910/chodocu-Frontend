package com.chodocu.app.ui.orders

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chodocu.app.data.model.Order
import com.chodocu.app.data.model.OrderRole
import com.chodocu.app.di.appViewModel
import com.chodocu.app.ui.components.AppFilterChip
import com.chodocu.app.ui.components.AppTopBar
import com.chodocu.app.ui.components.CountBadge
import com.chodocu.app.ui.components.EmptyView
import com.chodocu.app.ui.components.ErrorView
import com.chodocu.app.ui.components.LoadingView
import com.chodocu.app.ui.components.OrderAction
import com.chodocu.app.ui.components.OrderActionDialog
import com.chodocu.app.ui.components.PollWhileResumed
import com.chodocu.app.ui.components.RefreshOnResume
import com.chodocu.app.ui.components.openDialer
import com.chodocu.app.ui.theme.Space
import kotlinx.coroutines.launch

/** Cập nhật ngầm danh sách đơn mỗi 30 giây khi đang mở màn hình. */
private const val ORDERS_POLL_MILLIS = 30_000L

/** Màn "Giao dịch": đơn mình mua và đơn người khác mua đồ của mình. */
@Composable
fun OrdersScreen(
    onOpenProduct: (Long) -> Unit,
    onOpenChat: (partnerId: Long, productId: Long?, partnerName: String) -> Unit,
) {
    val viewModel = appViewModel { handle -> OrdersViewModel(orders, session, handle) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    RefreshOnResume { viewModel.refreshSilently() }
    PollWhileResumed(ORDERS_POLL_MILLIS) { viewModel.refreshSilently() }

    OrdersContent(
        state = state,
        onRetry = viewModel::retry,
        onRefresh = viewModel::refresh,
        onSelectRole = viewModel::selectRole,
        onSelectFilter = viewModel::selectFilter,
        onRunAction = viewModel::runAction,
        onUserMessageShown = viewModel::userMessageShown,
        onOpenProduct = { order -> onOpenProduct(order.productId) },
        onOpenChat = { order ->
            val partner = order.partnerOf(state.myUserId)
            onOpenChat(partner.id, order.productId, partner.fullName)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrdersContent(
    state: OrdersUiState,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onSelectRole: (OrderRole) -> Unit,
    onSelectFilter: (OrderFilter) -> Unit,
    onRunAction: (Order, OrderAction, String) -> Unit,
    onUserMessageShown: () -> Unit,
    onOpenProduct: (Order) -> Unit,
    onOpenChat: (Order) -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var confirm by remember { mutableStateOf<Pair<Order, OrderAction>?>(null) }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            onUserMessageShown()
        }
    }
    fun showMessage(text: String) {
        scope.launch { snackbarHostState.showSnackbar(text) }
    }

    val cardActions = OrderCardActions(
        onOpenProduct = onOpenProduct,
        onOpenChat = onOpenChat,
        onCall = { phone -> if (!context.openDialer(phone)) showMessage("Không mở được ứng dụng gọi điện") },
        onCopyPhone = { phone ->
            clipboard.setText(AnnotatedString(phone))
            // Android 13 trở lên tự hiện thông báo đã sao chép
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) showMessage("Đã sao chép số điện thoại")
        },
        onAction = { order, action -> confirm = order to action },
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                AppTopBar(title = "Giao dịch", showDivider = false)
                RoleTabs(state = state, onSelectRole = onSelectRole)
            }
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding),
        ) {
            when {
                state.isLoading -> LoadingView()
                state.errorMessage != null -> ErrorView(message = state.errorMessage, onRetry = onRetry)
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = Space.lg),
                    verticalArrangement = Arrangement.spacedBy(Space.sm),
                ) {
                    item(key = "filters") {
                        FilterChipsRow(state = state, onSelectFilter = onSelectFilter)
                    }
                    if (state.visibleOrders.isEmpty()) {
                        item(key = "empty") { OrdersEmptyView(role = state.role, filter = state.filter) }
                    }
                    items(state.visibleOrders, key = { it.id }) { order ->
                        OrderCard(
                            order = order,
                            myUserId = state.myUserId,
                            isBusy = state.runningOrderId == order.id,
                            actions = cardActions,
                            modifier = Modifier.padding(horizontal = Space.screen),
                        )
                    }
                }
            }
        }
    }

    confirm?.let { (order, action) ->
        OrderActionDialog(
            action = action,
            order = order,
            myUserId = state.myUserId,
            onConfirm = { reason ->
                confirm = null
                onRunAction(order, action, reason)
            },
            onDismiss = { confirm = null },
        )
    }
}

/** Hai tab "Đơn mua" / "Đơn bán", kèm số đơn đang cần xử lý. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoleTabs(state: OrdersUiState, onSelectRole: (OrderRole) -> Unit) {
    val roles = OrderRole.entries
    Column {
        PrimaryTabRow(
            selectedTabIndex = roles.indexOf(state.role),
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            roles.forEach { role ->
                Tab(
                    selected = role == state.role,
                    onClick = { onSelectRole(role) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.xxs)) {
                            Text(role.label, style = MaterialTheme.typography.titleSmall)
                            CountBadge(count = state.activeCount(role))
                        }
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun FilterChipsRow(state: OrdersUiState, onSelectFilter: (OrderFilter) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Space.screen, vertical = Space.xs),
        horizontalArrangement = Arrangement.spacedBy(Space.xs),
    ) {
        items(OrderFilter.entries) { filter ->
            AppFilterChip(
                label = filter.label,
                selected = filter == state.filter,
                onClick = { onSelectFilter(filter) },
                count = state.countFor(filter),
            )
        }
    }
}

@Composable
private fun OrdersEmptyView(role: OrderRole, filter: OrderFilter) {
    val (title, message) = when {
        filter != OrderFilter.ALL -> "Không có đơn nào" to "Chưa có đơn ở mục \"${filter.label}\"."
        role == OrderRole.BUYER ->
            "Chưa có đơn mua" to "Tìm món đồ ưng ý ở trang chủ rồi gửi yêu cầu mua để hẹn gặp người bán."
        else -> "Chưa có ai đặt mua" to "Khi có người gửi yêu cầu mua đồ của bạn, đơn sẽ hiện ở đây."
    }
    EmptyView(
        icon = if (role == OrderRole.BUYER) Icons.Outlined.ShoppingBag else Icons.AutoMirrored.Outlined.ReceiptLong,
        title = title,
        message = message,
        modifier = Modifier.padding(top = Space.xl),
    )
}
