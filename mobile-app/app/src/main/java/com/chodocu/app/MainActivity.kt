package com.chodocu.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chodocu.app.di.LocalAppContainer
import com.chodocu.app.push.PushNotificationManager
import com.chodocu.app.push.PushTarget
import com.chodocu.app.ui.navigation.AppRoot
import com.chodocu.app.ui.theme.ChoDoCuTheme
import kotlinx.coroutines.flow.MutableStateFlow

/** Activity duy nhất của app: bật giao diện tràn viền và gắn toàn bộ UI Compose. */
class MainActivity : ComponentActivity() {

    private val pushTarget = MutableStateFlow<PushTarget?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pushTarget.value = PushNotificationManager.parsePushTarget(intent)
        // App dùng nền sáng nên icon thanh trạng thái / điều hướng luôn màu tối
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(LIGHT_SCRIM, DARK_SCRIM),
        )
        val container = (application as ChoDoCuApp).container
        setContent {
            val pendingTarget by pushTarget.collectAsStateWithLifecycle()
            CompositionLocalProvider(LocalAppContainer provides container) {
                ChoDoCuTheme {
                    AppRoot(
                        pushTarget = pendingTarget,
                        onPushTargetHandled = { pushTarget.value = null },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        PushNotificationManager.parsePushTarget(intent)?.let { pushTarget.value = it }
    }

    private companion object {
        val LIGHT_SCRIM = Color.argb(0xE6, 0xFF, 0xFF, 0xFF)
        val DARK_SCRIM = Color.argb(0x80, 0x1B, 0x1B, 0x1B)
    }
}
