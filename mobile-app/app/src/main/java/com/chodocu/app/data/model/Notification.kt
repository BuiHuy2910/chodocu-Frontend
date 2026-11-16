package com.chodocu.app.data.model

import java.time.Instant

/**
 * Thông báo hệ thống gửi cho người dùng.
 * `type` ví dụ: PRODUCT_APPROVED, PRODUCT_REJECTED, ORDER_RECEIVED, ORDER_ACCEPTED...
 * `referenceId` là id sản phẩm (PRODUCT_*) hoặc id đơn (ORDER_*).
 */
data class AppNotification(
    val id: Long,
    val type: String,
    val title: String,
    val message: String,
    val referenceId: Long?,
    val isRead: Boolean,
    val createdAt: Instant?,
) {
    val isProductRelated: Boolean get() = type.startsWith("PRODUCT_")
    val isOrderRelated: Boolean get() = type.startsWith("ORDER_")

    /** Thông báo về đơn mà mình là người bán (người mua gửi yêu cầu, khiếu nại...). */
    val isForSeller: Boolean get() = type == "ORDER_RECEIVED"
}
