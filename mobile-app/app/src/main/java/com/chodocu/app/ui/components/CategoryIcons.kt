package com.chodocu.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Desk
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Đổi tên icon lưu trong bảng categories (tên Material Symbols, ví dụ "menu_book")
 * sang icon Compose. Tên lạ thì dùng icon danh mục chung.
 */
fun categoryIcon(iconName: String?): ImageVector = when (iconName?.trim()?.lowercase()) {
    "menu_book", "book", "auto_stories" -> Icons.AutoMirrored.Outlined.MenuBook
    "mode_fan", "air", "electrical_services" -> Icons.Outlined.Air
    "desk", "chair", "table_restaurant" -> Icons.Outlined.Desk
    "laptop", "computer", "devices" -> Icons.Outlined.Laptop
    "checkroom", "apparel" -> Icons.Outlined.Checkroom
    "two_wheeler", "pedal_bike", "directions_bike" -> Icons.Outlined.TwoWheeler
    "fitness_center", "sports" -> Icons.Outlined.FitnessCenter
    "more_horiz" -> Icons.Outlined.MoreHoriz
    else -> Icons.Outlined.Category
}
