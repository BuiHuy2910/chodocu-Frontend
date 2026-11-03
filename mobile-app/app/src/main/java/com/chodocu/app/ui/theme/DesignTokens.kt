package com.chodocu.app.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Token khoảng cách & bán kính dùng thống nhất toàn app.
 * Quy ước 4-based scale, tránh các giá trị tuỳ hứng 14dp / 18dp / 22dp rải rác.
 */
object Space {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 24.dp
    val xxl = 32.dp
    val screen = 16.dp
}

object Radius {
    val chip = 8.dp
    val input = 12.dp
    val card = 16.dp
    val sheet = 20.dp
    val pill = 100.dp
}

object Elevation {
    val flat = 0.dp
    val subtle = 1.dp
    val raised = 3.dp
    val overlay = 8.dp
}
