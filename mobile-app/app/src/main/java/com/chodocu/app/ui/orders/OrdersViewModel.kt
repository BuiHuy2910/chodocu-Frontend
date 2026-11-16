package com.chodocu.app.ui.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chodocu.app.core.safeCall
import com.chodocu.app.data.common.toUserMessage
import com.chodocu.app.data.model.Order
import com.chodocu.app.data.model.OrderRole
import com.chodocu.app.data.model.OrderStatus
import com.chodocu.app.data.repository.OrderRepository
import com.chodocu.app.data.session.SessionManager
import com.chodocu.app.ui.components.OrderAction
import com.chodocu.app.ui.components.perform
import com.chodocu.app.ui.components.successMessage
import com.chodocu.app.ui.navigation.NavArgs
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Bộ lọc trạng thái đơn trên màn Giao dịch. */
enum class OrderFilter(val label: String, private val statuses: Set<OrderStatus>?) {
    ALL("Tất cả", null),
    ACTIVE("Đang xử lý", setOf(OrderStatus.PENDING, OrderStatus.ACCEPTED, OrderStatus.DISPUTED)),
    COMPLETED("Hoàn tất", setOf(OrderStatus.COMPLETED)),
    CLOSED("Đã hủy", setOf(OrderStatus.REJECTED, OrderStatus.CANCELLED));

    fun matches(order: Order): Boolean = statuses == null || order.status in statuses
}

/** Trạng thái màn Giao dịch (đơn mua + đơn bán). */
data class OrdersUiState(
    val myUserId: Long,
    val role: OrderRole,
    val buyerOrders: List<Order> = emptyList(),
    val sellerOrders: List<Order> = emptyList(),
    val filter: OrderFilter = OrderFilter.ALL,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    /** Đơn đang được thao tác (hiện vòng xoay trên nút). */
    val runningOrderId: Long? = null,
    val userMessage: String? = null,
) {
    /** Đơn của tab đang chọn (chưa lọc). */
    val ordersOfRole: List<Order> get() = if (role == OrderRole.BUYER) buyerOrders else sellerOrders

    /** Đơn hiển thị sau khi lọc theo trạng thái. */
    val visibleOrders: List<Order> get() = ordersOfRole.filter(filter::matches)

    fun countFor(filter: OrderFilter): Int = ordersOfRole.count(filter::matches)

    /** Số đơn cần chú ý (chưa kết thúc) của mỗi tab, hiện trên tiêu đề tab. */
    fun activeCount(role: OrderRole): Int =
        (if (role == OrderRole.BUYER) buyerOrders else sellerOrders).count { !it.status.isFinal }
}

/** Danh sách đơn đặt mua / đơn bán và các thao tác trên đơn. */
class OrdersViewModel(
    private val orders: OrderRepository,
    session: SessionManager,
    handle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        OrdersUiState(
            myUserId = session.currentUser.value?.id ?: -1L,
            role = OrderRole.fromApi(handle.get<String>(NavArgs.TAB)),
        )
    )
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

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

    /** Cập nhật ngầm (định kỳ / khi quay lại màn hình). */
    fun refreshSilently() {
        if (loadJob?.isActive == true) return
        fetch(showErrors = false)
    }

    fun selectRole(role: OrderRole) {
        _uiState.update { it.copy(role = role, filter = OrderFilter.ALL) }
    }

    fun selectFilter(filter: OrderFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun runAction(order: Order, action: OrderAction, reason: String) {
        val state = _uiState.value
        if (state.runningOrderId != null) return
        _uiState.update { it.copy(runningOrderId = order.id) }
        viewModelScope.launch {
            safeCall { orders.perform(action, order, state.myUserId, reason) }
                .onSuccess { updated ->
                    _uiState.update {
                        it.copy(
                            buyerOrders = it.buyerOrders.replace(updated),
                            sellerOrders = it.sellerOrders.replace(updated),
                            runningOrderId = null,
                            userMessage = action.successMessage(),
                        )
                    }
                    // Chấp nhận một đơn thì các yêu cầu khác cho cùng món đồ bị từ chối -> tải lại cho đúng
                    refreshSilently()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(runningOrderId = null, userMessage = error.toUserMessage()) }
                    refreshSilently()
                }
        }
    }

    private fun fetch(showErrors: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            safeCall {
                coroutineScope {
                    val buying = async { orders.getOrders(OrderRole.BUYER) }
                    val selling = async { orders.getOrders(OrderRole.SELLER) }
                    buying.await() to selling.await()
                }
            }.onSuccess { (buying, selling) ->
                _uiState.update {
                    it.copy(
                        buyerOrders = buying,
                        sellerOrders = selling,
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = null,
                    )
                }
            }.onFailure { error ->
                val message = error.toUserMessage()
                _uiState.update {
                    val hasData = it.buyerOrders.isNotEmpty() || it.sellerOrders.isNotEmpty()
                    when {
                        !showErrors -> it.copy(isLoading = false, isRefreshing = false)
                        hasData -> it.copy(isLoading = false, isRefreshing = false, userMessage = message)
                        else -> it.copy(isLoading = false, isRefreshing = false, errorMessage = message)
                    }
                }
            }
        }
    }

    /** Thay đơn cũ bằng bản mới máy chủ trả về (giữ nguyên vị trí trong danh sách). */
    private fun List<Order>.replace(updated: Order): List<Order> = map { if (it.id == updated.id) updated else it }
}
