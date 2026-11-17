package com.chodocu.app.push

import android.util.Log
import com.chodocu.app.BuildConfig
import com.chodocu.app.ChoDoCuApp
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Nhận token mới và thông báo đẩy FCM khi app đang mở hoặc nhận data payload. */
class ChoDoCuMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        PushNotificationManager.saveToken(applicationContext, token)
        val container = (application as? ChoDoCuApp)?.container ?: return
        if (container.session.token != null) {
            scope.launch {
                runCatching {
                    container.notifications.registerDeviceToken(token, BuildConfig.APPLICATION_ID)
                }.onFailure {
                    Log.w("ChoDoCuFCM", "Không thể cập nhật token mới lên backend: ${it.message}")
                }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        val title = message.notification?.title ?: data["title"].orEmpty()
        val body = message.notification?.body ?: data["body"].orEmpty()
        if (title.isBlank() && body.isBlank()) return
        PushNotificationManager.showNotification(applicationContext, title, body, data)
    }
}
