package com.chodocu.app.ui.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chodocu.app.data.common.ApiException
import com.chodocu.app.data.model.OrderRole
import com.chodocu.app.data.model.Product
import com.chodocu.app.data.model.ProductStatus
import com.chodocu.app.data.model.ReportReason
import com.chodocu.app.di.appViewModel
import com.chodocu.app.ui.components.ConfirmDialog
import com.chodocu.app.ui.components.ErrorView
import com.chodocu.app.ui.components.FullScreenImageViewer
import com.chodocu.app.ui.components.LoadingView
import com.chodocu.app.ui.components.PrimaryButton
import com.chodocu.app.ui.components.RefreshOnResume
import com.chodocu.app.ui.components.SecondaryButton
import com.chodocu.app.ui.theme.Space

/** Màn chi tiết một tin đăng. */
@Composable
fun ProductDetailScreen(
    onBack: () -> Unit,
    onOpenChat: (partnerId: Long, productId: Long, partnerName: String) -> Unit,
    onEditProduct: (Long) -> Unit,
    onOpenOrders: (OrderRole) -> Unit,
) {
    val viewModel = appViewModel { handle -> ProductDetailViewModel(products, orders, catalog, session, handle) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    RefreshOnResume { viewModel.onResume() }
    // Xóa tin xong thì quay lại màn trước
    LaunchedEffect(state.isDeleted) { if (state.isDeleted) onBack() }

    val chatWithSeller: () -> Unit = {
        state.product?.let { onOpenChat(it.seller.id, it.id, it.seller.fullName) }
    }
    ProductDetailContent(
        state = state,
        actions = ProductDetailActions(
            onBack = onBack,
            onRetry = viewModel::retry,
            onToggleFavorite = viewModel::toggleFavorite,
            onReport = viewModel::report,
            onChat = chatWithSeller,
            onOpenOrderForm = viewModel::openOrderForm,
            onEdit = { state.product?.let { onEditProduct(it.id) } },
            onChangeStatus = viewModel::changeStatus,
            onDelete = viewModel::delete,
            onOpenSales = { onOpenOrders(OrderRole.SELLER) },
            onUserMessageShown = viewModel::userMessageShown,
        ),
    )

    state.orderForm?.let { form ->
        state.product?.let { product ->
            OrderSheet(
                product = product,
                form = form,
                meetupSpots = state.meetupSpots,
                onPriceChange = viewModel::onOfferedPriceChange,
                onLocationSelected = viewModel::onMeetupLocationSelected,
                onTimeSelected = viewModel::onMeetupTimeSelected,
                onNoteChange = viewModel::onNoteChange,
                onSubmit = viewModel::submitOrder,
                onDismiss = viewModel::closeOrderForm,
            )
        }
    }

    if (state.placedOrder != null) {
        OrderPlacedDialog(
            onViewOrders = {
                viewModel.placedOrderShown()
                onOpenOrders(OrderRole.BUYER)
            },
            onChat = {
                viewModel.placedOrderShown()
                chatWithSeller()
            },
            onDismiss = viewModel::placedOrderShown,
        )
    }
}

/** Các hành động của màn chi tiết. */
data class ProductDetailActions(
    val onBack: () -> Unit,
    val onRetry: () -> Unit,
    val onToggleFavorite: () -> Unit,
    val onReport: (ReportReason, String) -> Unit,
    val onChat: () -> Unit,
    val onOpenOrderForm: () -> Unit,
    val onEdit: () -> Unit,
    val onChangeStatus: (ProductStatus) -> Unit,
    val onDelete: () -> Unit,
    val onOpenSales: () -> Unit,
    val onUserMessageShown: () -> Unit,
)

@Composable
private fun ProductDetailContent(state: ProductDetailUiState, actions: ProductDetailActions) {
    val snackbarHostState = remember { SnackbarHostState() }
    var viewerPage by remember { mutableStateOf<Int?>(null) }
    var showReport by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            actions.onUserMessageShown()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DetailTopBar(
                state = state,
                onBack = actions.onBack,
                onToggleFavorite = actions.onToggleFavorite,
                onReport = { showReport = true },
                onEdit = actions.onEdit,
                onChangeStatus = actions.onChangeStatus,
                onDelete = { showDeleteConfirm = true },
            )
        },
        bottomBar = {
            state.product?.let { product ->
                DetailBottomBar(
                    product = product,
                    isOwner = state.isOwner,
                    canOrder = state.canOrder,
                    onChat = actions.onChat,
                    onOrder = actions.onOpenOrderForm,
                    onEdit = actions.onEdit,
                    onOpenSales = actions.onOpenSales,
                )
            }
        },
    ) { padding ->
        val product = state.product
        val contentModifier = Modifier.padding(padding).consumeWindowInsets(padding)
        when {
            product == null && state.isLoading -> LoadingView(modifier = contentModifier)
            product == null -> ErrorView(
                message = state.errorMessage ?: ApiException.MSG_UNKNOWN,
                onRetry = actions.onRetry,
                modifier = contentModifier,
            )
            else -> LazyColumn(
                modifier = contentModifier,
                contentPadding = PaddingValues(bottom = Space.lg),
                verticalArrangement = Arrangement.spacedBy(Space.sm),
            ) {
                item { ProductGallery(imageUrls = product.imageUrls, title = product.title, onOpenImage = { viewerPage = it }) }
                item { ProductHeader(product = product) }
                item { ProductStatusNotice(product = product, isOwner = state.isOwner) }
                item { ProductInfoCard(product = product) }
                item { ProductDescriptionCard(product = product) }
                item { SellerCard(product = product, isOwner = state.isOwner, onChat = actions.onChat) }
                if (!state.isOwner) item { SafetyTipsCard() }
            }
        }
    }

    val images = state.product?.imageUrls.orEmpty()
    viewerPage?.let { page ->
        if (images.isNotEmpty()) {
            FullScreenImageViewer(urls = images, initialPage = page, onDismiss = { viewerPage = null })
        }
    }
    if (showReport) {
        ReportDialog(
            onSubmit = { reason, detail ->
                showReport = false
                actions.onReport(reason, detail)
            },
            onDismiss = { showReport = false },
        )
    }
    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "Xóa tin đăng?",
            message = "Tin sẽ bị xóa vĩnh viễn. Tin đã có người đặt mua thì không xóa được — khi đó bạn có thể ẩn tin.",
            confirmText = "Xóa tin",
            destructive = true,
            onConfirm = {
                showDeleteConfirm = false
                actions.onDelete()
            },
            onDismiss = { showDeleteConfirm = false },
        )
    }
}

/** Thanh nút dưới cùng: người mua thấy "Nhắn tin" + "Đặt mua", chủ tin thấy "Đơn bán" + "Sửa tin". */
@Composable
private fun DetailBottomBar(
    product: Product,
    isOwner: Boolean,
    canOrder: Boolean,
    onChat: () -> Unit,
    onOrder: () -> Unit,
    onEdit: () -> Unit,
    onOpenSales: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = Space.screen, vertical = Space.sm),
                horizontalArrangement = Arrangement.spacedBy(Space.sm),
            ) {
                if (isOwner) {
                    SecondaryButton(
                        text = "Đơn bán",
                        onClick = onOpenSales,
                        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                        modifier = Modifier.weight(1f),
                    )
                    PrimaryButton(
                        text = "Sửa tin",
                        onClick = onEdit,
                        icon = Icons.Outlined.Edit,
                        enabled = product.status != ProductStatus.IN_TRANSACTION && product.status != ProductStatus.SOLD,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    SecondaryButton(
                        text = "Nhắn tin",
                        onClick = onChat,
                        icon = Icons.AutoMirrored.Outlined.Chat,
                        modifier = Modifier.weight(1f),
                    )
                    PrimaryButton(
                        text = orderButtonText(product),
                        onClick = onOrder,
                        enabled = canOrder,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

private fun orderButtonText(product: Product): String = when (product.status) {
    ProductStatus.APPROVED -> if (product.isFree) "Xin nhận" else "Đặt mua"
    ProductStatus.IN_TRANSACTION -> "Đang giao dịch"
    ProductStatus.SOLD -> "Đã bán"
    else -> "Chưa mở bán"
}

/** Hộp thoại sau khi gửi yêu cầu mua thành công. */
@Composable
private fun OrderPlacedDialog(onViewOrders: () -> Unit, onChat: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Đã gửi yêu cầu mua", style = MaterialTheme.typography.titleLarge) },
        text = {
            Text(
                text = "Người bán sẽ xác nhận lịch hẹn của bạn. Theo dõi trạng thái trong mục Giao dịch > Đơn mua. " +
                    "Bạn chỉ thanh toán khi gặp mặt và kiểm tra hàng.",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = { TextButton(onClick = onViewOrders) { Text("Xem đơn mua") } },
        dismissButton = { TextButton(onClick = onChat) { Text("Nhắn người bán") } },
        containerColor = MaterialTheme.colorScheme.surface,
    )
}
