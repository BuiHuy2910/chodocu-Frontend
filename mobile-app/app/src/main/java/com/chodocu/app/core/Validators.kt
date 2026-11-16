package com.chodocu.app.core

/**
 * Kiểm tra dữ liệu nhập ở phía app (máy chủ vẫn kiểm tra lại lần nữa).
 * Mỗi hàm trả về câu báo lỗi tiếng Việt, hoặc null nếu dữ liệu hợp lệ.
 */
object Validators {

    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val phoneRegex = Regex("^(0|\\+84)[0-9]{9,10}$")

    const val PASSWORD_MIN = 6
    const val PASSWORD_MAX = 72
    const val NAME_MAX = 100
    const val TITLE_MIN = 5
    const val TITLE_MAX = 120
    const val DESCRIPTION_MAX = 3000
    const val NOTE_MAX = 255
    const val BIO_MAX = 500
    const val MESSAGE_MAX = 1000
    const val MAX_IMAGES = 5
    const val MAX_PRICE = 1_000_000_000L

    fun fullName(value: String): String? {
        val name = value.trim()
        return when {
            name.isEmpty() -> "Vui lòng nhập họ và tên"
            name.length < 2 -> "Họ và tên quá ngắn"
            name.length > NAME_MAX -> "Họ và tên tối đa $NAME_MAX ký tự"
            else -> null
        }
    }

    fun email(value: String): String? {
        val email = value.trim()
        return when {
            email.isEmpty() -> "Vui lòng nhập email"
            !emailRegex.matches(email) -> "Email không hợp lệ"
            else -> null
        }
    }

    /** Số điện thoại Việt Nam: bắt đầu bằng 0 hoặc +84, sau đó 9-10 chữ số. */
    fun phone(value: String): String? {
        val phone = value.trim().replace(" ", "")
        return when {
            phone.isEmpty() -> "Vui lòng nhập số điện thoại"
            !phoneRegex.matches(phone) -> "Số điện thoại không hợp lệ (VD: 0912345678)"
            else -> null
        }
    }

    fun password(value: String): String? = when {
        value.isEmpty() -> "Vui lòng nhập mật khẩu"
        value.length < PASSWORD_MIN -> "Mật khẩu cần ít nhất $PASSWORD_MIN ký tự"
        value.length > PASSWORD_MAX -> "Mật khẩu tối đa $PASSWORD_MAX ký tự"
        else -> null
    }

    fun confirmPassword(password: String, confirm: String): String? = when {
        confirm.isEmpty() -> "Vui lòng nhập lại mật khẩu"
        confirm != password -> "Mật khẩu nhập lại không khớp"
        else -> null
    }

    fun productTitle(value: String): String? {
        val title = value.trim()
        return when {
            title.isEmpty() -> "Vui lòng nhập tên sản phẩm"
            title.length < TITLE_MIN -> "Tên sản phẩm cần ít nhất $TITLE_MIN ký tự"
            title.length > TITLE_MAX -> "Tên sản phẩm tối đa $TITLE_MAX ký tự"
            else -> null
        }
    }

    /** Giá bán: bắt buộc > 0 khi không phải đồ tặng. */
    fun price(value: Long?, isFree: Boolean): String? = when {
        isFree -> null
        value == null || value <= 0L -> "Vui lòng nhập giá bán"
        value > MAX_PRICE -> "Giá bán quá lớn"
        else -> null
    }

    /** Chuẩn hóa số điện thoại trước khi gửi lên máy chủ (bỏ khoảng trắng). */
    fun normalizePhone(value: String): String = value.trim().replace(" ", "")

    /** Chuẩn hóa email trước khi gửi lên máy chủ. */
    fun normalizeEmail(value: String): String = value.trim().lowercase()
}
