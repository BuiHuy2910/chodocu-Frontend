package com.chodocu.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.chodocu.app.di.appViewModel
import com.chodocu.app.ui.components.CountBadge
import com.chodocu.app.ui.components.PollWhileResumed
import com.chodocu.app.ui.theme.BrandOrange

/** Thời gian giữa hai lần cập nhật huy hiệu chưa đọc. */
private const val BADGE_POLL_MILLIS = 15_000L

/** Một mục trên thanh điều hướng dưới. */
private data class BottomTab(
    val label: String,
    val routePattern: String,
    val navigateRoute: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
)

private val leftTabs = listOf(
    BottomTab("Trang chủ", Routes.HOME, Routes.HOME, Icons.Outlined.Home, Icons.Filled.Home),
    BottomTab("Tin nhắn", Routes.INBOX, Routes.INBOX, Icons.AutoMirrored.Outlined.Chat, Icons.AutoMirrored.Filled.Chat),
)
private val rightTabs = listOf(
    BottomTab(
        "Giao dịch", Routes.ORDERS, Routes.ORDERS_TAB,
        Icons.AutoMirrored.Outlined.ReceiptLong, Icons.AutoMirrored.Filled.ReceiptLong,
    ),
    BottomTab("Tài khoản", Routes.PROFILE, Routes.PROFILE, Icons.Outlined.Person, Icons.Filled.Person),
)
private val tabRoutes = (leftTabs + rightTabs).map { it.routePattern }.toSet()

/**
 * Khung chính sau khi đăng nhập: NavHost các màn hình + thanh điều hướng dưới (chỉ hiện ở 4 tab chính).
 * [userId] dùng làm khóa ViewModel để đổi tài khoản thì số chưa đọc được tính lại từ đầu.
 */
@Composable
fun MainShell(userId: Long) {
    val navController = rememberNavController()
    val shellViewModel = appViewModel(key = "shell-$userId") { _ -> MainShellViewModel(chat, notifications) }
    val badges by shellViewModel.badges.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = currentDestination?.route in tabRoutes

    // Cập nhật huy hiệu khi đổi màn hình và định kỳ khi app đang mở
    LaunchedEffect(currentDestination?.route) { shellViewModel.refreshBadges() }
    PollWhileResumed(BADGE_POLL_MILLIS) { shellViewModel.refreshBadges() }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(
                    currentDestination = currentDestination,
                    unreadMessages = badges.unreadMessages,
                    onTabClick = { tab -> navController.navigateToTab(tab.navigateRoute) },
                    onPostClick = { navController.navigate(Routes.POST_NEW) },
                )
            }
        },
    ) { innerPadding ->
        MainNavHost(
            navController = navController,
            unreadNotifications = badges.unreadNotifications,
            modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding),
        )
    }
}

/** Chuyển tab: giữ lại trạng thái từng tab, không chồng nhiều bản cùng một tab. */
internal fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun AppBottomBar(
    currentDestination: NavDestination?,
    unreadMessages: Int,
    onTabClick: (BottomTab) -> Unit,
    onPostClick: () -> Unit,
) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
            leftTabs.forEach { tab ->
                TabItem(tab, currentDestination, badgeCount = if (tab.routePattern == Routes.INBOX) unreadMessages else 0, onTabClick)
            }
            // Nút "Đăng tin" nổi bật ở giữa — mở màn đăng tin, không phải một tab
            NavigationBarItem(
                selected = false,
                onClick = onPostClick,
                icon = {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(BrandOrange),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null, tint = Color.White)
                    }
                },
                label = { Text("Đăng tin", style = MaterialTheme.typography.labelSmall) },
                colors = navItemColors(),
            )
            rightTabs.forEach { tab -> TabItem(tab, currentDestination, badgeCount = 0, onTabClick) }
        }
    }
}

@Composable
private fun RowScope.TabItem(
    tab: BottomTab,
    currentDestination: NavDestination?,
    badgeCount: Int,
    onTabClick: (BottomTab) -> Unit,
) {
    val selected = currentDestination?.hierarchy?.any { it.route == tab.routePattern } == true
    NavigationBarItem(
        selected = selected,
        onClick = { if (!selected) onTabClick(tab) },
        icon = {
            BadgedBox(badge = { CountBadge(badgeCount) }) {
                Icon(if (selected) tab.selectedIcon else tab.icon, contentDescription = null)
            }
        },
        label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
        colors = navItemColors(),
    )
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.primary,
    selectedTextColor = MaterialTheme.colorScheme.primary,
    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
)
