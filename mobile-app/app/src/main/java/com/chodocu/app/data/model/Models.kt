package com.chodocu.app.data.model

import java.math.BigDecimal

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)

data class PageResponse<T>(
    val content: List<T>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val studentId: String? = null,
    val phone: String? = null,
    val universityId: Long? = 1L,
    val dormArea: String? = "KTX Khu B - ĐHQG"
)

data class AuthResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val user: UserDto
)

data class UserDto(
    val id: Long,
    val email: String,
    val fullName: String,
    val studentId: String?,
    val phone: String?,
    val avatarUrl: String?,
    val universityId: Long?,
    val universityName: String?,
    val universityShortName: String?,
    val dormArea: String?,
    val role: String = "STUDENT",
    val verifiedStudent: Boolean = true,
    val active: Boolean = true,
    val rating: Double = 5.0,
    val completedDeals: Int = 0
)

data class CategoryDto(
    val id: Long,
    val name: String,
    val slug: String,
    val icon: String?
)

data class UniversityDto(
    val id: Long,
    val name: String,
    val shortName: String,
    val emailDomain: String?,
    val address: String?,
    val safeMeetupSpots: String?
)

data class ProductDto(
    val id: Long,
    val title: String,
    val description: String?,
    val price: BigDecimal,
    val originalPrice: BigDecimal?,
    val isFree: Boolean = false,
    val isUrgent: Boolean = false,
    val condition: String = "LIKE_NEW_95",
    val conditionLabel: String = "Như mới (95%)",
    val conditionNote: String? = "Mới 95%",
    val passReason: String? = "Dọn phòng KTX",
    val pickupSpot: String? = "Sảnh Tầng 1 Tòa BA1 - KTX Khu B",
    val status: String = "APPROVED",
    val viewCount: Int = 1,
    val favoriteCount: Int = 0,
    val isFavorited: Boolean = false,
    val categoryId: Long? = 1L,
    val categoryName: String = "Đồ dùng & Bàn",
    val universityId: Long? = 1L,
    val universityName: String = "KTX Khu B - ĐHQG TP.HCM",
    val universityShortName: String = "KTX Khu B",
    val seller: UserDto?,
    val imageUrls: List<String> = emptyList(),
    val timeAgo: String = "Vừa xong"
)

data class ProductCreateRequest(
    val title: String,
    val description: String,
    val price: BigDecimal,
    val originalPrice: BigDecimal? = null,
    val isFree: Boolean = false,
    val isUrgent: Boolean = false,
    val condition: String = "LIKE_NEW_95",
    val conditionNote: String = "Mới 95%",
    val passReason: String = "Dọn phòng KTX / Chuyển trọ",
    val pickupSpot: String = "Sảnh Tầng 1 Tòa BA1 - KTX Khu B",
    val categoryId: Long,
    val universityId: Long,
    val imageUrls: List<String>
)

data class OrderCreateRequest(
    val productId: Long,
    val offeredPrice: BigDecimal,
    val meetupLocation: String,
    val meetupTime: String,
    val buyerNote: String
)

data class OrderDto(
    val id: Long,
    val product: ProductDto,
    val buyer: UserDto,
    val seller: UserDto,
    val offeredPrice: BigDecimal,
    val meetupLocation: String?,
    val meetupTime: String?,
    val buyerNote: String?,
    val sellerNote: String?,
    val status: String,
    val statusLabel: String,
    val createdAt: String
)

data class ChatMessageRequest(
    val receiverId: Long,
    val productId: Long?,
    val content: String,
    val messageType: String = "TEXT",
    val offeredPrice: BigDecimal? = null,
    val meetupLocation: String? = null,
    val meetupTime: String? = null
)

data class ChatMessageDto(
    val id: Long,
    val sender: UserDto,
    val receiver: UserDto,
    val productId: Long?,
    val productTitle: String?,
    val content: String,
    val messageType: String = "TEXT",
    val offeredPrice: BigDecimal? = null,
    val meetupLocation: String? = null,
    val meetupTime: String? = null,
    val createdAt: String
)

enum class PopupType {
    SUCCESS, INFO, WARNING, ERROR
}

data class PopupDialogState(
    val visible: Boolean = false,
    val title: String = "",
    val message: String = "",
    val type: PopupType = PopupType.SUCCESS,
    val confirmText: String = "Đã hiểu",
    val onConfirm: (() -> Unit)? = null
)

