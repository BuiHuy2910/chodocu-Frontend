package com.chodocu.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Bảng màu sáng của app (app chỉ dùng giao diện sáng cho đồng nhất với nhận diện thương hiệu). */
private val LightColors = lightColorScheme(
    primary = BrandGreen,
    onPrimary = Color.White,
    primaryContainer = GreenContainer,
    onPrimaryContainer = OnGreenContainer,
    inversePrimary = Color(0xFF8FD6A6),
    secondary = BrandOrange,
    onSecondary = Color.White,
    secondaryContainer = OrangeContainer,
    onSecondaryContainer = OnOrangeContainer,
    tertiary = BrandLeaf,
    onTertiary = Color(0xFF1C3307),
    tertiaryContainer = LeafContainer,
    onTertiaryContainer = OnLeafContainer,
    background = Background,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = TextSecondary,
    surfaceTint = Color.Transparent,
    inverseSurface = Color(0xFF1D2939),
    inverseOnSurface = Color(0xFFF2F4F7),
    error = DangerText,
    onError = Color.White,
    errorContainer = DangerBg,
    onErrorContainer = Color(0xFF7A271A),
    outline = Outline,
    outlineVariant = Divider,
    scrim = Color.Black,
    surfaceBright = SurfaceWhite,
    surfaceDim = Color(0xFFE4E7EC),
    surfaceContainerLowest = SurfaceWhite,
    surfaceContainerLow = SurfaceWhite,
    surfaceContainer = SurfaceWhite,
    surfaceContainerHigh = SurfaceWhite,
    surfaceContainerHighest = SurfaceMuted,
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(Radius.chip),
    small = RoundedCornerShape(Radius.chip),
    medium = RoundedCornerShape(Radius.input),
    large = RoundedCornerShape(Radius.card),
    extraLarge = RoundedCornerShape(Radius.sheet),
)

/** Theme gốc của app — bọc toàn bộ giao diện trong MainActivity. */
@Composable
fun ChoDoCuTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
