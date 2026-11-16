package com.chodocu.app.ui.product

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import com.chodocu.app.data.model.ProductStatus
import com.chodocu.app.data.model.ReportReason
import com.chodocu.app.ui.components.AppTextField
import com.chodocu.app.ui.components.AppTopBar
import com.chodocu.app.ui.theme.BrandOrange
import com.chodocu.app.ui.theme.Space

/** Thanh tiêu đề: nút lưu tin (người mua) và menu "thêm" (báo cáo / quản lý tin). */
@Composable
internal fun DetailTopBar(
    state: ProductDetailUiState,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onReport: () -> Unit,
    onEdit: () -> Unit,
    onChangeStatus: (ProductStatus) -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val product = state.product
    AppTopBar(
        title = "Chi tiết tin",
        onBack = onBack,
        actions = {
            if (product == null) return@AppTopBar
            if (!state.isOwner) {
                IconButton(onClick = onToggleFavorite, enabled = !state.isFavoriteBusy) {
                    Icon(
                        imageVector = if (product.isFavorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (product.isFavorited) "Bỏ lưu tin" else "Lưu tin",
                        tint = if (product.isFavorited) BrandOrange else LocalContentColor.current,
                    )
                }
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "Tùy chọn khác")
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    val close = { menuOpen = false }
                    if (!state.isOwner) {
                        MenuItem("Báo cáo tin đăng", Icons.Outlined.Flag) {
                            close()
                            onReport()
                        }
                        return@DropdownMenu
                    }
                    val editable = product.status != ProductStatus.IN_TRANSACTION && product.status != ProductStatus.SOLD
                    if (editable) {
                        MenuItem("Sửa tin", Icons.Outlined.Edit) {
                            close()
                            onEdit()
                        }
                    }
                    if (product.status == ProductStatus.APPROVED) {
                        MenuItem("Ẩn tin", Icons.Outlined.VisibilityOff) {
                            close()
                            onChangeStatus(ProductStatus.HIDDEN)
                        }
                    }
                    if (product.status == ProductStatus.HIDDEN) {
                        MenuItem("Hiện lại tin", Icons.Outlined.Visibility) {
                            close()
                            onChangeStatus(ProductStatus.APPROVED)
                        }
                    }
                    if (product.status == ProductStatus.APPROVED || product.status == ProductStatus.IN_TRANSACTION) {
                        MenuItem("Đánh dấu đã bán", Icons.Outlined.TaskAlt) {
                            close()
                            onChangeStatus(ProductStatus.SOLD)
                        }
                    }
                    MenuItem("Xóa tin", Icons.Outlined.DeleteOutline, destructive = true) {
                        close()
                        onDelete()
                    }
                }
            }
        },
    )
}

@Composable
private fun MenuItem(text: String, icon: ImageVector, destructive: Boolean = false, onClick: () -> Unit) {
    val color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    DropdownMenuItem(
        text = { Text(text, color = color) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = color) },
        onClick = onClick,
    )
}

/** Hộp thoại báo cáo tin đăng vi phạm: chọn lý do + mô tả thêm (không bắt buộc). */
@Composable
internal fun ReportDialog(onSubmit: (ReportReason, String) -> Unit, onDismiss: () -> Unit) {
    var reason by rememberSaveable { mutableStateOf<ReportReason?>(null) }
    var detail by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Báo cáo tin đăng", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "Tin này có vấn đề gì? Báo cáo được gửi ẩn danh tới quản trị viên.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                ReportReason.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = reason == option,
                                role = Role.RadioButton,
                                onClick = {
                                    reason = option
                                    error = null
                                },
                            )
                            .padding(vertical = Space.xxs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = reason == option, onClick = null)
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = Space.xs),
                        )
                    }
                }
                error?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
                AppTextField(
                    value = detail,
                    onValueChange = { detail = it },
                    label = "Mô tả thêm (không bắt buộc)",
                    singleLine = false,
                    minLines = 2,
                    maxLines = 4,
                    maxLength = 500,
                    modifier = Modifier.padding(top = Space.xs),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val selected = reason
                if (selected == null) error = "Vui lòng chọn lý do báo cáo" else onSubmit(selected, detail)
            }) { Text("Gửi báo cáo") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } },
        containerColor = MaterialTheme.colorScheme.surface,
    )
}
