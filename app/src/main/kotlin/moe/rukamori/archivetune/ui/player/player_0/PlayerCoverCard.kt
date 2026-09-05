package moe.rukamori.archivetune.ui.player.player_0
 
import android.graphics.drawable.Drawable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.painter.Painter
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import kotlinx.coroutines.launch
import kotlin.math.abs

import moe.rukamori.archivetune.constants.NothingMatrixArtworkKey
import moe.rukamori.archivetune.ui.component.MatrixCoverArtwork
import moe.rukamori.archivetune.utils.rememberPreference

@Composable
fun PlayerCoverCard(
    coverUrl: String? = null,
    modifier: Modifier = Modifier,
    placeholderResId: Int,
    isAlbumCoverGlowEnabled: Boolean = false,
    vibrantColor: Color = Color.Transparent,
    gestureEnabled: Boolean = true,
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {}
) {
    val context = LocalContext.current
    val (matrixPrefEnabled) = rememberPreference(NothingMatrixArtworkKey, defaultValue = true)
    var isMatrixModeLocal by remember(coverUrl, matrixPrefEnabled) { mutableStateOf(matrixPrefEnabled) }
    val shadowColor = MaterialTheme.colorScheme.scrim
    val surfaceColor: Color = MaterialTheme.colorScheme.surface
    val outlineColor: Color = MaterialTheme.colorScheme.outlineVariant
    
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    
    val offsetX = remember { Animatable(0f) }
    var accumulatedDragX by remember { mutableStateOf(0f) }
    
    val maxTensionOffsetPx = with(density) { 60.dp.toPx() }
    val snapThresholdPx = with(density) { 80.dp.toPx() }
    
    var currentPainter by remember { mutableStateOf<Painter?>(null) }
    var activeVibrantColor by remember { mutableStateOf(Color.Transparent) }
    
    val request = remember(coverUrl) {
        ImageRequest.Builder(context)
            .data(coverUrl.takeIf { !it.isNullOrEmpty() })
            .build()
    }
    
    val painter = rememberAsyncImagePainter(model = request)
    val state by painter.state.collectAsState()
    
    LaunchedEffect(state, vibrantColor) {
        when (state) {
            is AsyncImagePainter.State.Success -> {
                currentPainter = state.painter
                activeVibrantColor = vibrantColor
            }
            is AsyncImagePainter.State.Error,
            is AsyncImagePainter.State.Empty -> {
                currentPainter = null
                activeVibrantColor = Color.Transparent
            }
            else -> {
                // Keep currentPainter and activeVibrantColor during Loading
            }
        }
    }
    
    val animatedVibrantColor by animateColorAsState(
        targetValue = activeVibrantColor,
        animationSpec = tween(500),
        label = "CoverGlowColor"
    )
    
    val coverShape = RoundedCornerShape(12.dp)
    
    Box(
        modifier = modifier
            .aspectRatio(1f, matchHeightConstraintsFirst = true)
            .graphicsLayer {
                translationX = offsetX.value
                shadowElevation = 8.dp.toPx()
                shape = coverShape
                clip = false
                ambientShadowColor = Color.Black.copy(alpha = 0.5f)
                spotShadowColor = Color.Black.copy(alpha = 0.8f)
            }
            .then(
                if (gestureEnabled) {
                    Modifier.pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                accumulatedDragX = 0f
                            },
                            onDragEnd = {
                                coroutineScope.launch {
                                    if (abs(accumulatedDragX) > snapThresholdPx) {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (accumulatedDragX > 0) {
                                            onPrevious()
                                        } else {
                                            onNext()
                                        }
                                    }
                                    accumulatedDragX = 0f
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = 0.78f,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                                }
                            },
                            onDragCancel = {
                                coroutineScope.launch {
                                    accumulatedDragX = 0f
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = 0.78f,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                coroutineScope.launch {
                                    accumulatedDragX += dragAmount
                                    val dragFraction = (abs(accumulatedDragX) / (size.width.toFloat() * 1.5f)).coerceIn(0f, 1f)
                                    val tensionOffset = lerp(0f, maxTensionOffsetPx, dragFraction)
                                    val finalOffset = if (accumulatedDragX > 0) tensionOffset else -tensionOffset
                                    offsetX.snapTo(finalOffset)
                                }
                            }
                        )
                    }
                } else {
                    Modifier
                }
            )
            .clip(coverShape)
            .background(Color(0xFF141414))
            .border(BorderStroke(1.dp, Color(0xFF262626)), coverShape),
        contentAlignment = Alignment.Center
    ) {
        MatrixCoverArtwork(
            coverUrl = coverUrl,
            placeholderResId = placeholderResId,
            modifier = Modifier
                .fillMaxSize()
                .clickable { isMatrixModeLocal = !isMatrixModeLocal },
            cornerRadius = 12.dp,
            borderColor = Color(0xFF262626),
            isMatrixMode = isMatrixModeLocal,
            contentDescription = "Album Art Large"
        )
    }
}
