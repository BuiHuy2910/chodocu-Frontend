package com.chodocu.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chodocu.app.R
import com.chodocu.app.ui.theme.BrandGreen
import com.chodocu.app.ui.theme.Space

/** Tên app và khẩu hiệu dùng thống nhất trên mọi màn hình. */
const val APP_NAME = "Chợ Đồ Cũ"
const val APP_TAGLINE = "Cũ người mới ta"

/** Biểu tượng logo (túi mua sắm + mũi tên tái chế + kính lúp) trên nền xanh bo tròn. */
@Composable
fun AppLogoMark(size: Dp = 56.dp, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.ic_logo_mark),
        contentDescription = "Logo $APP_NAME",
        modifier = modifier.size(size),
    )
}

/** Khối thương hiệu đầy đủ cho màn đăng nhập / đăng ký: logo, tên app, khẩu hiệu. */
@Composable
fun AppBrandHeader(modifier: Modifier = Modifier, markSize: Dp = 88.dp) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        AppLogoMark(size = markSize)
        Spacer(Modifier.height(Space.sm))
        Text(
            text = APP_NAME.uppercase(),
            style = MaterialTheme.typography.headlineSmall.copy(letterSpacing = 1.sp),
            color = BrandGreen,
            textAlign = TextAlign.Center,
        )
        Text(
            text = APP_TAGLINE,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/** Logo nhỏ kèm tên app, dùng ở thanh trên cùng của trang chủ. */
@Composable
fun AppWordmark(modifier: Modifier = Modifier, markSize: Dp = 32.dp) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.xs),
    ) {
        AppLogoMark(size = markSize)
        Text(
            text = APP_NAME,
            style = MaterialTheme.typography.titleLarge,
            color = BrandGreen,
        )
    }
}
