package com.chodocu.app.data.session

import android.content.Context
import com.chodocu.app.core.AppConfig
import com.chodocu.app.data.common.ApiException
import com.chodocu.app.data.model.User
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Lưu phiên đăng nhập (token + hồ sơ) vào SharedPreferences để mở lại app không phải đăng nhập lại.
 *
 * Giao diện chỉ cần theo dõi [currentUser]: null = chưa đăng nhập -> hiện màn đăng nhập.
 */
class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _currentUser = MutableStateFlow(loadStoredUser())
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    /** Đánh dấu phiên vừa hết hạn để màn đăng nhập báo cho người dùng biết. */
    @Volatile
    private var expiredNotice = false

    /** Token hiện tại (JWT với backend, mã phiên với Supabase). */
    val token: String?
        get() = prefs.getString(KEY_TOKEN, null)

    fun requireToken(): String =
        token ?: throw ApiException(ApiException.MSG_SESSION_EXPIRED, ApiException.Kind.UNAUTHORIZED)

    fun requireUserId(): Long =
        _currentUser.value?.id
            ?: throw ApiException(ApiException.MSG_SESSION_EXPIRED, ApiException.Kind.UNAUTHORIZED)

    /** Lưu phiên sau khi đăng nhập / đăng ký thành công. */
    fun saveSession(token: String, user: User) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER, gson.toJson(user))
            .putString(KEY_MODE, currentMode())
            .apply()
        expiredNotice = false
        _currentUser.value = user
    }

    /** Cập nhật hồ sơ (sau khi sửa hồ sơ, đổi ảnh đại diện...). */
    fun updateUser(user: User) {
        if (token == null) return
        prefs.edit().putString(KEY_USER, gson.toJson(user)).apply()
        _currentUser.value = user
    }

    /** Đăng xuất: xóa sạch phiên trên máy. */
    fun clear() {
        prefs.edit().clear().apply()
        _currentUser.value = null
    }

    /** Phiên hết hạn (máy chủ từ chối token): xóa phiên và ghi nhớ để báo người dùng. */
    fun expire() {
        if (_currentUser.value != null) expiredNotice = true
        clear()
    }

    /** Trả về true một lần duy nhất nếu lần đăng xuất gần nhất là do hết phiên. */
    fun consumeExpiredNotice(): Boolean {
        val result = expiredNotice
        expiredNotice = false
        return result
    }

    private fun loadStoredUser(): User? {
        // Token của chế độ Supabase không dùng được với backend và ngược lại.
        if (prefs.getString(KEY_MODE, null) != currentMode()) {
            prefs.edit().clear().apply()
            return null
        }
        if (prefs.getString(KEY_TOKEN, null) == null) return null
        val json = prefs.getString(KEY_USER, null) ?: return null
        return try {
            gson.fromJson(json, User::class.java)
        } catch (e: Exception) {
            prefs.edit().clear().apply()
            null
        }
    }

    private fun currentMode(): String = if (AppConfig.USE_SUPABASE_DIRECT) "supabase" else "backend"

    private companion object {
        const val PREFS_NAME = "chodocu_session"
        const val KEY_TOKEN = "token"
        const val KEY_USER = "user"
        const val KEY_MODE = "mode"
    }
}
