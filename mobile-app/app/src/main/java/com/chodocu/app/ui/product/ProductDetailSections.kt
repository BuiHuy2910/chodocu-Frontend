package com.chodocu.app.ui.product

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.chodocu.app.core.formatCompactCount
import com.chodocu.app.core.formatRelativeTime
import com.chodocu.app.core.formatVnd
import com.chodocu.app.data.model.Product
import com.chodocu.app.data.model.ProductStatus
import com.chodocu.app.data.model.UserSummary
import com.chodocu.app.ui.components.InfoRow
import com.chodocu.app.ui.components.InlineBanner
import com.chodocu.app.ui.components.PriceText
import com.chodocu.app.ui.components.RemoteImage
import com.chodocu.app.ui.components.SectionCard
import com.chodocu.app.ui.components.StatusChip
import com.chodocu.app.ui.components.StatusTone
import com.chodocu.app.ui.components.UserAvatar
import com.chodocu.app.ui.components.tone
import com.chodocu.app.ui.theme.AppColors
import com.chodocu.app.ui.theme.Radius
import com.chodocu.app.ui.theme.Space

private val cardPadding = Modifier.padding(horizontal = Space.screen)

/** Ảnh sản phẩm: vuốt ngang để xem ảnh khác, chạm để phóng to. */
@Composable
internal fun ProductGallery(imageUrls: List<String>, title: String, onOpenImage: (Int) -> Unit) {
    val galleryModifier = Modifier
        .fillMaxWidth()
        .aspectRatio(4f / 3f)
        .background(MaterialTheme.colorScheme.surfaceVariant)
    if (imageUrls.isEmpty()) {
        RemoteImage(url = null, contentDescription = title, modifier = galleryModifier)
        return
    }
    val pagerState = rememberPagerState(pageCount = { imageUrls.size })
    Box(modifier = galleryModifier) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            RemoteImage(
                url = imageUrls[page],
                contentDescription = "$title - ảnh ${page + 1}",
                modifier = Modifier.fillMaxSize().clickable { onOpenImage(page) },
            )
        }
        if (imageUrls.size > 1) {
            Text(
                text = "${pagerState.currentPage + 1}/${imageUrls.size}",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Space.sm)
                    .background(AppColors.ImageScrim, RoundedCornerShape(Radius.pill))
                    .padding(horizontal = Space.xs, vertical = 2.dp),
            )
        }
    }
}

/** Giá, tên món đồ, các nhãn và dòng thông tin phụ. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ProductHeader(product: Product) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(Space.screen),
        verticalArrangement = Arrangement.spacedBy(Space.xs),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
            PriceText(price = product.price, isFree = product.isFree, style = MaterialTheme.typography.headlineSmall)
            val original = product.originalPrice
            if (!product.isFree && original != null && original > product.price) {
                Text(
                    text = formatVnd(original),
                    style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            product.discountPercent?.let { StatusChip(text = "-$it%", tone = StatusTone.WARNING) }
        }
        Text(text = product.title, style = MaterialTheme.typography.titleLarge)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Space.xs),
            verticalArrangement = Arrangement.spacedBy(Space.xs),
        ) {
            StatusChip(text = product.condition.label, tone = StatusTone.INFO)
            if (product.isFree) StatusChip(text = "Tặng miễn phí", tone = StatusTone.SUCCESS)
            if (product.isUrgent) StatusChip(text = "Cần bán gấp", tone = StatusTone.WARNING)
            if (product.status != ProductStatus.APPROVED) {
                StatusChip(text = product.status.label, tone = product.status.tone())
            }
        }
        val meta = listOfNotNull(
            product.universityShortName,
            formatRelativeTime(product.createdAt).takeIf { it.isNotEmpty() },
            "${formatCompactCount(product.viewCount)} lượt xem",
            "${formatCompactCount(product.favoriteCount)} lượt lưu",
        ).joinToString(" · ")
        Text(text = meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Dải thông báo về trạng thái tin (chờ duyệt, bị từ chối, đã bán...). Không cần thì không hiện. */
@Composable
internal fun ProductStatusNotice(product: Product, isOwner: Boolean) {
    val notice = when {
        isOwner -> when (product.status) {
            ProductStatus.PENDING -> Triple(
                "Tin đang chờ duyệt",
                "Quản trị viên sẽ kiểm tra tin trước khi hiển thị trên trang chủ.",
                StatusTone.INFO,
            )
            ProductStatus.REJECTED -> Triple(
                "Tin bị từ chối",
                (product.rejectReason?.let { "Lý do: $it. " } ?: "") + "Hãy sửa lại tin để gửi duyệt lần nữa.",
                StatusTone.DANGER,
            )
            ProductStatus.HIDDEN -> Triple(
                "Tin đang ẩn",
                "Người khác không thấy tin này. Chọn \"Hiện lại tin\" trong menu để bán tiếp.",
                StatusTone.WARNING,
            )
            ProductStatus.IN_TRANSACTION -> Triple(
                "Đang giao dịch",
                "Bạn đã chốt hẹn với một người mua. Theo dõi trong Giao dịch > Đơn bán.",
                StatusTone.INFO,
            )
            ProductStatus.SOLD -> Triple("Đã bán", "Món đồ này đã được bán thành công.", StatusTone.SUCCESS)
            ProductStatus.APPROVED -> null
        }
        product.status == ProductStatus.IN_TRANSACTION -> Triple(
            "Đang giao dịch",
            "Người bán đã chốt hẹn với người khác. Bạn vẫn có thể nhắn tin hỏi thêm.",
            StatusTone.WARNING,
        )
        product.status == ProductStatus.SOLD -> Triple("Đã bán", "Món đồ này đã có chủ mới.", StatusTone.NEUTRAL)
        else -> null
    } ?: return

    InlineBanner(title = notice.first, text = notice.second, tone = notice.third, modifier = cardPadding)
}

/** Thông tin chính: tình trạng, danh mục, trường, điểm giao dịch. */
@Composable
internal fun ProductInfoCard(product: Product) {
    SectionCard(title = "Thông tin", modifier = cardPadding) {
        InfoRow(icon = Icons.Outlined.Inventory2, label = "Tình trạng", value = product.condition.label)
        product.categoryName?.let { InfoRow(icon = Icons.Outlined.Category, label = "Danh mục", value = it) }
        product.universityName?.let { InfoRow(icon = Icons.Outlined.School, label = "Trường", value = it) }
        product.pickupSpot?.let { InfoRow(icon = Icons.Outlined.Place, label = "Điểm giao dịch", value = it) }
        InfoRow(icon = Icons.Outlined.Schedule, label = "Đăng lúc", value = formatRelativeTime(product.createdAt))
    }
}

/** Mô tả của người bán, ghi chú tình trạng và lý do pass đồ. */
@Composable
internal fun ProductDescriptionCard(product: Product) {
    SectionCard(title = "Mô tả", modifier = cardPadding) {
        Text(
            text = product.description.ifBlank { "Người bán chưa thêm mô tả." },
            style = MaterialTheme.typography.bodyMedium,
        )
        product.conditionNote?.takeIf { it.isNotBlank() }?.let { LabeledText(label = "Ghi chú tình trạng", text = it) }
        product.passReason?.takeIf { it.isNotBlank() }?.let { LabeledText(label = "Lý do pass đồ", text = it) }
    }
}

@Composable
private fun LabeledText(label: String, text: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Thẻ người bán: ảnh đại diện (chữ cái đầu nếu chưa có ảnh), trường, số giao dịch thành công. */
@Composable
internal fun SellerCard(product: Product, isOwner: Boolean, onChat: () -> Unit) {
    val seller = product.seller
    SectionCard(title = if (isOwner) "Người đăng (bạn)" else "Người bán", modifier = cardPadding) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            UserAvatar(name = seller.fullName, imageUrl = seller.avatarUrl, size = 48.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(seller.fullName, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = sellerStats(seller),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (!isOwner) {
                TextButton(onClick = onChat) {
                    Icon(Icons.AutoMirrored.Outlined.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Nhắn tin", modifier = Modifier.padding(start = Space.xxs))
                }
            }
        }
    }
}

private fun sellerStats(seller: UserSummary): String = listOfNotNull(
    seller.universityShortName,
    "${seller.completedDeals} giao dịch thành công",
    seller.rating?.takeIf { it > 0 }?.let { "%.1f sao".format(it) },
).joinToString(" · ")

/** Lời khuyên giao dịch an toàn cho người mua. */
@Composable
internal fun SafetyTipsCard() {
    SectionCard(title = "Giao dịch an toàn", modifier = cardPadding) {
        listOf(
            "Hẹn gặp tại điểm giao dịch công khai trong trường, vào ban ngày.",
            "Kiểm tra kỹ món đồ trước khi trả tiền.",
            "Không chuyển khoản đặt cọc trước cho người chưa gặp mặt.",
        ).forEach { tip ->
            Row(horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Text(tip, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            }
        }
    }
}
