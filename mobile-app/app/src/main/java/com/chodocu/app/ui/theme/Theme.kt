package com.chodocu.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val EmeraldPrimary = Color(0xFF006948)
val EmeraldContainer = Color(0xFF00855D)
val EmeraldLight = Color(0xFFECFDF5)
val CoralSecondary = Color(0xFFFD761A)
val CoralDark = Color(0xFF9D4300)
val CampusBlue = Color(0xFF006194)
val CampusBlueLight = Color(0xFFE0F2FE)
val SurfaceBg = Color(0xFFFAF8FF)
val CardWhite = Color(0xFFFFFFFF)
val SlateNavy = Color(0xFF131B2E)
val SlateMuted = Color(0xFF64748B)

private val PassUniColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldLight,
    onPrimaryContainer = EmeraldPrimary,
    secondary = CoralSecondary,
    onSecondary = Color.White,
    tertiary = CampusBlue,
    onTertiary = Color.White,
    background = SurfaceBg,
    onBackground = SlateNavy,
    surface = CardWhite,
    onSurface = SlateNavy,
    surfaceVariant = Color(0xFFF2F3FF),
    onSurfaceVariant = Color(0xFF3D4A42),
    error = Color(0xFFBA1A1A)
)

@Composable
fun PassUniTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PassUniColorScheme,
        content = content
    )
}

