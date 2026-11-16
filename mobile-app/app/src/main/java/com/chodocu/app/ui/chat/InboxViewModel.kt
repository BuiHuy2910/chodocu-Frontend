package com.chodocu.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chodocu.app.core.safeCall
import com.chodocu.app.data.common.toUserMessage
import com.chodocu.app.data.model.Conversation
import com.chodocu.app.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Trạng thái hộp thư. */
data class InboxUiState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val userMessage: String? = null,
)

/** Danh sách cuộc trò chuyện, tự cập nhật định kỳ khi đang mở. */
class InboxViewModel(private val chat: ChatRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(InboxUiState())
    val uiState: StateFlow<InboxUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        fetch(showErrors = true)
    }

    fun retry() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        fetch(showErrors = true)
    }

    /** Kéo để làm mới. */
    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        fetch(showErrors = true)
    }

    /** Cập nhật ngầm (định kỳ / khi quay lại màn hình): lỗi không làm phiền người dùng. */
    fun refreshSilently() {
        if (loadJob?.isActive == true) return
        fetch(showErrors = false)
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun fetch(showErrors: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            safeCall { chat.getConversations() }
                .onSuccess { list ->
                    _uiState.update {
                        it.copy(conversations = list, isLoading = false, isRefreshing = false, errorMessage = null)
                    }
                }
                .onFailure { error ->
                    val message = error.toUserMessage()
                    _uiState.update {
                        when {
                            it.conversations.isEmpty() && showErrors ->
                                it.copy(isLoading = false, isRefreshing = false, errorMessage = message)
                            showErrors -> it.copy(isLoading = false, isRefreshing = false, userMessage = message)
                            else -> it.copy(isLoading = false, isRefreshing = false)
                        }
                    }
                }
        }
    }
}
