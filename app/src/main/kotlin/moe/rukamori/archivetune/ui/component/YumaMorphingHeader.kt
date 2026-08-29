/*
 * YumaPlayer (2026) | Modified work by MuwMix
 * GPL-3.0 License | Contributors: see git history
 */

package moe.rukamori.archivetune.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.transformations
import moe.rukamori.archivetune.utils.FastBlurTransformation

@Composable
fun YumaMorphingHeader(
    imageUrl: String,
    collapseFraction: Float,
    expandedHeight: Dp,
    collapsedSize: Dp,
    topBarHeight: Dp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val blurredImageRequest = remember(imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .size(100)
            .transformations(FastBlurTransformation(radius = 24, sampling = 1f))
            .crossfade(true)
            .build()
    }

    val clearImageRequest = remember(imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .build()
    }

    val fraction = collapseFraction.coerceIn(0f, 1f)
    val screenWidthPx = with(density) { screenWidth.toPx() }
    val expandedHeightPx = with(density) { expandedHeight.toPx() }
    val collapsedSizePx = with(density) { collapsedSize.toPx() }
    val topBarHeightPx = with(density) { topBarHeight.toPx() }

    Box(
        modifier = modifier
            .size(screenWidth, expandedHeight)
            .graphicsLayer {
                val scaleX = if (screenWidthPx > 0f) {
                    lerp(1f, collapsedSizePx / screenWidthPx, fraction)
                } else {
                    1f
                }
                val scaleY = if (expandedHeightPx > 0f) {
                    lerp(1f, collapsedSizePx / expandedHeightPx, fraction)
                } else {
                    1f
                }

                this.scaleX = scaleX
                this.scaleY = scaleY

                val collapsedY = (topBarHeightPx - expandedHeightPx) / 2f
                translationY = lerp(0f, collapsedY, fraction)

                val targetRadiusPx = lerp(0f, collapsedSizePx / 2f, fraction)
                val adjustedRadius = if (scaleY > 0f) targetRadiusPx / scaleY else 0f

                shape = RoundedCornerShape(adjustedRadius)
                clip = true
            }
    ) {
        AsyncImage(
            model = blurredImageRequest,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = 1f - fraction
                }
        )

        AsyncImage(
            model = clearImageRequest,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black,
                                Color.Black.copy(alpha = fraction)
                            ),
                            startY = size.height * 0.5f,
                            endY = size.height
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
        )
    }
}
