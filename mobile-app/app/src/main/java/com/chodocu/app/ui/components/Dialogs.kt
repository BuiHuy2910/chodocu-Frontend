package com.chodocu.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.chodocu.app.ui.theme.Space

/**
 * Hộp thoại xác nhận trước thao tác quan trọng (xóa tin, đăng xuất, hủy giao dịch...).
 * [destructive] = true thì nút xác nhận màu đỏ.
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissText: String = "Hủy",
    destructive: Boolean = false,
    icon: ImageVector? = null,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = icon?.let { { Icon(it, contentDescription = null) } },
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    fontWeight = FontWeight.SemiBold,
                    color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismissText) } },
        containerColor = MaterialTheme.colorScheme.surface,
    )
}

/**
 * Hộp thoại nhập lý do (từ chối đơn, hủy hẹn, khiếu nại...).
 * [suggestions] là các lý do gợi ý, bấm vào sẽ điền nhanh vào ô nhập.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReasonDialog(
    title: String,
    message: String?,
    label: String,
    confirmText: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    required: Boolean = false,
    placeholder: String? = null,
    maxLength: Int = 500,
    destructive: Boolean = false,
    suggestions: List<String> = emptyList(),
) {
    var reason by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                if (message != null) Text(message, style = MaterialTheme.typography.bodyMedium)
                if (suggestions.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                        suggestions.forEach { suggestion ->
                            FilterChip(
                                selected = reason == suggestion,
                                onClick = {
                                    reason = suggestion
                                    error = null
                                },
                                label = { Text(suggestion, style = MaterialTheme.typography.labelMedium) },
                            )
                        }
                    }
                }
                AppTextField(
                    value = reason,
                    onValueChange = {
                        reason = it
                        error = null
                    },
                    label = label,
                    placeholder = placeholder,
                    required = required,
                    singleLine = false,
                    minLines = 2,
                    maxLines = 4,
                    maxLength = maxLength,
                    error = error,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val trimmed = reason.trim()
                if (required && trimmed.isEmpty()) {
                    error = "Vui lòng nhập lý do"
                } else {
                    onConfirm(trimmed)
                }
            }) {
                Text(
                    text = confirmText,
                    fontWeight = FontWeight.SemiBold,
                    color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Đóng") } },
        containerColor = MaterialTheme.colorScheme.surface,
    )
}
