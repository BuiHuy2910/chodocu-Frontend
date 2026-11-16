package com.chodocu.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chodocu.app.core.safeCall
import com.chodocu.app.data.common.toUserMessage
import com.chodocu.app.data.model.Category
import com.chodocu.app.data.model.Product
import com.chodocu.app.data.model.ProductQuery
import com.chodocu.app.data.model.ProductSort
import com.chodocu.app.data.model.University
import com.chodocu.app.data.repository.CatalogRepository
import com.chodocu.app.data.repository.ProductRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 20
private const val SEARCH_DELAY_MILLIS = 400L

/** Trạng thái trang chủ: bộ lọc, danh sách tin và các trạng thái tải. */
data class HomeUiState(
    /** Chữ đang gõ trong ô tìm kiếm (áp dụng vào [query] sau một nhịp ngắn). */
    val keyword: String = "",
    val query: ProductQuery = ProductQuery(),
    val categories: List<Category> = emptyList(),
    val universities: List<University> = emptyList(),
    val products: List<Product> = emptyList(),
    /** Đang tải trang đầu (lần đầu hoặc sau khi đổi bộ lọc). */
    val isLoading: Boolean = true,
    /** Đang kéo để làm mới. */
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    /** Lỗi khi chưa có dữ liệu nào để hiển thị. */
    val errorMessage: String? = null,
    val loadMoreError: String? = null,
    val userMessage: String? = null,
) {
    val selectedUniversity: University? get() = universities.firstOrNull { it.id == query.universityId }

    val hasFilters: Boolean
        get() = query.keyword.isNotBlank() || query.categoryId != null ||
            query.universityId != null || query.sort != ProductSort.NEWEST
}

/** Trang chủ: tìm kiếm, lọc theo danh mục / trường, sắp xếp và tải thêm khi cuộn. */
class HomeViewModel(
    private val products: ProductRepository,
    private val catalog: CatalogRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var nextPage = 0
    private var loadJob: Job? = null
    private var searchJob: Job? = null

    init {
        loadCatalog()
        loadFirstPage(isRefresh = false)
    }

    private fun loadCatalog() {
        viewModelScope.launch {
            safeCall { catalog.getCategories() }
                .onSuccess { list -> _uiState.update { it.copy(categories = list) } }
            safeCall { catalog.getUniversities() }
                .onSuccess { list -> _uiState.update { it.copy(universities = list) } }
        }
    }

    /** Gõ tới đâu tìm tới đó, nhưng chờ người dùng ngừng gõ một chút để không gọi máy chủ liên tục. */
    fun onKeywordChange(value: String) {
        _uiState.update { it.copy(keyword = value) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DELAY_MILLIS)
            applyQuery { it.copy(keyword = value.trim()) }
        }
    }

    fun onSearchSubmit() {
        searchJob?.cancel()
        applyQuery { it.copy(keyword = _uiState.value.keyword.trim()) }
    }

    fun onCategorySelected(categoryId: Long?) = applyQuery { it.copy(categoryId = categoryId) }

    fun onUniversitySelected(universityId: Long?) = applyQuery { it.copy(universityId = universityId) }

    fun onSortSelected(sort: ProductSort) = applyQuery { it.copy(sort = sort) }

    fun clearFilters() {
        searchJob?.cancel()
        _uiState.update { it.copy(keyword = "") }
        applyQuery { ProductQuery() }
    }

    fun retry() = loadFirstPage(isRefresh = false)

    fun refresh() = loadFirstPage(isRefresh = true)

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun applyQuery(change: (ProductQuery) -> ProductQuery) {
        val newQuery = change(_uiState.value.query)
        if (newQuery == _uiState.value.query) return
        _uiState.update { it.copy(query = newQuery) }
        loadFirstPage(isRefresh = false)
    }

    private fun loadFirstPage(isRefresh: Boolean) {
        loadJob?.cancel()
        _uiState.update {
            it.copy(
                isLoading = !isRefresh,
                isRefreshing = isRefresh,
                isLoadingMore = false,
                errorMessage = null,
                loadMoreError = null,
            )
        }
        val query = _uiState.value.query
        loadJob = viewModelScope.launch {
            safeCall { products.searchProducts(query, page = 0, pageSize = PAGE_SIZE) }
                .onSuccess { page ->
                    nextPage = 1
                    _uiState.update {
                        it.copy(products = page.items, hasMore = page.hasMore, isLoading = false, isRefreshing = false)
                    }
                }
                .onFailure { error ->
                    val message = error.toUserMessage()
                    _uiState.update {
                        if (isRefresh && it.products.isNotEmpty()) {
                            // Làm mới thất bại: giữ danh sách cũ, chỉ báo lỗi nhỏ
                            it.copy(isRefreshing = false, userMessage = message)
                        } else {
                            it.copy(
                                products = emptyList(),
                                hasMore = false,
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = message,
                            )
                        }
                    }
                }
        }
    }

    /** Tải trang kế tiếp khi người dùng cuộn gần cuối danh sách. */
    fun loadMore() {
        val state = _uiState.value
        if (state.isLoading || state.isRefreshing || state.isLoadingMore || !state.hasMore) return

        _uiState.update { it.copy(isLoadingMore = true, loadMoreError = null) }
        val page = nextPage
        loadJob = viewModelScope.launch {
            safeCall { products.searchProducts(state.query, page = page, pageSize = PAGE_SIZE) }
                .onSuccess { result ->
                    nextPage = page + 1
                    _uiState.update { current ->
                        // Bỏ tin trùng (tin mới đăng có thể đẩy tin cũ sang trang sau)
                        val knownIds = current.products.map { it.id }.toSet()
                        current.copy(
                            products = current.products + result.items.filterNot { it.id in knownIds },
                            hasMore = result.hasMore,
                            isLoadingMore = false,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoadingMore = false, loadMoreError = error.toUserMessage()) }
                }
        }
    }
}
