package com.chodocu.app.ui.theme

import androidx.compose.ui.unit.dp

/** Khoảng cách chuẩn — dùng thay cho số dp rải rác trong code. */
object Space {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 24.dp
    val xxl = 32.dp

    /** Lề ngang của nội dung màn hình. */
    val screen = 16.dp
}

/** Bo góc chuẩn: chip 8, ô nhập / nút 12, thẻ 16, bottom sheet 20. */
object Radius {
    val chip = 8.dp
    val input = 12.dp
    val button = 12.dp
    val card = 16.dp
    val sheet = 20.dp
    val pill = 100.dp
}

/** Kích thước dùng lại nhiều lần. */
object Sizes {
    val buttonHeight = 50.dp
    val inputMinHeight = 52.dp
    val iconSmall = 16.dp
    val icon = 20.dp
    val iconLarge = 24.dp
    val avatarSmall = 32.dp
    val avatar = 40.dp
    val avatarLarge = 72.dp
    val thumb = 56.dp
    val thumbLarge = 72.dp
    val border = 1.dp
}
