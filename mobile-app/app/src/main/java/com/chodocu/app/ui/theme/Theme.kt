package com.chodocu.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =============================================================================
// BẢNG MÀU
// Nền trung tính + 1 màu thương hiệu (emerald). Các màu phụ chỉ dùng cho
// trạng thái ngữ nghĩa, không dùng làm nền lớn hay gradient trang trí.
// =============================================================================

val EmeraldPrimary = Color(0xFF0B6B4A)
val EmeraldContainer = Color(0xFF14805A)
val EmeraldLight = Color(0xFFEAF4EF)
val EmeraldPressed = Color(0xFF07523A)

val CoralSecondary = Color(0xFFB4530A)
val CoralDark = Color(0xFF7C3A06)
val CoralLight = Color(0xFFFDF3E8)

val CampusBlue = Color(0xFF0D5C8C)
val CampusBlueLight = Color(0xFFEAF2F9)
val CampusBluePressed = Color(0xFF084566)

val SurfaceBg = Color(0xFFF6F7F8)
val SurfaceSunken = Color(0xFFEFF1F3)
val CardWhite = Color(0xFFFFFFFF)

val SlateNavy = Color(0xFF12181F)
val SlateMuted = Color(0xFF667085)
val SlateFaint = Color(0xFF98A2B3)
val DividerLine = Color(0xFFE4E7EC)

val DangerRed = Color(0xFFB42318)
val DangerLight = Color(0xFFFEF3F2)
val SuccessGreen = Color(0xFF067647)
val SuccessLight = Color(0xFFECFDF3)
val AmberGold = Color(0xFFB54708)
val AmberLight = Color(0xFFFEF6E7)

private val PassUniColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldLight,
    onPrimaryContainer = EmeraldPressed,

    secondary = SlateNavy,
    onSecondary = Color.White,
    secondaryContainer = SurfaceSunken,
    onSecondaryContainer = SlateNavy,

    tertiary = CampusBlue,
    onTertiary = Color.White,
    tertiaryContainer = CampusBlueLight,
    onTertiaryContainer = CampusBluePressed,

    background = SurfaceBg,
    onBackground = SlateNavy,
    surface = CardWhite,
    onSurface = SlateNavy,
    surfaceVariant = SurfaceSunken,
    onSurfaceVariant = SlateMuted,

    surfaceContainerLowest = CardWhite,
    surfaceContainerLow = SurfaceBg,
    surfaceContainer = SurfaceSunken,

    outline = DividerLine,
    outlineVariant = Color(0xFFF0F2F4),

    error = DangerRed,
    onError = Color.White,
    errorContainer = DangerLight,
    onErrorContainer = DangerRed
)

// =============================================================================
// KIỂU CHỮ — phân cấp rõ ràng, không lạm dụng ExtraBold
// =============================================================================
private val AppFont = FontFamily.Default

val PassUniTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = AppFont, fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = (-0.3).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = AppFont, fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.2).sp
    ),
    titleLarge = TextStyle(
        fontFamily = AppFont, fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp, lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = AppFont, fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp, lineHeight = 21.sp
    ),
    titleSmall = TextStyle(
        fontFamily = AppFont, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = AppFont, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AppFont, fontWeight = FontWeight.Normal,
        fontSize = 13.sp, lineHeight = 19.sp
    ),
    bodySmall = TextStyle(
        fontFamily = AppFont, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 17.sp
    ),
    labelLarge = TextStyle(
        fontFamily = AppFont, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = AppFont, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = AppFont, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.2.sp
    )
)

// Bo góc nhỏ, nhất quán (6 / 8 / 12 / 16 / 20)
val PassUniShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

@Composable
fun PassUniTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PassUniColorScheme,
        typography = PassUniTypography,
        shapes = PassUniShapes,
        content = content
    )
}
