package com.chodocu.app.data.supabase

import com.chodocu.app.core.parseMeetupTime
import com.chodocu.app.core.parseServerInstant
import com.chodocu.app.data.common.array
import com.chodocu.app.data.common.boolean
import com.chodocu.app.data.common.doubleOrNull
import com.chodocu.app.data.common.int
import com.chodocu.app.data.common.long
import com.chodocu.app.data.common.longOrNull
import com.chodocu.app.data.common.string
import com.chodocu.app.data.common.stringOrNull
import com.chodocu.app.data.common.strings
import com.chodocu.app.data.model.AppNotification
import com.chodocu.app.data.model.Category
import com.chodocu.app.data.model.ChatMessage
import com.chodocu.app.data.model.Conversation
import com.chodocu.app.data.model.FeePreview
import com.chodocu.app.data.model.MessageType
import com.chodocu.app.data.model.Order
import com.chodocu.app.data.model.OrderStatus
import com.chodocu.app.data.model.Product
import com.chodocu.app.data.model.ProductCondition
import com.chodocu.app.data.model.ProductStatus
import com.chodocu.app.data.model.University
import com.chodocu.app.data.model.User
import com.chodocu.app.data.model.UserRole
import com.chodocu.app.data.model.UserSummary
import com.google.gson.JsonObject

/*
 * Đổi JSON (khóa snake_case do các hàm api_* trả về) thành model của app.
 * Tên khóa khớp với phần "DỰNG JSON TRẢ VỀ CHO APP" trong supabase_direct_setup.sql.
 */

internal fun JsonObject.toUser() = User(
    id = long("id"),
    email = string("email"),
    fullName = string("full_name"),
    phone = stringOrNull("phone"),
    avatarUrl = stringOrNull("avatar_url"),
    bio = stringOrNull("bio"),
    dormArea = stringOrNull("dorm_area"),
    role = UserRole.fromApi(stringOrNull("role")),
    universityId = longOrNull("university_id"),
    universityName = stringOrNull("university_name"),
    universityShortName = stringOrNull("university_short_name"),
    rating = doubleOrNull("rating"),
    completedDeals = int("completed_deals"),
)

internal fun JsonObject.toCategory() = Category(
    id = long("id"),
    name = string("name"),
    icon = stringOrNull("icon"),
)

internal fun JsonObject.toUniversity() = University(
    id = long("id"),
    name = string("name"),
    shortName = string("short_name"),
    meetupSpots = University.parseSpots(stringOrNull("safe_meetup_spots")),
)

internal fun JsonObject.toProduct() = Product(
    id = long("id"),
    title = string("title"),
    description = string("description"),
    price = long("price"),
    originalPrice = longOrNull("original_price"),
    isFree = boolean("is_free"),
    isUrgent = boolean("is_urgent"),
    condition = ProductCondition.fromApi(stringOrNull("item_condition")),
    conditionNote = stringOrNull("condition_note"),
    passReason = stringOrNull("pass_reason"),
    pickupSpot = stringOrNull("pickup_spot"),
    status = ProductStatus.fromApi(stringOrNull("status")),
    rejectReason = stringOrNull("reject_reason"),
    viewCount = int("view_count"),
    favoriteCount = int("favorite_count"),
    isFavorited = boolean("is_favorited"),
    categoryId = longOrNull("category_id"),
    categoryName = stringOrNull("category_name"),
    universityId = longOrNull("university_id"),
    universityName = stringOrNull("university_name"),
    universityShortName = stringOrNull("university_short_name"),
    seller = UserSummary(
        id = long("seller_id"),
        fullName = string("seller_name"),
        avatarUrl = stringOrNull("seller_avatar_url"),
        universityShortName = stringOrNull("seller_university_short_name"),
        rating = doubleOrNull("seller_rating"),
        completedDeals = int("seller_completed_deals"),
    ),
    imageUrls = array("image_urls").strings(),
    createdAt = parseServerInstant(stringOrNull("created_at")),
)

internal fun JsonObject.toOrder(): Order {
    val meetupRaw = stringOrNull("meetup_time")
    return Order(
        id = long("id"),
        code = stringOrNull("order_code"),
        status = OrderStatus.fromApi(stringOrNull("status")),
        offeredPrice = long("offered_price"),
        meetupLocation = stringOrNull("meetup_location"),
        meetupTime = parseMeetupTime(meetupRaw),
        meetupTimeRaw = meetupRaw,
        buyerNote = stringOrNull("buyer_note"),
        sellerNote = stringOrNull("seller_note"),
        cancelReason = stringOrNull("cancel_reason"),
        disputeReason = stringOrNull("dispute_reason"),
        // Số điện thoại chỉ có khi đơn đã chốt hẹn
        buyer = UserSummary(
            id = long("buyer_id"),
            fullName = string("buyer_name"),
            avatarUrl = stringOrNull("buyer_avatar_url"),
            phone = stringOrNull("buyer_phone"),
        ),
        seller = UserSummary(
            id = long("seller_id"),
            fullName = string("seller_name"),
            avatarUrl = stringOrNull("seller_avatar_url"),
            phone = stringOrNull("seller_phone"),
        ),
        productId = long("product_id"),
        productTitle = string("product_title"),
        productImage = stringOrNull("product_image"),
        productPrice = long("product_price"),
        productStatus = stringOrNull("product_status")?.let(ProductStatus::fromApi),
        platformFeeAmount = longOrNull("platform_fee_amount"),
        finalSellerAmount = longOrNull("final_seller_amount"),
        finalBuyerAmount = longOrNull("final_buyer_amount"),
        createdAt = parseServerInstant(stringOrNull("created_at")),
        completedAt = parseServerInstant(stringOrNull("completed_at")),
    )
}

internal fun JsonObject.toFeePreview() = FeePreview(
    feeEnabled = boolean("fee_enabled"),
    feeRatePercent = stringOrNull("fee_rate_percent"),
    platformFee = long("platform_fee_amount"),
    sellerReceive = long("seller_receive_amount"),
    buyerPay = long("buyer_pay_amount"),
    feePayer = stringOrNull("fee_payer"),
)

internal fun JsonObject.toChatMessage() = ChatMessage(
    id = long("id"),
    sender = UserSummary(
        id = long("sender_id"),
        fullName = string("sender_name"),
        avatarUrl = stringOrNull("sender_avatar_url"),
    ),
    receiver = UserSummary(
        id = long("receiver_id"),
        fullName = string("receiver_name"),
        avatarUrl = stringOrNull("receiver_avatar_url"),
    ),
    type = MessageType.fromApi(stringOrNull("message_type")),
    content = string("content"),
    imageUrl = stringOrNull("image_url"),
    offeredPrice = longOrNull("offered_price"),
    meetupLocation = stringOrNull("meetup_location"),
    meetupTime = parseMeetupTime(stringOrNull("meetup_time")),
    productId = longOrNull("product_id"),
    productTitle = stringOrNull("product_title"),
    productImage = stringOrNull("product_image"),
    productPrice = longOrNull("product_price"),
    isRead = boolean("is_read"),
    sentAt = parseServerInstant(stringOrNull("created_at")),
)

internal fun JsonObject.toConversation() = Conversation(
    partner = UserSummary(
        id = long("partner_id"),
        fullName = string("partner_name"),
        avatarUrl = stringOrNull("partner_avatar_url"),
    ),
    productId = longOrNull("product_id"),
    productTitle = stringOrNull("product_title"),
    productImage = stringOrNull("product_image"),
    productPrice = longOrNull("product_price"),
    lastMessage = string("last_message"),
    lastMessageType = MessageType.fromApi(stringOrNull("last_message_type")),
    lastMessageMine = boolean("last_message_mine"),
    lastMessageAt = parseServerInstant(stringOrNull("last_message_at")),
    unreadCount = int("unread_count"),
)

internal fun JsonObject.toNotification() = AppNotification(
    id = long("id"),
    type = string("type"),
    title = string("title"),
    message = string("message"),
    referenceId = longOrNull("reference_id"),
    isRead = boolean("is_read"),
    createdAt = parseServerInstant(stringOrNull("created_at")),
)
