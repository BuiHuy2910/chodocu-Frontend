package com.chodocu.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.chodocu.app.core.initialsOf
import com.chodocu.app.ui.theme.Sizes

/** Bảng màu nền cho avatar chữ cái (tông trầm, hợp màu thương hiệu). */
private val avatarPalette = listOf(
    Color(0xFF0D6531),
    Color(0xFFC2500A),
    Color(0xFF0D5C8C),
    Color(0xFF6941C6),
    Color(0xFF3E7A1F),
    Color(0xFF9E3A6B),
    Color(0xFF475467),
)

/** Chọn màu ổn định theo tên: cùng một người luôn cùng một màu. */
private fun avatarColor(name: String?): Color {
    val key = name.orEmpty().trim().lowercase()
    if (key.isEmpty()) return avatarPalette.last()
    return avatarPalette[Math.floorMod(key.hashCode(), avatarPalette.size)]
}

/**
 * Ảnh đại diện tròn. Không có ảnh (hoặc ảnh lỗi) thì hiện chữ cái đầu của tên —
 * app không dùng ảnh đại diện tạo tự động.
 */
@Composable
fun UserAvatar(
    name: String?,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = Sizes.avatar,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(avatarColor(name)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initialsOf(name),
            color = Color.White,
            style = initialsStyle(size),
            maxLines = 1,
        )
        if (!imageUrl.isNullOrBlank()) {
            // Ảnh tải lỗi thì AsyncImage không vẽ gì, chữ cái phía dưới vẫn hiện.
            AsyncImage(
                model = imageUrl,
                contentDescription = "Ảnh đại diện của ${name.orEmpty()}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

@Composable
private fun initialsStyle(size: Dp): TextStyle = when {
    size >= 64.dp -> MaterialTheme.typography.headlineSmall
    size >= 44.dp -> MaterialTheme.typography.titleMedium
    size >= 32.dp -> MaterialTheme.typography.labelLarge
    else -> MaterialTheme.typography.labelSmall
}
