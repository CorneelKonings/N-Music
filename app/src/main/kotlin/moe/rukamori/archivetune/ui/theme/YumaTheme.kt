package moe.rukamori.archivetune.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class YumaColorScheme(
    val glassBackground: Color = Color(0x12FFFFFF),
    val glassBorder: Color = Color(0x1FFFFFFF),
    val cardBackgroundOpaque: Color = Color(0xFF1C1C1E),
    val textPrimary: Color = Color.White,
    val textSecondary: Color = Color(0x80FFFFFF),
    val rippleColor: Color = Color(0x1FFFFFFF)
)

val LocalYumaColors = staticCompositionLocalOf {
    YumaColorScheme()
}

@Composable
fun YumaTheme(
    colors: YumaColorScheme = LocalYumaColors.current,
    content: @Composable () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()
    val onSurface = MaterialTheme.colorScheme.onSurface
    val outline = MaterialTheme.colorScheme.outline

    val computedGlassBackground = remember(darkTheme, onSurface) {
        if (darkTheme) onSurface.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.65f)
    }
    val computedGlassBorder = remember(darkTheme, outline) {
        if (darkTheme) outline.copy(alpha = 0.10f) else outline.copy(alpha = 0.25f)
    }
    val yumaColors = remember(computedGlassBackground, computedGlassBorder, onSurface) {
        YumaColorScheme(
            glassBackground = computedGlassBackground,
            glassBorder = computedGlassBorder,
            cardBackgroundOpaque = Color(0xFF1C1C1E),
            textPrimary = onSurface,
            textSecondary = onSurface.copy(alpha = 0.65f),
            rippleColor = Color(0x1FFFFFFF)
        )
    }

    CompositionLocalProvider(
        LocalYumaColors provides yumaColors,
        content = content
    )
}
