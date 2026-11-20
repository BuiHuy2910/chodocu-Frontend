package com.chodocu.app.push

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.chodocu.app.BuildConfig
import com.chodocu.app.MainActivity
import com.chodocu.app.R
import com.chodocu.app.data.repository.NotificationRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Dữ liệu điều hướng khi người dùng bấm vào thông báo đẩy. */
data class PushTarget(
    val type: String,
    val referenceId: Long?,
    val partnerId: Long?,
    val partnerName: String?,
    val productId: Long?,
)

/**
 * Quản lý kênh thông báo, lưu / đồng bộ FCM token với backend và hiển thị thông báo hệ thống.
 */
object PushNotificationManager {

    const val CHANNEL_ID = "chodocu_default"
    private const val CHANNEL_NAME = "Thông báo Chợ Đồ Cũ"
    private const val PREFS_NAME = "chodocu_push"
    private const val KEY_FCM_TOKEN = "fcm_token"

    const val EXTRA_TYPE = "push_type"
    const val EXTRA_REFERENCE_ID = "reference_id"
    const val EXTRA_PARTNER_ID = "partner_id"
    const val EXTRA_PARTNER_NAME = "partner_name"
    const val EXTRA_PRODUCT_ID = "product_id"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Thông báo tin nhắn mới, duyệt tin và trạng thái đơn hàng"
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun saveToken(context: Context, token: String) {
        if (token.isBlank()) return
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FCM_TOKEN, token.trim())
            .apply()
    }

    fun getSavedToken(context: Context): String? =
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_FCM_TOKEN, null)

    suspend fun syncTokenWithBackend(context: Context, notifications: NotificationRepository) {
        val token = fetchCurrentFcmToken() ?: getSavedToken(context) ?: return
        saveToken(context, token)
        try {
            notifications.registerDeviceToken(token, BuildConfig.APPLICATION_ID)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("PushManager", "Không đồng bộ được FCM token: ${e.message}")
        }
    }

    suspend fun unregisterFromBackend(context: Context, notifications: NotificationRepository) {
        val token = getSavedToken(context) ?: return
        try {
            notifications.unregisterDeviceToken(token)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("PushManager", "Hủy đăng ký FCM token thất bại: ${e.message}")
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        body: String,
        data: Map<String, String>,
    ) {
        createChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data["type"]?.let {
                putExtra("type", it)
                putExtra(EXTRA_TYPE, it)
            }
            data["referenceId"]?.toLongOrNull()?.let {
                putExtra("referenceId", it.toString())
                putExtra(EXTRA_REFERENCE_ID, it)
            }
            data["partnerId"]?.toLongOrNull()?.let {
                putExtra("partnerId", it.toString())
                putExtra(EXTRA_PARTNER_ID, it)
            }
            data["partnerName"]?.let {
                putExtra("partnerName", it)
                putExtra(EXTRA_PARTNER_NAME, it)
            }
            data["productId"]?.toLongOrNull()?.let {
                putExtra("productId", it.toString())
                putExtra(EXTRA_PRODUCT_ID, it)
            }
        }

        val requestCode = (System.currentTimeMillis() and 0x7FFFFFFF).toInt()
        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setColor(ContextCompat.getColor(context, R.color.brand_green))
            .setContentTitle(title.ifBlank { context.getString(R.string.app_name) })
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(requestCode, notification)
    }

    fun parsePushTarget(intent: Intent?): PushTarget? {
        val extras = intent?.extras ?: return null
        val type = readStringExtra(extras, "type", EXTRA_TYPE) ?: return null
        return PushTarget(
            type = type,
            referenceId = readLongExtra(extras, "referenceId", EXTRA_REFERENCE_ID),
            partnerId = readLongExtra(extras, "partnerId", EXTRA_PARTNER_ID),
            partnerName = readStringExtra(extras, "partnerName", EXTRA_PARTNER_NAME),
            productId = readLongExtra(extras, "productId", EXTRA_PRODUCT_ID),
        )
    }

    private fun readStringExtra(extras: android.os.Bundle, vararg keys: String): String? =
        keys.firstNotNullOfOrNull { key -> extras.getString(key)?.takeIf { it.isNotBlank() } }

    @Suppress("DEPRECATION")
    private fun readLongExtra(extras: android.os.Bundle, vararg keys: String): Long? {
        for (key in keys) {
            if (!extras.containsKey(key)) continue
            when (val raw = extras.get(key)) {
                is Number -> return raw.toLong().takeIf { it > 0L }
                is String -> raw.toLongOrNull()?.takeIf { it > 0L }?.let { return it }
            }
        }
        return null
    }

    private suspend fun fetchCurrentFcmToken(): String? = suspendCancellableCoroutine { cont ->
        try {
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (!cont.isActive) return@addOnCompleteListener
                    if (task.isSuccessful) {
                        cont.resume(task.result)
                    } else {
                        Log.w("PushManager", "Lấy FCM token thất bại", task.exception)
                        cont.resume(null)
                    }
                }
        } catch (e: Exception) {
            if (cont.isActive) cont.resume(null)
        }
    }
}
