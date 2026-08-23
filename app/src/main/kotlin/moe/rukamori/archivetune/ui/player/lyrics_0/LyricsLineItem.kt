package moe.rukamori.archivetune.ui.player.lyrics_0

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import moe.rukamori.archivetune.lyrics.LyricsEntry
import moe.rukamori.archivetune.lyrics.WordTimestamp
import kotlin.math.PI
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LyricsLineItem(
    item: LyricsEntry,
    isActive: Boolean,
    isPast: Boolean,
    currentPositionMs: Long,
    textColor: Color,
    inactiveAlpha: Float,
    baseFontSize: Float,
    lyricsFontFamily: FontFamily?,
    bounceFactor: Float,
    glowFactor: Float,
    fillTransitionWidth: Float,
    onLineClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    distanceFromActive: Int = 0,
    isManualScrolling: Boolean = false,
    lyricsLineBlur: Boolean = true
) {
    val horizontalAlignment = remember(item.agent) {
        when (item.agent?.lowercase()) {
            "v2" -> Alignment.End
            "v1", null -> Alignment.Start
            else -> Alignment.CenterHorizontally
        }
    }

    val textAlign = remember(item.agent) {
        when (item.agent?.lowercase()) {
            "v2" -> TextAlign.End
            "v1", null -> TextAlign.Start
            else -> TextAlign.Center
        }
    }

    val lineTransformOrigin = remember(item.agent) {
        when (item.agent?.lowercase()) {
            "v2" -> TransformOrigin(1f, 0.5f)
            "v1", null -> TransformOrigin(0f, 0.5f)
            else -> TransformOrigin(0.5f, 0.5f)
        }
    }

    val lineAlpha = when {
        isActive -> 1f
        isManualScrolling -> when {
            distanceFromActive == 1 -> 0.72f
            distanceFromActive == 2 -> 0.56f
            distanceFromActive == 3 -> 0.40f
            else -> 0.28f
        }
        distanceFromActive == 1 -> 0.52f
        distanceFromActive == 2 -> 0.30f
        distanceFromActive == 3 -> 0.18f
        else -> inactiveAlpha
    }

    val targetBlur = when {
        isActive || !lyricsLineBlur || isManualScrolling -> 0f
        distanceFromActive == 1 -> 2f
        distanceFromActive == 2 -> 5f
        else -> 12f
    }

    val animatedBlur by animateFloatAsState(
        targetValue = targetBlur,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "lineBlur"
    )

    val animatedLineScale by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.95f,
        animationSpec = tween(durationMillis = 166, easing = FastOutSlowInEasing),
        label = "lineScale"
    )

    val animatedLineAlpha by animateFloatAsState(
        targetValue = lineAlpha,
        animationSpec = tween(durationMillis = if (isActive) 330 else 500, easing = FastOutSlowInEasing),
        label = "lineAlpha"
    )

    val isAllBackground = item.words?.all { it.isBackground || it.text.isBlank() } == true

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = if (isAllBackground) 32.dp else 24.dp,
                end = 24.dp,
                top = 3.dp,
                bottom = 3.dp
            )
            .then(
                if (animatedBlur > 0f) {
                    Modifier.blur(radiusX = animatedBlur.dp, radiusY = animatedBlur.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                } else {
                    Modifier
                }
            )
            .graphicsLayer {
                scaleX = animatedLineScale
                scaleY = animatedLineScale
                alpha = animatedLineAlpha
                transformOrigin = lineTransformOrigin
            }
            .clickable {
                if (item.time >= 0L) onLineClick(item.time)
            },
        horizontalAlignment = horizontalAlignment
    ) {
        val romanizedText by item.romanizedTextFlow.collectAsState()
        val translationText = item.providerTranslationText

        val supplementaryBaseTextStyle = MaterialTheme.typography.bodyMedium
        val supplementaryTextStyle = remember(supplementaryBaseTextStyle, baseFontSize, lyricsFontFamily, isAllBackground) {
            supplementaryBaseTextStyle.copy(
                fontSize = (baseFontSize * 0.55f).sp,
                lineHeight = (baseFontSize * 0.75f).sp,
                fontWeight = FontWeight.Normal,
                fontStyle = if (isAllBackground) FontStyle.Italic else FontStyle.Normal,
                fontFamily = lyricsFontFamily ?: supplementaryBaseTextStyle.fontFamily
            )
        }

        val mainWords = item.words?.filter { !it.isBackground } ?: emptyList()
        val bgWords = item.words?.filter { it.isBackground } ?: emptyList()

        val romajiWords = remember(item.providerRomanizedWords, romanizedText, mainWords.size) {
            if (item.providerRomanizedWords?.size == mainWords.size) {
                item.providerRomanizedWords
            } else {
                romanizedText?.split(Regex("\\s+"))?.filter { it.isNotBlank() }
            }
        }
        
        val canMapRomaji = romajiWords != null && romajiWords.size == mainWords.size

        if (!romanizedText.isNullOrBlank() && !canMapRomaji) {
            Text(
                text = romanizedText!!,
                style = supplementaryTextStyle,
                color = textColor.copy(alpha = if (isActive) 0.76f else 0.42f),
                textAlign = textAlign,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = (baseFontSize * 0.18f).dp)
            )
        }

        if (item.words != null) {
            val arrangement = when (textAlign) {
                TextAlign.Center -> Arrangement.Center
                TextAlign.End -> Arrangement.End
                else -> Arrangement.Start
            }

            if (mainWords.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = arrangement
                ) {
                    mainWords.forEachIndexed { index, word ->
                        if (word.text == " ") {
                            Text(
                                text = " ",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = if (isAllBackground) (baseFontSize * 0.82f).sp else baseFontSize.sp,
                                    fontFamily = lyricsFontFamily ?: MaterialTheme.typography.headlineMedium.fontFamily
                                ),
                                color = Color.Transparent
                            )
                            return@forEachIndexed
                        }
                        if (word.text == "\n") {
                            Spacer(modifier = Modifier.fillMaxWidth())
                            return@forEachIndexed
                        }

                        if (canMapRomaji) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 1.dp)
                            ) {
                                Text(
                                    text = romajiWords!![index],
                                    style = supplementaryTextStyle,
                                    color = textColor.copy(alpha = if (isActive) 0.76f else 0.42f),
                                    textAlign = TextAlign.Center
                                )
                                AnimatedWordV2(
                                    word = word,
                                    isLineActive = isActive,
                                    isLinePast = isPast,
                                    currentPositionMs = currentPositionMs,
                                    textColor = textColor,
                                    inactiveAlpha = inactiveAlpha,
                                    fontSize = if (isAllBackground) baseFontSize * 0.82f else baseFontSize,
                                    isBackground = isAllBackground,
                                    lyricsFontFamily = lyricsFontFamily,
                                    bounceFactor = bounceFactor,
                                    glowFactor = glowFactor,
                                    fillTransitionWidth = fillTransitionWidth
                                )
                            }
                        } else {
                            AnimatedWordV2(
                                word = word,
                                isLineActive = isActive,
                                isLinePast = isPast,
                                currentPositionMs = currentPositionMs,
                                textColor = textColor,
                                inactiveAlpha = inactiveAlpha,
                                fontSize = if (isAllBackground) baseFontSize * 0.82f else baseFontSize,
                                isBackground = isAllBackground,
                                lyricsFontFamily = lyricsFontFamily,
                                bounceFactor = bounceFactor,
                                glowFactor = glowFactor,
                                fillTransitionWidth = fillTransitionWidth
                            )
                        }
                    }
                }
            }

            if (bgWords.isNotEmpty()) {
                val spacerHeight = if (mainWords.isNotEmpty()) 4.dp else 0.dp
                if (mainWords.isNotEmpty()) Spacer(modifier = Modifier.height(spacerHeight))

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.85f),
                    horizontalArrangement = arrangement
                ) {
                    bgWords.forEachIndexed { _, word ->
                        if (word.text == " ") {
                            Text(
                                text = " ",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = (baseFontSize * 0.65f).sp,
                                    fontFamily = lyricsFontFamily ?: MaterialTheme.typography.headlineMedium.fontFamily
                                ),
                                color = Color.Transparent
                            )
                            return@forEachIndexed
                        }

                        AnimatedWordV2(
                            word = word,
                            isLineActive = isActive,
                            isLinePast = isPast,
                            currentPositionMs = currentPositionMs,
                            textColor = textColor,
                            inactiveAlpha = inactiveAlpha,
                            fontSize = baseFontSize * 0.65f,
                            isBackground = true,
                            lyricsFontFamily = lyricsFontFamily,
                            bounceFactor = bounceFactor,
                            glowFactor = glowFactor,
                            fillTransitionWidth = fillTransitionWidth
                        )
                    }
                }
            }
        } else {
            Text(
                text = item.text,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = if (isAllBackground) (baseFontSize * 0.82f).sp else baseFontSize.sp,
                    fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                    fontStyle = if (isAllBackground) FontStyle.Italic else FontStyle.Normal,
                    lineHeight = (baseFontSize * 1.2f).sp,
                    fontFamily = lyricsFontFamily ?: MaterialTheme.typography.headlineMedium.fontFamily
                ),
                color = textColor.copy(alpha = if (isActive) 1f else 0.52f),
                textAlign = textAlign,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (!translationText.isNullOrBlank()) {
            Text(
                text = translationText,
                style = supplementaryTextStyle,
                color = textColor.copy(alpha = if (isActive) 0.76f else 0.42f),
                textAlign = textAlign,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = (baseFontSize * 0.3f).dp)
            )
        }
    }
}

@Composable
fun AnimatedWordV2(
    word: WordTimestamp,
    isLineActive: Boolean,
    isLinePast: Boolean,
    currentPositionMs: Long,
    textColor: Color,
    inactiveAlpha: Float,
    fontSize: Float,
    isBackground: Boolean,
    lyricsFontFamily: FontFamily?,
    bounceFactor: Float,
    glowFactor: Float,
    fillTransitionWidth: Float
) {
    val wordStartMs = (word.startTime * 1000).toLong()
    val wordEndMs = (word.endTime * 1000).toLong()
    val wordDuration = (wordEndMs - wordStartMs).coerceAtLeast(1L)

    val isWordComplete = currentPositionMs >= wordEndMs
    val isWordActive = currentPositionMs in wordStartMs until wordEndMs

    val progress = when {
        isWordComplete -> 1f
        currentPositionMs <= wordStartMs -> 0f
        else -> ((currentPositionMs - wordStartMs).toFloat() / wordDuration).coerceIn(0f, 1f)
    }

    val sinProgress = sin(progress * PI).toFloat()
    val wordScale = 1f + (0.015f * bounceFactor * sinProgress)

    val targetFloat = if (isWordActive) -4f * bounceFactor * sinProgress else 0f
    val floatOffset by animateFloatAsState(
        targetValue = targetFloat,
        animationSpec = tween(
            durationMillis = if (isWordActive) 50 else 350,
            easing = FastOutSlowInEasing
        ),
        label = "wordFloatOffset"
    )

    val glowProgress = (progress * 2f).coerceAtMost(1f)
    val glowAlpha = if (isWordActive) glowProgress * 0.45f * glowFactor else 0f
    val glowRadius = if (isWordActive) glowProgress * 12f * glowFactor else 0f

    val actualFontSize = if (isBackground) fontSize * 0.85f else fontSize
    val fontWeight = if (isLineActive || isLinePast) FontWeight.ExtraBold else FontWeight.SemiBold
    val glowPadding = 10.dp

    Box(
        modifier = Modifier
            .layout { measurable, constraints ->
                val glowPaddingPx = glowPadding.roundToPx()
                val looseConstraints = constraints.copy(
                    minWidth = 0,
                    maxWidth = constraints.maxWidth,
                    minHeight = 0,
                    maxHeight = Constraints.Infinity
                )
                val placeable = measurable.measure(looseConstraints)

                val coreWidth = (placeable.width - glowPaddingPx * 2).coerceAtLeast(0)
                val coreHeight = (placeable.height - glowPaddingPx * 2).coerceAtLeast(0)

                layout(coreWidth, coreHeight) {
                    placeable.place(-glowPaddingPx, -glowPaddingPx)
                }
            }
            .graphicsLayer {
                clip = false
                translationY = floatOffset * density
                scaleX = wordScale
                scaleY = wordScale
            }
    ) {
        Text(
            text = word.text,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = actualFontSize.sp,
                fontWeight = fontWeight,
                fontStyle = FontStyle.Normal,
                lineHeight = (actualFontSize * 1.35f).sp,
                fontFamily = lyricsFontFamily ?: MaterialTheme.typography.headlineMedium.fontFamily
            ),
            color = textColor.copy(alpha = if (isBackground) inactiveAlpha * 0.7f else inactiveAlpha),
            modifier = Modifier.padding(glowPadding)
        )

        if (isWordComplete || isWordActive || isLinePast) {
            Text(
                text = word.text,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = actualFontSize.sp,
                    fontWeight = fontWeight,
                    fontStyle = FontStyle.Normal,
                    lineHeight = (actualFontSize * 1.35f).sp,
                    fontFamily = lyricsFontFamily ?: MaterialTheme.typography.headlineMedium.fontFamily,
                    shadow = if (glowAlpha > 0f) {
                        Shadow(
                            color = textColor.copy(alpha = glowAlpha),
                            offset = Offset.Zero,
                            blurRadius = glowRadius.coerceAtLeast(1f)
                        )
                    } else {
                        null
                    }
                ),
                color = textColor.copy(alpha = if (isBackground) 0.75f else 1f),
                modifier = if (isWordActive && !isWordComplete) {
                    Modifier
                        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                        .drawWithContent {
                            drawContent()
                            val edgeWidth = fillTransitionWidth.dp.toPx()
                            val center = (size.width + edgeWidth * 2) * progress - edgeWidth
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color.Black, Color.Transparent),
                                    startX = center - edgeWidth,
                                    endX = center + edgeWidth
                                ),
                                blendMode = BlendMode.DstIn
                            )
                        }
                        .padding(glowPadding)
                } else {
                    Modifier.padding(glowPadding)
                }
            )
        }
    }
}
