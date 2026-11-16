package com.chodocu.app.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chodocu.app.data.model.ProductSort
import com.chodocu.app.data.model.University
import com.chodocu.app.di.appViewModel
import com.chodocu.app.ui.components.EmptyView
import com.chodocu.app.ui.components.ErrorView
import com.chodocu.app.ui.components.OptionPickerSheet
import com.chodocu.app.ui.theme.Space

/** Trang chủ: danh sách đồ cũ đang bán, có tìm kiếm và bộ lọc. */
@Composable
fun HomeScreen(unreadNotifications: Int, onOpenProduct: (Long) -> Unit, onOpenNotifications: () -> Unit) {
    val viewModel = appViewModel { _ -> HomeViewModel(products, catalog) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(
        state = state,
        unreadNotifications = unreadNotifications,
        actions = HomeActions(
            onKeywordChange = viewModel::onKeywordChange,
            onSearchSubmit = viewModel::onSearchSubmit,
            onCategorySelected = viewModel::onCategorySelected,
            onUniversitySelected = viewModel::onUniversitySelected,
            onSortSelected = viewModel::onSortSelected,
            onClearFilters = viewModel::clearFilters,
            onRetry = viewModel::retry,
            onRefresh = viewModel::refresh,
            onLoadMore = viewModel::loadMore,
            onUserMessageShown = viewModel::userMessageShown,
            onOpenProduct = onOpenProduct,
            onOpenNotifications = onOpenNotifications,
        ),
    )
}

/** Gom các hành động của trang chủ để hàm giao diện không có quá nhiều tham số. */
data class HomeActions(
    val onKeywordChange: (String) -> Unit,
    val onSearchSubmit: () -> Unit,
    val onCategorySelected: (Long?) -> Unit,
    val onUniversitySelected: (Long?) -> Unit,
    val onSortSelected: (ProductSort) -> Unit,
    val onClearFilters: () -> Unit,
    val onRetry: () -> Unit,
    val onRefresh: () -> Unit,
    val onLoadMore: () -> Unit,
    val onUserMessageShown: () -> Unit,
    val onOpenProduct: (Long) -> Unit,
    val onOpenNotifications: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(state: HomeUiState, unreadNotifications: Int, actions: HomeActions) {
    val snackbarHostState = remember { SnackbarHostState() }
    val gridState = rememberLazyGridState()
    var showUniversitySheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            actions.onUserMessageShown()
        }
    }
    // Đổi bộ lọc thì cuộn về đầu danh sách
    LaunchedEffect(state.query) { gridState.scrollToItem(0) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            HomeHeader(
                keyword = state.keyword,
                unreadNotifications = unreadNotifications,
                onKeywordChange = actions.onKeywordChange,
                onSearchSubmit = actions.onSearchSubmit,
                onOpenNotifications = actions.onOpenNotifications,
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).consumeWindowInsets(padding)) {
            HomeFilterBar(
                state = state,
                onCategorySelected = actions.onCategorySelected,
                onUniversityClick = { showUniversitySheet = true },
                onSortClick = { showSortSheet = true },
                onClearFilters = actions.onClearFilters,
            )
            // Đã có danh sách cũ mà đang tải theo bộ lọc mới: chỉ hiện thanh tiến trình mảnh
            if (state.isLoading && state.products.isNotEmpty()) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = actions.onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                when {
                    state.isLoading && state.products.isEmpty() -> ProductGridPlaceholder()
                    state.errorMessage != null -> ErrorView(message = state.errorMessage, onRetry = actions.onRetry)
                    state.products.isEmpty() -> HomeEmptyState(hasFilters = state.hasFilters, onClearFilters = actions.onClearFilters)
                    else -> ProductGrid(
                        products = state.products,
                        gridState = gridState,
                        hasMore = state.hasMore,
                        isLoadingMore = state.isLoadingMore,
                        loadMoreError = state.loadMoreError,
                        onLoadMore = actions.onLoadMore,
                        onOpenProduct = actions.onOpenProduct,
                    )
                }
            }
        }
    }

    if (showUniversitySheet) {
        // null = "Tất cả trường" (không lọc theo trường)
        OptionPickerSheet<University?>(
            title = "Lọc theo trường",
            options = listOf(null) + state.universities,
            selected = state.selectedUniversity,
            optionLabel = { it?.name ?: "Tất cả trường" },
            optionSupportingText = { it?.shortName },
            onSelect = { actions.onUniversitySelected(it?.id) },
            onDismiss = { showUniversitySheet = false },
            searchable = state.universities.size > 8,
        )
    }
    if (showSortSheet) {
        OptionPickerSheet(
            title = "Sắp xếp theo",
            options = ProductSort.entries,
            selected = state.query.sort,
            optionLabel = { it.label },
            onSelect = actions.onSortSelected,
            onDismiss = { showSortSheet = false },
            searchable = false,
        )
    }
}

/** Không có kết quả. Bọc trong vùng cuộn để vẫn kéo xuống làm mới được. */
@Composable
private fun HomeEmptyState(hasFilters: Boolean, onClearFilters: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        EmptyView(
            icon = Icons.Outlined.SearchOff,
            title = if (hasFilters) "Không tìm thấy món đồ phù hợp" else "Chưa có tin đăng nào",
            message = if (hasFilters) {
                "Thử từ khóa khác hoặc bỏ bớt bộ lọc nhé."
            } else {
                "Hãy là người đầu tiên đăng bán món đồ bạn không còn dùng."
            },
            actionLabel = if (hasFilters) "Xóa bộ lọc" else null,
            onAction = if (hasFilters) onClearFilters else null,
            modifier = Modifier.padding(top = Space.xxl),
        )
    }
}
