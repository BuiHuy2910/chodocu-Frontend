package com.chodocu.app.data.repository

import com.chodocu.app.data.model.AppNotification

/** Thông báo hệ thống (duyệt tin, đơn hàng...). */
interface NotificationRepository {
    suspend fun getNotifications(): List<AppNotification>

    suspend fun getUnreadCount(): Int

    suspend fun markAllRead()
}
