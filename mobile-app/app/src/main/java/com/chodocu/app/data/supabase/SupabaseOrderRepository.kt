package com.chodocu.app.data.supabase

import com.chodocu.app.core.toMeetupString
import com.chodocu.app.data.model.FeePreview
import com.chodocu.app.data.model.Order
import com.chodocu.app.data.model.OrderInput
import com.chodocu.app.data.model.OrderRole
import com.chodocu.app.data.repository.OrderRepository

/** Đơn đặt mua qua các hàm api_place_order, api_my_orders, api_update_order, api_fee_preview. */
class SupabaseOrderRepository(private val client: SupabaseClient) : OrderRepository {

    override suspend fun placeOrder(input: OrderInput): Order =
        client.callObject(
            "api_place_order",
            "p_product_id" to input.productId,
            "p_offered_price" to input.offeredPrice,
            "p_meetup_location" to input.meetupLocation,
            "p_meetup_time" to input.meetupTime.toMeetupString(),
            "p_buyer_note" to input.note?.trim()?.ifEmpty { null },
        ).toOrder()

    override suspend fun getOrders(role: OrderRole): List<Order> =
        client.callList("api_my_orders", "p_role" to role.name).map { it.toOrder() }

    override suspend fun acceptOrder(orderId: Long): Order = updateOrder(orderId, "ACCEPT")

    override suspend fun rejectOrder(orderId: Long, reason: String?): Order = updateOrder(orderId, "REJECT", reason)

    override suspend fun cancelOrder(orderId: Long, reason: String?): Order = updateOrder(orderId, "CANCEL", reason)

    override suspend fun completeOrder(orderId: Long): Order = updateOrder(orderId, "COMPLETE")

    override suspend fun disputeOrder(orderId: Long, reason: String): Order = updateOrder(orderId, "DISPUTE", reason)

    override suspend fun previewFee(amount: Long): FeePreview =
        client.callObject("api_fee_preview", "p_amount" to amount, authenticated = false).toFeePreview()

    /** Một hàm SQL xử lý mọi thao tác trên đơn: ACCEPT / REJECT / CANCEL / COMPLETE / DISPUTE. */
    private suspend fun updateOrder(orderId: Long, action: String, reason: String? = null): Order =
        client.callObject(
            "api_update_order",
            "p_order_id" to orderId,
            "p_action" to action,
            "p_reason" to reason?.trim()?.ifEmpty { null },
        ).toOrder()
}
