package com.chodocu.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.chodocu.app.ui.theme.Radius
import com.chodocu.app.ui.theme.Sizes

/**
 * Chip lọc dùng chung (danh mục, trạng thái tin, trạng thái đơn...).
 * Đang chọn: nền xanh nhạt, chữ xanh đậm. Không chọn: nền trắng, viền mảnh.
 * [count] (nếu có) hiện ngay sau nhãn, ví dụ "Chờ duyệt (2)".
 */
@Composable
fun AppFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    count: Int? = null,
) {
    val colors = MaterialTheme.colorScheme
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = if (count != null) "$label ($count)" else label,
                style = MaterialTheme.typography.labelLarge,
            )
        },
        modifier = modifier,
        leadingIcon = leadingIcon?.let { icon ->
            { Icon(icon, contentDescription = null, modifier = Modifier.size(Sizes.iconSmall)) }
        },
        shape = RoundedCornerShape(Radius.chip),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = colors.surface,
            labelColor = colors.onSurface,
            iconColor = colors.onSurfaceVariant,
            selectedContainerColor = colors.primaryContainer,
            selectedLabelColor = colors.onPrimaryContainer,
            selectedLeadingIconColor = colors.onPrimaryContainer,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = colors.outlineVariant,
            selectedBorderColor = Color.Transparent,
        ),
    )
}

/** Chip mở bảng chọn (ví dụ "Trường: UIT", "Sắp xếp: Mới nhất"), có mũi tên xuống ở cuối. */
@Composable
fun DropdownChip(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    val colors = MaterialTheme.colorScheme
    FilterChip(
        selected = active,
        onClick = onClick,
        label = { Text(text = label, style = MaterialTheme.typography.labelLarge, maxLines = 1) },
        modifier = modifier,
        leadingIcon = leadingIcon?.let { icon ->
            { Icon(icon, contentDescription = null, modifier = Modifier.size(Sizes.iconSmall)) }
        },
        trailingIcon = {
            Icon(Icons.Outlined.ExpandMore, contentDescription = null, modifier = Modifier.size(Sizes.iconSmall))
        },
        shape = RoundedCornerShape(Radius.chip),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = colors.surface,
            labelColor = colors.onSurface,
            iconColor = colors.onSurfaceVariant,
            selectedContainerColor = colors.primaryContainer,
            selectedLabelColor = colors.onPrimaryContainer,
            selectedLeadingIconColor = colors.onPrimaryContainer,
            selectedTrailingIconColor = colors.onPrimaryContainer,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = active,
            borderColor = colors.outlineVariant,
            selectedBorderColor = Color.Transparent,
        ),
    )
}
