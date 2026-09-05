package moe.rukamori.archivetune.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Nothing Design System Core Tokens.
 * Follows the industrial, monochrome, high-contrast, functional Nothing OS aesthetic.
 */
object NothingTokens {
    // Colors
    val PureBlack = Color(0xFF000000)
    val PureWhite = Color(0xFFFFFFFF)
    val WindowGrey = Color(0xFFB1B3B3)
    val NGrey = Color(0xFFDCD7D2)
    val DarkSurface = Color(0xFF0D0D0D)
    val DarkSurfaceElevated = Color(0xFF141414)
    val DarkSurfaceCard = Color(0xFF181818)
    val DarkBorder = Color(0xFF262626)
    val DarkBorderSubtle = Color(0xFF1C1C1C)
    val DarkDivider = Color(0xFF222222)

    // Accents
    val NYellow = Color(0xFFFFC700)
    val NRed = Color(0xFFD71921)

    // Text Tokens
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFB1B3B3)
    val TextTertiary = Color(0xFF707070)
    val TextDisabled = Color(0xFF484848)

    // Shapes
    val ShapeSquare = RoundedCornerShape(0.dp)
    val ShapeSmall = RoundedCornerShape(4.dp)
    val ShapeMedium = RoundedCornerShape(8.dp)
    val ShapeLarge = RoundedCornerShape(12.dp)
    val ShapeCard = RoundedCornerShape(16.dp)
    val ShapePill = RoundedCornerShape(50)

    // Spacing
    val Space2 = 2.dp
    val Space4 = 4.dp
    val Space8 = 8.dp
    val Space12 = 12.dp
    val Space16 = 16.dp
    val Space20 = 20.dp
    val Space24 = 24.dp
    val Space32 = 32.dp
    val Space48 = 48.dp
}

@Immutable
data class NothingColorScheme(
    val background: Color = NothingTokens.PureBlack,
    val surface: Color = NothingTokens.DarkSurface,
    val surfaceElevated: Color = NothingTokens.DarkSurfaceElevated,
    val surfaceCard: Color = NothingTokens.DarkSurfaceCard,
    val border: Color = NothingTokens.DarkBorder,
    val borderSubtle: Color = NothingTokens.DarkBorderSubtle,
    val divider: Color = NothingTokens.DarkDivider,
    val accent: Color = NothingTokens.NRed,
    val accentRed: Color = NothingTokens.NRed,
    val textPrimary: Color = NothingTokens.TextPrimary,
    val textSecondary: Color = NothingTokens.TextSecondary,
    val textTertiary: Color = NothingTokens.TextTertiary,
    val textDisabled: Color = NothingTokens.TextDisabled,
)

val LocalNothingColors = staticCompositionLocalOf { NothingColorScheme() }

object NothingTheme {
    val colors: NothingColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalNothingColors.current

    val typography = NothingTypography
    val shapes = NothingTokens
}

object NothingTypography {
    val Headline = TextStyle(
        fontFamily = NTypeHeadlineFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.5).sp,
        color = NothingTokens.TextPrimary
    )

    val SectionHeader = TextStyle(
        fontFamily = NDotFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.2.sp,
        color = NothingTokens.TextSecondary
    )

    val Title = TextStyle(
        fontFamily = NTypeHeadlineFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.2).sp,
        color = NothingTokens.TextPrimary
    )

    val Body = TextStyle(
        fontFamily = NTypeRegularFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        color = NothingTokens.TextSecondary
    )

    val Mono = TextStyle(
        fontFamily = NDotFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = NothingTokens.TextSecondary
    )

    val MonoLarge = TextStyle(
        fontFamily = NDotFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 1.sp,
        color = NothingTokens.TextPrimary
    )

    val Label = TextStyle(
        fontFamily = NDotFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.8.sp,
        color = NothingTokens.TextTertiary
    )
}

/**
 * Modifier extension for industrial Nothing card styling.
 */
fun Modifier.nothingCard(
    backgroundColor: Color = NothingTokens.DarkSurfaceCard,
    borderColor: Color = NothingTokens.DarkBorder,
    borderWidth: Dp = 1.dp,
    shape: Shape = NothingTokens.ShapeLarge
): Modifier = this
    .clip(shape)
    .background(backgroundColor, shape)
    .border(borderWidth, borderColor, shape)

/**
 * Modifier extension for industrial Nothing pill button / chip styling.
 */
fun Modifier.nothingPill(
    backgroundColor: Color = NothingTokens.DarkSurfaceElevated,
    borderColor: Color = NothingTokens.DarkBorder,
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(NothingTokens.ShapePill)
    .background(backgroundColor, NothingTokens.ShapePill)
    .border(borderWidth, borderColor, NothingTokens.ShapePill)

/**
 * Modifier extension for micro-dot background matrix pattern.
 */
fun Modifier.nothingDotGrid(
    dotColor: Color = Color(0xFF222222),
    dotRadius: Float = 1.2f,
    spacing: Float = 24f
): Modifier = this.drawBehind {
    val width = size.width
    val height = size.height
    var x = spacing / 2f
    while (x < width) {
        var y = spacing / 2f
        while (y < height) {
            drawCircle(
                color = dotColor,
                radius = dotRadius,
                center = Offset(x, y)
            )
            y += spacing
        }
        x += spacing
    }
}
