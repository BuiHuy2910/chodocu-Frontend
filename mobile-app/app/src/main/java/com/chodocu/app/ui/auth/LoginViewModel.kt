package com.chodocu.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chodocu.app.core.Validators
import com.chodocu.app.core.safeCall
import com.chodocu.app.data.common.toUserMessage
import com.chodocu.app.data.repository.AuthRepository
import com.chodocu.app.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Trạng thái màn đăng nhập. */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isSubmitting: Boolean = false,
    /** Lỗi từ máy chủ (sai mật khẩu, mất mạng...) hiện ở dải đỏ phía trên form. */
    val errorMessage: String? = null,
    /** Lần trước bị đưa về màn này vì phiên đăng nhập hết hạn. */
    val showSessionExpired: Boolean = false,
)

/** Xử lý đăng nhập. Thành công thì SessionManager lưu phiên và AppRoot tự chuyển sang trang chủ. */
class LoginViewModel(
    private val auth: AuthRepository,
    session: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState(showSessionExpired = session.consumeExpiredNotice()))
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, errorMessage = null) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return

        val emailError = Validators.email(state.email)
        val passwordError = if (state.password.isEmpty()) "Vui lòng nhập mật khẩu" else null
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null, showSessionExpired = false) }
        viewModelScope.launch {
            safeCall { auth.login(Validators.normalizeEmail(state.email), state.password) }
                .onFailure { error ->
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = error.toUserMessage()) }
                }
        }
    }
}
