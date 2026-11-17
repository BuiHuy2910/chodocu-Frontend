package com.chodocu.app.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chodocu.app.di.LocalAppContainer
import com.chodocu.app.push.PushTarget
import com.chodocu.app.ui.auth.LoginScreen
import com.chodocu.app.ui.auth.RegisterScreen
import kotlinx.coroutines.CancellationException

/**
 * Gốc giao diện: chưa đăng nhập thì hiện nhóm màn đăng nhập / đăng ký,
 * đã đăng nhập thì hiện khung chính có thanh điều hướng dưới.
 * Đăng nhập, đăng xuất hay hết phiên chỉ cần đổi session — màn hình tự chuyển theo.
 */
@Composable
fun AppRoot(
    pushTarget: PushTarget? = null,
    onPushTargetHandled: () -> Unit = {},
) {
    val container = LocalAppContainer.current
    val user by container.session.currentUser.collectAsStateWithLifecycle()

    // Mở app khi đã đăng nhập: làm mới thông tin tài khoản (phiên hết hạn sẽ tự đưa về màn đăng nhập)
    LaunchedEffect(Unit) {
        if (container.session.currentUser.value != null) {
            try {
                container.auth.refreshCurrentUser()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Không có mạng: vẫn dùng thông tin đã lưu
            }
        }
    }

    Crossfade(targetState = user?.id, label = "appRoot") { userId ->
        if (userId == null) {
            AuthNavHost()
        } else {
            MainShell(
                userId = userId,
                pushTarget = pushTarget,
                onPushTargetHandled = onPushTargetHandled,
            )
        }
    }
}

/** Nhóm màn hình khi chưa đăng nhập. */
@Composable
private fun AuthNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(onNavigateToRegister = { navController.navigate(Routes.REGISTER) { launchSingleTop = true } })
        }
        composable(Routes.REGISTER) {
            RegisterScreen(onBack = { navController.popBackStack() })
        }
    }
}
