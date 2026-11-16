package com.chodocu.app.ui.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chodocu.app.core.formatClock
import com.chodocu.app.core.formatMeetupTime
import com.chodocu.app.core.formatVnd
import com.chodocu.app.data.model.ChatMessage
import com.chodocu.app.data.model.MessageType
import com.chodocu.app.ui.components.RemoteImage
import com.chodocu.app.ui.components.UserAvatar
import com.chodocu.app.ui.components.hairlineBorder
import com.chodocu.app.ui.theme.AppColors
import com.chodocu.app.ui.theme.Radius
import com.chodocu.app.ui.theme.Sizes
import com.chodocu.app.ui.theme.Space
import com.chodocu.app.ui.theme.TextTertiary
import java.time.LocalDateTime

private val AVATAR_SIZE = 28.dp
private val BUBBLE_MAX_WIDTH = 280.dp
private val CARD_WIDTH = 260.dp
private val BUBBLE_RADIUS = 18.dp
private val BUBBLE_TAIL_RADIUS = 6.dp

/** Những gì cần vẽ trong một bong bóng — dùng chung cho tin đã gửi và tin đang gửi. */
private data class BubbleContent(
    val type: MessageType,
    val text: String,
    /** Link ảnh trên máy chủ, hoặc uri ảnh trong máy khi chưa tải lên xong. */
    val imageModel: String?,
    val offeredPrice: Long?,
    val meetupLocation: String?,
    val meetupTime: LocalDateTime?,
    val productId: Long?,
    val productTitle: String?,
)

private fun ChatMessage.toBubbleContent() = BubbleContent(
    type = type,
    text = content,
    imageModel = imageUrl,
    offeredPrice = offeredPrice,
    meetupLocation = meetupLocation,
    meetupTime = meetupTime,
    productId = productId,
    productTitle = productTitle,
)

private fun PendingMessage.toBubbleContent(productTitle: String?) = BubbleContent(
    type = input.type,
    text = input.content,
    imageModel = input.imageUrl ?: localImage?.toString(),
    offeredPrice = input.offeredPrice,
    meetupLocation = input.meetupLocation,
    meetupTime = input.meetupTime,
    productId = input.productId,
    productTitle = productTitle,
)

/**
 * Câu tóm tắt máy chủ tự điền khi người gửi không ghi chú (tin trả giá / hẹn gặp / đặt mua).
 * Thẻ đã hiện đủ giá, điểm hẹn, giờ hẹn nên không lặp lại câu này trong thẻ.
 */
private val AUTO_SUMMARIES = listOf(
    Regex("""Mình đề nghị giá [\d.]+\s?đ\."""),
    Regex("""Mình đề xuất hẹn gặp tại .+ lúc .+\."""),
    Regex("""Mình muốn mua món này\. Hẹn gặp bạn theo lịch bên dưới nhé\."""),
)

/** Lời nhắn người gửi tự viết kèm thẻ (rỗng nếu chỉ là câu tóm tắt tự động). */
private val BubbleContent.note: String
    get() = if (AUTO_SUMMARIES.any { it.matches(text) }) "" else text

// ---------------------------------------------------------------------------
// Các dòng trong danh sách chat
// ---------------------------------------------------------------------------

/** Nhãn ngày ở giữa ("Hôm nay", "Hôm qua", "Thứ Hai, 16/11/2026"). */
@Composable
internal fun DayHeaderRow(label: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = Space.sm), contentAlignment = Alignment.Center) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .clip(RoundedCornerShape(Radius.pill))
                .background(MaterialTheme.colorScheme.surface)
                .hairlineBorder(MaterialTheme.colorScheme.outlineVariant, Radius.pill)
                .padding(horizontal = Space.sm, vertical = Space.xxs),
        )
    }
}

/** Một tin đã gửi: bong bóng chữ / ảnh / thẻ trả giá / thẻ hẹn gặp, hoặc dòng thông báo hệ thống. */
@Composable
internal fun MessageRow(
    item: ChatItem.Message,
    partnerName: String,
    partnerAvatarUrl: String?,
    onImageClick: (String) -> Unit,
    onProductClick: (Long) -> Unit,
) {
    val message = item.message
    if (message.type == MessageType.SYSTEM) {
        SystemMessageRow(text = message.content, time = formatClock(message.sentAt))
        return
    }
    // Giờ gửi (và "Đã xem") chỉ hiện dưới tin cuối của mỗi nhóm cho gọn
    val footerText = if (item.isLastInGroup) {
        listOf(formatClock(message.sentAt), item.deliveryStatus.orEmpty()).filter { it.isNotEmpty() }.joinToString(" · ")
    } else {
        ""
    }
    val footer: (@Composable () -> Unit)? = if (footerText.isEmpty()) null else { { FooterText(footerText) } }
    BubbleLayout(
        isMine = item.isMine,
        topSpacing = if (item.isFirstInGroup) Space.sm else 2.dp,
        avatar = {
            if (item.isLastInGroup) UserAvatar(name = partnerName, imageUrl = partnerAvatarUrl, size = AVATAR_SIZE)
        },
        footer = footer,
    ) {
        BubbleBody(
            content = message.toBubbleContent(),
            isMine = item.isMine,
            hasTail = item.isLastInGroup,
            onImageClick = onImageClick,
            onProductClick = onProductClick,
        )
    }
}

/** Tin mình đang gửi (mờ) hoặc gửi lỗi (kèm nút Gửi lại / Xóa). */
@Composable
internal fun PendingMessageRow(
    pending: PendingMessage,
    productTitle: String?,
    onRetry: () -> Unit,
    onDiscard: () -> Unit,
) {
    BubbleLayout(
        isMine = true,
        topSpacing = Space.sm,
        avatar = {},
        footer = { PendingFooter(isFailed = pending.isFailed, onRetry = onRetry, onDiscard = onDiscard) },
    ) {
        Box(modifier = Modifier.alpha(if (pending.isFailed) 1f else 0.6f)) {
            BubbleBody(
                content = pending.toBubbleContent(productTitle),
                isMine = true,
                hasTail = true,
                onImageClick = null,
                onProductClick = null,
            )
        }
    }
}

/** Lời nhắc an toàn ở đầu cuộc trò chuyện. */
@Composable
internal fun SafetyNoticeRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Space.sm)
            .clip(RoundedCornerShape(Radius.input))
            .background(MaterialTheme.colorScheme.surface)
            .hairlineBorder(MaterialTheme.colorScheme.outlineVariant, Radius.input)
            .padding(Space.sm),
        horizontalArrangement = Arrangement.spacedBy(Space.xs),
    ) {
        Icon(
            imageVector = Icons.Outlined.Shield,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Sizes.icon),
        )
        Text(
            text = "Hẹn gặp tại điểm giao dịch trong trường, kiểm tra hàng rồi mới thanh toán. " +
                "Không chuyển khoản đặt cọc trước cho người lạ.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Dòng thông báo do hệ thống tạo (đặt mua, chấp nhận, hủy...). */
@Composable
private fun SystemMessageRow(text: String, time: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = Space.xs), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(Radius.input))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = Space.sm, vertical = Space.xs),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (time.isNotEmpty()) {
                Text(text = time, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Khung bố cục và các loại bong bóng
// ---------------------------------------------------------------------------

/** Tin của mình nằm bên phải; tin người kia nằm bên trái kèm ảnh đại diện nhỏ. */
@Composable
private fun BubbleLayout(
    isMine: Boolean,
    topSpacing: Dp,
    avatar: @Composable () -> Unit,
    footer: (@Composable () -> Unit)?,
    bubble: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = topSpacing),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            if (!isMine) {
                Box(modifier = Modifier.size(AVATAR_SIZE)) { avatar() }
                Spacer(Modifier.width(Space.xs))
            }
            Box(modifier = Modifier.widthIn(max = BUBBLE_MAX_WIDTH)) { bubble() }
        }
        if (footer != null) {
            Box(modifier = Modifier.padding(top = 2.dp, start = if (isMine) 0.dp else AVATAR_SIZE + Space.xs)) {
                footer()
            }
        }
    }
}

@Composable
private fun BubbleBody(
    content: BubbleContent,
    isMine: Boolean,
    hasTail: Boolean,
    onImageClick: ((String) -> Unit)?,
    onProductClick: ((Long) -> Unit)?,
) {
    when (content.type) {
        MessageType.TEXT, MessageType.SYSTEM -> TextBubble(text = content.text, isMine = isMine, hasTail = hasTail)
        MessageType.IMAGE -> ImageBubble(content = content, isMine = isMine, hasTail = hasTail, onImageClick = onImageClick)
        MessageType.OFFER -> ActionCard(isMine = isMine, icon = Icons.Outlined.LocalOffer, title = "Đề nghị giá") {
            content.offeredPrice?.let { price ->
                Text(
                    text = formatVnd(price),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Price,
                )
            }
            CardNote(content.note)
            ProductReference(content = content, onProductClick = onProductClick)
        }
        MessageType.MEETUP -> ActionCard(isMine = isMine, icon = Icons.Outlined.Event, title = "Đề xuất hẹn gặp") {
            content.meetupLocation?.let { DetailLine(icon = Icons.Outlined.Place, text = it) }
            content.meetupTime?.let { DetailLine(icon = Icons.Outlined.Schedule, text = formatMeetupTime(it)) }
            content.offeredPrice?.let { DetailLine(icon = Icons.Outlined.Payments, text = "Giá: ${formatVnd(it)}") }
            CardNote(content.note)
            ProductReference(content = content, onProductClick = onProductClick)
        }
    }
}

/** Góc dưới phía người gửi bo nhỏ hơn ở tin cuối nhóm, tạo cảm giác "đuôi" bong bóng. */
private fun bubbleShape(isMine: Boolean, hasTail: Boolean) = RoundedCornerShape(
    topStart = BUBBLE_RADIUS,
    topEnd = BUBBLE_RADIUS,
    bottomEnd = if (isMine && hasTail) BUBBLE_TAIL_RADIUS else BUBBLE_RADIUS,
    bottomStart = if (!isMine && hasTail) BUBBLE_TAIL_RADIUS else BUBBLE_RADIUS,
)

@Composable
private fun TextBubble(text: String, isMine: Boolean, hasTail: Boolean) {
    Surface(
        shape = bubbleShape(isMine, hasTail),
        color = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        contentColor = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        border = if (isMine) null else BorderStroke(Sizes.border, MaterialTheme.colorScheme.outlineVariant),
    ) {
        // Nhấn giữ để chọn / sao chép nội dung
        SelectionContainer {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = Space.sm, vertical = Space.xs),
            )
        }
    }
}

@Composable
private fun ImageBubble(
    content: BubbleContent,
    isMine: Boolean,
    hasTail: Boolean,
    onImageClick: ((String) -> Unit)?,
) {
    val model = content.imageModel
    val hasCaption = content.text.isNotBlank()
    Column(
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        RemoteImage(
            url = model,
            contentDescription = "Ảnh trong tin nhắn",
            modifier = Modifier
                .size(width = 210.dp, height = 250.dp)
                .clip(bubbleShape(isMine, hasTail && !hasCaption))
                .then(
                    if (model != null && onImageClick != null) Modifier.clickable { onImageClick(model) } else Modifier
                ),
        )
        if (hasCaption) TextBubble(text = content.text, isMine = isMine, hasTail = hasTail)
    }
}

/** Thẻ cho tin trả giá / hẹn gặp: nổi bật hơn tin chữ thường. */
@Composable
private fun ActionCard(
    isMine: Boolean,
    icon: ImageVector,
    title: String,
    body: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(Radius.card),
        color = if (isMine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(Sizes.border, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.width(CARD_WIDTH),
    ) {
        Column(modifier = Modifier.padding(Space.sm), verticalArrangement = Arrangement.spacedBy(Space.xxs)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.xxs)) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(Sizes.iconSmall))
                Text(text = title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            body()
        }
    }
}

@Composable
private fun DetailLine(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp).size(Sizes.iconSmall),
        )
        Text(text = text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CardNote(text: String) {
    if (text.isBlank()) return
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = Space.xxs),
    )
}

/** Tên món đồ được nhắc tới trong thẻ — bấm để mở trang món đồ. */
@Composable
private fun ProductReference(content: BubbleContent, onProductClick: ((Long) -> Unit)?) {
    val title = content.productTitle ?: return
    val productId = content.productId
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Space.xxs)
            .clip(RoundedCornerShape(Radius.chip))
            .then(
                if (productId != null && onProductClick != null) Modifier.clickable { onProductClick(productId) } else Modifier
            )
            .padding(vertical = Space.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Món: $title",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (productId != null && onProductClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Sizes.iconSmall),
            )
        }
    }
}

@Composable
private fun FooterText(text: String) {
    Text(text = text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun PendingFooter(isFailed: Boolean, onRetry: () -> Unit, onDiscard: () -> Unit) {
    if (!isFailed) {
        FooterText("Đang gửi…")
        return
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(Space.xxs))
        Text(text = "Chưa gửi được", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
        TextButton(onClick = onRetry, contentPadding = PaddingValues(horizontal = Space.xs)) {
            Text("Gửi lại", style = MaterialTheme.typography.labelMedium)
        }
        TextButton(onClick = onDiscard, contentPadding = PaddingValues(horizontal = Space.xs)) {
            Text("Xóa", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
