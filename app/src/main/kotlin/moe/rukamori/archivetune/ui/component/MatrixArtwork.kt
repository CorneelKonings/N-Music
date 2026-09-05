package moe.rukamori.archivetune.ui.component

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import android.util.LruCache
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.crossfade
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.rukamori.archivetune.ui.theme.NothingTokens

/**
 * Nothing-inspired Dot-Matrix Album Art & Display Components.
 * Uses high-contrast monochrome matrices inspired by Nothing OS.
 */

object MatrixImageCache {
    private val lruCache = LruCache<String, Bitmap>(24)

    fun get(key: String): Bitmap? = lruCache.get(key)
    fun put(key: String, bitmap: Bitmap) {
        lruCache.put(key, bitmap)
    }

    /**
     * Converts an image bitmap into a clean, high-contrast Nothing dot-matrix representation.
     */
    fun createMatrixBitmap(source: Bitmap, targetSize: Int = 400, dotColumns: Int = 48): Bitmap {
        val output = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(output)
        canvas.drawColor(android.graphics.Color.BLACK)

        val scaledSource = Bitmap.createScaledBitmap(source, dotColumns, dotColumns, true)
        val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = AndroidPaint.Style.FILL
        }

        val cellSize = targetSize.toFloat() / dotColumns
        val maxRadius = cellSize * 0.46f

        for (y in 0 until dotColumns) {
            for (x in 0 until dotColumns) {
                val pixel = scaledSource.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF

                // Standard luminance formula
                var lum = (0.299f * r + 0.587f * g + 0.114f * b) / 255f

                // S-curve contrast boost
                lum = (lum * lum * (3f - 2f * lum)).coerceIn(0f, 1f)

                if (lum > 0.08f) {
                    val radius = maxRadius * lum
                    val cx = x * cellSize + cellSize / 2f
                    val cy = y * cellSize + cellSize / 2f
                    val alpha = ((lum.coerceIn(0.2f, 1.0f)) * 255).toInt()
                    paint.alpha = alpha
                    canvas.drawCircle(cx, cy, radius, paint)
                }
            }
        }
        scaledSource.recycle()
        return output
    }
}

@Composable
fun MatrixCoverArtwork(
    coverUrl: String?,
    placeholderResId: Int,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    borderColor: Color = NothingTokens.DarkBorder,
    isMatrixMode: Boolean = false,
    dotOverlay: Boolean = true,
    contentDescription: String? = "Album Artwork"
) {
    val context = LocalContext.current
    val shape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }
    var matrixBitmap by remember(coverUrl, isMatrixMode) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(coverUrl, isMatrixMode) {
        if (isMatrixMode && !coverUrl.isNullOrEmpty()) {
            val cached = MatrixImageCache.get(coverUrl)
            if (cached != null) {
                matrixBitmap = cached
            } else {
                withContext(Dispatchers.IO) {
                    runCatching {
                        val loader = ImageLoader(context)
                        val req = ImageRequest.Builder(context)
                            .data(coverUrl)
                            .build()
                        val result = (loader.execute(req) as? SuccessResult)?.image?.toBitmap()
                        if (result != null) {
                            val processed = MatrixImageCache.createMatrixBitmap(result)
                            MatrixImageCache.put(coverUrl, processed)
                            withContext(Dispatchers.Main) {
                                matrixBitmap = processed
                            }
                        }
                    }
                }
            }
        } else {
            matrixBitmap = null
        }
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(NothingTokens.PureBlack, shape)
            .border(1.dp, borderColor, shape),
        contentAlignment = Alignment.Center
    ) {
        if (isMatrixMode && matrixBitmap != null) {
            Image(
                bitmap = matrixBitmap!!.asImageBitmap(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (!coverUrl.isNullOrEmpty()) {
            val request = remember(coverUrl) {
                ImageRequest.Builder(context)
                    .data(coverUrl)
                    .crossfade(400)
                    .build()
            }
            AsyncImage(
                model = request,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }),
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (dotOverlay) {
                            Modifier.drawWithContent {
                                drawContent()
                                // Subtle Nothing dot-matrix texture overlay
                                val spacing = 4.dp.toPx()
                                val radius = 0.8.dp.toPx()
                                var x = spacing / 2f
                                while (x < size.width) {
                                    var y = spacing / 2f
                                    while (y < size.height) {
                                        drawCircle(
                                            color = Color.Black,
                                            radius = radius,
                                            center = Offset(x, y)
                                        )
                                        y += spacing
                                    }
                                    x += spacing
                                }
                            }
                        } else {
                            Modifier
                        }
                    )
            )
        } else {
            Image(
                painter = painterResource(id = placeholderResId),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Nothing Industrial Section Title Header (e.g. `// QUICK ACCESS` or `// RECENTLY PLAYED`)
 */
@Composable
fun NothingSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    trailingText: String? = null,
    accentColor: Color = NothingTokens.NRed
) {
    Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "//",
                color = accentColor,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = title.uppercase(),
                color = NothingTokens.TextPrimary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.5.sp
            )
        }

        if (!trailingText.isNullOrEmpty()) {
            Text(
                text = trailingText,
                color = NothingTokens.TextTertiary,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Industrial Nothing Badge / Chip (e.g. `[ LOSSLESS ]` or `[ 03:45 ]`)
 */
@Composable
fun NothingBadge(
    text: String,
    modifier: Modifier = Modifier,
    accentColor: Color = NothingTokens.TextSecondary,
    backgroundColor: Color = NothingTokens.DarkSurfaceElevated,
    borderColor: Color = NothingTokens.DarkBorderSubtle
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .border(0.8.dp, borderColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = accentColor,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            letterSpacing = 0.8.sp
        )
    }
}

/**
 * Real-time Nothing Dot-Matrix Audio Equalizer Bars.
 */
@Composable
fun MatrixAudioEqualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barColor: Color = NothingTokens.NRed,
    barCount: Int = 4
) {
    val transition = rememberInfiniteTransition(label = "MatrixEqualizerTransition")

    val h1 by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar1"
    )
    val h2 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar2"
    )
    val h3 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar3"
    )
    val h4 by transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(480, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar4"
    )

    val heights = if (isPlaying) listOf(h1, h2, h3, h4) else listOf(0.3f, 0.3f, 0.3f, 0.3f)

    Canvas(modifier = modifier.size(width = 18.dp, height = 14.dp)) {
        val barWidth = 2.5.dp.toPx()
        val spacing = (size.width - (barWidth * barCount)) / (barCount - 1).coerceAtLeast(1)

        heights.take(barCount).forEachIndexed { index, fraction ->
            val barHeight = size.height * fraction
            val left = index * (barWidth + spacing)
            val top = size.height - barHeight

            drawRect(
                color = barColor,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight)
            )
        }
    }
}

