package moe.rukamori.archivetune.ui.component

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil3.compose.AsyncImage

fun lerp3(start: Float, mid: Float, end: Float, fraction: Float): Float {
    return if (fraction < 0.5f) {
        lerp(start, mid, fraction * 2f)
    } else {
        lerp(mid, end, (fraction - 0.5f) * 2f)
    }
}

@Composable
fun YumaMorphingHeader(
    imageUrl: String,
    collapseFraction: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val fraction = collapseFraction.coerceIn(0f, 1f)

    val expandedSize = LocalConfiguration.current.screenWidthDp.dp
    val expandedSizePx = with(density) { expandedSize.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(expandedSize)
    ) {
        Box(
            modifier = Modifier
                .size(expandedSize)
                .align(Alignment.TopCenter)
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0.5f, 0f)

                    val currentScaleX = lerp3(start = 1f, mid = 0.6f, end = 0f, fraction = fraction)
                    val currentScaleY = lerp3(start = 1f, mid = 0.6f, end = 0.2f, fraction = fraction)
                    scaleX = currentScaleX
                    scaleY = currentScaleY

                    val midY = -expandedSizePx * 0.05f
                    val endY = -expandedSizePx * 0.5f
                    translationY = lerp3(start = 0f, mid = midY, end = endY, fraction = fraction)

                    val midRadius = 32.dp.toPx()
                    val endRadius = expandedSizePx / 2f
                    val targetRadius = lerp3(start = 0f, mid = midRadius, end = endRadius, fraction = fraction)
                    
                    val adjustedRadius = if (currentScaleX > 0f) targetRadius / currentScaleX else 0f
                    shape = RoundedCornerShape(adjustedRadius)
                    clip = true

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (fraction > 0.5f) {
                            val blurRadius = lerp(0.1f, 40f, (fraction - 0.5f) * 2f)
                            renderEffect = android.graphics.RenderEffect.createBlurEffect(
                                blurRadius, blurRadius, android.graphics.Shader.TileMode.CLAMP
                            ).asComposeRenderEffect()
                        } else {
                            renderEffect = null
                        }
                    }
                }
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = lerp3(start = 0f, mid = 0.3f, end = 1f, fraction = fraction)
                    }
                    .background(Color.Black)
            )
        }
    }
}
