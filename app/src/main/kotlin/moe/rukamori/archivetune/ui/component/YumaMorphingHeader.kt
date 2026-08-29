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
                    transformOrigin = TransformOrigin(0.5f, 0.5f)

                    // 1. Масштаб: сохраняет форму капли/бусины (без схлопывания в плоскую щель)
                    // На 0.5: 32% (аккуратное превью)
                    // На 1.0: синхронно стягивается в точку камеры
                    scaleX = lerp3(start = 1f, mid = 0.48f, end = 0f, fraction = fraction)
                    scaleY = lerp3(start = 1f, mid = 0.48f, end = 0.02f, fraction = fraction)

                    // 2. Смещение Y: сбалансированные дельты (0 -> 16% -> 44%)
                    // Устраняет резкий рывок скорости на второй фазе
                    val midOffset = -expandedSizePx * 0.09f
                    val endOffset = -expandedSizePx * 0.44f
                    translationY = lerp3(start = 0f, mid = midOffset, end = endOffset, fraction = fraction)

                    // 3. Форма: уже к 0.5 переходит в почти идеальный круг (36%), во 2-й фазе остается круглой каплей
                    val cornerPercent = lerp3(start = 0f, mid = 36f, end = 50f, fraction = fraction).toInt()
                    shape = RoundedCornerShape(percent = cornerPercent)
                    clip = true

                    // 4. Аппаратный блюр: полностью отключен до 0.70, мягко моет только перед самой камерой
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (fraction > 0.70f) {
                            val blurProgress = (fraction - 0.70f) / 0.30f
                            val blurRadius = lerp(0.1f, 40f, blurProgress)
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

            // Черный оверлей: чистая картинка (alpha = 0) до 0.70, уход в черный только в финале
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = if (fraction < 0.70f) 0f else lerp(0f, 1f, (fraction - 0.70f) / 0.30f)
                    }
                    .background(Color.Black)
            )
        }
    }
}