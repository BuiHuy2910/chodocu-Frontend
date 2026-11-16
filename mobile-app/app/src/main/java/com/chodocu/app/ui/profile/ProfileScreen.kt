package com.chodocu.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chodocu.app.BuildConfig
import com.chodocu.app.data.model.OrderRole
import com.chodocu.app.data.model.User
import com.chodocu.app.di.appViewModel
import com.chodocu.app.ui.components.ActionSheet
import com.chodocu.app.ui.components.AppTopBar
import com.chodocu.app.ui.components.AppWordmark
import com.chodocu.app.ui.components.ConfirmDialog
import com.chodocu.app.ui.components.FullScreenImageViewer
import com.chodocu.app.ui.components.RefreshOnResume
import com.chodocu.app.ui.components.SecondaryButton
import com.chodocu.app.ui.components.SectionCard
import com.chodocu.app.ui.components.SheetAction
import com.chodocu.app.ui.components.UserAvatar
import com.chodocu.app.ui.components.hairlineBorder
import com.chodocu.app.ui.components.rememberSingleImagePicker
import com.chodocu.app.ui.theme.AppColors
import com.chodocu.app.ui.theme.Radius
import com.chodocu.app.ui.theme.Sizes
import com.chodocu.app.ui.theme.Space
import java.util.Locale

/** Tab "Tài khoản": hồ sơ, lối tắt tới tin đăng / đơn hàng, cài đặt tài khoản và đăng xuất. */
@Composable
fun ProfileScreen(
    onOpenMyPosts: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenOrders: (OrderRole) -> Unit,
    onOpenNotifications: () -> Unit,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
) {
    val viewModel = appViewModel { _ -> ProfileViewModel(auth, products, uploads, session) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnResume { viewModel.refresh() }

    val pickAvatar = rememberSingleImagePicker(onPicked = viewModel::changeAvatar)
    val snackbarHostState = remember { SnackbarHostState() }
    var showAvatarSheet by remember { mutableStateOf(false) }
    var showAvatarViewer by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.userMessageShown()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AppTopBar(title = "Tài khoản") },
    ) { padding ->
        val user = state.user ?: return@Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .verticalScroll(rememberScrollState())
                .padding(Space.screen),
            verticalArrangement = Arrangement.spacedBy(Space.md),
        ) {
            ProfileHeader(
                user = user,
                isAvatarBusy = state.isAvatarBusy,
                onAvatarClick = { showAvatarSheet = true },
            )
            ProfileStats(user = user, sellingCount = state.sellingCount)
            MenuSection(title = "Mua bán") {
                MenuRow(Icons.Outlined.Inventory, "Tin đăng của tôi", "Đang bán, chờ duyệt, đã bán", onClick = onOpenMyPosts)
                MenuRow(Icons.Outlined.FavoriteBorder, "Món đồ đã lưu", null, onClick = onOpenFavorites)
                MenuRow(Icons.Outlined.ShoppingBag, "Đơn mua", "Yêu cầu mua bạn đã gửi") { onOpenOrders(OrderRole.BUYER) }
                MenuRow(
                    icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                    title = "Đơn bán",
                    subtitle = "Người khác muốn mua đồ của bạn",
                    showDivider = false,
                ) {
                    onOpenOrders(OrderRole.SELLER)
                }
            }
            MenuSection(title = "Tài khoản") {
                MenuRow(Icons.Outlined.Notifications, "Thông báo", null, onClick = onOpenNotifications)
                MenuRow(Icons.Outlined.Person, "Sửa hồ sơ", "Họ tên, số điện thoại, trường, giới thiệu", onClick = onEditProfile)
                MenuRow(Icons.Outlined.Lock, "Đổi mật khẩu", null, showDivider = false, onClick = onChangePassword)
            }
            SecondaryButton(
                text = "Đăng xuất",
                onClick = { showLogoutDialog = true },
                icon = Icons.AutoMirrored.Outlined.Logout,
                loading = state.isLoggingOut,
                contentColor = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth(),
            )
            AppFooter()
        }
    }

    if (showAvatarSheet) {
        val hasAvatar = !state.user?.avatarUrl.isNullOrBlank()
        ActionSheet(
            title = "Ảnh đại diện",
            actions = listOfNotNull(
                SheetAction(icon = Icons.Outlined.PhotoLibrary, label = "Chọn ảnh mới", onClick = pickAvatar),
                if (hasAvatar) {
                    SheetAction(icon = Icons.Outlined.Visibility, label = "Xem ảnh đại diện", onClick = { showAvatarViewer = true })
                } else {
                    null
                },
                if (hasAvatar) {
                    SheetAction(
                        icon = Icons.Outlined.DeleteOutline,
                        label = "Gỡ ảnh đại diện",
                        description = "Hiện chữ cái đầu của tên thay cho ảnh",
                        destructive = true,
                        onClick = viewModel::removeAvatar,
                    )
                } else {
                    null
                },
            ),
            onDismiss = { showAvatarSheet = false },
        )
    }
    val avatarUrl = state.user?.avatarUrl
    if (showAvatarViewer && !avatarUrl.isNullOrBlank()) {
        FullScreenImageViewer(urls = listOf(avatarUrl), initialPage = 0, onDismiss = { showAvatarViewer = false })
    }
    if (showLogoutDialog) {
        ConfirmDialog(
            title = "Đăng xuất?",
            message = "Bạn sẽ cần đăng nhập lại để mua bán và nhắn tin.",
            confirmText = "Đăng xuất",
            destructive = true,
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout()
            },
            onDismiss = { showLogoutDialog = false },
        )
    }
}

/** Ảnh đại diện (bấm để đổi), họ tên, email, trường, nơi ở và lời giới thiệu. */
@Composable
private fun ProfileHeader(user: User, isAvatarBusy: Boolean, onAvatarClick: () -> Unit) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.md)) {
            Box(modifier = Modifier.clip(CircleShape).clickable(enabled = !isAvatarBusy, onClick = onAvatarClick)) {
                UserAvatar(name = user.fullName, imageUrl = user.avatarUrl, size = Sizes.avatarLarge)
                if (isAvatarBusy) {
                    Box(
                        modifier = Modifier.size(Sizes.avatarLarge).background(AppColors.ImageScrim),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                    }
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = user.fullName, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                user.universityName?.takeIf { it.isNotBlank() }?.let { IconText(icon = Icons.Outlined.School, text = it) }
                user.dormArea?.takeIf { it.isNotBlank() }?.let { IconText(icon = Icons.Outlined.Home, text = it) }
            }
        }
        // Nút máy ảnh nhỏ giúp người dùng biết có thể bấm vào ảnh để đổi
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(Radius.pill))
                .clickable(enabled = !isAvatarBusy, onClick = onAvatarClick)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(Radius.pill))
                .padding(horizontal = Space.sm, vertical = Space.xxs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.xxs),
        ) {
            Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(Sizes.iconSmall))
            Text(
                text = if (user.avatarUrl.isNullOrBlank()) "Thêm ảnh đại diện" else "Đổi ảnh đại diện",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        user.bio?.takeIf { it.isNotBlank() }?.let { bio ->
            Text(text = bio, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun IconText(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.xxs)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(Sizes.iconSmall))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Ba con số nhanh: giao dịch thành công, đánh giá, tin đang bán. */
@Composable
private fun ProfileStats(user: User, sellingCount: Int?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.card))
            .background(MaterialTheme.colorScheme.surface)
            .hairlineBorder(MaterialTheme.colorScheme.outlineVariant, Radius.card)
            .padding(vertical = Space.sm),
    ) {
        StatCell(value = user.completedDeals.toString(), label = "Giao dịch xong", modifier = Modifier.weight(1f))
        StatCell(
            value = user.rating?.let { String.format(Locale.forLanguageTag("vi-VN"), "%.1f", it) } ?: "—",
            label = "Đánh giá",
            modifier = Modifier.weight(1f),
        )
        StatCell(value = sellingCount?.toString() ?: "—", label = "Tin đang bán", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCell(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Nhóm các dòng menu trong một thẻ, có tiêu đề nhỏ phía trên. */
@Composable
private fun MenuSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = Space.xxs),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.card))
                .background(MaterialTheme.colorScheme.surface)
                .hairlineBorder(MaterialTheme.colorScheme.outlineVariant, Radius.card),
            content = content,
        )
    }
}

@Composable
private fun MenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    showDivider: Boolean = true,
    onClick: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = Space.md, vertical = Space.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.sm),
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(Sizes.iconLarge))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (showDivider) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(start = Space.md + Sizes.iconLarge + Space.sm),
            )
        }
    }
}

/** Logo + phiên bản app ở cuối trang. */
@Composable
private fun AppFooter() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = Space.sm, bottom = Space.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Space.xxs),
    ) {
        AppWordmark(markSize = 24.dp)
        Text(
            text = "Phiên bản ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
