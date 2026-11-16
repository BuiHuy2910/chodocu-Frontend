package com.chodocu.app.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

/** AppContainer được cung cấp cho mọi màn hình qua CompositionLocal (xem MainActivity). */
val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("Chưa cung cấp AppContainer. Hãy bọc giao diện trong CompositionLocalProvider.")
}

/**
 * Tạo ViewModel cho màn hình hiện tại, có sẵn AppContainer và SavedStateHandle (chứa tham số điều hướng).
 *
 * Ví dụ: `val viewModel = appViewModel { handle -> ProductDetailViewModel(products, session, handle) }`
 */
@Composable
inline fun <reified VM : ViewModel> appViewModel(
    key: String? = null,
    crossinline create: AppContainer.(SavedStateHandle) -> VM,
): VM {
    val container = LocalAppContainer.current
    return viewModel(
        key = key,
        factory = viewModelFactory {
            initializer { container.create(createSavedStateHandle()) }
        },
    )
}
