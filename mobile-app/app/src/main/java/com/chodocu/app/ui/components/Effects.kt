package com.chodocu.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/**
 * Gọi [action] mỗi khi màn hình được hiển thị LẠI (quay về từ màn khác, mở lại app).
 * Lần hiển thị đầu tiên bị bỏ qua vì ViewModel đã tự tải dữ liệu khi khởi tạo.
 */
@Composable
fun RefreshOnResume(action: () -> Unit) {
    val currentAction by rememberUpdatedState(action)
    var isFirstResume by rememberSaveable { mutableStateOf(true) }
    LifecycleResumeEffect(Unit) {
        if (isFirstResume) isFirstResume = false else currentAction()
        onPauseOrDispose { }
    }
}

/**
 * Gọi [action] lặp lại mỗi [intervalMillis] khi màn hình đang hiển thị (dừng khi ẩn app / sang màn khác).
 * Lần gọi đầu tiên diễn ra sau [intervalMillis]. Lỗi trong [action] được bỏ qua để vòng lặp không dừng.
 */
@Composable
fun PollWhileResumed(intervalMillis: Long, action: suspend () -> Unit) {
    val currentAction by rememberUpdatedState(action)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner, intervalMillis) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                delay(intervalMillis)
                try {
                    currentAction()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // Lỗi mạng tạm thời: bỏ qua, lần sau thử lại
                }
            }
        }
    }
}
