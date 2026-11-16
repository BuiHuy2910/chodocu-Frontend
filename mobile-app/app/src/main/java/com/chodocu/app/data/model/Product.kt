package com.chodocu.app.data.model

import java.time.Instant

/** Tình trạng món đồ (khớp enum ProductCondition của backend). */
enum class ProductCondition(val label: String, val description: String) {
    NEW_100("Mới 100%", "Chưa bóc hộp, chưa sử dụng"),
    LIKE_NEW_95("Như mới (95-99%)", "Dùng vài lần, giữ kỹ"),
    GOOD_85("Tốt (80-90%)", "Có xước nhẹ bề mặt"),
    USABLE("Còn dùng tốt", "Đã dùng nhiều nhưng vẫn hoạt động tốt");

    companion object {
        fun fromApi(value: String?): ProductCondition =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: LIKE_NEW_95
    }
}

/** Trạng thái tin đăng. */
enum class ProductStatus(val label: String) {
    PENDING("Chờ duyệt"),
    APPROVED("Đang bán"),
    IN_TRANSACTION("Đang giao dịch"),
    SOLD("Đã bán"),
    REJECTED("Bị từ chối"),
    HIDDEN("Đã ẩn");

    companion object {
        fun fromApi(value: String?): ProductStatus =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: PENDING
    }
}

/** Cách sắp xếp danh sách tin. */
enum class ProductSort(val label: String) {
    NEWEST("Mới nhất"),
    PRICE_ASC("Giá thấp đến cao"),
    PRICE_DESC("Giá cao đến thấp"),
    POPULAR("Xem nhiều nhất"),
}

/** Lý do báo cáo tin đăng (khớp cột reports.reason). */
enum class ReportReason(val label: String) {
    FAKE_PRODUCT("Hàng giả, không đúng mô tả"),
    SPAM("Spam hoặc đăng trùng lặp"),
    INAPPROPRIATE("Nội dung không phù hợp"),
    FRAUD("Có dấu hiệu lừa đảo"),
    OTHER("Lý do khác"),
}

/** Một tin đăng bán / tặng đồ. Giá tính bằng đồng (VND). */
data class Product(
    val id: Long,
    val title: String,
    val description: String,
    val price: Long,
    val originalPrice: Long?,
    val isFree: Boolean,
    val isUrgent: Boolean,
    val condition: ProductCondition,
    val conditionNote: String?,
    val passReason: String?,
    val pickupSpot: String?,
    val status: ProductStatus,
    val rejectReason: String?,
    val viewCount: Int,
    val favoriteCount: Int,
    val isFavorited: Boolean,
    val categoryId: Long?,
    val categoryName: String?,
    val universityId: Long?,
    val universityName: String?,
    val universityShortName: String?,
    val seller: UserSummary,
    val imageUrls: List<String>,
    val createdAt: Instant?,
) {
    /** Ảnh bìa = ảnh đầu tiên. */
    val coverImage: String? get() = imageUrls.firstOrNull()

    /** Phần trăm giảm so với giá gốc (null nếu không có giá gốc hợp lệ). */
    val discountPercent: Int?
        get() {
            val original = originalPrice ?: return null
            if (isFree || original <= price || original <= 0L) return null
            return (((original - price) * 100) / original).toInt().takeIf { it > 0 }
        }

    /** Còn có thể đặt mua không. */
    val isAvailable: Boolean get() = status == ProductStatus.APPROVED
}

/** Bộ lọc tìm kiếm trang chủ. */
data class ProductQuery(
    val keyword: String = "",
    val categoryId: Long? = null,
    val universityId: Long? = null,
    val sort: ProductSort = ProductSort.NEWEST,
)

/** Một trang kết quả, `hasMore` = còn trang sau. */
data class PageResult<T>(
    val items: List<T>,
    val hasMore: Boolean,
)

/** Dữ liệu form đăng / sửa tin. `imageUrls` là ảnh đã tải lên xong. */
data class ProductInput(
    val title: String,
    val description: String,
    val price: Long,
    val originalPrice: Long?,
    val isFree: Boolean,
    val isUrgent: Boolean,
    val condition: ProductCondition,
    val conditionNote: String?,
    val passReason: String?,
    val categoryId: Long,
    val universityId: Long,
    val pickupSpot: String,
    val imageUrls: List<String>,
)
