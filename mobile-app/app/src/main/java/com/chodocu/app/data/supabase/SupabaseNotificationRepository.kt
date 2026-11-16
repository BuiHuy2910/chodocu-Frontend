package com.chodocu.app.data.supabase

import com.chodocu.app.data.common.int
import com.chodocu.app.data.model.AppNotification
import com.chodocu.app.data.repository.NotificationRepository

/** Thông báo qua các hàm api_my_notifications, api_notification_unread_count, api_mark_notifications_read. */
class SupabaseNotificationRepository(private val client: SupabaseClient) : NotificationRepository {

    override suspend fun getNotifications(): List<AppNotification> =
        client.callList("api_my_notifications").map { it.toNotification() }

    override suspend fun getUnreadCount(): Int =
        client.callObject("api_notification_unread_count").int("count")

    override suspend fun markAllRead() {
        client.callObject("api_mark_notifications_read")
    }
}
