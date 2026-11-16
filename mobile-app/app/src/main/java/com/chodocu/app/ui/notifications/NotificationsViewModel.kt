package com.chodocu.app.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chodocu.app.core.safeCall
import com.chodocu.app.data.common.toUserMessage
import com.chodocu.app.data.model.AppNotification
import com.chodocu.app.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Trạng thái màn thông báo. */
data class NotificationsUiState(
    val items: List<AppNotification> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val userMessage: String? = null,
)

/**
 * Danh sách thông báo. Mở màn hình là coi như đã đọc hết (huy hiệu ở trang chủ tự về 0),
 * nhưng danh sách đang xem vẫn giữ màu nổi cho các thông báo mới để người dùng dễ nhận ra.
 */
class NotificationsViewModel(private val notifications: NotificationRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        load(isRefresh = false)
    }

    fun retry() = load(isRefresh = false)

    fun refresh() = load(isRefresh = true)

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun load(isRefresh: Boolean) {
        _uiState.update {
            it.copy(isLoading = !isRefresh && it.items.isEmpty(), isRefreshing = isRefresh, errorMessage = null)
        }
        viewModelScope.launch {
            safeCall { notifications.getNotifications() }
                .onSuccess { list ->
                    _uiState.update { it.copy(items = list, isLoading = false, isRefreshing = false) }
                    if (list.any { !it.isRead }) safeCall { notifications.markAllRead() }
                }
                .onFailure { error ->
                    val message = error.toUserMessage()
                    _uiState.update {
                        if (it.items.isEmpty()) {
                            it.copy(isLoading = false, isRefreshing = false, errorMessage = message)
                        } else {
                            it.copy(isRefreshing = false, userMessage = message)
                        }
                    }
                }
        }
    }
}
