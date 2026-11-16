package com.chodocu.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chodocu.app.core.Validators
import com.chodocu.app.core.safeCall
import com.chodocu.app.data.common.toUserMessage
import com.chodocu.app.data.model.ProfileInput
import com.chodocu.app.data.model.University
import com.chodocu.app.data.repository.AuthRepository
import com.chodocu.app.data.repository.CatalogRepository
import com.chodocu.app.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Lỗi của từng ô trong form sửa hồ sơ (null = hợp lệ). */
data class EditProfileErrors(
    val fullName: String? = null,
    val phone: String? = null,
    val university: String? = null,
) {
    val hasAny: Boolean get() = fullName != null || phone != null || university != null
}

/** Trạng thái form sửa hồ sơ. Email chỉ để xem, không đổi được. */
data class EditProfileUiState(
    val email: String = "",
    val fullName: String = "",
    val phone: String = "",
    val university: University? = null,
    val dormArea: String = "",
    val bio: String = "",
    val universities: List<University> = emptyList(),
    val isLoadingUniversities: Boolean = true,
    val universitiesError: String? = null,
    val errors: EditProfileErrors = EditProfileErrors(),
    /** Đã sửa gì đó chưa lưu (để hỏi lại khi thoát). */
    val isDirty: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val userMessage: String? = null,
)

class EditProfileViewModel(
    private val auth: AuthRepository,
    private val catalog: CatalogRepository,
    session: SessionManager,
) : ViewModel() {

    /** Hồ sơ hiện tại — điền sẵn vào form. */
    private val user = session.currentUser.value

    private val _uiState = MutableStateFlow(
        EditProfileUiState(
            email = user?.email.orEmpty(),
            fullName = user?.fullName.orEmpty(),
            phone = user?.phone.orEmpty(),
            dormArea = user?.dormArea.orEmpty(),
            bio = user?.bio.orEmpty(),
        )
    )
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadUniversities()
    }

    fun loadUniversities() {
        _uiState.update { it.copy(isLoadingUniversities = true, universitiesError = null) }
        viewModelScope.launch {
            safeCall { catalog.getUniversities() }
                .onSuccess { list ->
                    _uiState.update { state ->
                        state.copy(
                            universities = list,
                            // Giữ trường người dùng vừa chọn; chưa chọn thì dùng trường đang lưu trong hồ sơ
                            university = state.university ?: list.firstOrNull { it.id == user?.universityId },
                            isLoadingUniversities = false,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoadingUniversities = false, universitiesError = error.toUserMessage()) }
                }
        }
    }

    fun onFullNameChange(value: String) = edit { it.copy(fullName = value, errors = it.errors.copy(fullName = null)) }

    fun onPhoneChange(value: String) = edit { it.copy(phone = value, errors = it.errors.copy(phone = null)) }

    fun onUniversitySelected(value: University) =
        edit { it.copy(university = value, errors = it.errors.copy(university = null)) }

    fun onDormAreaChange(value: String) = edit { it.copy(dormArea = value) }

    fun onBioChange(value: String) = edit { it.copy(bio = value) }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun save() {
        val state = _uiState.value
        if (state.isSaving) return
        val errors = EditProfileErrors(
            fullName = Validators.fullName(state.fullName),
            phone = Validators.phone(state.phone),
            university = if (state.university == null) "Vui lòng chọn trường của bạn" else null,
        )
        val university = state.university
        if (errors.hasAny || university == null) {
            _uiState.update { it.copy(errors = errors) }
            return
        }

        _uiState.update { it.copy(isSaving = true, errors = errors) }
        viewModelScope.launch {
            val input = ProfileInput(
                fullName = state.fullName.trim(),
                phone = Validators.normalizePhone(state.phone),
                universityId = university.id,
                dormArea = state.dormArea.trim().ifEmpty { null },
                bio = state.bio.trim().ifEmpty { null },
            )
            safeCall { auth.updateProfile(input) }
                .onSuccess { _uiState.update { it.copy(isSaving = false, isDirty = false, isSaved = true) } }
                .onFailure { error -> _uiState.update { it.copy(isSaving = false, userMessage = error.toUserMessage()) } }
        }
    }

    private fun edit(change: (EditProfileUiState) -> EditProfileUiState) {
        _uiState.update { change(it).copy(isDirty = true) }
    }
}
