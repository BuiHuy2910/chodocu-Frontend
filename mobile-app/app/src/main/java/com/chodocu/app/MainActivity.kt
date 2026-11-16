package com.chodocu.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.chodocu.app.di.LocalAppContainer
import com.chodocu.app.ui.navigation.AppRoot
import com.chodocu.app.ui.theme.ChoDoCuTheme

/** Activity duy nhất của app: bật giao diện tràn viền và gắn toàn bộ UI Compose. */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // App dùng nền sáng nên icon thanh trạng thái / điều hướng luôn màu tối
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(LIGHT_SCRIM, DARK_SCRIM),
        )
        val container = (application as ChoDoCuApp).container
        setContent {
            CompositionLocalProvider(LocalAppContainer provides container) {
                ChoDoCuTheme {
                    AppRoot()
                }
            }
        }
    }

    private companion object {
        val LIGHT_SCRIM = Color.argb(0xE6, 0xFF, 0xFF, 0xFF)
        val DARK_SCRIM = Color.argb(0x80, 0x1B, 0x1B, 0x1B)
    }
}
