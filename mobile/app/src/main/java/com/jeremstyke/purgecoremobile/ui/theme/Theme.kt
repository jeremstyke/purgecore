package com.jeremstyke.purgecoremobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme

// Same accent palette as PurgeCore Windows and the website, kept consistent
// across every surface of the product.
val Accent = Color(0xFF2563EB)
val AccentDeep = Color(0xFF1D3A8A)
val Teal = Color(0xFF0D9488)
val Violet = Color(0xFF7C3AED)
val Amber = Color(0xFFD97706)
val Rose = Color(0xFFE11D48)

// Same blue -> violet -> teal diagonal gradient as the Windows app's
// sidebar, reused here for the top bar so the two products read as one
// family at a glance.
val BrandGradient = Brush.linearGradient(colors = listOf(Accent, Violet, Teal))

private val LightColors = lightColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    secondary = Teal,
    background = Color(0xFFF8FAFC),
    surface = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF0B1220),
    secondary = Color(0xFF2DD4BF),
    background = Color(0xFF0B1220),
    surface = Color(0xFF111827),
)

@Composable
fun PurgeCoreMobileTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
