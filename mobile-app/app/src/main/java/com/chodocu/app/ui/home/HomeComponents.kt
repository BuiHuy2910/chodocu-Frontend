package com.chodocu.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.chodocu.app.data.model.Product
import com.chodocu.app.data.model.ProductSort
import com.chodocu.app.ui.components.AppFilterChip
import com.chodocu.app.ui.components.AppTextField
import com.chodocu.app.ui.components.AppWordmark
import com.chodocu.app.ui.components.CountBadge
import com.chodocu.app.ui.components.DropdownChip
import com.chodocu.app.ui.components.ProductCardPlaceholder
import com.chodocu.app.ui.components.ProductGridCard
import com.chodocu.app.ui.components.categoryIcon
import com.chodocu.app.ui.theme.Space

/** Phần đầu trang chủ: logo, nút thông báo và ô tìm kiếm. */
@Composable
internal fun HomeHeader(
    keyword: String,
    unreadNotifications: Int,
    onKeywordChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onOpenNotifications: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = Space.screen, end = Space.xxs, top = Space.xs, bottom = Space.sm),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppWordmark(modifier = Modifier.weight(1f))
                IconButton(onClick = onOpenNotifications) {
                    BadgedBox(badge = { CountBadge(unreadNotifications) }) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Thông báo")
                    }
                }
            }
            AppTextField(
                value = keyword,
                onValueChange = onKeywordChange,
                label = null,
                placeholder = "Tìm sách, đồ điện tử, đồ dùng...",
                leadingIcon = Icons.Outlined.Search,
                trailingContent = if (keyword.isNotEmpty()) {
                    {
                        IconButton(onClick = { onKeywordChange("") }) {
                            Icon(Icons.Outlined.Close, contentDescription = "Xóa từ khóa")
                        }
                    }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                    onSearchSubmit()
                }),
                modifier = Modifier.padding(top = Space.xs, end = Space.sm),
            )
        }
    }
}

/** Hàng chip danh mục + hàng lọc theo trường / sắp xếp. */
@Composable
internal fun HomeFilterBar(
    state: HomeUiState,
    onCategorySelected: (Long?) -> Unit,
    onUniversityClick: () -> Unit,
    onSortClick: () -> Unit,
    onClearFilters: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column {
            LazyRow(
                contentPadding = PaddingValues(horizontal = Space.screen),
                horizontalArrangement = Arrangement.spacedBy(Space.xs),
            ) {
                item {
                    AppFilterChip(
                        label = "Tất cả",
                        selected = state.query.categoryId == null,
                        onClick = { onCategorySelected(null) },
                        leadingIcon = Icons.Outlined.Apps,
                    )
                }
                items(state.categories, key = { it.id }) { category ->
                    AppFilterChip(
                        label = category.name,
                        selected = state.query.categoryId == category.id,
                        onClick = { onCategorySelected(category.id) },
                        leadingIcon = categoryIcon(category.icon),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = Space.screen, end = Space.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.xs),
            ) {
                DropdownChip(
                    label = state.selectedUniversity?.shortName ?: "Tất cả trường",
                    active = state.query.universityId != null,
                    onClick = onUniversityClick,
                    leadingIcon = Icons.Outlined.School,
                )
                DropdownChip(
                    label = state.query.sort.label,
                    active = state.query.sort != ProductSort.NEWEST,
                    onClick = onSortClick,
                    leadingIcon = Icons.AutoMirrored.Outlined.Sort,
                )
                Spacer(Modifier.weight(1f))
                if (state.hasFilters) {
                    TextButton(onClick = onClearFilters) { Text("Xóa lọc", style = MaterialTheme.typography.labelLarge) }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

/** Lưới 2 cột các tin đăng; cuộn tới gần cuối thì tự tải thêm trang mới (không spam lặp). */
@Composable
internal fun ProductGrid(
    products: List<Product>,
    gridState: LazyGridState,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    loadMoreError: String?,
    onLoadMore: () -> Unit,
    onOpenProduct: (Long) -> Unit,
) {
    // Chỉ kích hoạt tải thêm khi người dùng thật sự cuộn gần cuối danh sách (cách 4 item trở xuống)
    val shouldLoadMore by remember(hasMore, isLoadingMore, loadMoreError) {
        derivedStateOf {
            if (!hasMore || isLoadingMore || loadMoreError != null) return@derivedStateOf false
            val lastVisibleItemIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItemsCount = gridState.layoutInfo.totalItemsCount
            totalItemsCount > 0 && lastVisibleItemIndex >= totalItemsCount - 4
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        contentPadding = PaddingValues(Space.screen),
        horizontalArrangement = Arrangement.spacedBy(Space.sm),
        verticalArrangement = Arrangement.spacedBy(Space.sm),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(products, key = { it.id }) { product ->
            ProductGridCard(product = product, onClick = { onOpenProduct(product.id) })
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            GridFooter(
                hasMore = hasMore,
                isLoadingMore = isLoadingMore,
                loadMoreError = loadMoreError,
                onLoadMore = onLoadMore,
            )
        }
    }
}

@Composable
private fun GridFooter(hasMore: Boolean, isLoadingMore: Boolean, loadMoreError: String?, onLoadMore: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = Space.sm),
        contentAlignment = Alignment.Center,
    ) {
        when {
            loadMoreError != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = loadMoreError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onLoadMore) { Text("Thử lại") }
            }
            isLoadingMore || hasMore -> CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            else -> Text(
                text = "Bạn đã xem hết tin đăng",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Khung xương khi đang tải trang đầu. */
@Composable
internal fun ProductGridPlaceholder() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(Space.screen),
        horizontalArrangement = Arrangement.spacedBy(Space.sm),
        verticalArrangement = Arrangement.spacedBy(Space.sm),
        userScrollEnabled = false,
        modifier = Modifier.fillMaxSize(),
    ) {
        items(6) { ProductCardPlaceholder() }
    }
}
