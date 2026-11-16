package com.chodocu.app.data.backend

import com.chodocu.app.core.parseMeetupTime
import com.chodocu.app.core.parseServerInstant
import com.chodocu.app.data.common.array
import com.chodocu.app.data.common.boolean
import com.chodocu.app.data.common.doubleOrNull
import com.chodocu.app.data.common.int
import com.chodocu.app.data.common.long
import com.chodocu.app.data.common.longOrNull
import com.chodocu.app.data.common.objectOrNull
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
 * Đổi JSON của backend Spring (khóa camelCase, xem các lớp *Response / *Dto bên Java) thành model.
 * Thời gian luôn đọc từ các trường dạng ISO (createdAtIso, sentAt...), không đọc chuỗi đã định dạng.
 */

internal fun JsonObject.toUser() = User(
    id = long("id"),
    email = string("email"),
    fullName = string("fullName"),
    phone = stringOrNull("phone"),
    avatarUrl = stringOrNull("avatarUrl"),
    bio = stringOrNull("bio"),
    dormArea = stringOrNull("dormArea"),
    role = UserRole.fromApi(stringOrNull("role")),
    universityId = longOrNull("universityId"),
    universityName = stringOrNull("universityName"),
    universityShortName = stringOrNull("universityShortName"),
    rating = doubleOrNull("rating"),
    completedDeals = int("completedDeals"),
)

/** Người dùng lồng bên trong tin đăng / đơn / tin nhắn (UserSummaryDto). */
private fun JsonObject?.toUserSummary(): UserSummary = UserSummary(
    id = this?.long("id") ?: 0L,
    fullName = this?.string("fullName").orEmpty(),
    avatarUrl = this?.stringOrNull("avatarUrl"),
    universityShortName = this?.stringOrNull("universityShortName"),
    phone = this?.stringOrNull("phone"),
    rating = this?.doubleOrNull("rating"),
    completedDeals = this?.int("completedDeals") ?: 0,
)

internal fun JsonObject.toCategory() = Category(
    id = long("id"),
    name = string("name"),
    icon = stringOrNull("icon"),
)

internal fun JsonObject.toUniversity() = University(
    id = long("id"),
    name = string("name"),
    shortName = string("shortName"),
    meetupSpots = University.parseSpots(stringOrNull("safeMeetupSpots")),
)

internal fun JsonObject.toProduct() = Product(
    id = long("id"),
    title = string("title"),
    description = string("description"),
    price = long("price"),
    originalPrice = longOrNull("originalPrice"),
    isFree = boolean("isFree"),
    isUrgent = boolean("isUrgent"),
    condition = ProductCondition.fromApi(stringOrNull("condition")),
    conditionNote = stringOrNull("conditionNote"),
    passReason = stringOrNull("passReason"),
    pickupSpot = stringOrNull("pickupSpot"),
    status = ProductStatus.fromApi(stringOrNull("status")),
    rejectReason = stringOrNull("rejectReason"),
    viewCount = int("viewCount"),
    favoriteCount = int("favoriteCount"),
    isFavorited = boolean("isFavorited"),
    categoryId = longOrNull("categoryId"),
    categoryName = stringOrNull("categoryName"),
    universityId = longOrNull("universityId"),
    universityName = stringOrNull("universityName"),
    universityShortName = stringOrNull("universityShortName"),
    seller = objectOrNull("seller").toUserSummary(),
    imageUrls = array("imageUrls").strings(),
    createdAt = parseServerInstant(stringOrNull("createdAtIso")),
)

internal fun JsonObject.toOrder(): Order {
    val product = objectOrNull("product")
    val meetupRaw = stringOrNull("meetupTime")
    return Order(
        id = long("id"),
        code = stringOrNull("orderCode"),
        status = OrderStatus.fromApi(stringOrNull("status")),
        offeredPrice = long("offeredPrice"),
        meetupLocation = stringOrNull("meetupLocation"),
        meetupTime = parseMeetupTime(meetupRaw),
        meetupTimeRaw = meetupRaw,
        buyerNote = stringOrNull("buyerNote"),
        sellerNote = stringOrNull("sellerNote"),
        cancelReason = stringOrNull("cancelReason"),
        disputeReason = stringOrNull("disputeReason"),
        buyer = objectOrNull("buyer").toUserSummary(),
        seller = objectOrNull("seller").toUserSummary(),
        productId = product?.long("id") ?: 0L,
        productTitle = product?.string("title").orEmpty(),
        productImage = product?.array("imageUrls")?.strings()?.firstOrNull(),
        productPrice = product?.long("price") ?: 0L,
        productStatus = product?.stringOrNull("status")?.let(ProductStatus::fromApi),
        platformFeeAmount = longOrNull("platformFeeAmount"),
        finalSellerAmount = longOrNull("finalSellerAmount"),
        finalBuyerAmount = longOrNull("finalBuyerAmount"),
        createdAt = parseServerInstant(stringOrNull("createdAtIso")),
        completedAt = parseServerInstant(stringOrNull("completedAtIso")),
    )
}

internal fun JsonObject.toFeePreview() = FeePreview(
    feeEnabled = boolean("feeEnabled"),
    feeRatePercent = stringOrNull("feeRatePercent"),
    platformFee = long("platformFeeAmount"),
    sellerReceive = long("sellerReceiveAmount"),
    buyerPay = long("buyerPayAmount"),
    feePayer = stringOrNull("feePayer"),
)

internal fun JsonObject.toChatMessage() = ChatMessage(
    id = long("id"),
    sender = objectOrNull("sender").toUserSummary(),
    receiver = objectOrNull("receiver").toUserSummary(),
    type = MessageType.fromApi(stringOrNull("messageType")),
    content = string("content"),
    imageUrl = stringOrNull("imageUrl"),
    offeredPrice = longOrNull("offeredPrice"),
    meetupLocation = stringOrNull("meetupLocation"),
    meetupTime = parseMeetupTime(stringOrNull("meetupTime")),
    productId = longOrNull("productId"),
    productTitle = stringOrNull("productTitle"),
    productImage = stringOrNull("productImage"),
    productPrice = longOrNull("productPrice"),
    isRead = boolean("isRead"),
    sentAt = parseServerInstant(stringOrNull("sentAt")),
)

internal fun JsonObject.toConversation() = Conversation(
    partner = objectOrNull("partner").toUserSummary(),
    productId = longOrNull("productId"),
    productTitle = stringOrNull("productTitle"),
    productImage = stringOrNull("productImage"),
    productPrice = longOrNull("productPrice"),
    lastMessage = string("lastMessage"),
    lastMessageType = MessageType.fromApi(stringOrNull("lastMessageType")),
    lastMessageMine = boolean("lastMessageMine"),
    lastMessageAt = parseServerInstant(stringOrNull("lastMessageAtIso")),
    unreadCount = int("unreadCount"),
)

internal fun JsonObject.toNotification() = AppNotification(
    id = long("id"),
    type = string("type"),
    title = string("title"),
    message = string("message"),
    referenceId = longOrNull("referenceId"),
    isRead = boolean("isRead"),
    createdAt = parseServerInstant(stringOrNull("createdAtIso")),
)
