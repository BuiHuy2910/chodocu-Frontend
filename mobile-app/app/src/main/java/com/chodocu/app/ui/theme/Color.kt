package com.chodocu.app.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * Bảng màu lấy từ logo "Chợ Đồ Cũ": xanh lá đậm + cam + xanh lá non.
 * Nguyên tắc: nền trung tính, thẻ trắng viền mảnh, màu thương hiệu chỉ dùng cho điểm nhấn.
 */

// Màu thương hiệu (lấy mẫu trực tiếp từ ảnh logo)
val BrandGreen = Color(0xFF0D6531)
val BrandGreenDark = Color(0xFF0A4F26)
val BrandOrange = Color(0xFFFE6B1D)
val BrandLeaf = Color(0xFF80B840)
val BrandInk = Color(0xFF0C3622)

// Nền và chữ
val Background = Color(0xFFF6F7F8)
val SurfaceWhite = Color(0xFFFFFFFF)
val SurfaceMuted = Color(0xFFF1F3F5)
val TextPrimary = Color(0xFF101828)
val TextSecondary = Color(0xFF475467)
val TextTertiary = Color(0xFF98A2B3)
val Outline = Color(0xFFD0D5DD)
val Divider = Color(0xFFE4E7EC)

// Vùng chứa (container) nhạt của màu thương hiệu
val GreenContainer = Color(0xFFDDEFE2)
val OnGreenContainer = Color(0xFF0A3D1F)
val OrangeContainer = Color(0xFFFFE7D9)
val OnOrangeContainer = Color(0xFF8A3200)
val LeafContainer = Color(0xFFEAF5DD)
val OnLeafContainer = Color(0xFF2F5212)

// Màu ngữ nghĩa (trạng thái)
val SuccessText = Color(0xFF067647)
val SuccessBg = Color(0xFFECFDF3)
val WarningText = Color(0xFFB4530A)
val WarningBg = Color(0xFFFEF6E7)
val DangerText = Color(0xFFB42318)
val DangerBg = Color(0xFFFEF3F2)
val InfoText = Color(0xFF0D5C8C)
val InfoBg = Color(0xFFEFF8FF)
val NeutralText = Color(0xFF475467)
val NeutralBg = Color(0xFFF2F4F7)

/** Màu dùng trực tiếp ở giao diện ngoài bảng màu Material. */
object AppColors {
    /** Màu chữ giá tiền: cam đậm, đủ tương phản trên nền trắng (4.7:1). */
    val Price = Color(0xFFC2500A)

    /** Màu mực tối của logo (viền, chữ thương hiệu trên nền sáng). */
    val Ink = BrandInk

    /** Lớp phủ tối trên ảnh (nhãn trên ảnh, trình xem ảnh). */
    val ImageScrim = Color(0x99000000)
}
