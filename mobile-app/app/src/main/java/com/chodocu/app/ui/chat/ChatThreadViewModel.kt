package com.chodocu.app.ui.chat

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chodocu.app.core.Validators
import com.chodocu.app.core.safeCall
import com.chodocu.app.data.common.toUserMessage
import com.chodocu.app.data.model.ChatMessage
import com.chodocu.app.data.model.MessageType
import com.chodocu.app.data.model.Order
import com.chodocu.app.data.model.OrderRole
import com.chodocu.app.data.model.OrderStatus
import com.chodocu.app.data.model.Product
import com.chodocu.app.data.model.SendMessageInput
import com.chodocu.app.data.model.UploadFolder
import com.chodocu.app.data.model.UserSummary
import com.chodocu.app.data.repository.CatalogRepository
import com.chodocu.app.data.repository.ChatRepository
import com.chodocu.app.data.repository.OrderRepository
import com.chodocu.app.data.repository.ProductRepository
import com.chodocu.app.data.repository.UploadRepository
import com.chodocu.app.data.session.SessionManager
import com.chodocu.app.ui.components.OrderAction
import com.chodocu.app.ui.components.perform
import com.chodocu.app.ui.components.successMessage
import com.chodocu.app.ui.navigation.NavArgs
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/** Tin nhắn mình vừa gửi nhưng máy chủ chưa xác nhận (đang gửi hoặc gửi lỗi). */
data class PendingMessage(
    /** Số âm, chỉ dùng trong app để phân biệt các tin đang gửi. */
    val localId: Long,
    val input: SendMessageInput,
    /** Ảnh trong máy (tin ảnh), dùng để xem trước khi chưa tải lên xong. */
    val localImage: Uri? = null,
    val isFailed: Boolean = false,
)

/** Trạng thái màn trò chuyện với một người. */
data class ChatThreadUiState(
    val myUserId: Long,
    val partnerId: Long,
    val partnerName: String,
    val partner: UserSummary? = null,
    /** Tin đã gửi thành công, cũ nhất trước. */
    val messages: List<ChatMessage> = emptyList(),
    val pending: List<PendingMessage> = emptyList(),
    /** Món đồ hai người đang trao đổi (null nếu chat không gắn món đồ nào). */
    val product: Product? = null,
    /** Điểm hẹn chuẩn của trường đăng món đồ — người dùng chọn trong danh sách này. */
    val meetupSpots: List<String> = emptyList(),
    /** Đơn đặt mua đang mở giữa hai người (chờ xác nhận / đã chốt hẹn / đang khiếu nại). */
    val activeOrder: Order? = null,
    val isOrderActionRunning: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val draft: String = "",
    val userMessage: String? = null,
) {
    /** Mình là người bán món đồ đang trao đổi. */
    val isSeller: Boolean get() = product != null && product.seller.id == myUserId

    /** Trả giá chỉ có ý nghĩa với món đồ có giá bán. */
    val canSendOffer: Boolean get() = product != null && !product.isFree

    /** Hẹn gặp cần món đồ để biết trường (danh sách điểm hẹn chuẩn). */
    val canProposeMeetup: Boolean get() = product != null

    /** Gợi ý câu hỏi nhanh cho người mua khi cuộc trò chuyện còn trống. */
    val showQuickReplies: Boolean get() = messages.isEmpty() && pending.isEmpty() && product != null && !isSeller

    /** Số điện thoại người kia — máy chủ chỉ trả về khi hai bên đã chốt hẹn. */
    val partnerPhone: String?
        get() = activeOrder
            ?.takeIf { it.status == OrderStatus.ACCEPTED || it.status == OrderStatus.DISPUTED }
            ?.partnerOf(myUserId)?.phone
            ?.takeIf { it.isNotBlank() }
}

/**
 * Màn trò chuyện giữa người mua và người bán.
 * - Tin mới được lấy bằng cách hỏi máy chủ định kỳ (xem [poll]).
 * - Tin mình gửi hiện ngay (đang gửi...), gửi lỗi thì có nút gửi lại.
 * - Đơn đặt mua đang mở giữa hai người hiện ngay trong khung chat để thao tác nhanh.
 */
class ChatThreadViewModel(
    private val chat: ChatRepository,
    private val products: ProductRepository,
    private val orders: OrderRepository,
    private val catalog: CatalogRepository,
    private val uploads: UploadRepository,
    session: SessionManager,
    handle: SavedStateHandle,
) : ViewModel() {

    private val partnerId: Long = checkNotNull(handle.get<Long>(NavArgs.PARTNER_ID)) { "Thiếu partnerId" }

    /** Món đồ đang trao đổi: lấy từ màn trước, hoặc từ tin nhắn gần nhất có gắn món đồ. */
    private var contextProductId: Long? = handle.get<Long>(NavArgs.PRODUCT_ID)?.takeIf { it != NavArgs.NO_ID }

    private val _uiState = MutableStateFlow(
        ChatThreadUiState(
            myUserId = session.currentUser.value?.id ?: -1L,
            partnerId = partnerId,
            partnerName = handle.get<String>(NavArgs.PARTNER_NAME)?.takeIf { it.isNotBlank() } ?: "Người dùng",
        )
    )
    val uiState: StateFlow<ChatThreadUiState> = _uiState.asStateFlow()

    /** Id tin chưa đọc mới nhất đã báo "đã đọc" lên máy chủ (tránh gọi lặp lại). */
    private var lastMarkedReadId = 0L
    private var nextLocalId = -1L

    init {
        load()
    }

    fun retry() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        load()
    }

    /** Quay lại màn hình: cập nhật tin nhắn, đơn và món đồ (có thể đã thay đổi ở màn khác). */
    fun onResume() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            safeCall { poll() }
            refreshContext()
        }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }

    // ---------------------------------------------------------------------
    // Tải dữ liệu
    // ---------------------------------------------------------------------

    private fun load() {
        viewModelScope.launch {
            safeCall { chat.getMessages(partnerId) }
                .onSuccess { messages ->
                    if (contextProductId == null) {
                        contextProductId = messages.lastOrNull { it.productId != null }?.productId
                    }
                    _uiState.update { it.copy(messages = messages, isLoading = false, errorMessage = null) }
                    updatePartner()
                    markReadIfNeeded()
                    refreshContext()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) }
                }
        }
    }

    /**
     * Hỏi máy chủ tin mới. Màn hình gọi hàm này định kỳ khi đang hiển thị.
     * Lỗi mạng được ném ra ngoài để vòng lặp bỏ qua và thử lại ở lần sau.
     */
    suspend fun poll() {
        val before = _uiState.value
        if (before.isLoading || before.errorMessage != null) return
        // Đang gửi tin: đợi gửi xong rồi hỏi tiếp, tránh một tin hiện hai lần
        if (before.pending.any { !it.isFailed }) return

        val server = chat.getMessages(partnerId)
        val newestKnownId = before.messages.lastOrNull()?.id ?: 0L
        val serverNewestId = server.lastOrNull()?.id ?: 0L

        // Nếu số lượng tin và id tin mới nhất không đổi: bỏ qua để tránh re-render không cần thiết
        val hasChanges = server.size != before.messages.size || serverNewestId != newestKnownId
        if (!hasChanges) return

        _uiState.update { current ->
            // Tin vừa gửi xong có thể chưa có trong kết quả này -> giữ lại
            val sentAfterRequest = current.messages.filter { it.id > serverNewestId }
            current.copy(messages = server + sentAfterRequest)
        }
        updatePartner()
        markReadIfNeeded()

        // Tin hệ thống (chấp nhận, hủy...) hoặc thẻ hẹn từ đơn mới -> đơn / món đồ đã đổi trạng thái
        val newMessages = server.filter { it.id > newestKnownId }
        if (contextProductId == null) {
            contextProductId = newMessages.lastOrNull { it.productId != null }?.productId
        }
        if (newMessages.any { it.type == MessageType.SYSTEM || it.type == MessageType.MEETUP }) {
            refreshContext()
        }
    }

    /** Tải lại món đồ (kèm điểm hẹn của trường) và đơn đang mở, chạy song song. */
    private suspend fun refreshContext() {
        coroutineScope {
            launch { loadProduct() }
            launch { loadActiveOrder() }
        }
    }

    private suspend fun loadProduct() {
        val productId = contextProductId ?: return
        safeCall { products.getProduct(productId) }
            .onSuccess { product ->
                _uiState.update { it.copy(product = product) }
                if (_uiState.value.meetupSpots.isEmpty()) loadMeetupSpots(product.universityId)
            }
        // Lỗi: bỏ qua — vẫn nhắn tin được, chỉ ẩn thẻ món đồ và các nút trả giá / hẹn gặp
    }

    private suspend fun loadMeetupSpots(universityId: Long?) {
        if (universityId == null) return
        safeCall { catalog.getUniversities() }.onSuccess { list ->
            val spots = list.firstOrNull { it.id == universityId }?.meetupSpots.orEmpty()
            _uiState.update { it.copy(meetupSpots = spots) }
        }
    }

    /** Tìm đơn chưa kết thúc mới nhất giữa hai người (cho món đồ đang trao đổi nếu biết). */
    private suspend fun loadActiveOrder() {
        val myUserId = _uiState.value.myUserId
        val productId = contextProductId
        safeCall {
            coroutineScope {
                val buying = async { orders.getOrders(OrderRole.BUYER) }
                val selling = async { orders.getOrders(OrderRole.SELLER) }
                buying.await() + selling.await()
            }
        }.onSuccess { all ->
            val order = all
                .filter { it.partnerOf(myUserId).id == partnerId && !it.status.isFinal }
                .filter { productId == null || it.productId == productId }
                .maxByOrNull { it.id }
            _uiState.update { it.copy(activeOrder = order) }
            updatePartner()
        }
    }

    /** Lấy thông tin người kia (tên, ảnh) từ tin nhắn mới nhất, hoặc từ món đồ / đơn hàng. */
    private fun updatePartner() {
        _uiState.update { state ->
            val partner = state.messages.asReversed().firstNotNullOfOrNull { message ->
                when (partnerId) {
                    message.sender.id -> message.sender
                    message.receiver.id -> message.receiver
                    else -> null
                }
            }
                ?: state.activeOrder?.partnerOf(state.myUserId)
                ?: state.product?.seller?.takeIf { it.id == partnerId }
                ?: state.partner
            state.copy(
                partner = partner,
                partnerName = partner?.fullName?.takeIf { it.isNotBlank() } ?: state.partnerName,
            )
        }
    }

    /** Báo "đã đọc" khi có tin mới của người kia (chỉ gọi khi thật sự có tin chưa đọc). */
    private fun markReadIfNeeded() {
        val state = _uiState.value
        val newestUnreadId = state.messages
            .lastOrNull { it.receiver.id == state.myUserId && !it.isRead }
            ?.id ?: return
        if (newestUnreadId <= lastMarkedReadId) return
        lastMarkedReadId = newestUnreadId
        viewModelScope.launch {
            safeCall { chat.markConversationRead(partnerId) }
                .onFailure { lastMarkedReadId = 0L } // thử lại ở lần hỏi sau
        }
    }

    // ---------------------------------------------------------------------
    // Gửi tin nhắn
    // ---------------------------------------------------------------------

    fun onDraftChange(value: String) {
        if (value.length <= Validators.MESSAGE_MAX) _uiState.update { it.copy(draft = value) }
    }

    fun sendDraft() {
        val text = _uiState.value.draft.trim()
        if (text.isEmpty()) return
        _uiState.update { it.copy(draft = "") }
        send(newInput(MessageType.TEXT, content = text))
    }

    /** Câu hỏi nhanh gợi ý khi cuộc trò chuyện còn trống. */
    fun sendQuickReply(text: String) {
        send(newInput(MessageType.TEXT, content = text))
    }

    fun sendImage(uri: Uri) {
        send(newInput(MessageType.IMAGE), localImage = uri)
    }

    fun sendOffer(price: Long, note: String) {
        send(newInput(MessageType.OFFER, content = note.trim(), offeredPrice = price))
    }

    fun sendMeetup(location: String, time: LocalDateTime, note: String) {
        send(newInput(MessageType.MEETUP, content = note.trim(), meetupLocation = location, meetupTime = time))
    }

    fun retryPending(localId: Long) {
        val pending = _uiState.value.pending.firstOrNull { it.localId == localId } ?: return
        updatePending(localId) { it.copy(isFailed = false) }
        deliver(pending.copy(isFailed = false))
    }

    fun discardPending(localId: Long) {
        _uiState.update { state -> state.copy(pending = state.pending.filterNot { it.localId == localId }) }
    }

    private fun newInput(
        type: MessageType,
        content: String = "",
        offeredPrice: Long? = null,
        meetupLocation: String? = null,
        meetupTime: LocalDateTime? = null,
    ) = SendMessageInput(
        receiverId = partnerId,
        productId = _uiState.value.product?.id,
        type = type,
        content = content,
        offeredPrice = offeredPrice,
        meetupLocation = meetupLocation,
        meetupTime = meetupTime,
    )

    /** Hiện tin ngay trong danh sách (trạng thái "đang gửi") rồi mới gửi lên máy chủ. */
    private fun send(input: SendMessageInput, localImage: Uri? = null) {
        val pending = PendingMessage(localId = nextLocalId--, input = input, localImage = localImage)
        _uiState.update { it.copy(pending = it.pending + pending) }
        deliver(pending)
    }

    private fun deliver(pending: PendingMessage) {
        viewModelScope.launch {
            safeCall {
                var input = pending.input
                if (input.type == MessageType.IMAGE && input.imageUrl == null) {
                    val uri = checkNotNull(pending.localImage) { "Thiếu ảnh cần gửi" }
                    input = input.copy(imageUrl = uploads.uploadImage(uri, UploadFolder.CHAT))
                    // Lưu lại link ảnh: lỡ gửi tin lỗi thì lần gửi lại không phải tải ảnh lên nữa
                    updatePending(pending.localId) { it.copy(input = input) }
                }
                chat.sendMessage(input)
            }.onSuccess { message ->
                _uiState.update { state ->
                    val alreadyShown = state.messages.any { it.id == message.id }
                    state.copy(
                        messages = if (alreadyShown) state.messages else state.messages + message,
                        pending = state.pending.filterNot { it.localId == pending.localId },
                    )
                }
            }.onFailure { error ->
                updatePending(pending.localId) { it.copy(isFailed = true) }
                _uiState.update { it.copy(userMessage = error.toUserMessage()) }
            }
        }
    }

    private fun updatePending(localId: Long, change: (PendingMessage) -> PendingMessage) {
        _uiState.update { state ->
            state.copy(pending = state.pending.map { if (it.localId == localId) change(it) else it })
        }
    }

    // ---------------------------------------------------------------------
    // Thao tác với đơn đang mở
    // ---------------------------------------------------------------------

    fun runOrderAction(action: OrderAction, reason: String) {
        val state = _uiState.value
        val order = state.activeOrder ?: return
        if (state.isOrderActionRunning) return
        _uiState.update { it.copy(isOrderActionRunning = true) }
        viewModelScope.launch {
            safeCall { orders.perform(action, order, state.myUserId, reason) }
                .onSuccess { updated ->
                    // Đơn đã kết thúc (hoàn tất / hủy) thì ẩn khỏi khung chat
                    val stillOpen = if (updated.status.isFinal) null else updated
                    _uiState.update {
                        it.copy(
                            activeOrder = stillOpen,
                            isOrderActionRunning = false,
                            userMessage = action.successMessage(),
                        )
                    }
                    // Máy chủ đã thêm tin hệ thống vào cuộc trò chuyện, món đồ cũng đổi trạng thái
                    safeCall { poll() }
                    loadProduct()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isOrderActionRunning = false, userMessage = error.toUserMessage()) }
                    // Đơn có thể đã được người kia thay đổi trước đó -> lấy lại trạng thái mới nhất
                    loadActiveOrder()
                }
        }
    }
}
