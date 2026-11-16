package com.chodocu.app.data.model

/** Vai trò tài khoản. Sinh viên dùng app; admin/kiểm duyệt viên dùng trang quản trị. */
enum class UserRole(val label: String) {
    STUDENT("Sinh viên"),
    MODERATOR("Kiểm duyệt viên"),
    ADMIN("Quản trị viên");

    companion object {
        fun fromApi(value: String?): UserRole =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: STUDENT
    }
}

/** Người dùng đang đăng nhập (đầy đủ thông tin hồ sơ). */
data class User(
    val id: Long,
    val email: String,
    val fullName: String,
    val phone: String?,
    val avatarUrl: String?,
    val bio: String?,
    val dormArea: String?,
    val role: UserRole,
    val universityId: Long?,
    val universityName: String?,
    val universityShortName: String?,
    val rating: Double?,
    val completedDeals: Int,
) {
    fun toSummary() = UserSummary(
        id = id,
        fullName = fullName,
        avatarUrl = avatarUrl,
        universityShortName = universityShortName,
        phone = phone,
        rating = rating,
        completedDeals = completedDeals,
    )
}

/** Thông tin rút gọn của một người (người bán, người mua, người chat cùng). */
data class UserSummary(
    val id: Long,
    val fullName: String,
    val avatarUrl: String? = null,
    val universityShortName: String? = null,
    val phone: String? = null,
    val rating: Double? = null,
    val completedDeals: Int = 0,
)

/** Dữ liệu form đăng ký. */
data class RegisterInput(
    val fullName: String,
    val email: String,
    val phone: String,
    val password: String,
    val universityId: Long,
)

/** Dữ liệu form sửa hồ sơ (ảnh đại diện đổi riêng bằng updateAvatar). */
data class ProfileInput(
    val fullName: String,
    val phone: String,
    val universityId: Long,
    val dormArea: String?,
    val bio: String?,
)
