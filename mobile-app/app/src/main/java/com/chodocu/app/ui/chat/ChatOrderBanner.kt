package com.chodocu.app.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chodocu.app.core.formatMeetupTime
import com.chodocu.app.core.formatPrice
import com.chodocu.app.data.model.Order
import com.chodocu.app.data.model.OrderRole
import com.chodocu.app.data.model.OrderStatus
import com.chodocu.app.data.model.Product
import com.chodocu.app.data.model.ProductStatus
import com.chodocu.app.ui.components.OrderAction
import com.chodocu.app.ui.components.OrderActionButtons
import com.chodocu.app.ui.components.PriceText
import com.chodocu.app.ui.components.RemoteImage
import com.chodocu.app.ui.components.StatusChip
import com.chodocu.app.ui.components.tone
import com.chodocu.app.ui.theme.Radius
import com.chodocu.app.ui.theme.Sizes
import com.chodocu.app.ui.theme.Space

/** Dải món đồ đang trao đổi ở đầu khung chat — bấm để mở trang món đồ. */
@Composable
internal fun ChatProductStrip(product: Product, onClick: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = Space.screen, vertical = Space.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.sm),
            ) {
                RemoteImage(
                    url = product.coverImage,
                    contentDescription = product.title,
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(Radius.chip)),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.title,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                        PriceText(price = product.price, isFree = product.isFree, style = MaterialTheme.typography.labelLarge)
                        if (product.status != ProductStatus.APPROVED) {
                            StatusChip(text = product.status.label, tone = product.status.tone())
                        }
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = "Xem món đồ",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

/**
 * Đơn đặt mua đang mở giữa hai người, hiện ngay dưới dải món đồ.
 * Nút chính (Chấp nhận / Đã giao dịch xong) nằm ngoài, các thao tác ít dùng nằm trong menu "⋮".
 * [onAction] chỉ mở hộp thoại xác nhận — nơi gọi lo việc thực hiện.
 */
@Composable
internal fun ChatOrderBanner(
    order: Order,
    myUserId: Long,
    isBusy: Boolean,
    onAction: (OrderAction) -> Unit,
    onOpenOrders: (OrderRole) -> Unit,
) {
    val role = if (order.isBuyer(myUserId)) OrderRole.BUYER else OrderRole.SELLER

    Surface(color = MaterialTheme.colorScheme.surface) {
        Column {
            Column(
                modifier = Modifier.padding(start = Space.screen, end = Space.xs, top = Space.xs, bottom = Space.sm),
                verticalArrangement = Arrangement.spacedBy(Space.xs),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(text = order.status.label, tone = order.status.tone())
                    Box(modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radius.chip))
                            .clickable { onOpenOrders(role) }
                            .padding(horizontal = Space.xs, vertical = Space.xxs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Xem đơn", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(Sizes.iconSmall),
                        )
                    }
                }
                Text(
                    text = order.summaryLine(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = Space.xs),
                )
                order.hintFor(myUserId)?.let { hint ->
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(end = Space.xs),
                    )
                }
                OrderActionButtons(
                    order = order,
                    myUserId = myUserId,
                    isBusy = isBusy,
                    onAction = onAction,
                    modifier = Modifier.padding(end = Space.xxs),
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

/** "150.000 đ · Sảnh nhà A · 09:30, Thứ Ba 17/11/2026". */
private fun Order.summaryLine(): String = listOfNotNull(
    formatPrice(offeredPrice, isFree = false),
    meetupLocation?.takeIf { it.isNotBlank() },
    meetupTime?.let(::formatMeetupTime) ?: meetupTimeRaw?.takeIf { it.isNotBlank() },
).joinToString(" · ")

/** Gợi ý ngắn cho bước tiếp theo, tùy trạng thái và vai trò. */
private fun Order.hintFor(myUserId: Long): String? {
    val isBuyer = isBuyer(myUserId)
    return when (status) {
        OrderStatus.PENDING -> if (isBuyer) {
            "Người bán sẽ xác nhận lịch hẹn. Bạn có thể nhắn tin để thống nhất thêm."
        } else {
            "Người mua muốn mua món này. Xem lại giá và lịch hẹn rồi trả lời nhé."
        }
        OrderStatus.ACCEPTED -> "Hai bên đã chốt hẹn. Gặp nhau, kiểm tra hàng rồi bấm \"Đã giao dịch xong\"."
        OrderStatus.DISPUTED -> "Quản trị viên đang xem xét khiếu nại và sẽ liên hệ hai bên."
        else -> null
    }
}
