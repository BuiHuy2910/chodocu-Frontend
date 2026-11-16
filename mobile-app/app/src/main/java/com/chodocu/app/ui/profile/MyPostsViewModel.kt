package com.chodocu.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chodocu.app.core.safeCall
import com.chodocu.app.data.common.toUserMessage
import com.chodocu.app.data.model.Product
import com.chodocu.app.data.model.ProductStatus
import com.chodocu.app.data.repository.ProductRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Trạng thái màn "Tin đăng của tôi". [statusFilter] = null nghĩa là xem tất cả. */
data class MyPostsUiState(
    val products: List<Product> = emptyList(),
    val statusFilter: ProductStatus? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    /** Tin đang được thao tác (ẩn, xóa...). */
    val busyProductId: Long? = null,
    val userMessage: String? = null,
) {
    val visibleProducts: List<Product>
        get() = if (statusFilter == null) products else products.filter { it.status == statusFilter }

    /** Chỉ hiện chip của các trạng thái đang có tin, theo thứ tự cố định. */
    val availableStatuses: List<ProductStatus>
        get() = ProductStatus.entries.filter { status -> products.any { it.status == status } }

    fun countOf(status: ProductStatus?): Int =
        if (status == null) products.size else products.count { it.status == status }
}

/** Danh sách tin mình đã đăng và các thao tác của chủ tin. */
class MyPostsViewModel(private val products: ProductRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MyPostsUiState())
    val uiState: StateFlow<MyPostsUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        fetch(showErrors = true)
    }

    fun retry() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        fetch(showErrors = true)
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        fetch(showErrors = true)
    }

    /** Quay lại từ màn sửa tin / chi tiết: tải lại ngầm. */
    fun refreshSilently() {
        if (loadJob?.isActive == true) return
        fetch(showErrors = false)
    }

    fun selectStatus(status: ProductStatus?) {
        _uiState.update { it.copy(statusFilter = status) }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }

    /** Ẩn tin, hiện lại tin hoặc đánh dấu đã bán. */
    fun changeStatus(product: Product, status: ProductStatus) {
        runOnProduct(product) {
            val updated = products.changeStatus(product.id, status)
            _uiState.update { state ->
                state.copy(products = state.products.map { if (it.id == updated.id) updated else it })
            }
            when (status) {
                ProductStatus.HIDDEN -> "Đã ẩn tin. Người khác sẽ không thấy tin này."
                ProductStatus.SOLD -> "Đã đánh dấu là đã bán."
                else -> "Tin đã hiển thị trở lại."
            }
        }
    }

    fun delete(product: Product) {
        runOnProduct(product) {
            products.deleteProduct(product.id)
            _uiState.update { state -> state.copy(products = state.products.filterNot { it.id == product.id }) }
            "Đã xóa tin \"${product.title}\"."
        }
    }

    /** Chạy một thao tác trên tin; [block] trả về câu báo thành công. */
    private fun runOnProduct(product: Product, block: suspend () -> String) {
        if (_uiState.value.busyProductId != null) return
        _uiState.update { it.copy(busyProductId = product.id) }
        viewModelScope.launch {
            safeCall { block() }
                .onSuccess { message ->
                    _uiState.update { state ->
                        // Bộ lọc đang chọn không còn tin nào thì quay về "Tất cả"
                        val filter = state.statusFilter?.takeIf { status -> state.products.any { it.status == status } }
                        state.copy(busyProductId = null, statusFilter = filter, userMessage = message)
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(busyProductId = null, userMessage = error.toUserMessage()) }
                }
        }
    }

    private fun fetch(showErrors: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            safeCall { products.getMyProducts() }
                .onSuccess { list ->
                    _uiState.update { state ->
                        val filter = state.statusFilter?.takeIf { status -> list.any { it.status == status } }
                        state.copy(
                            products = list,
                            statusFilter = filter,
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { error ->
                    val message = error.toUserMessage()
                    _uiState.update {
                        when {
                            !showErrors -> it.copy(isLoading = false, isRefreshing = false)
                            it.products.isNotEmpty() -> it.copy(isLoading = false, isRefreshing = false, userMessage = message)
                            else -> it.copy(isLoading = false, isRefreshing = false, errorMessage = message)
                        }
                    }
                }
        }
    }
}
