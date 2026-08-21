package moe.rukamori.archivetune.ui.player.player_0
 
import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlin.math.abs

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayerCoverCard(
    coverDrawable: Drawable? = null,
    coverUrl: String? = null,
    modifier: Modifier = Modifier,
    placeholderResId: Int,
    isAlbumCoverGlowEnabled: Boolean = false,
    vibrantColor: Color = Color.Transparent,
    gestureEnabled: Boolean = true,
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {}
) {
    val shadowColor = MaterialTheme.colorScheme.scrim
    val surfaceColor: Color = MaterialTheme.colorScheme.surface
    val outlineColor: Color = MaterialTheme.colorScheme.outlineVariant
    
    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current
    var hasVibrated by remember { mutableStateOf(false) }
    var slideDirection by remember { mutableIntStateOf(1) }
    
    Box(
        modifier = modifier
            .pointerInput(gestureEnabled) {
                if (!gestureEnabled) return@pointerInput
                val snapThresholdPx = 100f * density.density
                var accumulatedDragX = 0f
                
                detectHorizontalDragGestures(
                    onDragStart = {
                        accumulatedDragX = 0f
                        hasVibrated = false
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        accumulatedDragX += dragAmount
                        
                        if (abs(accumulatedDragX) > snapThresholdPx && !hasVibrated) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            hasVibrated = true
                        }
                    },
                    onDragEnd = {
                        if (abs(accumulatedDragX) > snapThresholdPx) {
                            if (accumulatedDragX < 0) {
                                slideDirection = 1
                                onNext()
                            } else {
                                slideDirection = -1
                                onPrevious()
                            }
                        }
                    }
                )
            }
            .aspectRatio(1f, matchHeightConstraintsFirst = true)
            .then(
                if (isAlbumCoverGlowEnabled) {
                    Modifier.shadow(
                        elevation = 48.dp,
                        shape = RoundedCornerShape(24.dp),
                        clip = false,
                        ambientColor = vibrantColor.copy(alpha = 0.8f),
                        spotColor = vibrantColor
                    )
                } else {
                    Modifier.shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(24.dp),
                        clip = false,
                        ambientColor = shadowColor,
                        spotColor = shadowColor.copy(alpha = 0.6f)
                    )
                }
            )
            .clip(RoundedCornerShape(24.dp))
            .background(surfaceColor)
            .border(BorderStroke(1.dp, outlineColor), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        val context = LocalContext.current
        val currentData = coverDrawable ?: coverUrl.takeIf { !it.isNullOrEmpty() }
        
        AnimatedContent(
            targetState = currentData,
            transitionSpec = {
                (slideInHorizontally(
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
                    initialOffsetX = { fullWidth -> slideDirection * fullWidth }
                ) + fadeIn(animationSpec = tween(200))).togetherWith(
                    slideOutHorizontally(
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
                        targetOffsetX = { fullWidth -> -slideDirection * fullWidth }
                    ) + fadeOut(animationSpec = tween(200))
                )
            },
            label = "CoverCarousel"
        ) { data ->
            val previousPainter = androidx.compose.runtime.remember { arrayOf<androidx.compose.ui.graphics.painter.Painter?>(null) }
            
            val request = androidx.compose.runtime.remember(data) {
                ImageRequest.Builder(context)
                    .data(data)
                    .crossfade(500)
                    .build()
            }
            
            coil3.compose.AsyncImage(
                model = request,
                contentDescription = "Album Art Large",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = previousPainter[0] ?: painterResource(id = placeholderResId),
                error = painterResource(id = placeholderResId),
                fallback = painterResource(id = placeholderResId),
                onSuccess = { state ->
                    previousPainter[0] = state.painter
                }
            )
        }
    }
}
