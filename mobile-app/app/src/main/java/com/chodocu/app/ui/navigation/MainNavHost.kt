package com.chodocu.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.chodocu.app.data.model.OrderRole
import com.chodocu.app.ui.chat.ChatThreadScreen
import com.chodocu.app.ui.chat.InboxScreen
import com.chodocu.app.ui.home.HomeScreen
import com.chodocu.app.ui.notifications.NotificationsScreen
import com.chodocu.app.ui.orders.OrdersScreen
import com.chodocu.app.ui.post.PostEditorScreen
import com.chodocu.app.ui.product.ProductDetailScreen
import com.chodocu.app.ui.profile.ChangePasswordScreen
import com.chodocu.app.ui.profile.EditProfileScreen
import com.chodocu.app.ui.profile.FavoritesScreen
import com.chodocu.app.ui.profile.MyPostsScreen
import com.chodocu.app.ui.profile.ProfileScreen

/** Bản đồ điều hướng của khung chính: khai báo mọi màn hình và cách chuyển giữa chúng. */
@Composable
fun MainNavHost(
    navController: NavHostController,
    unreadNotifications: Int,
    modifier: Modifier = Modifier,
) {
    // Các hành động điều hướng dùng chung cho nhiều màn hình
    val openProduct: (Long) -> Unit = { id -> navController.navigate(Routes.productDetail(id)) }
    val openChat: (Long, Long?, String) -> Unit = { partnerId, productId, partnerName ->
        navController.navigate(Routes.chatThread(partnerId, productId, partnerName)) { launchSingleTop = true }
    }
    val openOrders: (OrderRole) -> Unit = { role -> navController.openOrdersTab(role) }
    val editProduct: (Long) -> Unit = { id -> navController.navigate(Routes.postEdit(id)) }
    val back: () -> Unit = { navController.popBackStack() }

    NavHost(navController = navController, startDestination = Routes.HOME, modifier = modifier) {
        // ---- 4 tab chính ----
        composable(Routes.HOME) {
            HomeScreen(
                unreadNotifications = unreadNotifications,
                onOpenProduct = openProduct,
                onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
            )
        }
        composable(Routes.INBOX) {
            InboxScreen(onOpenThread = openChat)
        }
        composable(
            route = Routes.ORDERS,
            arguments = listOf(
                navArgument(NavArgs.TAB) {
                    type = NavType.StringType
                    defaultValue = OrderRole.BUYER.name
                },
            ),
        ) {
            OrdersScreen(onOpenProduct = openProduct, onOpenChat = openChat)
        }
        composable(Routes.PROFILE) {
            ProfileScreen(
                onOpenMyPosts = { navController.navigate(Routes.MY_POSTS) },
                onOpenFavorites = { navController.navigate(Routes.FAVORITES) },
                onOpenOrders = openOrders,
                onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                onEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                onChangePassword = { navController.navigate(Routes.CHANGE_PASSWORD) },
            )
        }

        // ---- Sản phẩm & đăng tin ----
        composable(
            route = Routes.PRODUCT_DETAIL,
            arguments = listOf(navArgument(NavArgs.PRODUCT_ID) { type = NavType.LongType }),
        ) {
            ProductDetailScreen(
                onBack = back,
                onOpenChat = { partnerId, productId, partnerName -> openChat(partnerId, productId, partnerName) },
                onEditProduct = editProduct,
                onOpenOrders = openOrders,
            )
        }
        composable(
            route = Routes.POST_NEW,
            arguments = listOf(
                navArgument(NavArgs.PRODUCT_ID) {
                    type = NavType.LongType
                    defaultValue = NavArgs.NO_ID
                },
            ),
        ) {
            PostEditorScreen(
                onClose = back,
                onSubmitted = { _ ->
                    // Đăng xong: thay màn đăng tin bằng "Tin của tôi" để thấy tin đang chờ duyệt
                    navController.navigate(Routes.MY_POSTS) {
                        popUpTo(Routes.POST_NEW) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(
            route = Routes.POST_EDIT,
            arguments = listOf(navArgument(NavArgs.PRODUCT_ID) { type = NavType.LongType }),
        ) {
            PostEditorScreen(
                onClose = back,
                onSubmitted = { _ -> navController.popBackStack() },
            )
        }

        // ---- Chat ----
        composable(
            route = Routes.CHAT_THREAD,
            arguments = listOf(
                navArgument(NavArgs.PARTNER_ID) { type = NavType.LongType },
                navArgument(NavArgs.PRODUCT_ID) {
                    type = NavType.LongType
                    defaultValue = NavArgs.NO_ID
                },
                navArgument(NavArgs.PARTNER_NAME) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            ChatThreadScreen(onBack = back, onOpenProduct = openProduct, onOpenOrders = openOrders)
        }

        // ---- Tài khoản ----
        composable(Routes.MY_POSTS) {
            MyPostsScreen(
                onBack = back,
                onOpenProduct = openProduct,
                onEditProduct = editProduct,
                onCreatePost = { navController.navigate(Routes.POST_NEW) },
            )
        }
        composable(Routes.FAVORITES) {
            FavoritesScreen(onBack = back, onOpenProduct = openProduct)
        }
        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(onBack = back)
        }
        composable(Routes.CHANGE_PASSWORD) {
            ChangePasswordScreen(onBack = back)
        }
        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                onBack = back,
                onOpenProduct = openProduct,
                onOpenMyPosts = { navController.navigate(Routes.MY_POSTS) },
                onOpenOrders = openOrders,
            )
        }
    }
}

/**
 * Mở tab Giao dịch ở đúng tab con (Đơn mua / Đơn bán).
 * Không khôi phục trạng thái cũ để tham số tab mới luôn có hiệu lực.
 */
private fun NavHostController.openOrdersTab(role: OrderRole) {
    navigate(Routes.orders(role)) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = false
    }
}
