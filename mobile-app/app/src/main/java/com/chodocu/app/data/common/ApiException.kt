package com.chodocu.app.data.common

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Lỗi chung của tầng dữ liệu. `message` luôn là câu tiếng Việt hiển thị được cho người dùng.
 * `kind` giúp giao diện xử lý riêng một số trường hợp (ví dụ hết phiên đăng nhập).
 */
class ApiException(
    override val message: String,
    val kind: Kind = Kind.UNKNOWN,
    cause: Throwable? = null,
) : Exception(message, cause) {

    enum class Kind { NETWORK, UNAUTHORIZED, FORBIDDEN, NOT_FOUND, VALIDATION, CONFLICT, SERVER, UNKNOWN }

    companion object {
        const val MSG_NETWORK = "Không có kết nối mạng. Vui lòng kiểm tra Internet và thử lại."
        const val MSG_TIMEOUT = "Máy chủ phản hồi quá lâu. Vui lòng thử lại."
        const val MSG_SESSION_EXPIRED = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
        const val MSG_UNKNOWN = "Đã có lỗi xảy ra. Vui lòng thử lại."
        const val MSG_SERVER = "Máy chủ đang gặp sự cố. Vui lòng thử lại sau."
    }
}

/** Đổi lỗi mạng (IOException) thành ApiException có câu báo dễ hiểu. */
fun networkError(error: IOException): ApiException = when (error) {
    is SocketTimeoutException -> ApiException(ApiException.MSG_TIMEOUT, ApiException.Kind.NETWORK, error)
    is UnknownHostException -> ApiException(ApiException.MSG_NETWORK, ApiException.Kind.NETWORK, error)
    else -> ApiException(ApiException.MSG_NETWORK, ApiException.Kind.NETWORK, error)
}

/** Câu báo lỗi an toàn để hiện lên giao diện cho mọi loại lỗi. */
fun Throwable.toUserMessage(): String = when (this) {
    is ApiException -> message
    is IOException -> networkError(this).message
    else -> ApiException.MSG_UNKNOWN
}

/**
 * Chạy [block] và chuyển mọi lỗi lạ thành ApiException.
 * Các lớp Repository dùng hàm này để giao diện chỉ phải bắt một loại lỗi.
 */
suspend fun <T> wrapErrors(block: suspend () -> T): T = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (e: ApiException) {
    throw e
} catch (e: IOException) {
    throw networkError(e)
} catch (e: Exception) {
    throw ApiException(ApiException.MSG_UNKNOWN, ApiException.Kind.UNKNOWN, e)
}
