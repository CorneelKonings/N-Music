package moe.rukamori.archivetune.home.effects

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import moe.rukamori.archivetune.constants.HomeBackgroundStyle

data class HomeBackgroundSettings(
    val style: HomeBackgroundStyle = HomeBackgroundStyle.TONAL,
    val parallaxEnabled: Boolean = true,
    val parallaxSensitivity: Float = 0.6f,
    val brightness: Float = 1f,
)

val LocalHomeBackgroundStyle = compositionLocalOf { HomeBackgroundSettings() }

@Composable
fun ScreenBackground(modifier: Modifier = Modifier, isVisible: Boolean = true) {
    val homeBackground = LocalHomeBackgroundStyle.current
    var isBooted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(1000)
        isBooted = true
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .graphicsLayer {
                compositingStrategy = if (isBooted) CompositingStrategy.Offscreen else CompositingStrategy.Auto
                alpha = if (isVisible) 1f else 0f
            }
    ) {
        when (homeBackground.style) {
            HomeBackgroundStyle.TONAL -> {
                HomePremiumBackground(
                    blobColor = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(430.dp)
                        .align(Alignment.TopCenter)
                )
            }
            HomeBackgroundStyle.CIRCLES -> {
                CirclesBackground(
                    parallaxEnabled = homeBackground.parallaxEnabled && isBooted,
                    parallaxSensitivity = homeBackground.parallaxSensitivity,
                    brightness = homeBackground.brightness,
                )
            }
            HomeBackgroundStyle.RINGS -> {
                RingsBackground(
                    parallaxEnabled = homeBackground.parallaxEnabled && isBooted,
                    parallaxSensitivity = homeBackground.parallaxSensitivity,
                    brightness = homeBackground.brightness,
                )
            }
            HomeBackgroundStyle.MESH -> {
                MeshBackground(
                    parallaxEnabled = homeBackground.parallaxEnabled && isBooted,
                    parallaxSensitivity = homeBackground.parallaxSensitivity,
                    brightness = homeBackground.brightness,
                )
            }
            HomeBackgroundStyle.GRID -> {
                GridBackground(
                    parallaxEnabled = homeBackground.parallaxEnabled && isBooted,
                    parallaxSensitivity = homeBackground.parallaxSensitivity,
                    brightness = homeBackground.brightness,
                )
            }
            HomeBackgroundStyle.PARTICLES -> {
                ParticlesBackground(
                    parallaxEnabled = homeBackground.parallaxEnabled && isBooted,
                    parallaxSensitivity = homeBackground.parallaxSensitivity,
                    brightness = homeBackground.brightness,
                    isVisible = isVisible,
                )
            }
            HomeBackgroundStyle.SNOW -> {
                SnowBackground(
                    parallaxEnabled = homeBackground.parallaxEnabled && isBooted,
                    parallaxSensitivity = homeBackground.parallaxSensitivity,
                    brightness = homeBackground.brightness,
                )
            }
            HomeBackgroundStyle.SPACE -> {
                SpaceBackground(
                    parallaxEnabled = homeBackground.parallaxEnabled && isBooted,
                    parallaxSensitivity = homeBackground.parallaxSensitivity,
                    brightness = homeBackground.brightness,
                    isVisible = isVisible,
                )
            }
        }
    }
}


