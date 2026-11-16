package com.chodocu.app.ui.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chodocu.app.core.safeCall
import com.chodocu.app.data.common.toUserMessage
import com.chodocu.app.data.model.FeePreview
import com.chodocu.app.data.model.Order
import com.chodocu.app.data.model.OrderInput
import com.chodocu.app.data.model.Product
import com.chodocu.app.data.model.ProductStatus
import com.chodocu.app.data.model.ReportReason
import com.chodocu.app.data.repository.CatalogRepository
import com.chodocu.app.data.repository.OrderRepository
import com.chodocu.app.data.repository.ProductRepository
import com.chodocu.app.data.session.SessionManager
import com.chodocu.app.ui.navigation.NavArgs
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

private const val FEE_DELAY_MILLIS = 400L

/** Dữ liệu form đặt mua. Điểm hẹn và giờ hẹn để trống cho tới khi người mua tự chọn. */
data class OrderForm(
    /** Giá đề nghị, chỉ gồm chữ số. */
    val offeredPrice: String = "",
    val meetupLocation: String? = null,
    val meetupTime: LocalDateTime? = null,
    val note: String = "",
    val priceError: String? = null,
    val locationError: String? = null,
    val timeError: String? = null,
    val fee: FeePreview? = null,
    val isSubmitting: Boolean = false,
)

/** Trạng thái màn chi tiết sản phẩm. */
data class ProductDetailUiState(
    val product: Product? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val currentUserId: Long? = null,
    /** Các điểm hẹn chuẩn của trường đăng tin. */
    val meetupSpots: List<String> = emptyList(),
    val isFavoriteBusy: Boolean = false,
    val isOwnerActionBusy: Boolean = false,
    /** Khác null = đang mở form đặt mua. */
    val orderForm: OrderForm? = null,
    /** Đặt mua thành công: hiện hộp thoại hướng dẫn bước tiếp theo. */
    val placedOrder: Order? = null,
    /** Chủ tin vừa xóa tin: màn hình tự quay lại. */
    val isDeleted: Boolean = false,
    val userMessage: String? = null,
) {
    val isOwner: Boolean get() = product != null && product.seller.id == currentUserId
    val canOrder: Boolean get() = product != null && !isOwner && product.isAvailable
}

/** Xem chi tiết tin, lưu tin, đặt mua (người mua) và quản lý tin (chủ tin). */
class ProductDetailViewModel(
    private val products: ProductRepository,
    private val orders: OrderRepository,
    private val catalog: CatalogRepository,
    session: SessionManager,
    handle: SavedStateHandle,
) : ViewModel() {

    private val productId: Long = handle[NavArgs.PRODUCT_ID] ?: NavArgs.NO_ID

    private val _uiState = MutableStateFlow(ProductDetailUiState(currentUserId = session.currentUser.value?.id))
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private var feeJob: Job? = null

    init {
        load(showLoading = true)
    }

    fun retry() = load(showLoading = true)

    /** Quay lại màn hình (ví dụ sau khi sửa tin): chủ tin cần thấy dữ liệu mới nhất. */
    fun onResume() {
        if (_uiState.value.isOwner) load(showLoading = false)
    }

    private fun load(showLoading: Boolean) {
        if (showLoading) _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            safeCall { products.getProduct(productId) }
                .onSuccess { product ->
                    _uiState.update { it.copy(product = product, isLoading = false) }
                    loadMeetupSpots(product)
                }
                .onFailure { error ->
                    _uiState.update {
                        if (it.product == null) it.copy(isLoading = false, errorMessage = error.toUserMessage())
                        else it.copy(isLoading = false, userMessage = error.toUserMessage())
                    }
                }
        }
    }

    private suspend fun loadMeetupSpots(product: Product) {
        val universityId = product.universityId ?: return
        safeCall { catalog.getUniversities() }.onSuccess { list ->
            val spots = list.firstOrNull { it.id == universityId }?.meetupSpots.orEmpty()
            _uiState.update { it.copy(meetupSpots = spots) }
        }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }

    // ---------------------------------------------------------------------------------------------
    // Người mua: lưu tin, báo cáo
    // ---------------------------------------------------------------------------------------------

    fun toggleFavorite() {
        val product = _uiState.value.product ?: return
        if (_uiState.value.isFavoriteBusy) return
        _uiState.update { it.copy(isFavoriteBusy = true) }
        viewModelScope.launch {
            safeCall { products.toggleFavorite(product.id) }
                .onSuccess { favorited ->
                    _uiState.update { state ->
                        val current = state.product ?: return@update state
                        val count = (current.favoriteCount + if (favorited) 1 else -1).coerceAtLeast(0)
                        state.copy(
                            product = current.copy(isFavorited = favorited, favoriteCount = count),
                            isFavoriteBusy = false,
                            userMessage = if (favorited) "Đã lưu tin vào mục Đã lưu" else "Đã bỏ lưu tin",
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isFavoriteBusy = false, userMessage = error.toUserMessage()) }
                }
        }
    }

    fun report(reason: ReportReason, detail: String) {
        viewModelScope.launch {
            safeCall { products.reportProduct(productId, reason, detail.trim().ifEmpty { null }) }
                .onSuccess {
                    _uiState.update { it.copy(userMessage = "Cảm ơn bạn! Quản trị viên sẽ xem xét báo cáo này.") }
                }
                .onFailure { error -> _uiState.update { it.copy(userMessage = error.toUserMessage()) } }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Người mua: form đặt mua
    // ---------------------------------------------------------------------------------------------

    fun openOrderForm() {
        val product = _uiState.value.product ?: return
        if (!_uiState.value.canOrder) return
        // Giá đề nghị gợi ý bằng giá đăng (người mua sửa được); điểm hẹn, giờ hẹn để trống
        val price = if (product.isFree) "0" else product.price.toString()
        _uiState.update { it.copy(orderForm = OrderForm(offeredPrice = price)) }
        loadFee(price)
    }

    fun closeOrderForm() {
        feeJob?.cancel()
        _uiState.update { it.copy(orderForm = null) }
    }

    fun onOfferedPriceChange(digits: String) {
        updateForm { it.copy(offeredPrice = digits, priceError = null) }
        loadFee(digits)
    }

    fun onMeetupLocationSelected(location: String) = updateForm { it.copy(meetupLocation = location, locationError = null) }

    fun onMeetupTimeSelected(time: LocalDateTime) = updateForm { it.copy(meetupTime = time, timeError = null) }

    fun onNoteChange(note: String) = updateForm { it.copy(note = note) }

    /** Xem trước phí nền tảng (chờ người dùng ngừng gõ giá). */
    private fun loadFee(priceDigits: String) {
        feeJob?.cancel()
        val amount = priceDigits.toLongOrNull() ?: 0L
        if (amount <= 0L) {
            updateForm { it.copy(fee = null) }
            return
        }
        feeJob = viewModelScope.launch {
            delay(FEE_DELAY_MILLIS)
            safeCall { orders.previewFee(amount) }
                .onSuccess { fee -> updateForm { it.copy(fee = fee) } }
                .onFailure { updateForm { it.copy(fee = null) } }
        }
    }

    fun submitOrder() {
        val state = _uiState.value
        val product = state.product ?: return
        val form = state.orderForm ?: return
        if (form.isSubmitting) return

        val price = if (product.isFree) 0L else form.offeredPrice.toLongOrNull()
        val priceError = if (!product.isFree && (price == null || price <= 0L)) "Vui lòng nhập giá bạn muốn trả" else null
        val locationError = if (form.meetupLocation == null) "Vui lòng chọn điểm hẹn" else null
        val time = form.meetupTime
        val timeError = when {
            time == null -> "Vui lòng chọn thời gian hẹn"
            time.isBefore(LocalDateTime.now()) -> "Thời gian hẹn đã qua, vui lòng chọn lại"
            else -> null
        }
        if (priceError != null || locationError != null || timeError != null || price == null || time == null) {
            updateForm { it.copy(priceError = priceError, locationError = locationError, timeError = timeError) }
            return
        }

        val input = OrderInput(
            productId = product.id,
            offeredPrice = price,
            meetupLocation = form.meetupLocation.orEmpty(),
            meetupTime = time,
            note = form.note.trim().ifEmpty { null },
        )
        updateForm { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            safeCall { orders.placeOrder(input) }
                .onSuccess { order -> _uiState.update { it.copy(orderForm = null, placedOrder = order) } }
                .onFailure { error ->
                    updateForm { it.copy(isSubmitting = false) }
                    _uiState.update { it.copy(userMessage = error.toUserMessage()) }
                }
        }
    }

    fun placedOrderShown() {
        _uiState.update { it.copy(placedOrder = null) }
    }

    private fun updateForm(change: (OrderForm) -> OrderForm) {
        _uiState.update { state -> state.orderForm?.let { state.copy(orderForm = change(it)) } ?: state }
    }

    // ---------------------------------------------------------------------------------------------
    // Chủ tin: ẩn / hiện, đánh dấu đã bán, xóa
    // ---------------------------------------------------------------------------------------------

    fun changeStatus(status: ProductStatus) {
        if (_uiState.value.isOwnerActionBusy) return
        _uiState.update { it.copy(isOwnerActionBusy = true) }
        viewModelScope.launch {
            safeCall { products.changeStatus(productId, status) }
                .onSuccess { product ->
                    val message = when (status) {
                        ProductStatus.HIDDEN -> "Đã ẩn tin. Người khác sẽ không thấy tin này."
                        ProductStatus.SOLD -> "Đã đánh dấu là đã bán."
                        else -> "Tin đã hiển thị trở lại."
                    }
                    _uiState.update { it.copy(product = product, isOwnerActionBusy = false, userMessage = message) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isOwnerActionBusy = false, userMessage = error.toUserMessage()) }
                }
        }
    }

    fun delete() {
        if (_uiState.value.isOwnerActionBusy) return
        _uiState.update { it.copy(isOwnerActionBusy = true) }
        viewModelScope.launch {
            safeCall { products.deleteProduct(productId) }
                .onSuccess { _uiState.update { it.copy(isOwnerActionBusy = false, isDeleted = true) } }
                .onFailure { error ->
                    _uiState.update { it.copy(isOwnerActionBusy = false, userMessage = error.toUserMessage()) }
                }
        }
    }
}
