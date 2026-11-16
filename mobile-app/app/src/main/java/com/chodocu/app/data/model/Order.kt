package com.chodocu.app.data.model

import java.time.Instant
import java.time.LocalDateTime

/** Trạng thái đơn đặt mua / hẹn gặp. */
enum class OrderStatus(val label: String) {
    PENDING("Chờ người bán xác nhận"),
    ACCEPTED("Đã chốt hẹn"),
    COMPLETED("Đã hoàn tất"),
    DISPUTED("Đang khiếu nại"),
    REJECTED("Người bán đã từ chối"),
    CANCELLED("Đã hủy");

    /** Đơn đã kết thúc, không thao tác được nữa. */
    val isFinal: Boolean get() = this == COMPLETED || this == REJECTED || this == CANCELLED

    companion object {
        fun fromApi(value: String?): OrderStatus =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: PENDING
    }
}

/** Vai trò của mình trong đơn: người mua hay người bán. */
enum class OrderRole(val label: String) {
    BUYER("Đơn mua"),
    SELLER("Đơn bán");

    companion object {
        fun fromApi(value: String?): OrderRole =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: BUYER
    }
}

/** Một yêu cầu mua kèm lịch hẹn gặp giữa người mua và người bán. */
data class Order(
    val id: Long,
    val code: String?,
    val status: OrderStatus,
    val offeredPrice: Long,
    val meetupLocation: String?,
    /** Giờ hẹn đã đọc được; null nếu dữ liệu cũ không đúng định dạng (xem meetupTimeRaw). */
    val meetupTime: LocalDateTime?,
    val meetupTimeRaw: String?,
    val buyerNote: String?,
    val sellerNote: String?,
    val cancelReason: String?,
    val disputeReason: String?,
    val buyer: UserSummary,
    val seller: UserSummary,
    val productId: Long,
    val productTitle: String,
    val productImage: String?,
    val productPrice: Long,
    val productStatus: ProductStatus?,
    val platformFeeAmount: Long?,
    val finalSellerAmount: Long?,
    val finalBuyerAmount: Long?,
    val createdAt: Instant?,
    val completedAt: Instant?,
) {
    fun isBuyer(userId: Long): Boolean = buyer.id == userId

    /** Người còn lại trong giao dịch (đối tác). */
    fun partnerOf(userId: Long): UserSummary = if (isBuyer(userId)) seller else buyer
}

/** Dữ liệu gửi khi đặt mua. Điểm hẹn và giờ hẹn do người mua tự chọn. */
data class OrderInput(
    val productId: Long,
    val offeredPrice: Long,
    val meetupLocation: String,
    val meetupTime: LocalDateTime,
    val note: String?,
)

/** Xem trước phí sàn trước khi đặt mua. */
data class FeePreview(
    val feeEnabled: Boolean,
    val feeRatePercent: String?,
    val platformFee: Long,
    val sellerReceive: Long,
    val buyerPay: Long,
    val feePayer: String?,
)
