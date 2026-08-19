package moe.rukamori.archivetune.ui.player.player_0

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import moe.rukamori.archivetune.ui.state.PlayerUiState

@Composable
fun PlayerBackgroundLayers(
    state: PlayerUiState,
    gradientBrush: Brush,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val isLightTheme = colorScheme.surface.luminance() > 0.5f
    val standardVeilColor = if (isLightTheme) colorScheme.surface else Color.Black

    val immersiveMaskBrush = remember {
        Brush.verticalGradient(
            0.0f to Color.Black,
            0.65f to Color.Black,
            1.0f to Color.Transparent
        )
    }

    val standardVeilBrush = remember(standardVeilColor) {
        Brush.verticalGradient(
            colors = listOf(
                standardVeilColor.copy(alpha = 0.50f),
                standardVeilColor.copy(alpha = 0.30f),
                standardVeilColor.copy(alpha = 0.70f)
            )
        )
    }

    val blurVeilBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.50f),
                Color.Black.copy(alpha = 0.25f),
                Color.Black.copy(alpha = 0.70f)
            )
        )
    }

    val immersiveVeilBrush = remember {
        Brush.verticalGradient(
            0.0f to Color.Transparent,
            0.35f to Color.Transparent,
            0.65f to Color.Black.copy(alpha = 0.35f),
            1.0f to Color.Black.copy(alpha = 0.30f)
        )
    }

    val blurOverlayAlpha by animateFloatAsState(
        targetValue = if (state.isBlurBackgroundEnabled) 1f else 0f,
        animationSpec = tween(500),
        label = "BlurOverlayTransition"
    )

    val immersiveTransitionAlpha by animateFloatAsState(
        targetValue = if (state.isImmersiveEnabled && !state.isLyricsVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "ImmersiveThemeTransition"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 1f - blurOverlayAlpha }
                .background(gradientBrush)
        )

        androidx.compose.animation.Crossfade(
            targetState = state.coverUrl.takeIf { it.isNotEmpty() },
            animationSpec = tween(500),
            label = "BackgroundCrossfade"
        ) { targetUrl ->
            val blurImageRequest = remember(targetUrl) {
                ImageRequest.Builder(context)
                    .data(targetUrl)
                    .size(64)
                    .crossfade(500)
                    .build()
            }

            val clearImageRequest = remember(targetUrl) {
                ImageRequest.Builder(context)
                    .data(targetUrl)
                    .crossfade(500)
                    .build()
            }

            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = blurImageRequest,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = blurOverlayAlpha }
                        .blur(56.dp),
                    contentScale = ContentScale.Crop
                )

                AsyncImage(
                    model = clearImageRequest,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.75f)
                        .align(Alignment.TopCenter)
                        .graphicsLayer {
                            alpha = immersiveTransitionAlpha
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                brush = immersiveMaskBrush,
                                blendMode = BlendMode.DstIn
                            )
                        },
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.TopCenter
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = (1f - blurOverlayAlpha) * (1f - immersiveTransitionAlpha) }
                .background(standardVeilBrush)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = blurOverlayAlpha * (1f - immersiveTransitionAlpha) }
                .background(blurVeilBrush)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = immersiveTransitionAlpha }
                .background(immersiveVeilBrush)
        )
    }
}