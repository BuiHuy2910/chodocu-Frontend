package com.chodocu.app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chodocu.app.data.model.ProductStatus
import com.chodocu.app.di.appViewModel
import com.chodocu.app.ui.components.AppTopBar
import com.chodocu.app.ui.components.EmptyView
import com.chodocu.app.ui.components.ErrorView
import com.chodocu.app.ui.components.LoadingView
import com.chodocu.app.ui.components.ProductRowCard
import com.chodocu.app.ui.components.RefreshOnResume
import com.chodocu.app.ui.theme.BrandOrange
import com.chodocu.app.ui.theme.Space

/** Màn "Món đồ đã lưu". */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(onBack: () -> Unit, onOpenProduct: (Long) -> Unit) {
    val viewModel = appViewModel { _ -> FavoritesViewModel(products) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnResume { viewModel.refreshSilently() }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.userMessage) {
        state.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.userMessageShown()
        }
    }
    // Bỏ lưu xong: báo kèm nút "Hoàn tác"
    LaunchedEffect(state.lastRemoved) {
        if (state.lastRemoved == null) return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = "Đã bỏ lưu món đồ",
            actionLabel = "Hoàn tác",
            duration = SnackbarDuration.Short,
        )
        if (result == SnackbarResult.ActionPerformed) viewModel.undoRemove() else viewModel.removalShown()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AppTopBar(title = "Món đồ đã lưu", onBack = onBack) },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding),
        ) {
            val errorMessage = state.errorMessage
            when {
                state.isLoading -> LoadingView()
                errorMessage != null -> ErrorView(message = errorMessage, onRetry = viewModel::retry)
                state.products.isEmpty() -> EmptyView(
                    icon = Icons.Outlined.FavoriteBorder,
                    title = "Chưa lưu món đồ nào",
                    message = "Bấm biểu tượng trái tim ở trang chi tiết để lưu lại món đồ bạn quan tâm.",
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(Space.screen),
                    verticalArrangement = Arrangement.spacedBy(Space.sm),
                ) {
                    items(state.products, key = { it.id }) { product ->
                        ProductRowCard(
                            product = product,
                            onClick = { onOpenProduct(product.id) },
                            // Tin vẫn đang bán thì không cần nhãn trạng thái
                            showStatus = product.status != ProductStatus.APPROVED,
                            trailing = {
                                IconButton(onClick = { viewModel.remove(product) }) {
                                    Icon(Icons.Filled.Favorite, contentDescription = "Bỏ lưu", tint = BrandOrange)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
