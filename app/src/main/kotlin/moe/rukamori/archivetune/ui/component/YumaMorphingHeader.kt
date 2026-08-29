package moe.rukamori.archivetune.ui.component

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
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
    modifier: Modifier = Modifier,
    heightRatio: Float = 1.45f
) {
    val density = LocalDensity.current
    val fraction = collapseFraction.coerceIn(0f, 1f)

    val screenWidthPx = LocalWindowInfo.current.containerSize.width.toFloat()
    val screenWidthDp = with(density) { screenWidthPx.toDp() }

    val expandedHeightDp = screenWidthDp * heightRatio
    val surfaceColor = MaterialTheme.colorScheme.background

    // Динамический расчет точки фронтальной камеры
    val statusBarHeightPx = WindowInsets.statusBars.getTop(density).toFloat()
    val targetCameraY = if (statusBarHeightPx > 0f) statusBarHeightPx / 2f else with(density) { 24.dp.toPx() }
    val containerCenterY = screenWidthPx / 2f
    val endOffset = targetCameraY - containerCenterY

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(expandedHeightDp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0.5f, 0.5f)

                    // 1. Масштаб: 1.0 -> 0.44 (на 1-й фазе) -> 0.0 (в точку камеры на 2-й)
                    val currentScale = lerp3(start = 1f, mid = 0.44f, end = 0f, fraction = fraction)
                    scaleX = currentScale
                    scaleY = currentScale

                    // 2. Смещение: 0f -> 0f (стоит на месте на 1-й фазе) -> endOffset (летит на 2-й)
                    translationY = lerp3(start = 0f, mid = 0f, end = endOffset, fraction = fraction)

                    // 3. Форма: 0% -> 32% (скругление) -> 50% (круг перед полетом)
                    val cornerPercent = lerp3(start = 0f, mid = 32f, end = 50f, fraction = fraction).toInt()
                    shape = RoundedCornerShape(percent = cornerPercent)
                    clip = true

                    // 4. Блюр перед самым поглощением камерой
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
                // Нормализация пропорций постера до квадрата 1:1 на 1-й фазе
                .height(
                    lerp(expandedHeightDp.value, screenWidthDp.value, (fraction * 2f).coerceIn(0f, 1f)).dp
                )
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            )

            // Градиентное растворение нижнего края в цвет фона
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = (1f - fraction * 2f).coerceIn(0f, 1f)
                    }
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Transparent,
                            0.60f to Color.Transparent,
                            0.88f to surfaceColor.copy(alpha = 0.75f),
                            1.0f to surfaceColor
                        )
                    )
            )

            // Черный оверлей под цвет объектива камеры
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