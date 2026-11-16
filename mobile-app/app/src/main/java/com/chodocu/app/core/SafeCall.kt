package com.chodocu.app.core

import kotlin.coroutines.cancellation.CancellationException

/**
 * Gọi repository an toàn trong ViewModel: lỗi được gói vào [Result] để giao diện tự hiển thị,
 * riêng CancellationException vẫn được ném lại để coroutine dừng đúng cách khi rời màn hình.
 *
 * Ví dụ:
 * ```
 * safeCall { products.getProduct(id) }
 *     .onSuccess { product -> ... }
 *     .onFailure { error -> showMessage(error.toUserMessage()) }
 * ```
 */
suspend fun <T> safeCall(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}
