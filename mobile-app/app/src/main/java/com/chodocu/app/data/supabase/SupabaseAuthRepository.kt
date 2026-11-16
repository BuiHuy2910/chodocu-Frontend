package com.chodocu.app.data.supabase

import com.chodocu.app.data.common.ApiException
import com.chodocu.app.data.common.objectOrNull
import com.chodocu.app.data.common.string
import com.chodocu.app.data.model.ProfileInput
import com.chodocu.app.data.model.RegisterInput
import com.chodocu.app.data.model.User
import com.chodocu.app.data.repository.AuthRepository
import com.chodocu.app.data.session.SessionManager
import com.google.gson.JsonObject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull

/** Đăng nhập / đăng ký / hồ sơ qua các hàm api_login, api_register, api_me... */
class SupabaseAuthRepository(
    private val client: SupabaseClient,
    private val session: SessionManager,
) : AuthRepository {

    override suspend fun login(email: String, password: String): User {
        val result = client.callObject(
            "api_login",
            "p_email" to email.trim(),
            "p_password" to password,
            authenticated = false,
        )
        return saveSession(result)
    }

    override suspend fun register(input: RegisterInput): User {
        val result = client.callObject(
            "api_register",
            "p_full_name" to input.fullName.trim(),
            "p_email" to input.email.trim(),
            "p_phone" to input.phone.trim(),
            "p_password" to input.password,
            "p_university_id" to input.universityId,
            authenticated = false,
        )
        return saveSession(result)
    }

    override suspend fun logout() {
        val token = session.token
        try {
            // Báo máy chủ hủy phiên, nhưng không bắt người dùng chờ lâu khi mạng yếu
            if (token != null) {
                withTimeoutOrNull(LOGOUT_TIMEOUT_MILLIS) {
                    client.callObject("api_logout", "p_token" to token, authenticated = false)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Bỏ qua: phiên trên máy vẫn bị xóa ở dưới, phiên trên máy chủ tự hết hạn sau 30 ngày
        } finally {
            session.clear()
        }
    }

    override suspend fun refreshCurrentUser(): User =
        client.callObject("api_me").toUser().also(session::updateUser)

    override suspend fun updateProfile(input: ProfileInput): User =
        client.callObject(
            "api_update_profile",
            "p_full_name" to input.fullName.trim(),
            "p_phone" to input.phone.trim(),
            "p_university_id" to input.universityId,
            "p_dorm_area" to input.dormArea?.trim(),
            "p_bio" to input.bio?.trim(),
        ).toUser().also(session::updateUser)

    override suspend fun updateAvatar(avatarUrl: String?): User =
        client.callObject("api_update_avatar", "p_avatar_url" to avatarUrl)
            .toUser()
            .also(session::updateUser)

    override suspend fun changePassword(currentPassword: String, newPassword: String) {
        client.callObject(
            "api_change_password",
            "p_current_password" to currentPassword,
            "p_new_password" to newPassword,
        )
    }

    /** Kết quả đăng nhập / đăng ký có dạng {"token": "...", "user": {...}}. */
    private fun saveSession(result: JsonObject): User {
        val token = result.string("token")
        val user = result.objectOrNull("user")?.toUser()
        if (token.isBlank() || user == null) throw ApiException(ApiException.MSG_UNKNOWN)
        session.saveSession(token, user)
        return user
    }

    private companion object {
        const val LOGOUT_TIMEOUT_MILLIS = 3_000L
    }
}
