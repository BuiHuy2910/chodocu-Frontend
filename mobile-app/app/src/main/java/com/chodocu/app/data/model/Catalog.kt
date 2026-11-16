package com.chodocu.app.data.model

/** Danh mục sản phẩm. `icon` là tên icon Material (vd "menu_book"), xem categoryIcon(). */
data class Category(
    val id: Long,
    val name: String,
    val icon: String? = null,
)

/**
 * Trường đại học / khu vực giao dịch.
 * `meetupSpots` là danh sách điểm hẹn an toàn chuẩn của trường — người dùng phải tự chọn trong danh sách này.
 */
data class University(
    val id: Long,
    val name: String,
    val shortName: String,
    val meetupSpots: List<String>,
) {
    companion object {
        /** Tách chuỗi "Sảnh A, Thư viện B" thành danh sách điểm hẹn (giống backend). */
        fun parseSpots(raw: String?): List<String> =
            raw.orEmpty().split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
}

/** Thư mục lưu ảnh trên máy chủ. */
enum class UploadFolder(val path: String) {
    PRODUCTS("products"),
    AVATARS("avatars"),
    CHAT("chat"),
}
