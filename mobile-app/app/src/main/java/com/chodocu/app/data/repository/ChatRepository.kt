package com.chodocu.app.data.repository

import com.chodocu.app.data.model.ChatMessage
import com.chodocu.app.data.model.Conversation
import com.chodocu.app.data.model.SendMessageInput

/** Nhắn tin giữa người mua và người bán (app tự hỏi máy chủ định kỳ để lấy tin mới). */
interface ChatRepository {
    /** Danh sách cuộc trò chuyện, mới nhất trước. */
    suspend fun getConversations(): List<Conversation>

    /** Tin nhắn với một người, cũ nhất trước (tối đa 200 tin gần nhất). */
    suspend fun getMessages(partnerId: Long): List<ChatMessage>

    suspend fun sendMessage(input: SendMessageInput): ChatMessage

    /** Đánh dấu đã đọc mọi tin người kia gửi cho mình. */
    suspend fun markConversationRead(partnerId: Long)

    /** Tổng số tin nhắn chưa đọc (hiện badge ở thanh điều hướng). */
    suspend fun getUnreadMessageCount(): Int
}
