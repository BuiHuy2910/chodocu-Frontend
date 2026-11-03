package com.chodocu.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.chodocu.app.data.model.ProductDto
import com.chodocu.app.ui.theme.*

/**
 * Bộ component dùng chung.
 * Nguyên tắc: phẳng (flat), viền 1px, bo góc nhỏ, không gradient, không emoji,
 * màu nhấn chỉ dùng cho giá và trạng thái cần chú ý.
 */

@Composable
fun PassUniAvatar(
    name: String?,
    avatarUrl: String?,
    size: Int = 40,
    modifier: Modifier = Modifier
) {
    val initial = name?.trim()?.split(" ")?.lastOrNull()?.take(1)?.uppercase() ?: "?"
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(EmeraldLight)
            .border(1.dp, DividerLine, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = initial,
                color = EmeraldPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = (size * 0.38).sp
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = SlateNavy
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        if (actionLabel != null && onAction != null) {
            TextButton(
                onClick = onAction,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(actionLabel, style = MaterialTheme.typography.labelMedium, color = EmeraldPrimary)
            }
        }
    }
}

private data class StatusStyle(val label: String, val fg: Color, val bg: Color)

private fun statusStyle(status: String): StatusStyle? = when (status) {
    "PENDING" -> StatusStyle("Chờ duyệt", AmberGold, AmberLight)
    "IN_TRANSACTION" -> StatusStyle("Đang giao dịch", CampusBlue, CampusBlueLight)
    "SOLD" -> StatusStyle("Đã bán", SlateMuted, SurfaceSunken)
    "REJECTED" -> StatusStyle("Bị từ chối", DangerRed, DangerLight)
    "HIDDEN" -> StatusStyle("Đã ẩn", SlateMuted, SurfaceSunken)
    else -> null // APPROVED: không hiện nhãn, tránh nhiễu thị giác
}

@Composable
fun ProductStatusChip(status: String, modifier: Modifier = Modifier) {
    val style = statusStyle(status) ?: return
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.chip))
            .background(style.bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = style.label,
            style = MaterialTheme.typography.labelSmall,
            color = style.fg
        )
    }
}

/**
 * Thẻ sản phẩm trong feed.
 * Bố cục: ảnh 4:3 → giá → tiêu đề → meta (trường · thời gian · tình trạng).
 */
@Composable
fun ProductCard(
    product: ProductDto,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.card),
        color = CardWhite,
        border = BorderStroke(1.dp, DividerLine),
        onClick = onClick
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .background(SurfaceSunken)
            ) {
                AsyncImage(
                    model = product.imageUrls.firstOrNull(),
                    contentDescription = product.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (product.status != "APPROVED") {
                    ProductStatusChip(
                        status = product.status,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.92f))
                        .clickable(onClick = onToggleFavorite),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (product.isFavorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (product.isFavorited) "Bỏ lưu" else "Lưu tin",
                        tint = if (product.isFavorited) DangerRed else SlateMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(Space.sm)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (product.isFree) "Tặng miễn phí" else formatPrice(product.price),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (product.isFree) SuccessGreen else EmeraldPrimary
                    )
                    val original = product.originalPrice
                    if (original != null && original > product.price) {
                        Text(
                            text = formatPrice(original),
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateFaint,
                            textDecoration = TextDecoration.LineThrough,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }

                Text(
                    text = product.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = SlateNavy,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    modifier = Modifier.padding(top = Space.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = SlateFaint,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = product.universityShortName,
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 3.dp)
                    )
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateFaint,
                        modifier = Modifier.padding(horizontal = 5.dp)
                    )
                    Text(
                        text = product.timeAgo,
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateFaint
                    )
                }

                val condition = product.conditionLabel
                if (!condition.isNullOrBlank()) {
                    Text(
                        text = condition,
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateMuted,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
    }
}

/**
 * Dải thông tin 1 dòng (thay cho banner gradient nhiều màu).
 */
@Composable
fun InfoStrip(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.card))
            .background(EmeraldLight)
            .padding(Space.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
        Column(modifier = Modifier.padding(start = Space.sm)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = EmeraldPressed)
            Text(description, style = MaterialTheme.typography.bodySmall, color = SlateMuted)
        }
    }
}

@Composable
fun PassUniEmptyState(
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    icon: ImageVector = Icons.Outlined.SearchOff,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.card))
            .background(CardWhite)
            .border(1.dp, DividerLine, RoundedCornerShape(Radius.card))
            .padding(horizontal = Space.lg, vertical = Space.xxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = SlateFaint, modifier = Modifier.size(32.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = SlateNavy,
            modifier = Modifier.padding(top = Space.sm)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = SlateMuted,
            modifier = Modifier.padding(top = 4.dp)
        )
        if (actionLabel != null && onAction != null) {
            OutlinedButton(
                onClick = onAction,
                shape = RoundedCornerShape(Radius.chip),
                border = BorderStroke(1.dp, DividerLine),
                modifier = Modifier.padding(top = Space.md)
            ) {
                Text(actionLabel, style = MaterialTheme.typography.labelMedium, color = EmeraldPrimary)
            }
        }
    }
}

/**
 * Nút tài khoản dùng thử (màn đăng nhập) — gọn, không emoji.
 */
@Composable
fun DemoAccountButton(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.chip))
            .background(SurfaceBg)
            .border(1.dp, DividerLine, RoundedCornerShape(Radius.chip))
            .clickable(onClick = onClick)
            .padding(horizontal = Space.xs, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = SlateNavy,
            maxLines = 1
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = SlateMuted,
            maxLines = 1
        )
    }
}

/** Nút lọc dạng pill, viền mảnh — thay FilterChip mặc định cho gọn thị giác. */@Composable
fun FilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) EmeraldPrimary else CardWhite
    val fg = if (selected) Color.White else SlateMuted
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.pill))
            .background(bg)
            .border(1.dp, if (selected) EmeraldPrimary else DividerLine, RoundedCornerShape(Radius.pill))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = fg)
    }
}

/** Định dạng giá dùng chung — không phụ thuộc file màn hình. */
fun formatPrice(amount: java.math.BigDecimal?): String {
    if (amount == null) return "0đ"
    val formatter = java.text.NumberFormat.getInstance(java.util.Locale("vi", "VN"))
    return "${formatter.format(amount)}đ"
}
