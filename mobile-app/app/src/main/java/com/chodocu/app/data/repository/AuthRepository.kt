package com.chodocu.app.data.repository

import com.chodocu.app.data.model.ProfileInput
import com.chodocu.app.data.model.RegisterInput
import com.chodocu.app.data.model.User

/**
 * Đăng nhập, đăng ký và hồ sơ cá nhân.
 * Đăng nhập / đăng ký thành công sẽ tự lưu phiên vào SessionManager.
 */
interface AuthRepository {
    suspend fun login(email: String, password: String): User

    suspend fun register(input: RegisterInput): User

    /** Luôn xóa phiên trên máy, kể cả khi gọi máy chủ thất bại. */
    suspend fun logout()

    /** Lấy lại hồ sơ mới nhất từ máy chủ và cập nhật phiên. */
    suspend fun refreshCurrentUser(): User

    suspend fun updateProfile(input: ProfileInput): User

    /** Đổi ảnh đại diện (null = gỡ ảnh, app sẽ hiện chữ cái đầu tên). */
    suspend fun updateAvatar(avatarUrl: String?): User

    suspend fun changePassword(currentPassword: String, newPassword: String)
}
