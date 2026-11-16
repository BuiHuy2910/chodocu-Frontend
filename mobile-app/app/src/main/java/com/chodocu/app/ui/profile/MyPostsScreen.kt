package com.chodocu.app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chodocu.app.data.model.Product
import com.chodocu.app.data.model.ProductStatus
import com.chodocu.app.di.appViewModel
import com.chodocu.app.ui.components.AppFilterChip
import com.chodocu.app.ui.components.AppTopBar
import com.chodocu.app.ui.components.ConfirmDialog
import com.chodocu.app.ui.components.EmptyView
import com.chodocu.app.ui.components.ErrorView
import com.chodocu.app.ui.components.InlineBanner
import com.chodocu.app.ui.components.LoadingView
import com.chodocu.app.ui.components.ProductRowCard
import com.chodocu.app.ui.components.RefreshOnResume
import com.chodocu.app.ui.components.StatusTone
import com.chodocu.app.ui.theme.Space

/** Màn "Tin đăng của tôi": lọc theo trạng thái, sửa / ẩn / đánh dấu đã bán / xóa tin. */
@Composable
fun MyPostsScreen(
    onBack: () -> Unit,
    onOpenProduct: (Long) -> Unit,
    onEditProduct: (Long) -> Unit,
    onCreatePost: () -> Unit,
) {
    val viewModel = appViewModel { _ -> MyPostsViewModel(products) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnResume { viewModel.refreshSilently() }

    MyPostsContent(
        state = state,
        onBack = onBack,
        onRetry = viewModel::retry,
        onRefresh = viewModel::refresh,
        onSelectStatus = viewModel::selectStatus,
        onUserMessageShown = viewModel::userMessageShown,
        onCreatePost = onCreatePost,
        itemActions = PostItemActions(
            onOpen = { onOpenProduct(it.id) },
            onEdit = { onEditProduct(it.id) },
            onChangeStatus = viewModel::changeStatus,
            onDelete = viewModel::delete,
        ),
    )
}

/** Các hành động trên một tin trong danh sách. */
private data class PostItemActions(
    val onOpen: (Product) -> Unit,
    val onEdit: (Product) -> Unit,
    val onChangeStatus: (Product, ProductStatus) -> Unit,
    val onDelete: (Product) -> Unit,
)

/** Thao tác cần hỏi lại trước khi làm. */
private sealed interface PendingConfirm {
    data class MarkSold(val product: Product) : PendingConfirm
    data class Delete(val product: Product) : PendingConfirm
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyPostsContent(
    state: MyPostsUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onSelectStatus: (ProductStatus?) -> Unit,
    onUserMessageShown: () -> Unit,
    onCreatePost: () -> Unit,
    itemActions: PostItemActions,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var confirm by remember { mutableStateOf<PendingConfirm?>(null) }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            onUserMessageShown()
        }
    }

    // Đánh dấu đã bán và xóa tin cần hỏi lại; các thao tác khác làm ngay
    val actions = itemActions.copy(
        onChangeStatus = { product, status ->
            if (status == ProductStatus.SOLD) confirm = PendingConfirm.MarkSold(product)
            else itemActions.onChangeStatus(product, status)
        },
        onDelete = { product -> confirm = PendingConfirm.Delete(product) },
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AppTopBar(title = "Tin đăng của tôi", onBack = onBack) },
        floatingActionButton = {
            if (!state.isLoading && state.errorMessage == null && state.products.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onCreatePost,
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    text = { Text("Đăng tin") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
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
                state.products.isEmpty() -> EmptyView(
                    icon = Icons.Outlined.Inventory,
                    title = "Bạn chưa đăng tin nào",
                    message = "Đăng món đồ bạn không dùng nữa để các bạn trong trường mua lại.",
                    actionLabel = "Đăng tin đầu tiên",
                    actionIcon = Icons.Outlined.Add,
                    onAction = onCreatePost,
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    // Chừa chỗ cho nút "Đăng tin" nổi ở góc dưới
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(Space.sm),
                ) {
                    item(key = "filters") { StatusFilterRow(state = state, onSelectStatus = onSelectStatus) }
                    items(state.visibleProducts, key = { it.id }) { product ->
                        MyPostItem(
                            product = product,
                            isBusy = state.busyProductId == product.id,
                            actions = actions,
                            modifier = Modifier.padding(horizontal = Space.screen),
                        )
                    }
                }
            }
        }
    }

    when (val pending = confirm) {
        is PendingConfirm.MarkSold -> ConfirmDialog(
            title = "Đánh dấu đã bán?",
            message = "Tin sẽ không còn hiện trên trang chủ. Các yêu cầu mua đang chờ sẽ tự động bị từ chối.",
            confirmText = "Đã bán",
            onConfirm = {
                confirm = null
                itemActions.onChangeStatus(pending.product, ProductStatus.SOLD)
            },
            onDismiss = { confirm = null },
        )
        is PendingConfirm.Delete -> ConfirmDialog(
            title = "Xóa tin đăng?",
            message = "Tin \"${pending.product.title}\" sẽ bị xóa vĩnh viễn và không thể khôi phục.",
            confirmText = "Xóa",
            destructive = true,
            icon = Icons.Outlined.DeleteOutline,
            onConfirm = {
                confirm = null
                itemActions.onDelete(pending.product)
            },
            onDismiss = { confirm = null },
        )
        null -> Unit
    }
}

/** Chip lọc theo trạng thái, chỉ hiện những trạng thái đang có tin. */
@Composable
private fun StatusFilterRow(state: MyPostsUiState, onSelectStatus: (ProductStatus?) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Space.screen, vertical = Space.xs),
        horizontalArrangement = Arrangement.spacedBy(Space.xs),
    ) {
        item {
            AppFilterChip(
                label = "Tất cả",
                selected = state.statusFilter == null,
                onClick = { onSelectStatus(null) },
                count = state.countOf(null),
            )
        }
        items(state.availableStatuses) { status ->
            AppFilterChip(
                label = status.label,
                selected = state.statusFilter == status,
                onClick = { onSelectStatus(status) },
                count = state.countOf(status),
            )
        }
    }
}

/** Một tin: thẻ sản phẩm + menu thao tác, kèm lý do bị từ chối / ghi chú chờ duyệt nếu có. */
@Composable
private fun MyPostItem(
    product: Product,
    isBusy: Boolean,
    actions: PostItemActions,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Space.xxs)) {
        ProductRowCard(
            product = product,
            onClick = { actions.onOpen(product) },
            trailing = {
                if (isBusy) {
                    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    }
                } else {
                    PostOverflowMenu(product = product, actions = actions)
                }
            },
        )
        when (product.status) {
            ProductStatus.REJECTED -> Column {
                InlineBanner(
                    title = "Tin bị từ chối",
                    text = product.rejectReason?.takeIf { it.isNotBlank() } ?: "Tin chưa đạt yêu cầu kiểm duyệt.",
                    tone = StatusTone.DANGER,
                )
                TextButton(onClick = { actions.onEdit(product) }) { Text("Sửa và gửi duyệt lại") }
            }
            ProductStatus.PENDING -> InlineBanner(
                text = "Tin đang chờ kiểm duyệt và sẽ hiện trên trang chủ sau khi được duyệt.",
                tone = StatusTone.WARNING,
            )
            ProductStatus.IN_TRANSACTION -> InlineBanner(
                text = "Đang có giao dịch đã chốt hẹn. Xem ở mục Đơn bán.",
                tone = StatusTone.INFO,
            )
            else -> Unit
        }
    }
}

@Composable
private fun PostOverflowMenu(product: Product, actions: PostItemActions) {
    var expanded by remember { mutableStateOf(false) }
    val status = product.status
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Outlined.MoreVert, contentDescription = "Thao tác với tin")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            fun select(action: () -> Unit) {
                expanded = false
                action()
            }
            // Các bước chuyển trạng thái khớp quy tắc của máy chủ
            if (status != ProductStatus.IN_TRANSACTION && status != ProductStatus.SOLD) {
                MenuItem("Sửa tin", Icons.Outlined.Edit) { select { actions.onEdit(product) } }
            }
            if (status == ProductStatus.APPROVED) {
                MenuItem("Ẩn tin", Icons.Outlined.VisibilityOff) { select { actions.onChangeStatus(product, ProductStatus.HIDDEN) } }
            }
            if (status == ProductStatus.HIDDEN) {
                MenuItem("Hiện lại tin", Icons.Outlined.Visibility) {
                    select { actions.onChangeStatus(product, ProductStatus.APPROVED) }
                }
            }
            if (status == ProductStatus.APPROVED || status == ProductStatus.IN_TRANSACTION) {
                MenuItem("Đánh dấu đã bán", Icons.Outlined.TaskAlt) { select { actions.onChangeStatus(product, ProductStatus.SOLD) } }
            }
            MenuItem("Xóa tin", Icons.Outlined.DeleteOutline, destructive = true) { select { actions.onDelete(product) } }
        }
    }
}

@Composable
private fun MenuItem(text: String, icon: ImageVector, destructive: Boolean = false, onClick: () -> Unit) {
    val color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    DropdownMenuItem(
        text = { Text(text, color = color) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = color) },
        onClick = onClick,
    )
}
