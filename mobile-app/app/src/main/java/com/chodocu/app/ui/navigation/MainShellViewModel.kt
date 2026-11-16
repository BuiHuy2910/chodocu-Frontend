package com.chodocu.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chodocu.app.data.repository.ChatRepository
import com.chodocu.app.data.repository.NotificationRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Số tin nhắn / thông báo chưa đọc hiển thị trên thanh điều hướng. */
data class ShellBadges(
    val unreadMessages: Int = 0,
    val unreadNotifications: Int = 0,
)

/** ViewModel của khung chính: chỉ lo đếm số chưa đọc cho huy hiệu. */
class MainShellViewModel(
    private val chat: ChatRepository,
    private val notifications: NotificationRepository,
) : ViewModel() {

    private val _badges = MutableStateFlow(ShellBadges())
    val badges: StateFlow<ShellBadges> = _badges.asStateFlow()

    private var refreshJob: Job? = null

    init {
        refreshBadges()
    }

    /** Tải lại số chưa đọc. Bỏ qua nếu lần tải trước chưa xong; lỗi mạng thì giữ số cũ. */
    fun refreshBadges() {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            val messages = countOrNull { chat.getUnreadMessageCount() }
            val notices = countOrNull { notifications.getUnreadCount() }
            _badges.update { old ->
                old.copy(
                    unreadMessages = messages ?: old.unreadMessages,
                    unreadNotifications = notices ?: old.unreadNotifications,
                )
            }
        }
    }

    private suspend fun countOrNull(load: suspend () -> Int): Int? =
        try {
            load()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
}
