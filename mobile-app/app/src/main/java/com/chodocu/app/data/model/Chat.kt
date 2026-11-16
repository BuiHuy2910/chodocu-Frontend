package com.chodocu.app.data.model

import java.time.Instant
import java.time.LocalDateTime

/** Loại tin nhắn. SYSTEM do máy chủ tự tạo (đặt mua, chấp nhận, hủy...). */
enum class MessageType {
    TEXT,
    IMAGE,
    OFFER,
    MEETUP,
    SYSTEM;

    companion object {
        fun fromApi(value: String?): MessageType =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: TEXT
    }
}

/** Một tin nhắn giữa hai người, có thể gắn với một sản phẩm. */
data class ChatMessage(
    val id: Long,
    val sender: UserSummary,
    val receiver: UserSummary,
    val type: MessageType,
    val content: String,
    val imageUrl: String?,
    val offeredPrice: Long?,
    val meetupLocation: String?,
    val meetupTime: LocalDateTime?,
    val productId: Long?,
    val productTitle: String?,
    val productImage: String?,
    val productPrice: Long?,
    val isRead: Boolean,
    val sentAt: Instant?,
)

/** Một cuộc trò chuyện trong hộp thư (mỗi người chat cùng là một dòng). */
data class Conversation(
    val partner: UserSummary,
    val productId: Long?,
    val productTitle: String?,
    val productImage: String?,
    val productPrice: Long?,
    val lastMessage: String,
    val lastMessageType: MessageType,
    val lastMessageMine: Boolean,
    val lastMessageAt: Instant?,
    val unreadCount: Int,
)

/** Dữ liệu gửi một tin nhắn mới. */
data class SendMessageInput(
    val receiverId: Long,
    val productId: Long?,
    val type: MessageType,
    val content: String = "",
    val imageUrl: String? = null,
    val offeredPrice: Long? = null,
    val meetupLocation: String? = null,
    val meetupTime: LocalDateTime? = null,
)
