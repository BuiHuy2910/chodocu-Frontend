package com.chodocu.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chodocu.app.core.Validators
import com.chodocu.app.core.safeCall
import com.chodocu.app.data.common.toUserMessage
import com.chodocu.app.data.model.RegisterInput
import com.chodocu.app.data.model.University
import com.chodocu.app.data.repository.AuthRepository
import com.chodocu.app.data.repository.CatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Lỗi của từng ô trong form đăng ký (null = hợp lệ). */
data class RegisterErrors(
    val fullName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val university: String? = null,
    val password: String? = null,
    val confirmPassword: String? = null,
) {
    val hasAny: Boolean
        get() = listOf(fullName, email, phone, university, password, confirmPassword).any { it != null }
}

/** Trạng thái màn đăng ký. Trường đại học ban đầu để trống — người dùng phải tự chọn. */
data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val university: University? = null,
    val universities: List<University> = emptyList(),
    val isLoadingUniversities: Boolean = true,
    val universitiesError: String? = null,
    val errors: RegisterErrors = RegisterErrors(),
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)

/** Đăng ký tài khoản: họ tên, email, số điện thoại, trường, mật khẩu. Không có bước xác thực email. */
class RegisterViewModel(
    private val auth: AuthRepository,
    private val catalog: CatalogRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    init {
        loadUniversities()
    }

    fun loadUniversities() {
        _uiState.update { it.copy(isLoadingUniversities = true, universitiesError = null) }
        viewModelScope.launch {
            safeCall { catalog.getUniversities() }
                .onSuccess { list -> _uiState.update { it.copy(universities = list, isLoadingUniversities = false) } }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoadingUniversities = false, universitiesError = error.toUserMessage()) }
                }
        }
    }

    // Mỗi lần sửa một ô thì xóa lỗi của riêng ô đó
    fun onFullNameChange(value: String) = edit { it.copy(fullName = value, errors = it.errors.copy(fullName = null)) }

    fun onEmailChange(value: String) = edit { it.copy(email = value, errors = it.errors.copy(email = null)) }

    fun onPhoneChange(value: String) = edit { it.copy(phone = value, errors = it.errors.copy(phone = null)) }

    fun onPasswordChange(value: String) = edit { it.copy(password = value, errors = it.errors.copy(password = null)) }

    fun onConfirmPasswordChange(value: String) =
        edit { it.copy(confirmPassword = value, errors = it.errors.copy(confirmPassword = null)) }

    fun onUniversitySelected(university: University) =
        edit { it.copy(university = university, errors = it.errors.copy(university = null)) }

    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return

        val errors = RegisterErrors(
            fullName = Validators.fullName(state.fullName),
            email = Validators.email(state.email),
            phone = Validators.phone(state.phone),
            university = if (state.university == null) "Vui lòng chọn trường của bạn" else null,
            password = Validators.password(state.password),
            confirmPassword = Validators.confirmPassword(state.password, state.confirmPassword),
        )
        val university = state.university
        if (errors.hasAny || university == null) {
            _uiState.update { it.copy(errors = errors) }
            return
        }

        val input = RegisterInput(
            fullName = state.fullName.trim(),
            email = Validators.normalizeEmail(state.email),
            phone = Validators.normalizePhone(state.phone),
            password = state.password,
            universityId = university.id,
        )
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            // Thành công: phiên được lưu ngay, AppRoot tự chuyển vào trang chủ.
            safeCall { auth.register(input) }
                .onFailure { error ->
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = error.toUserMessage()) }
                }
        }
    }

    private fun edit(change: (RegisterUiState) -> RegisterUiState) {
        _uiState.update { change(it).copy(errorMessage = null) }
    }
}
