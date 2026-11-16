package com.chodocu.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chodocu.app.ui.theme.Radius
import com.chodocu.app.ui.theme.Sizes
import com.chodocu.app.ui.theme.Space

/** Trạng thái đang tải toàn màn hình. */
@Composable
fun LoadingView(modifier: Modifier = Modifier, text: String? = null) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 3.dp)
            if (text != null) {
                Spacer(Modifier.height(Space.sm))
                Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** Trạng thái lỗi toàn màn hình kèm nút "Thử lại". */
@Composable
fun ErrorView(message: String, onRetry: (() -> Unit)?, modifier: Modifier = Modifier) {
    EmptyView(
        icon = Icons.Outlined.ErrorOutline,
        title = "Không tải được dữ liệu",
        message = message,
        actionLabel = if (onRetry != null) "Thử lại" else null,
        actionIcon = Icons.Outlined.Refresh,
        onAction = onRetry,
        modifier = modifier,
    )
}

/** Trạng thái trống (chưa có dữ liệu) với biểu tượng, lời nhắn và nút hành động tùy chọn. */
@Composable
fun EmptyView(
    icon: ImageVector,
    title: String,
    message: String?,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    actionIcon: ImageVector? = null,
    onAction: (() -> Unit)? = null,
) {
    Box(modifier = modifier.fillMaxSize().padding(Space.xl), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.height(Space.md))
            Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            if (message != null) {
                Spacer(Modifier.height(Space.xxs))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(Space.lg))
                SecondaryButton(text = actionLabel, onClick = onAction, icon = actionIcon)
            }
        }
    }
}

/** Dải thông báo nhỏ trong nội dung (lỗi đăng nhập, ghi chú duyệt tin...). */
@Composable
fun InlineBanner(
    text: String,
    tone: StatusTone,
    modifier: Modifier = Modifier,
    icon: ImageVector = if (tone == StatusTone.DANGER) Icons.Outlined.ErrorOutline else Icons.Outlined.Info,
    title: String? = null,
) {
    val colors = tone.toneColors()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.input))
            .background(colors.container)
            .padding(Space.sm),
        horizontalArrangement = Arrangement.spacedBy(Space.xs),
    ) {
        Icon(icon, contentDescription = null, tint = colors.content, modifier = Modifier.size(Sizes.icon))
        Column(modifier = Modifier.weight(1f)) {
            if (title != null) {
                Text(title, style = MaterialTheme.typography.labelLarge, color = colors.content)
            }
            Text(text, style = MaterialTheme.typography.bodySmall, color = colors.content)
        }
    }
}
