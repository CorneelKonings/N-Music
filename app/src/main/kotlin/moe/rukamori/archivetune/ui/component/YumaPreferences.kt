/*
 * YumaPlayer (2026) | Modified work by MuwMix
 * ArchiveTune (2026) | Original work by © Rukamori
 * GPL-3.0 License | Contributors: see git history
 */

package moe.rukamori.archivetune.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import moe.rukamori.archivetune.R
import moe.rukamori.archivetune.ui.screens.settings.SettingsDimensions
import moe.rukamori.archivetune.ui.theme.LocalYumaColors
import moe.rukamori.archivetune.ui.theme.yumaClickable
import moe.rukamori.archivetune.ui.theme.yumaGlassCard

private val localFont = FontFamily(
    Font(R.font.google_sans_regular, FontWeight.Normal),
    Font(R.font.google_sans_bold, FontWeight.Bold)
)

/**
 * Стеклянный контейнер категории настроек YumaPlayer.
 */
@Composable
fun YumaPreferenceCategory(
    title: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalYumaColors.current
    Column(modifier = modifier.fillMaxWidth()) {
        if (title != null) {
            Text(
                text = title,
                style = TextStyle(
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = localFont
                ),
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp, top = 8.dp)
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

/**
 * Строка настроек YumaPlayer — плашка с 16dp отступами и упругим сжатием ВСЕЙ КАРТОЧКИ целиком.
 */
/**
 * Строка настроек YumaPlayer — 1 в 1 как SettingsMenuRow из шторки плеера.
 * Мгновенный отклик клика, темная плашка Color(0x0DFFFFFF), без 1dp граней.
 */
@Composable
fun YumaPreferenceRow(
    title: String,
    subtitle: String? = null,
    @DrawableRes iconResId: Int? = null,
    onClick: (() -> Unit)? = null,
    showArrow: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && onClick != null) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "SettingsRowScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(Color(0x1AFFFFFF), Color(0x08FFFFFF))))
            .then(
                if (onClick != null && enabled) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .padding(
                horizontal = SettingsDimensions.RowHorizontalPadding,
                vertical = SettingsDimensions.RowVerticalPadding
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (iconResId != null) {
                Icon(
                    painter = painterResource(iconResId),
                    contentDescription = title,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 12.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = localFont,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontFamily = localFont,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (showArrow) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_right),
                    contentDescription = "Go",
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Строка настроек YumaPlayer со Switch — 1 в 1 как в шторке плеера.
 */
@Composable
fun YumaSwitchPreferenceRow(
    title: String,
    subtitle: String? = null,
    @DrawableRes iconResId: Int? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "SettingsSwitchRowScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(SettingsDimensions.GroupCardCornerRadius))
            .background(Brush.verticalGradient(listOf(Color(0x1AFFFFFF), Color(0x08FFFFFF))))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(
                horizontal = SettingsDimensions.RowHorizontalPadding,
                vertical = SettingsDimensions.RowVerticalPadding
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (iconResId != null) {
                Icon(
                    painter = painterResource(iconResId),
                    contentDescription = title,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 12.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = localFont,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontFamily = localFont,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color.White.copy(alpha = 0.3f),
                    uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                    uncheckedTrackColor = Color(0x0DFFFFFF),
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
    }
}
