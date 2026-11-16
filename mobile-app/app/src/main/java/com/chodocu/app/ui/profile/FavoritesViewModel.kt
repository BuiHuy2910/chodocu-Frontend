package com.chodocu.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chodocu.app.core.safeCall
import com.chodocu.app.data.common.toUserMessage
import com.chodocu.app.data.model.Product
import com.chodocu.app.data.repository.ProductRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Món vừa bỏ lưu, giữ lại vị trí cũ để "Hoàn tác". */
data class RemovedFavorite(val product: Product, val index: Int)

/** Trạng thái màn "Món đồ đã lưu". */
data class FavoritesUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val lastRemoved: RemovedFavorite? = null,
    val userMessage: String? = null,
)

/** Danh sách món đồ đã lưu; bỏ lưu ngay trong danh sách và có thể hoàn tác. */
class FavoritesViewModel(private val products: ProductRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var removeJob: Job? = null

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

    fun refreshSilently() {
        if (loadJob?.isActive == true || removeJob?.isActive == true) return
        fetch(showErrors = false)
    }

    /** Bỏ lưu: ẩn khỏi danh sách ngay, lỗi thì trả lại chỗ cũ. */
    fun remove(product: Product) {
        val index = _uiState.value.products.indexOfFirst { it.id == product.id }
        if (index < 0) return
        _uiState.update { state ->
            state.copy(
                products = state.products.filterNot { it.id == product.id },
                lastRemoved = RemovedFavorite(product, index),
            )
        }
        removeJob = viewModelScope.launch {
            safeCall { setFavorite(product.id, favorite = false) }
                .onFailure { error ->
                    _uiState.update { it.copy(products = it.products.insertAt(index, product), lastRemoved = null, userMessage = error.toUserMessage()) }
                }
        }
    }

    /** Hoàn tác lần bỏ lưu gần nhất. */
    fun undoRemove() {
        val removed = _uiState.value.lastRemoved ?: return
        _uiState.update { it.copy(products = it.products.insertAt(removed.index, removed.product), lastRemoved = null) }
        viewModelScope.launch {
            removeJob?.join() // đợi lệnh bỏ lưu chạy xong rồi mới lưu lại
            safeCall { setFavorite(removed.product.id, favorite = true) }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            products = state.products.filterNot { it.id == removed.product.id },
                            userMessage = error.toUserMessage(),
                        )
                    }
                }
        }
    }

    /** Thông báo "Đã bỏ lưu" đã ẩn mà không hoàn tác. */
    fun removalShown() {
        _uiState.update { it.copy(lastRemoved = null) }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }

    /** Máy chủ chỉ có lệnh "đảo trạng thái lưu": gọi thêm lần nữa nếu kết quả chưa đúng ý. */
    private suspend fun setFavorite(productId: Long, favorite: Boolean) {
        if (products.toggleFavorite(productId) != favorite) products.toggleFavorite(productId)
    }

    private fun List<Product>.insertAt(index: Int, product: Product): List<Product> =
        if (any { it.id == product.id }) this
        else toMutableList().apply { add(index.coerceIn(0, size), product) }

    private fun fetch(showErrors: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            safeCall { products.getFavorites() }
                .onSuccess { list ->
                    _uiState.update {
                        it.copy(products = list, isLoading = false, isRefreshing = false, errorMessage = null)
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
