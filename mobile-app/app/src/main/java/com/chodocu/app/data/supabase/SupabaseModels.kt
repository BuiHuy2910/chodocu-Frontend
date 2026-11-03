package com.chodocu.app.data.supabase

import com.chodocu.app.data.model.*
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Các "row" Gson nhận trực tiếp từ view/RPC của Supabase.
 * Tên field camelCase được map với cột snake_case nhờ FieldNamingPolicy
 * trong SupabaseClient.gson.
 */

data class SUser(
    val id: Long = 0,
    val email: String? = null,
    val fullName: String? = null,
    val studentId: String? = null,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val dormArea: String? = null,
    val role: String? = null,
    val verifiedStudent: Boolean? = null,
    val active: Boolean? = null,
    val rating: Double? = null,
    val completedDeals: Int? = null,
    val universityId: Long? = null,
    val universityName: String? = null,
    val universityShortName: String? = null
)

data class SProduct(
    val id: Long = 0,
    val title: String? = null,
    val description: String? = null,
    val price: BigDecimal? = null,
    val originalPrice: BigDecimal? = null,
    val isFree: Boolean? = null,
    val isUrgent: Boolean? = null,
    val itemCondition: String? = null,
    val conditionNote: String? = null,
    val passReason: String? = null,
    val pickupSpot: String? = null,
    val status: String? = null,
    val rejectReason: String? = null,
    val viewCount: Int? = null,
    val favoriteCount: Int? = null,
    val createdAt: String? = null,
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val categoryIcon: String? = null,
    val universityId: Long? = null,
    val universityName: String? = null,
    val universityShortName: String? = null,
    val sellerId: Long? = null,
    val sellerName: String? = null,
    val sellerAvatarUrl: String? = null,
    val sellerVerified: Boolean? = null,
    val sellerRating: Double? = null,
    val sellerCompletedDeals: Int? = null,
    val sellerUniversityShortName: String? = null,
    val imageUrls: List<String>? = null
)

data class SOrder(
    val id: Long = 0,
    val orderCode: String? = null,
    val offeredPrice: BigDecimal? = null,
    val meetupLocation: String? = null,
    val meetupTime: String? = null,
    val buyerNote: String? = null,
    val sellerNote: String? = null,
    val status: String? = null,
    val paymentStatus: String? = null,
    val feePayer: String? = null,
    val platformFeeRate: BigDecimal? = null,
    val platformFeeAmount: BigDecimal? = null,
    val finalSellerAmount: BigDecimal? = null,
    val finalBuyerAmount: BigDecimal? = null,
    val createdAt: String? = null,
    val completedAt: String? = null,
    val buyerId: Long? = null,
    val buyerName: String? = null,
    val buyerAvatarUrl: String? = null,
    val sellerId: Long? = null,
    val sellerName: String? = null,
    val sellerAvatarUrl: String? = null,
    val productId: Long? = null,
    val productTitle: String? = null,
    val productPrice: BigDecimal? = null,
    val productDescription: String? = null,
    val productCondition: String? = null,
    val productUniversityId: Long? = null,
    val productCategoryId: Long? = null,
    val productImage: String? = null
)

data class SChat(
    val id: Long = 0,
    val content: String? = null,
    val messageType: String? = null,
    val offeredPrice: BigDecimal? = null,
    val meetupLocation: String? = null,
    val meetupTime: String? = null,
    val createdAt: String? = null,
    val senderId: Long? = null,
    val senderName: String? = null,
    val senderAvatarUrl: String? = null,
    val receiverId: Long? = null,
    val receiverName: String? = null,
    val receiverAvatarUrl: String? = null,
    val productId: Long? = null,
    val productTitle: String? = null
)

data class SLogin(
    val outToken: String? = null,
    val outUserId: Long? = null
)

data class SFee(
    val outRate: BigDecimal? = null,
    val outFee: BigDecimal? = null,
    val outSeller: BigDecimal? = null,
    val outBuyer: BigDecimal? = null,
    val outPayer: String? = null
)

// =================================================================================
// MAPPER: Row của Supabase -> DTO dùng trong UI
// =================================================================================

private val DISPLAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

private fun parseTime(raw: String?): LocalDateTime? {
    if (raw.isNullOrBlank()) return null
    return try {
        OffsetDateTime.parse(raw).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
    } catch (_: Exception) {
        try {
            LocalDateTime.parse(raw)
        } catch (_: Exception) {
            null
        }
    }
}

private fun timeAgo(raw: String?): String {
    val time = parseTime(raw) ?: return "Vừa xong"
    val minutes = Duration.between(time, LocalDateTime.now()).toMinutes()
    return when {
        minutes < 1 -> "Vừa xong"
        minutes < 60 -> "$minutes phút trước"
        minutes < 1440 -> "${minutes / 60} giờ trước"
        else -> "${minutes / 1440} ngày trước"
    }
}

private fun displayTime(raw: String?): String {
    val time = parseTime(raw) ?: return ""
    return time.format(DISPLAY_FORMAT)
}

fun SUser.toDto(): UserDto = UserDto(
    id = id,
    email = email.orEmpty(),
    fullName = fullName.orEmpty(),
    studentId = studentId,
    phone = phone,
    avatarUrl = avatarUrl,
    universityId = universityId,
    universityName = universityName,
    universityShortName = universityShortName,
    dormArea = dormArea,
    role = role ?: "STUDENT",
    verifiedStudent = verifiedStudent ?: true,
    active = active ?: true,
    rating = rating ?: 5.0,
    completedDeals = completedDeals ?: 0
)

fun SProduct.toDto(isFavorited: Boolean = false): ProductDto = ProductDto(
    id = id,
    title = title.orEmpty(),
    description = description,
    price = price ?: BigDecimal.ZERO,
    originalPrice = originalPrice,
    isFree = isFree ?: false,
    isUrgent = isUrgent ?: false,
    condition = itemCondition ?: "LIKE_NEW_95",
    conditionLabel = conditionNote ?: conditionLabelOf(itemCondition),
    conditionNote = conditionNote,
    passReason = passReason,
    pickupSpot = pickupSpot,
    status = status ?: "APPROVED",
    viewCount = viewCount ?: 0,
    favoriteCount = favoriteCount ?: 0,
    isFavorited = isFavorited,
    categoryId = categoryId,
    categoryName = categoryName ?: "Khác",
    universityId = universityId,
    universityName = universityName.orEmpty(),
    universityShortName = universityShortName.orEmpty(),
    seller = sellerId?.let {
        UserDto(
            id = it,
            email = "",
            fullName = sellerName.orEmpty(),
            studentId = null,
            phone = null,
            avatarUrl = sellerAvatarUrl,
            universityId = null,
            universityName = null,
            universityShortName = sellerUniversityShortName,
            dormArea = null,
            verifiedStudent = sellerVerified ?: true,
            rating = sellerRating ?: 5.0,
            completedDeals = sellerCompletedDeals ?: 0
        )
    },
    imageUrls = imageUrls ?: emptyList(),
    timeAgo = timeAgo(createdAt)
)

private fun conditionLabelOf(condition: String?): String = when (condition) {
    "NEW_100" -> "Mới 100%"
    "LIKE_NEW_99" -> "Như mới (99%)"
    "LIKE_NEW_95" -> "Như mới (95%)"
    "GOOD_85", "GOOD_80" -> "Tốt (80-90%)"
    "USABLE", "FAIR_70" -> "Còn dùng tốt"
    else -> "Đang dùng"
}

fun SOrder.toDto(): OrderDto {
    val product = ProductDto(
        id = productId ?: 0,
        title = productTitle.orEmpty(),
        description = productDescription,
        price = productPrice ?: offeredPrice ?: BigDecimal.ZERO,
        originalPrice = null,
        condition = productCondition ?: "LIKE_NEW_95",
        conditionLabel = conditionLabelOf(productCondition),
        status = status ?: "PENDING",
        categoryId = productCategoryId,
        universityId = productUniversityId,
        universityName = "",
        universityShortName = "",
        seller = sellerId?.let {
            UserDto(
                id = it, email = "", fullName = sellerName.orEmpty(), studentId = null, phone = null,
                avatarUrl = sellerAvatarUrl, universityId = null, universityName = null,
                universityShortName = null, dormArea = null
            )
        },
        imageUrls = listOfNotNull(productImage),
        timeAgo = ""
    )

    val buyer = UserDto(
        id = buyerId ?: 0, email = "", fullName = buyerName.orEmpty(), studentId = null, phone = null,
        avatarUrl = buyerAvatarUrl, universityId = null, universityName = null,
        universityShortName = null, dormArea = null
    )
    val seller = UserDto(
        id = sellerId ?: 0, email = "", fullName = sellerName.orEmpty(), studentId = null, phone = null,
        avatarUrl = sellerAvatarUrl, universityId = null, universityName = null,
        universityShortName = null, dormArea = null
    )

    return OrderDto(
        id = id,
        orderCode = orderCode,
        product = product,
        buyer = buyer,
        seller = seller,
        offeredPrice = offeredPrice ?: BigDecimal.ZERO,
        meetupLocation = meetupLocation,
        meetupTime = meetupTime,
        buyerNote = buyerNote,
        sellerNote = sellerNote,
        status = status ?: "PENDING",
        statusLabel = orderStatusLabel(status),
        paymentStatus = paymentStatus,
        feePayer = feePayer,
        platformFeeRate = platformFeeRate,
        platformFeeAmount = platformFeeAmount,
        finalSellerAmount = finalSellerAmount,
        finalBuyerAmount = finalBuyerAmount,
        createdAt = displayTime(createdAt),
        completedAt = displayTime(completedAt)
    )
}

fun orderStatusLabel(status: String?): String = when (status) {
    "PENDING" -> "Chờ người bán duyệt"
    "ACCEPTED" -> "Đang giao dịch / Hẹn gặp"
    "COMPLETED" -> "Đã hoàn tất giao dịch"
    "DISPUTED" -> "Đang tranh chấp - Admin xử lý"
    "REJECTED" -> "Người bán đã từ chối"
    "CANCELLED" -> "Đã hủy yêu cầu"
    else -> "Chờ xử lý"
}

fun SChat.toDto(): ChatMessageDto = ChatMessageDto(
    id = id,
    sender = UserDto(
        id = senderId ?: 0, email = "", fullName = senderName.orEmpty(), studentId = null, phone = null,
        avatarUrl = senderAvatarUrl, universityId = null, universityName = null,
        universityShortName = null, dormArea = null
    ),
    receiver = UserDto(
        id = receiverId ?: 0, email = "", fullName = receiverName.orEmpty(), studentId = null, phone = null,
        avatarUrl = receiverAvatarUrl, universityId = null, universityName = null,
        universityShortName = null, dormArea = null
    ),
    productId = productId,
    productTitle = productTitle,
    content = content.orEmpty(),
    messageType = messageType ?: "TEXT",
    offeredPrice = offeredPrice,
    meetupLocation = meetupLocation,
    meetupTime = meetupTime,
    createdAt = displayTime(createdAt)
)

fun SFee.toDto(amount: BigDecimal): FeeCalculationDto {
    val rate = outRate ?: BigDecimal.ZERO
    return FeeCalculationDto(
        orderAmount = amount,
        feeRate = rate,
        feeRatePercent = rate.multiply(BigDecimal(100)).stripTrailingZeros().toPlainString(),
        platformFeeAmount = outFee ?: BigDecimal.ZERO,
        sellerReceiveAmount = outSeller ?: amount,
        buyerPayAmount = outBuyer ?: amount,
        feePayer = outPayer ?: "SELLER",
        appliedTierName = null,
        feeEnabled = true
    )
}
