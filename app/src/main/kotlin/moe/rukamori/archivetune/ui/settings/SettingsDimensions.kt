/*
 * YumaPlayer (2026) | Modified work by MuwMix
 * ArchiveTune (2026) | Original work by © Rukamori
 * GPL-3.0 License | Contributors: see git history
 */

package moe.rukamori.archivetune.ui.settings

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import moe.rukamori.archivetune.LocalAnimationsDisabled

object SettingsDimensions {
    val GroupCardCornerRadius = 12.dp
    val BannerCardCornerRadius = 14.dp

    val ScreenHorizontalPadding = 16.dp
    val ScreenBottomPadding = 24.dp
    val SectionSpacing = 10.dp
    val RowVerticalPadding = 8.dp
    val RowHorizontalPadding = 14.dp

    val RowIconSize = 28.dp
    val RowIconInnerSize = 18.dp
    val BannerIconSize = 44.dp
    val BannerIconInnerSize = 22.dp
    val ChevronSize = 18.dp

    val ProfileCardAvatarSize = 56.dp
    val ProfileCardAvatarIconSize = 26.dp

    val DividerThickness = 0.5.dp
    val DividerStartIndent = 60.dp

    val SectionHeaderBottomPadding = 6.dp
    val SectionHeaderHorizontalPadding = 20.dp

    val SegmentedGroupHorizontalPadding = 16.dp
    val SegmentedItemGap = 2.dp
}

object SettingsAnimations {
    val PressScale = 0.97f

    @Composable
    fun <T> pressSpring(): FiniteAnimationSpec<T> =
        if (LocalAnimationsDisabled.current) {
            snap()
        } else {
            spring(stiffness = Spring.StiffnessHigh)
        }
}
