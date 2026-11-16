package com.chodocu.app.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chodocu.app.core.safeCall
import com.chodocu.app.data.common.toUserMessage
import com.chodocu.app.data.model.ProductStatus
import com.chodocu.app.data.model.UploadFolder
import com.chodocu.app.data.model.User
import com.chodocu.app.data.repository.AuthRepository
import com.chodocu.app.data.repository.ProductRepository
import com.chodocu.app.data.repository.UploadRepository
import com.chodocu.app.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Trạng thái màn Tài khoản. */
data class ProfileUiState(
    val user: User? = null,
    /** Số tin đang bán (null = chưa tải xong). */
    val sellingCount: Int? = null,
    val isAvatarBusy: Boolean = false,
    val isLoggingOut: Boolean = false,
    val userMessage: String? = null,
)

/** Hồ sơ của mình: thông tin, ảnh đại diện, thống kê nhanh và đăng xuất. */
class ProfileViewModel(
    private val auth: AuthRepository,
    private val products: ProductRepository,
    private val uploads: UploadRepository,
    session: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(user = session.currentUser.value))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // Hồ sơ trong phiên thay đổi (sửa hồ sơ, đổi ảnh...) thì màn này tự cập nhật theo
        viewModelScope.launch {
            session.currentUser.collect { user -> _uiState.update { it.copy(user = user) } }
        }
        refresh()
    }

    /** Lấy hồ sơ mới nhất (số giao dịch, đánh giá) và đếm tin đang bán. Lỗi thì giữ dữ liệu đang có. */
    fun refresh() {
        viewModelScope.launch { safeCall { auth.refreshCurrentUser() } }
        viewModelScope.launch {
            safeCall { products.getMyProducts() }.onSuccess { list ->
                _uiState.update { state -> state.copy(sellingCount = list.count { it.status == ProductStatus.APPROVED }) }
            }
        }
    }

    /** Tải ảnh mới lên rồi đặt làm ảnh đại diện. */
    fun changeAvatar(uri: Uri) {
        updateAvatar(successMessage = "Đã cập nhật ảnh đại diện.") {
            val url = uploads.uploadImage(uri, UploadFolder.AVATARS)
            auth.updateAvatar(url)
        }
    }

    /** Gỡ ảnh đại diện — app sẽ hiện chữ cái đầu của tên. */
    fun removeAvatar() {
        updateAvatar(successMessage = "Đã gỡ ảnh đại diện.") { auth.updateAvatar(null) }
    }

    private fun updateAvatar(successMessage: String, block: suspend () -> User) {
        if (_uiState.value.isAvatarBusy) return
        _uiState.update { it.copy(isAvatarBusy = true) }
        viewModelScope.launch {
            safeCall { block() }
                .onSuccess { _uiState.update { it.copy(isAvatarBusy = false, userMessage = successMessage) } }
                .onFailure { error ->
                    _uiState.update { it.copy(isAvatarBusy = false, userMessage = error.toUserMessage()) }
                }
        }
    }

    /** Đăng xuất: phiên bị xóa nên app tự quay về màn đăng nhập. */
    fun logout() {
        if (_uiState.value.isLoggingOut) return
        _uiState.update { it.copy(isLoggingOut = true) }
        viewModelScope.launch { safeCall { auth.logout() } }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
