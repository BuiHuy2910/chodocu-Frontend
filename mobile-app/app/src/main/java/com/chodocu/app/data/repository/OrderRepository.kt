package com.chodocu.app.data.repository

import com.chodocu.app.data.model.FeePreview
import com.chodocu.app.data.model.Order
import com.chodocu.app.data.model.OrderInput
import com.chodocu.app.data.model.OrderRole

/** Đơn đặt mua kèm lịch hẹn gặp. */
interface OrderRepository {
    suspend fun placeOrder(input: OrderInput): Order

    /** BUYER = đơn tôi mua, SELLER = đơn người khác mua đồ của tôi. Mới nhất trước. */
    suspend fun getOrders(role: OrderRole): List<Order>

    /** Người bán chấp nhận (PENDING -> ACCEPTED). */
    suspend fun acceptOrder(orderId: Long): Order

    /** Người bán từ chối / hủy hẹn. */
    suspend fun rejectOrder(orderId: Long, reason: String?): Order

    /** Người mua hủy yêu cầu / hủy hẹn. */
    suspend fun cancelOrder(orderId: Long, reason: String?): Order

    /** Một trong hai bên xác nhận đã giao dịch xong (ACCEPTED -> COMPLETED). */
    suspend fun completeOrder(orderId: Long): Order

    /** Báo có vấn đề để admin xử lý (chỉ khi đơn đang ACCEPTED). */
    suspend fun disputeOrder(orderId: Long, reason: String): Order

    suspend fun previewFee(amount: Long): FeePreview
}
