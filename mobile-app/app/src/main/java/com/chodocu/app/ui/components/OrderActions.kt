package com.chodocu.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chodocu.app.core.formatMeetupTime
import com.chodocu.app.data.model.Order
import com.chodocu.app.data.model.OrderStatus
import com.chodocu.app.data.repository.OrderRepository
import com.chodocu.app.ui.theme.Space

/**
 * Các thao tác trên một đơn đặt mua (dùng chung cho màn Giao dịch và màn Chat).
 * - ACCEPT / DECLINE: người bán trả lời yêu cầu đang chờ.
 * - CANCEL: hủy theo vai trò (người mua hủy, người bán từ chối) khi đơn chưa kết thúc.
 * - COMPLETE / DISPUTE: một trong hai bên xác nhận xong hoặc khiếu nại khi đã chốt hẹn.
 */
enum class OrderAction { ACCEPT, DECLINE, CANCEL, COMPLETE, DISPUTE }

/** Những thao tác mình được làm với đơn ở trạng thái hiện tại (theo quy tắc của máy chủ). */
fun Order.availableActions(myUserId: Long): List<OrderAction> {
    val isBuyer = isBuyer(myUserId)
    return when (status) {
        OrderStatus.PENDING -> if (isBuyer) listOf(OrderAction.CANCEL) else listOf(OrderAction.ACCEPT, OrderAction.DECLINE)
        OrderStatus.ACCEPTED -> listOf(OrderAction.COMPLETE, OrderAction.CANCEL, OrderAction.DISPUTE)
        OrderStatus.DISPUTED -> listOf(OrderAction.CANCEL)
        OrderStatus.COMPLETED, OrderStatus.REJECTED, OrderStatus.CANCELLED -> emptyList()
    }
}

/** Thao tác "phá hủy" (hủy, từ chối, khiếu nại) — hiện màu đỏ. */
val OrderAction.isDestructive: Boolean
    get() = this == OrderAction.DECLINE || this == OrderAction.CANCEL || this == OrderAction.DISPUTE

/** Nút chính, nút phụ và các thao tác ít dùng (cho vào menu "⋮") để hiển thị gọn trên một hàng. */
data class OrderActionLayout(
    val primary: OrderAction?,
    val secondary: OrderAction?,
    val overflow: List<OrderAction>,
)

fun Order.actionLayout(myUserId: Long): OrderActionLayout {
    val actions = availableActions(myUserId)
    val primary = actions.firstOrNull { it == OrderAction.ACCEPT || it == OrderAction.COMPLETE }
    val secondary = when {
        OrderAction.DECLINE in actions -> OrderAction.DECLINE
        primary == null -> actions.firstOrNull()
        else -> null
    }
    return OrderActionLayout(primary, secondary, actions - setOfNotNull(primary, secondary))
}

/**
 * Hàng nút thao tác của một đơn (dùng ở màn Giao dịch và khung chat).
 * [onAction] chỉ nên mở hộp thoại xác nhận ([OrderActionDialog]); [leading] là nút phụ đặt đầu hàng (vd "Nhắn tin").
 */
@Composable
fun OrderActionButtons(
    order: Order,
    myUserId: Long,
    isBusy: Boolean,
    onAction: (OrderAction) -> Unit,
    modifier: Modifier = Modifier,
    leading: @Composable RowScope.() -> Unit = {},
) {
    val layout = order.actionLayout(myUserId)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.xs),
    ) {
        leading()
        layout.primary?.let { action ->
            PrimaryButton(
                text = action.label(order),
                onClick = { onAction(action) },
                loading = isBusy,
                modifier = Modifier.weight(1f).height(COMPACT_BUTTON_HEIGHT),
            )
        }
        layout.secondary?.let { action ->
            SecondaryButton(
                text = action.label(order),
                onClick = { onAction(action) },
                enabled = !isBusy || layout.primary == null,
                loading = isBusy && layout.primary == null,
                contentColor = if (action.isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f).height(COMPACT_BUTTON_HEIGHT),
            )
        }
        if (layout.overflow.isNotEmpty()) {
            OrderOverflowMenu(order = order, actions = layout.overflow, enabled = !isBusy, onAction = onAction)
        }
    }
}

private val COMPACT_BUTTON_HEIGHT = 40.dp

@Composable
private fun OrderOverflowMenu(order: Order, actions: List<OrderAction>, enabled: Boolean, onAction: (OrderAction) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }, enabled = enabled) {
            Icon(Icons.Outlined.MoreVert, contentDescription = "Thao tác khác")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            actions.forEach { action ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = action.label(order),
                            color = if (action.isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    onClick = {
                        expanded = false
                        onAction(action)
                    },
                )
            }
        }
    }
}

/** Nhãn nút của từng thao tác. */
fun OrderAction.label(order: Order): String = when (this) {
    OrderAction.ACCEPT -> "Chấp nhận"
    OrderAction.DECLINE -> "Từ chối"
    OrderAction.COMPLETE -> "Đã giao dịch xong"
    OrderAction.DISPUTE -> "Khiếu nại"
    OrderAction.CANCEL -> when (order.status) {
        OrderStatus.PENDING -> "Hủy yêu cầu"
        OrderStatus.DISPUTED -> "Hủy giao dịch"
        else -> "Hủy hẹn"
    }
}

/** Câu báo sau khi thao tác thành công. */
fun OrderAction.successMessage(): String = when (this) {
    OrderAction.ACCEPT -> "Đã chấp nhận. Hai bên đã chốt lịch hẹn."
    OrderAction.DECLINE -> "Đã từ chối yêu cầu mua."
    OrderAction.CANCEL -> "Đã hủy."
    OrderAction.COMPLETE -> "Giao dịch đã hoàn tất. Cảm ơn bạn!"
    OrderAction.DISPUTE -> "Đã gửi khiếu nại tới quản trị viên."
}

/** Gọi đúng API cho thao tác: người mua hủy bằng cancelOrder, người bán bằng rejectOrder. */
suspend fun OrderRepository.perform(action: OrderAction, order: Order, myUserId: Long, reason: String): Order {
    val note = reason.trim().ifEmpty { null }
    return when (action) {
        OrderAction.ACCEPT -> acceptOrder(order.id)
        OrderAction.DECLINE -> rejectOrder(order.id, note)
        OrderAction.CANCEL -> if (order.isBuyer(myUserId)) cancelOrder(order.id, note) else rejectOrder(order.id, note)
        OrderAction.COMPLETE -> completeOrder(order.id)
        OrderAction.DISPUTE -> disputeOrder(order.id, note.orEmpty())
    }
}

/**
 * Hộp thoại xác nhận trước khi thực hiện [action]. [onConfirm] nhận lý do (chuỗi rỗng nếu không cần).
 * Nơi gọi tự đóng hộp thoại trong onConfirm / onDismiss.
 */
@Composable
fun OrderActionDialog(
    action: OrderAction,
    order: Order,
    myUserId: Long,
    onConfirm: (reason: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val isBuyer = order.isBuyer(myUserId)
    when (action) {
        OrderAction.ACCEPT -> ConfirmDialog(
            title = "Chấp nhận yêu cầu mua?",
            message = buildString {
                append("Hai bên sẽ gặp nhau")
                order.meetupLocation?.let { append(" tại $it") }
                order.meetupTime?.let { append(" lúc ${formatMeetupTime(it)}") }
                append(". Các yêu cầu khác đang chờ cho món đồ này sẽ tự động bị từ chối.")
            },
            confirmText = "Chấp nhận",
            onConfirm = { onConfirm("") },
            onDismiss = onDismiss,
        )
        OrderAction.COMPLETE -> ConfirmDialog(
            title = "Xác nhận đã giao dịch xong?",
            message = "Chỉ xác nhận khi hai bên đã gặp mặt, kiểm tra hàng và thanh toán đầy đủ.",
            confirmText = "Xác nhận",
            onConfirm = { onConfirm("") },
            onDismiss = onDismiss,
        )
        OrderAction.DECLINE -> ReasonDialog(
            title = "Từ chối yêu cầu mua",
            message = "Người mua sẽ nhận được thông báo kèm lý do.",
            label = "Lý do (không bắt buộc)",
            confirmText = "Từ chối",
            destructive = true,
            suggestions = listOf("Đã bán cho người khác", "Không hợp giờ hẹn", "Giá chưa phù hợp"),
            onConfirm = onConfirm,
            onDismiss = onDismiss,
        )
        OrderAction.CANCEL -> ReasonDialog(
            title = action.label(order),
            message = "Người kia sẽ nhận được thông báo. Tin đăng sẽ mở bán lại nếu hai bên đã chốt hẹn.",
            label = "Lý do (không bắt buộc)",
            confirmText = "Xác nhận hủy",
            destructive = true,
            suggestions = if (isBuyer) {
                listOf("Mình không mua nữa", "Bận đột xuất", "Đã mua được món khác")
            } else {
                listOf("Món đồ không còn", "Bận đột xuất", "Người mua không đến")
            },
            onConfirm = onConfirm,
            onDismiss = onDismiss,
        )
        OrderAction.DISPUTE -> ReasonDialog(
            title = "Khiếu nại giao dịch",
            message = "Quản trị viên sẽ xem xét và liên hệ hai bên. Hãy mô tả rõ vấn đề.",
            label = "Mô tả vấn đề",
            confirmText = "Gửi khiếu nại",
            required = true,
            destructive = true,
            suggestions = listOf("Hàng không đúng mô tả", "Người kia không đến", "Có dấu hiệu lừa đảo"),
            onConfirm = onConfirm,
            onDismiss = onDismiss,
        )
    }
}
