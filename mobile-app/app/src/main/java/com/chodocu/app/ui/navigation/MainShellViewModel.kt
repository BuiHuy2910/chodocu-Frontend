package com.chodocu.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chodocu.app.data.repository.ChatRepository
import com.chodocu.app.data.repository.NotificationRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    private var lastRefreshTimestamp: Long = 0L

    init {
        refreshBadges(force = true)
    }

    /**
     * Tải lại số chưa đọc.
     * Bỏ qua nếu lần tải trước chưa xong hoặc vừa tải cách đây dưới 5 giây (tránh spam khi chuyển màn hình liên tục).
     */
    fun refreshBadges(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && (now - lastRefreshTimestamp < MIN_REFRESH_INTERVAL_MS)) return
        if (refreshJob?.isActive == true) return

        lastRefreshTimestamp = now
        refreshJob = viewModelScope.launch {
            // Tải song song cả tin nhắn và thông báo để giảm độ trễ
            val (messages, notices) = coroutineScope {
                val msgDeferred = async { countOrNull { chat.getUnreadMessageCount() } }
                val notifDeferred = async { countOrNull { notifications.getUnreadCount() } }
                msgDeferred.await() to notifDeferred.await()
            }
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

    private companion object {
        const val MIN_REFRESH_INTERVAL_MS = 5_000L
    }
}
