package com.chodocu.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chodocu.app.core.Validators
import com.chodocu.app.core.safeCall
import com.chodocu.app.data.common.toUserMessage
import com.chodocu.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Trạng thái form đổi mật khẩu. */
data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val currentError: String? = null,
    val newError: String? = null,
    val confirmError: String? = null,
    val isSaving: Boolean = false,
    val isDone: Boolean = false,
    val errorMessage: String? = null,
)

class ChangePasswordViewModel(private val auth: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    fun onCurrentChange(value: String) {
        _uiState.update { it.copy(currentPassword = value, currentError = null, errorMessage = null) }
    }

    fun onNewChange(value: String) {
        _uiState.update { it.copy(newPassword = value, newError = null, errorMessage = null) }
    }

    fun onConfirmChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, confirmError = null, errorMessage = null) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isSaving) return
        val currentError = if (state.currentPassword.isEmpty()) "Vui lòng nhập mật khẩu hiện tại" else null
        val newError = Validators.password(state.newPassword)
            ?: if (state.newPassword == state.currentPassword) "Mật khẩu mới phải khác mật khẩu hiện tại" else null
        val confirmError = Validators.confirmPassword(state.newPassword, state.confirmPassword)
        if (currentError != null || newError != null || confirmError != null) {
            _uiState.update { it.copy(currentError = currentError, newError = newError, confirmError = confirmError) }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            safeCall { auth.changePassword(state.currentPassword, state.newPassword) }
                .onSuccess { _uiState.update { it.copy(isSaving = false, isDone = true) } }
                .onFailure { error -> _uiState.update { it.copy(isSaving = false, errorMessage = error.toUserMessage()) } }
        }
    }
}
