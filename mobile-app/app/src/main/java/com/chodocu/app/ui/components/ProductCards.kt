package com.chodocu.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chodocu.app.core.formatPrice
import com.chodocu.app.core.formatRelativeTime
import com.chodocu.app.core.formatVnd
import com.chodocu.app.data.model.Product
import com.chodocu.app.data.model.ProductStatus
import com.chodocu.app.ui.theme.AppColors
import com.chodocu.app.ui.theme.BrandGreen
import com.chodocu.app.ui.theme.BrandOrange
import com.chodocu.app.ui.theme.Radius
import com.chodocu.app.ui.theme.Sizes
import com.chodocu.app.ui.theme.Space

/** Giá tiền (màu cam đậm); món tặng miễn phí hiển thị "Miễn phí". */
@Composable
fun PriceText(
    price: Long,
    isFree: Boolean,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium,
) {
    Text(
        text = formatPrice(price, isFree),
        style = style,
        color = AppColors.Price,
        maxLines = 1,
        modifier = modifier,
    )
}

/** Dòng phụ "trường · thời gian đăng". */
private fun Product.metaLine(): String =
    listOfNotNull(universityShortName?.takeIf { it.isNotBlank() }, formatRelativeTime(createdAt).takeIf { it.isNotBlank() })
        .joinToString(" · ")

/**
 * Thẻ sản phẩm dạng lưới (trang chủ, yêu thích): ảnh 4:3, giá, tiêu đề 2 dòng, trường + thời gian, tình trạng.
 * Tin không còn bán (đã bán, đang giao dịch...) có nhãn trạng thái trên ảnh.
 */
@Composable
fun ProductGridCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.card))
            .background(MaterialTheme.colorScheme.surface)
            .hairlineBorder(MaterialTheme.colorScheme.outlineVariant, Radius.card)
            .clickable(onClick = onClick),
    ) {
        Box {
            RemoteImage(
                url = product.coverImage,
                contentDescription = product.title,
                modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f),
            )
            Row(
                modifier = Modifier.padding(Space.xs),
                horizontalArrangement = Arrangement.spacedBy(Space.xxs),
            ) {
                if (product.isFree) ImageBadge(text = "Miễn phí", background = BrandGreen)
                if (product.isUrgent) ImageBadge(text = "Gấp", background = BrandOrange)
            }
            if (product.status != ProductStatus.APPROVED) {
                StatusChip(
                    text = product.status.label,
                    tone = product.status.tone(),
                    modifier = Modifier.align(Alignment.TopEnd).padding(Space.xs),
                )
            }
        }
        Column(
            modifier = Modifier.padding(Space.sm),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(Space.xxs)) {
                PriceText(price = product.price, isFree = product.isFree, modifier = Modifier.weight(1f, fill = false))
                val original = product.originalPrice
                if (!product.isFree && original != null && original > product.price) {
                    Text(
                        text = formatVnd(original),
                        style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                text = product.title,
                style = MaterialTheme.typography.bodyMedium,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = product.metaLine(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = product.condition.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Nhãn nhỏ nằm trên ảnh ("Miễn phí", "Gấp"). */
@Composable
private fun ImageBadge(text: String, background: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.chip))
            .background(background)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

/**
 * Thẻ sản phẩm dạng hàng ngang (tin của tôi, đơn hàng...): ảnh vuông bên trái, thông tin bên phải.
 * [trailing] là vùng tùy chọn ở cuối hàng (ví dụ nút menu).
 */
@Composable
fun ProductRowCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showStatus: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.card))
            .background(MaterialTheme.colorScheme.surface)
            .hairlineBorder(MaterialTheme.colorScheme.outlineVariant, Radius.card)
            .clickable(onClick = onClick)
            .padding(Space.sm),
        horizontalArrangement = Arrangement.spacedBy(Space.sm),
    ) {
        RemoteImage(
            url = product.coverImage,
            contentDescription = product.title,
            modifier = Modifier.size(Sizes.thumbLarge + 8.dp).clip(RoundedCornerShape(Radius.input)),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = product.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            PriceText(price = product.price, isFree = product.isFree)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                if (showStatus) StatusChip(text = product.status.label, tone = product.status.tone())
                Text(
                    text = formatRelativeTime(product.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
        if (trailing != null) trailing()
    }
}

/** Khung xương nhấp nháy khi đang tải lưới sản phẩm (cùng kích thước với [ProductGridCard]). */
@Composable
fun ProductCardPlaceholder(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "placeholder")
    val alpha by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 800), RepeatMode.Reverse),
        label = "placeholderAlpha",
    )
    val block = MaterialTheme.colorScheme.surfaceVariant
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.card))
            .background(MaterialTheme.colorScheme.surface)
            .hairlineBorder(MaterialTheme.colorScheme.outlineVariant, Radius.card)
            .alpha(alpha),
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(4f / 3f).background(block))
        Column(modifier = Modifier.padding(Space.sm), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.fillMaxWidth(0.5f).height(16.dp).clip(RoundedCornerShape(4.dp)).background(block))
            Box(Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(4.dp)).background(block))
            Box(Modifier.fillMaxWidth(0.7f).height(14.dp).clip(RoundedCornerShape(4.dp)).background(block))
            Box(Modifier.fillMaxWidth(0.4f).height(10.dp).clip(RoundedCornerShape(4.dp)).background(block))
        }
    }
}
