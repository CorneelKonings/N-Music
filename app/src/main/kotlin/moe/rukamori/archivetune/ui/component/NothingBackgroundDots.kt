package moe.rukamori.archivetune.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.flowOf
import moe.rukamori.archivetune.LocalPlayerConnection
import moe.rukamori.archivetune.ui.theme.NothingTokens
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin

/**
 * Nothing OS inspired micro-dot background matrix.
 * - When no music plays: dots float calmly with an organic, gentle idle drift.
 * - When music plays: dots actively respond to the music with audio ripples,
 *   tempo-synchronized pulses, frequency-band amplitude modulation, and signature Nothing Red accents.
 */
@Composable
fun NothingBackgroundDots(
    enabled: Boolean = true,
    disableAnimations: Boolean = false,
    isPlaying: Boolean? = null,
    modifier: Modifier = Modifier,
    dotColor: Color = Color.White.copy(alpha = 0.28f),
    dotRadius: Dp = 1.3.dp,
    gridSpacing: Dp = 24.dp,
) {
    if (!enabled) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(NothingTokens.PureBlack)
        )
        return
    }

    // Automatically detect playback state from LocalPlayerConnection if not explicitly provided
    val playerConnection = LocalPlayerConnection.current
    val connectionPlaying by (playerConnection?.isPlaying ?: flowOf(false))
        .collectAsStateWithLifecycle(false)
    val effectiveIsPlaying = isPlaying ?: connectionPlaying

    // Smooth transition between calm floating and audio-reactive music mode
    val musicEnergy by animateFloatAsState(
        targetValue = if (effectiveIsPlaying && !disableAnimations) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "MusicEnergy"
    )

    val density = LocalDensity.current
    val spacingPx = with(density) { gridSpacing.toPx() }
    val radiusPx = with(density) { dotRadius.toPx() }

    val transition = rememberInfiniteTransition(label = "DotsMatrixTransition")

    // Base calm floating time loop
    val calmTime by if (!disableAnimations) {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 6283.18f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 60000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "CalmTimeLoop"
        )
    } else {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(tween(1000)),
            label = "StaticTime"
        )
    }

    // Rhythmic tempo beat (simulating ~125 BPM musical cadence)
    val beatPulse by if (!disableAnimations) {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 480, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "BeatPulse"
        )
    } else {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(tween(1000)),
            label = "StaticBeat"
        )
    }

    // Concentric audio wave ripple phase (propagates outward like sound waves)
    val audioWavePhase by if (!disableAnimations) {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 6.28318f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2400, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "AudioWavePhase"
        )
    } else {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(tween(1000)),
            label = "StaticWave"
        )
    }

    // Sub-bass frequency modulation (deep rhythmic swells)
    val bassPulse by if (!disableAnimations) {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 960, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "BassPulse"
        )
    } else {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(tween(1000)),
            label = "StaticBass"
        )
    }

    val nothingRedColor = NothingTokens.NRed

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(NothingTokens.PureBlack)
    ) {
        val width = size.width
        val height = size.height

        val cols = (width / spacingPx).toInt() + 3
        val rows = (height / spacingPx).toInt() + 3

        val startX = (width - cols * spacingPx) / 2f
        val startY = (height - rows * spacingPx) / 2f

        // Sound origin: bottom-center of the screen where player controls live
        val soundOriginX = width * 0.5f
        val soundOriginY = height * 0.85f
        val maxDist = hypot(width, height)

        for (col in 0 until cols) {
            for (row in 0 until rows) {
                val baseX = startX + col * spacingPx
                val baseY = startY + row * spacingPx

                // Unique per-dot deterministic seed based on grid position
                val dotSeed = col * 31f + row * 17f + col * row * 7f

                // 1. CALM FLOATING TRAJECTORY (Gentle, peaceful drift when no music plays)
                val calmSpeedX = 0.22f + (sin(dotSeed * 0.13f) * 0.14f)
                val calmSpeedY = 0.18f + (cos(dotSeed * 0.19f) * 0.12f)

                val phaseX = dotSeed * 1.37f
                val phaseY = dotSeed * 2.81f

                val calmAmpX = spacingPx * (0.12f + sin(dotSeed * 0.07f) * 0.08f)
                val calmAmpY = spacingPx * (0.12f + cos(dotSeed * 0.11f) * 0.08f)

                val calmOffsetX = if (!disableAnimations) sin(calmTime * calmSpeedX + phaseX) * calmAmpX else 0f
                val calmOffsetY = if (!disableAnimations) cos(calmTime * calmSpeedY + phaseY) * calmAmpY else 0f

                // 2. AUDIO-REACTIVE TRAJECTORY & PULSES (When music plays)
                // Distance from sound origin for ripple waves
                val dist = hypot(baseX - soundOriginX, baseY - soundOriginY)
                val normDist = (dist / maxDist).coerceIn(0f, 1f)

                // Expanding sound ripple wave
                val waveVal = max(0f, sin(normDist * 16f - audioWavePhase * 3f))

                // Vertical frequency response: bottom rows = bass, middle = vocals/melody, top = treble
                val normY = (baseY / height).coerceIn(0f, 1f)
                val bassBand = (normY * 0.8f) * bassPulse
                val trebleBand = ((1f - normY) * 0.6f) * (0.5f + 0.5f * sin(calmTime * 2.5f + dotSeed))
                val rhythmHit = (beatPulse * 0.7f + waveVal * 0.5f)

                // Reactive displacement: dots bounce to the music tempo
                val musicBounceX = sin(calmTime * 1.8f + phaseX) * spacingPx * 0.28f * rhythmHit
                val musicBounceY = -abs(cos(calmTime * 2.2f + phaseY)) * spacingPx * 0.35f * (rhythmHit + bassBand)

                // Blend offsets based on musicEnergy
                val currentOffsetX = calmOffsetX + (musicBounceX * musicEnergy)
                val currentOffsetY = calmOffsetY + (musicBounceY * musicEnergy)

                val finalX = baseX + currentOffsetX
                val finalY = baseY + currentOffsetY

                if (finalX >= -radiusPx * 3 && finalY >= -radiusPx * 3 &&
                    finalX <= width + radiusPx * 3 && finalY <= height + radiusPx * 3
                ) {
                    // Calm alpha: subtle breathing between 0.18 and 0.36
                    val calmAlphaPulse = if (!disableAnimations) {
                        0.75f + 0.25f * sin(calmTime * 0.35f + phaseX + phaseY)
                    } else 1f
                    val calmAlpha = (dotColor.alpha * calmAlphaPulse).coerceIn(0.12f, 0.42f)

                    // Music-reactive alpha: surges up to 0.85 - 0.95 on beat & wave crests
                    val musicAlpha = (0.35f + 0.55f * rhythmHit + 0.20f * bassBand + 0.15f * trebleBand)
                        .coerceIn(0.25f, 0.95f)

                    val effectiveAlpha = calmAlpha * (1f - musicEnergy) + musicAlpha * musicEnergy

                    // Dynamic dot radius: scales up when music plays
                    val musicRadiusScale = 1f + (0.45f * rhythmHit + 0.30f * waveVal + 0.35f * bassBand) * musicEnergy
                    val effectiveRadius = radiusPx * musicRadiusScale

                    // Signature Nothing Red accent on occasional rhythmic nodes
                    val isRedNode = (dotSeed.toInt() % 31 == 0)
                    val effectiveColor = if (isRedNode && musicEnergy > 0.3f && rhythmHit > 0.5f) {
                        nothingRedColor.copy(alpha = effectiveAlpha)
                    } else {
                        dotColor.copy(alpha = effectiveAlpha)
                    }

                    drawCircle(
                        color = effectiveColor,
                        radius = effectiveRadius,
                        center = Offset(finalX, finalY)
                    )
                }
            }
        }
    }
}

private fun abs(value: Float): Float = if (value < 0f) -value else value

