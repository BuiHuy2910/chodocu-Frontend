package com.chodocu.app.ui.orders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chodocu.app.core.formatDateTime
import com.chodocu.app.core.formatMeetupTime
import com.chodocu.app.core.formatPrice
import com.chodocu.app.core.formatRelativeTime
import com.chodocu.app.core.formatVnd
import com.chodocu.app.data.model.Order
import com.chodocu.app.data.model.OrderStatus
import com.chodocu.app.ui.components.InfoRow
import com.chodocu.app.ui.components.InlineBanner
import com.chodocu.app.ui.components.OrderAction
import com.chodocu.app.ui.components.OrderActionButtons
import com.chodocu.app.ui.components.RemoteImage
import com.chodocu.app.ui.components.SecondaryButton
import com.chodocu.app.ui.components.StatusChip
import com.chodocu.app.ui.components.StatusTone
import com.chodocu.app.ui.components.UserAvatar
import com.chodocu.app.ui.components.availableActions
import com.chodocu.app.ui.components.hairlineBorder
import com.chodocu.app.ui.components.tone
import com.chodocu.app.ui.theme.AppColors
import com.chodocu.app.ui.theme.Radius
import com.chodocu.app.ui.theme.Sizes
import com.chodocu.app.ui.theme.Space

/** Các hành động trên thẻ đơn. */
internal data class OrderCardActions(
    val onOpenProduct: (Order) -> Unit,
    val onOpenChat: (Order) -> Unit,
    val onCall: (phone: String) -> Unit,
    val onCopyPhone: (phone: String) -> Unit,
    /** Mở hộp thoại xác nhận cho thao tác (chấp nhận, hủy...). */
    val onAction: (Order, OrderAction) -> Unit,
)

/** Thẻ một đơn: món đồ, người kia, lịch hẹn, ghi chú / lý do và các nút thao tác. */
@Composable
internal fun OrderCard(
    order: Order,
    myUserId: Long,
    isBusy: Boolean,
    actions: OrderCardActions,
    modifier: Modifier = Modifier,
) {
    val isBuyer = order.isBuyer(myUserId)
    val partner = order.partnerOf(myUserId)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.card))
            .background(MaterialTheme.colorScheme.surface)
            .hairlineBorder(MaterialTheme.colorScheme.outlineVariant, Radius.card)
            .padding(Space.md),
        verticalArrangement = Arrangement.spacedBy(Space.sm),
    ) {
        // Mã đơn + thời gian + trạng thái
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = listOfNotNull(order.code, formatRelativeTime(order.createdAt).ifEmpty { null }).joinToString(" · "),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            StatusChip(text = order.status.label, tone = order.status.tone())
        }

        // Món đồ
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.input))
                .clickable { actions.onOpenProduct(order) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.sm),
        ) {
            RemoteImage(
                url = order.productImage,
                contentDescription = order.productTitle,
                modifier = Modifier.size(Sizes.thumb).clip(RoundedCornerShape(Radius.input)),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = order.productTitle,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatPrice(order.offeredPrice, isFree = false),
                    style = MaterialTheme.typography.titleMedium,
                    color = AppColors.Price,
                )
                if (order.productPrice > 0 && order.offeredPrice != order.productPrice) {
                    Text(
                        text = "Giá đăng ${formatVnd(order.productPrice)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Người kia trong giao dịch
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            UserAvatar(name = partner.fullName, imageUrl = partner.avatarUrl, size = Sizes.avatarSmall)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBuyer) "Người bán" else "Người mua",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = listOfNotNull(partner.fullName, partner.universityShortName).joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        // Số điện thoại chỉ có sau khi hai bên đã chốt hẹn
        partner.phone?.takeIf { it.isNotBlank() }?.let { phone ->
            PhoneRow(phone = phone, onCall = { actions.onCall(phone) }, onCopy = { actions.onCopyPhone(phone) })
        }

        // Lịch hẹn
        Column {
            InfoRow(icon = Icons.Outlined.Place, label = "Điểm hẹn", value = order.meetupLocation ?: "Chưa có")
            InfoRow(
                icon = Icons.Outlined.Schedule,
                label = "Thời gian",
                value = order.meetupTime?.let(::formatMeetupTime) ?: order.meetupTimeRaw ?: "Chưa có",
            )
            order.platformFeeAmount?.takeIf { it > 0 }?.let { fee ->
                InfoRow(icon = null, label = "Phí nền tảng", value = formatVnd(fee))
                val total = if (isBuyer) order.finalBuyerAmount else order.finalSellerAmount
                total?.let {
                    InfoRow(
                        icon = null,
                        label = if (isBuyer) "Bạn thanh toán" else "Bạn nhận được",
                        value = formatVnd(it),
                        valueColor = AppColors.Price,
                    )
                }
            }
        }

        OrderNotes(order = order)

        if (order.status == OrderStatus.COMPLETED && order.completedAt != null) {
            Text(
                text = "Hoàn tất lúc ${formatDateTime(order.completedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Nút thao tác
        if (order.availableActions(myUserId).isEmpty()) {
            SecondaryButton(
                text = if (isBuyer) "Nhắn tin với người bán" else "Nhắn tin với người mua",
                onClick = { actions.onOpenChat(order) },
                icon = Icons.AutoMirrored.Outlined.Chat,
                modifier = Modifier.fillMaxWidth().height(40.dp),
            )
        } else {
            OrderActionButtons(
                order = order,
                myUserId = myUserId,
                isBusy = isBusy,
                onAction = { action -> actions.onAction(order, action) },
                leading = {
                    OutlinedIconButton(
                        onClick = { actions.onOpenChat(order) },
                        shape = RoundedCornerShape(Radius.button),
                        border = BorderStroke(Sizes.border, MaterialTheme.colorScheme.outline),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Chat,
                            contentDescription = "Nhắn tin",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun PhoneRow(phone: String, onCall: () -> Unit, onCopy: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.input))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(start = Space.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Phone,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Sizes.icon),
        )
        Text(
            text = phone,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f).padding(start = Space.xs),
        )
        IconButton(onClick = onCopy) {
            Icon(Icons.Outlined.ContentCopy, contentDescription = "Sao chép số điện thoại", modifier = Modifier.size(Sizes.icon))
        }
        TextButton(onClick = onCall) { Text("Gọi") }
    }
}

/** Lời nhắn, ghi chú và lý do hủy / khiếu nại (chỉ hiện những gì có). */
@Composable
private fun OrderNotes(order: Order) {
    order.buyerNote?.takeIf { it.isNotBlank() }?.let {
        InlineBanner(title = "Lời nhắn của người mua", text = it, tone = StatusTone.NEUTRAL)
    }
    order.sellerNote?.takeIf { it.isNotBlank() && it != order.cancelReason }?.let {
        InlineBanner(title = "Ghi chú của người bán", text = it, tone = StatusTone.NEUTRAL)
    }
    order.cancelReason?.takeIf { it.isNotBlank() }?.let {
        InlineBanner(
            title = if (order.status == OrderStatus.REJECTED) "Lý do từ chối" else "Lý do hủy",
            text = it,
            tone = StatusTone.WARNING,
        )
    }
    order.disputeReason?.takeIf { it.isNotBlank() }?.let {
        InlineBanner(title = "Nội dung khiếu nại", text = it, tone = StatusTone.DANGER)
    }
}
