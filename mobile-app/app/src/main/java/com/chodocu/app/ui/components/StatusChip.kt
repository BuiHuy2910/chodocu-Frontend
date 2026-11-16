package com.chodocu.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.chodocu.app.data.model.OrderStatus
import com.chodocu.app.data.model.ProductStatus
import com.chodocu.app.ui.theme.DangerBg
import com.chodocu.app.ui.theme.DangerText
import com.chodocu.app.ui.theme.InfoBg
import com.chodocu.app.ui.theme.InfoText
import com.chodocu.app.ui.theme.NeutralBg
import com.chodocu.app.ui.theme.NeutralText
import com.chodocu.app.ui.theme.Radius
import com.chodocu.app.ui.theme.Space
import com.chodocu.app.ui.theme.SuccessBg
import com.chodocu.app.ui.theme.SuccessText
import com.chodocu.app.ui.theme.WarningBg
import com.chodocu.app.ui.theme.WarningText

/** Sắc thái màu của nhãn trạng thái. */
enum class StatusTone { NEUTRAL, SUCCESS, WARNING, DANGER, INFO }

/** Cặp màu (chữ, nền) của từng sắc thái. */
data class ToneColors(val content: Color, val container: Color)

fun StatusTone.toneColors(): ToneColors = when (this) {
    StatusTone.NEUTRAL -> ToneColors(NeutralText, NeutralBg)
    StatusTone.SUCCESS -> ToneColors(SuccessText, SuccessBg)
    StatusTone.WARNING -> ToneColors(WarningText, WarningBg)
    StatusTone.DANGER -> ToneColors(DangerText, DangerBg)
    StatusTone.INFO -> ToneColors(InfoText, InfoBg)
}

/** Màu trạng thái tin đăng. */
fun ProductStatus.tone(): StatusTone = when (this) {
    ProductStatus.PENDING -> StatusTone.WARNING
    ProductStatus.APPROVED -> StatusTone.SUCCESS
    ProductStatus.IN_TRANSACTION -> StatusTone.INFO
    ProductStatus.SOLD -> StatusTone.NEUTRAL
    ProductStatus.REJECTED -> StatusTone.DANGER
    ProductStatus.HIDDEN -> StatusTone.NEUTRAL
}

/** Màu trạng thái đơn hàng. */
fun OrderStatus.tone(): StatusTone = when (this) {
    OrderStatus.PENDING -> StatusTone.WARNING
    OrderStatus.ACCEPTED -> StatusTone.INFO
    OrderStatus.COMPLETED -> StatusTone.SUCCESS
    OrderStatus.DISPUTED -> StatusTone.DANGER
    OrderStatus.REJECTED -> StatusTone.NEUTRAL
    OrderStatus.CANCELLED -> StatusTone.NEUTRAL
}

/** Nhãn trạng thái nhỏ bo góc (ví dụ "Chờ duyệt", "Đã bán"). */
@Composable
fun StatusChip(
    text: String,
    tone: StatusTone,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val colors = tone.toneColors()
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.chip))
            .background(colors.container)
            .padding(horizontal = Space.xs, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.xxs),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = colors.content, modifier = Modifier.size(14.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = colors.content, maxLines = 1)
    }
}
