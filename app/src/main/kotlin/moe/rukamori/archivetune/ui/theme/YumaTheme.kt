/*
 * YumaPlayer (2026) | Modified work by MuwMix
 * ArchiveTune (2026) | Original work by © Rukamori
 * GPL-3.0 License | Contributors: see git history
 */

package moe.rukamori.archivetune.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
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
    CompositionLocalProvider(
        LocalYumaColors provides colors,
        content = content
    )
}
