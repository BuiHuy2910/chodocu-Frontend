package com.chodocu.app.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Mở ứng dụng gọi điện với số đã điền sẵn — người dùng tự bấm gọi nên app không cần quyền CALL_PHONE.
 * Trả về false nếu máy không có ứng dụng gọi điện (ví dụ máy tính bảng).
 */
fun Context.openDialer(phone: String): Boolean = try {
    startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone.trim()}")))
    true
} catch (e: ActivityNotFoundException) {
    false
}
