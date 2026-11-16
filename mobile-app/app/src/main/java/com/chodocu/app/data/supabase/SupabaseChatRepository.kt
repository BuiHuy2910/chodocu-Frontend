package com.chodocu.app.data.supabase

import com.chodocu.app.core.toMeetupString
import com.chodocu.app.data.common.int
import com.chodocu.app.data.model.ChatMessage
import com.chodocu.app.data.model.Conversation
import com.chodocu.app.data.model.SendMessageInput
import com.chodocu.app.data.repository.ChatRepository

/** Chat giữa người mua và người bán qua các hàm api_conversations, api_messages, api_send_message... */
class SupabaseChatRepository(private val client: SupabaseClient) : ChatRepository {

    override suspend fun getConversations(): List<Conversation> =
        client.callList("api_conversations").map { it.toConversation() }

    override suspend fun getMessages(partnerId: Long): List<ChatMessage> =
        client.callList("api_messages", "p_partner_id" to partnerId).map { it.toChatMessage() }

    override suspend fun sendMessage(input: SendMessageInput): ChatMessage =
        client.callObject(
            "api_send_message",
            "p_receiver_id" to input.receiverId,
            "p_product_id" to input.productId,
            "p_message_type" to input.type.name,
            "p_content" to input.content.trim(),
            "p_image_url" to input.imageUrl,
            "p_offered_price" to input.offeredPrice,
            "p_meetup_location" to input.meetupLocation,
            "p_meetup_time" to input.meetupTime?.toMeetupString(),
        ).toChatMessage()

    override suspend fun markConversationRead(partnerId: Long) {
        client.callObject("api_mark_read", "p_partner_id" to partnerId)
    }

    override suspend fun getUnreadMessageCount(): Int =
        client.callObject("api_unread_count").int("count")
}
