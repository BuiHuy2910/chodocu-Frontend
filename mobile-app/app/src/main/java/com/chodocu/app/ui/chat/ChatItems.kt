package com.chodocu.app.ui.chat

import com.chodocu.app.core.formatDayLabel
import com.chodocu.app.data.model.ChatMessage
import com.chodocu.app.data.model.MessageType
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Một dòng trong danh sách chat: nhãn ngày, tin đã gửi, hoặc tin mình đang gửi. */
internal sealed interface ChatItem {
    /** Khóa ổn định để danh sách không bị nhảy khi có tin mới. */
    val key: String

    data class DayHeader(val label: String) : ChatItem {
        override val key: String get() = "day-$label"
    }

    data class Message(
        val message: ChatMessage,
        val isMine: Boolean,
        /** Tin đầu / cuối của một nhóm tin liên tiếp cùng người gửi (để bo góc, hiện giờ và ảnh đại diện). */
        val isFirstInGroup: Boolean,
        val isLastInGroup: Boolean,
        /** Trạng thái dưới tin mới nhất của mình: "Đã xem" hoặc "Đã gửi". */
        val deliveryStatus: String?,
    ) : ChatItem {
        override val key: String get() = "message-${message.id}"
    }

    data class Pending(val pending: PendingMessage) : ChatItem {
        override val key: String get() = "pending-${pending.localId}"
    }
}

/** Hai tin liên tiếp cách nhau dưới khoảng này thì gộp chung một nhóm. */
private const val GROUP_GAP_MINUTES = 5L

/**
 * Dựng danh sách hiển thị từ [messages] (cũ nhất trước) và các tin đang gửi.
 * Kết quả xếp MỚI NHẤT TRƯỚC vì danh sách chat dùng reverseLayout (tin mới nằm sát ô nhập).
 */
internal fun buildChatItems(
    messages: List<ChatMessage>,
    pending: List<PendingMessage>,
    myUserId: Long,
    zone: ZoneId = ZoneId.systemDefault(),
): List<ChatItem> {
    val newestMineId = messages.lastOrNull { it.sender.id == myUserId && it.type != MessageType.SYSTEM }?.id
    val items = mutableListOf<ChatItem>()
    var previousDay: LocalDate? = null

    messages.forEachIndexed { index, message ->
        val sentAt = message.sentAt
        val day = sentAt?.atZone(zone)?.toLocalDate()
        if (sentAt != null && day != previousDay) {
            items += ChatItem.DayHeader(formatDayLabel(sentAt))
            previousDay = day
        }
        val deliveryStatus = when {
            message.id != newestMineId || pending.isNotEmpty() -> null
            message.isRead -> "Đã xem"
            else -> "Đã gửi"
        }
        items += ChatItem.Message(
            message = message,
            isMine = message.sender.id == myUserId,
            isFirstInGroup = !isSameGroup(messages.getOrNull(index - 1), message, zone),
            isLastInGroup = !isSameGroup(message, messages.getOrNull(index + 1), zone),
            deliveryStatus = deliveryStatus,
        )
    }

    if (pending.isNotEmpty()) {
        val now = Instant.now()
        if (previousDay != now.atZone(zone).toLocalDate()) items += ChatItem.DayHeader(formatDayLabel(now))
        pending.forEach { items += ChatItem.Pending(it) }
    }
    return items.asReversed()
}

/** Hai tin cùng một nhóm: cùng người gửi, cùng ngày, gửi sát nhau và không phải tin hệ thống. */
private fun isSameGroup(first: ChatMessage?, second: ChatMessage?, zone: ZoneId): Boolean {
    if (first == null || second == null) return false
    if (first.type == MessageType.SYSTEM || second.type == MessageType.SYSTEM) return false
    if (first.sender.id != second.sender.id) return false
    val firstAt = first.sentAt ?: return false
    val secondAt = second.sentAt ?: return false
    if (firstAt.atZone(zone).toLocalDate() != secondAt.atZone(zone).toLocalDate()) return false
    return Duration.between(firstAt, secondAt).toMinutes() < GROUP_GAP_MINUTES
}
