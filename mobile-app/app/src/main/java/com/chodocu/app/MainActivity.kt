package com.chodocu.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chodocu.app.data.model.PopupType
import com.chodocu.app.ui.components.PassUniAnimatedPopup
import com.chodocu.app.ui.screens.*
import com.chodocu.app.ui.theme.EmeraldPrimary
import com.chodocu.app.ui.theme.PassUniTheme
import com.chodocu.app.viewmodel.MainViewModel

enum class AppScreen {
    AUTH, HOME, DETAIL, CREATE_POST, CHAT, PROFILE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PassUniTheme {
                PassUniMainApp()
            }
        }
    }
}

@Composable
fun PassUniMainApp(viewModel: MainViewModel = viewModel()) {
    val currentUser by viewModel.currentUser.collectAsState()
    
    // BẮT BUỘC: Vào app là phải ở màn hình ĐĂNG NHẬP / ĐĂNG KÝ trước
    var currentScreen by remember {
        mutableStateOf(if (viewModel.currentUser.value == null) AppScreen.AUTH else AppScreen.HOME)
    }
    
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val popupState by viewModel.popupState.collectAsState()

    // Khi người dùng đăng xuất thì chuyển ngay về AUTH
    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            currentScreen = AppScreen.AUTH
        }
    }

    // Global Animated Popup Notification
    PassUniAnimatedPopup(
        state = popupState,
        onDismiss = { viewModel.dismissPopup() }
    )

    Scaffold(
        bottomBar = {
            // Chỉ hiển thị bottom bar khi đã vào các màn bên trong (không hiện ở màn Auth và Detail)
            if (currentScreen != AppScreen.AUTH && currentScreen != AppScreen.DETAIL) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = NavigationBarDefaults.Elevation
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.HOME,
                        onClick = { currentScreen = AppScreen.HOME },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Khám phá", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldPrimary,
                            selectedTextColor = EmeraldPrimary,
                            indicatorColor = Color(0xFFECFDF5)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.CREATE_POST,
                        onClick = {
                            if (currentUser == null) {
                                viewModel.showPopup(
                                    title = "Yêu cầu đăng nhập",
                                    message = "Bạn cần đăng nhập bằng Email sinh viên (.edu.vn) để đăng tin bán đồ!",
                                    type = PopupType.WARNING,
                                    confirmText = "Đến trang Đăng nhập",
                                    onConfirm = { currentScreen = AppScreen.AUTH }
                                )
                            } else {
                                currentScreen = AppScreen.CREATE_POST
                            }
                        },
                        icon = { Icon(Icons.Default.AddCircle, contentDescription = null) },
                        label = { Text("Đăng tin", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldPrimary,
                            selectedTextColor = EmeraldPrimary,
                            indicatorColor = Color(0xFFECFDF5)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.CHAT,
                        onClick = {
                            if (currentUser == null) {
                                viewModel.showPopup(
                                    title = "Yêu cầu đăng nhập",
                                    message = "Vui lòng đăng nhập để mở hộp thoại chat giao dịch với sinh viên khác!",
                                    type = PopupType.WARNING,
                                    confirmText = "Đến trang Đăng nhập",
                                    onConfirm = { currentScreen = AppScreen.AUTH }
                                )
                            } else {
                                currentScreen = AppScreen.CHAT
                            }
                        },
                        icon = { Icon(Icons.Default.ChatBubble, contentDescription = null) },
                        label = { Text("Tin nhắn", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldPrimary,
                            selectedTextColor = EmeraldPrimary,
                            indicatorColor = Color(0xFFECFDF5)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.PROFILE,
                        onClick = {
                            if (currentUser == null) {
                                viewModel.showPopup(
                                    title = "Yêu cầu đăng nhập",
                                    message = "Đăng nhập bằng Email sinh viên để xem Thẻ sinh viên, Tin đã đăng và Wishlist!",
                                    type = PopupType.WARNING,
                                    confirmText = "Đến trang Đăng nhập",
                                    onConfirm = { currentScreen = AppScreen.AUTH }
                                )
                            } else {
                                currentScreen = AppScreen.PROFILE
                            }
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Cá nhân", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldPrimary,
                            selectedTextColor = EmeraldPrimary,
                            indicatorColor = Color(0xFFECFDF5)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                },
                label = "screenTransition"
            ) { screen ->
                when (screen) {
                    AppScreen.AUTH -> AuthScreen(
                        viewModel = viewModel,
                        onLoginSuccess = { currentScreen = AppScreen.HOME },
                        onExploreAsGuest = { currentScreen = AppScreen.HOME }
                    )
                    AppScreen.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onProductClick = { prod ->
                            viewModel.selectProduct(prod)
                            currentScreen = AppScreen.DETAIL
                        },
                        onOpenAuth = { currentScreen = AppScreen.AUTH }
                    )
                    AppScreen.DETAIL -> {
                        val prod = selectedProduct ?: MainViewModel.defaultProducts().first()
                        ProductDetailScreen(
                            product = prod,
                            viewModel = viewModel,
                            onBack = { currentScreen = AppScreen.HOME },
                            onOpenChat = { currentScreen = AppScreen.CHAT },
                            onRequireAuth = { currentScreen = AppScreen.AUTH }
                        )
                    }
                    AppScreen.CREATE_POST -> CreatePostScreen(
                        viewModel = viewModel,
                        onPostSuccess = { currentScreen = AppScreen.HOME }
                    )
                    AppScreen.CHAT -> ChatScreen(viewModel = viewModel)
                    AppScreen.PROFILE -> ProfileScreen(
                        viewModel = viewModel,
                        onLogout = {
                            viewModel.logout {
                                currentScreen = AppScreen.AUTH
                            }
                        }
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "PassUni - Chợ Đồ Cũ Sinh Viên (Preview)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun PassUniAppPreview() {
    PassUniTheme {
        PassUniMainApp()
    }
}
