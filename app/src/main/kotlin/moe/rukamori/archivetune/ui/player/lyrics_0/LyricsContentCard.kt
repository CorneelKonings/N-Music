package moe.rukamori.archivetune.ui.player.lyrics_0

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import moe.rukamori.archivetune.ui.state.PlayerUiState
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import kotlinx.coroutines.launch
import moe.rukamori.archivetune.lyrics.LyricsEntry
import moe.rukamori.archivetune.lyrics.LyricsRomanizationPreferences
import moe.rukamori.archivetune.constants.LyricsLineBlurKey
import moe.rukamori.archivetune.constants.LyricsClickKey
import moe.rukamori.archivetune.constants.LyricsScrollKey
import moe.rukamori.archivetune.constants.LyricsTextSizeKey
import moe.rukamori.archivetune.constants.LyricsLineSpacingKey
import moe.rukamori.archivetune.constants.LyricsRomanizeJapaneseKey
import moe.rukamori.archivetune.constants.LyricsRomanizeKoreanKey
import moe.rukamori.archivetune.constants.LyricsRomanizeChineseKey
import moe.rukamori.archivetune.constants.LyricsRomanizeHindiKey
import moe.rukamori.archivetune.constants.LyricsRomanizeOtherLanguagesKey
import moe.rukamori.archivetune.constants.LyricsV2BounceFactorKey
import moe.rukamori.archivetune.constants.LyricsV2GlowFactorKey
import moe.rukamori.archivetune.constants.LyricsV2FillTransitionWidthKey
import moe.rukamori.archivetune.constants.ShowLyricsPlayerControlsKey
import moe.rukamori.archivetune.ui.theme.rememberArchiveTuneLyricsFontFamily
import moe.rukamori.archivetune.utils.makeTimeString
import moe.rukamori.archivetune.utils.rememberPreference

import moe.rukamori.archivetune.ui.player.player_0.buttons.PlayerAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.vector.rememberVectorPainter

import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.snap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsContentCard(
    state: PlayerUiState,
    animateProgressProvider: () -> Float,
    progressMsProvider: () -> Long,
    onSearchClick: () -> Unit,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
    onLineClick: (Long) -> Unit,
    onAction: (PlayerAction) -> Unit,
    onSeek: (Float) -> Unit,
    onSeekStarted: () -> Unit
) {
    val context = LocalContext.current
    val screenHeightPx = remember { context.resources.displayMetrics.heightPixels.toFloat() }

    val (lyricsClick) = rememberPreference(LyricsClickKey, defaultValue = true)
    val (lyricsScroll) = rememberPreference(LyricsScrollKey, defaultValue = true)
    val (lyricsTextSize) = rememberPreference(LyricsTextSizeKey, defaultValue = 26f)
    val (lyricsLineSpacing) = rememberPreference(LyricsLineSpacingKey, defaultValue = 1.3f)
    val (lyricsLineBlur) = rememberPreference(LyricsLineBlurKey, defaultValue = true)
    val (bounceFactor) = rememberPreference(LyricsV2BounceFactorKey, defaultValue = 1f)
    val (glowFactor) = rememberPreference(LyricsV2GlowFactorKey, defaultValue = 1f)
    val (fillTransitionWidth) = rememberPreference(LyricsV2FillTransitionWidthKey, defaultValue = 8f)
    val (showPlayerControls) = rememberPreference(ShowLyricsPlayerControlsKey, defaultValue = true)
    val (romanizeChinese) = rememberPreference(LyricsRomanizeChineseKey, defaultValue = true)
    val (romanizeHindi) = rememberPreference(LyricsRomanizeHindiKey, defaultValue = true)
    val (romanizeJapanese) = rememberPreference(LyricsRomanizeJapaneseKey, defaultValue = true)
    val (romanizeKorean) = rememberPreference(LyricsRomanizeKoreanKey, defaultValue = true)
    val (romanizeOtherLanguages) = rememberPreference(LyricsRomanizeOtherLanguagesKey, defaultValue = true)
    val romanizationPreferences =
        remember(
            romanizeJapanese,
            romanizeKorean,
            romanizeChinese,
            romanizeHindi,
            romanizeOtherLanguages,
        ) {
            LyricsRomanizationPreferences(
                romanizeJapanese = romanizeJapanese,
                romanizeKorean = romanizeKorean,
                romanizeChinese = romanizeChinese,
                romanizeHindi = romanizeHindi,
                romanizeOther = romanizeOtherLanguages,
            )
        }
    val lyricsFontFamily = rememberArchiveTuneLyricsFontFamily()
    
    var isManualScrolling by remember { mutableStateOf(false) }
    val MANUAL_SCROLL_TIMEOUT_MS = 3000L
    val coroutineScope = rememberCoroutineScope()
    var manualScrollJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: androidx.compose.ui.geometry.Offset, source: NestedScrollSource): androidx.compose.ui.geometry.Offset {
                if (source == NestedScrollSource.UserInput || source == NestedScrollSource.Drag) {
                    isManualScrolling = true
                    manualScrollJob?.cancel()
                    manualScrollJob = coroutineScope.launch {
                        kotlinx.coroutines.delay(MANUAL_SCROLL_TIMEOUT_MS)
                        isManualScrolling = false
                    }
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }

    LaunchedEffect(state.currentLineIndex, state.isLyricsVisible, isManualScrolling, lyricsScroll) {
        if (state.isLyricsVisible && state.isSynced && state.currentLineIndex >= 0 && state.currentLineIndex < state.lyricsList.size) {
            if (lyricsScroll && !isManualScrolling) {
                val visibleInfo = lazyListState.layoutInfo
                val viewportHeight = visibleInfo.viewportSize.height
                val targetOffset = (viewportHeight * 0.35f).toInt()
                
                val distance = kotlin.math.abs(state.currentLineIndex - lazyListState.firstVisibleItemIndex)
                if (distance > 15) {
                    lazyListState.scrollToItem(
                        (state.currentLineIndex - 2).coerceAtLeast(0),
                        0
                    )
                }
                lazyListState.animateScrollToItem(
                    index = state.currentLineIndex,
                    scrollOffset = -targetOffset
                )
            }
        }
    }

    // Анимируем базовые цвета палитры
    val animatedDarkMuted by animateColorAsState(
        targetValue = Color(state.darkMutedColor),
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "LyricsBgAnimation"
    )

    // 3-точечный градиент с затемнением 70/30 под шапкой
    val cardBackgroundBrush = remember(animatedDarkMuted) {
        val startColor = lerp(animatedDarkMuted, Color.Black, 0.7f)
        val midColor = animatedDarkMuted
        val endColor = Color(0xFF121212)

        Brush.verticalGradient(
            0.0f to startColor,
            0.2f to midColor,
            1.0f to endColor
        )
    }

    val searchInteractionSource = remember { MutableInteractionSource() }
    val searchPressed by searchInteractionSource.collectIsPressedAsState()
    val searchScale by animateFloatAsState(if (searchPressed) 0.94f else 1f, spring(dampingRatio = 0.5f))

    Column(modifier = modifier.fillMaxSize()) {
        // Отступ под парящую шапку
        Spacer(modifier = Modifier.height(120.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .graphicsLayer {
                    translationY = screenHeightPx * (1f - animateProgressProvider())
                }
                .clipToBounds()
        ) {

            // Внутренний контейнер карточки текста
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight() // ФИКС: Растягиваем на всю высоту, чтобы карта не схлопывалась при лоадере
                    .layout { measurable, constraints ->
                        val borderPx = 1.dp.roundToPx()
                        // Расширяем контейнер по бокам и снизу, чтобы спрятать швы бордера
                        val expandedConstraints = constraints.copy(
                            minWidth = constraints.maxWidth + borderPx * 2,
                            maxWidth = constraints.maxWidth + borderPx * 2,
                            minHeight = constraints.maxHeight + borderPx,
                            maxHeight = constraints.maxHeight + borderPx
                        )
                        val placeable = measurable.measure(expandedConstraints)
                        layout(constraints.maxWidth, constraints.maxHeight) {
                            // Теперь контейнер гарантированно шире экрана на 2px и центрируется ровно
                            placeable.place(-borderPx, 0)
                        }
                    }
                    // Рисуем аккуратную верхнюю обводку (бока и низ уйдут за экран благодаря .layout)
                    .border(BorderStroke(1.dp, Color(0x22FFFFFF)), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(
                        if (state.isBlurBackgroundEnabled) {
                            Color.Black.copy(alpha = 0.2f)
                        } else {
                            Color.Transparent
                        }
                    )
                    .then(
                        if (!state.isBlurBackgroundEnabled) {
                            Modifier.background(cardBackgroundBrush)
                        } else Modifier
                    )
            ) {

                if (state.lyricsList.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (state.isLoadingLyrics || state.isRefreshingLyrics) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 4.dp,
                                modifier = Modifier.size(48.dp)
                            )
                        } else {
                            Button(
                                onClick = onSearchClick,
                                interactionSource = searchInteractionSource,
                                shape = RoundedCornerShape(28.dp),
                                border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0x20FFFFFF),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                                modifier = Modifier.graphicsLayer { scaleX = searchScale; scaleY = searchScale }
                            ) {
                                Text("Search lyrics for this track", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }

                            if (state.lyricsError != null) {
                                Text(
                                    text = state.lyricsError,
                                    color = Color(0x80FFFFFF),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 16.dp)
                                )
                            }
                        }
                    }
                } else {
                    val currentMsState = produceState(initialValue = 0L, progressMsProvider) {
                        while (true) {
                            value = progressMsProvider()
                            kotlinx.coroutines.delay(32)
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(nestedScrollConnection)
                                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                                .drawWithContent {
                                    drawContent()
                                    val topFadePx = 48.dp.toPx()
                                    val bottomFadePx = 100.dp.toPx()
                                    val size = this.size

                                    // Прозрачное сглаживание сверху
                                    drawRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black),
                                            startY = 0f,
                                            endY = topFadePx
                                        ),
                                        blendMode = BlendMode.DstIn
                                    )
                                    // Прозрачное сглаживание снизу
                                    drawRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color.Black, Color.Transparent),
                                            startY = size.height - bottomFadePx,
                                            endY = size.height
                                        ),
                                        blendMode = BlendMode.DstIn
                                    )
                                },
                            contentPadding = PaddingValues(top = 32.dp, bottom = 220.dp, start = 0.dp, end = 0.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            itemsIndexed(
                                items = state.lyricsList,
                                key = { index, line -> "${line.time}_$index" },
                                contentType = { _, line ->
                                    when {
                                        line == LyricsEntry.HEAD_LYRICS_ENTRY -> "head"
                                        line.isInstrumental -> "instrumental"
                                        line.words != null && state.isSynced -> "wordSynced"
                                        else -> "lineSynced"
                                    }
                                }
                            ) { index, line ->
                                val syncOffset = state.lyricsSyncOffset
                                val nextTime = remember(state.lyricsList, index) {
                                    state.lyricsList.getOrNull(index + 1)?.time ?: Long.MAX_VALUE
                                }
                                val isActive by remember(line.time, nextTime, syncOffset) {
                                    derivedStateOf {
                                        val currentMs = currentMsState.value + syncOffset
                                        val target = currentMs + 300L
                                        target >= line.time && target < nextTime
                                    }
                                }

                                val isPast by remember(line.time, nextTime, syncOffset) {
                                    derivedStateOf {
                                        val currentMs = currentMsState.value + syncOffset
                                        currentMs >= nextTime
                                    }
                                }

                                val distanceFromActive = if (state.isSynced && state.currentLineIndex >= 0) kotlin.math.abs(index - state.currentLineIndex) else 0

                                if (line == LyricsEntry.HEAD_LYRICS_ENTRY) {
                                    Spacer(modifier = Modifier.height(120.dp))
                                } else if (line.isInstrumental) {
                                    val instrAlpha = when {
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
                                        else -> 0.35f
                                    }

                                    val targetInstrBlur = when {
                                        isActive || !lyricsLineBlur || isManualScrolling -> 0f
                                        distanceFromActive == 1 -> 2f
                                        distanceFromActive == 2 -> 5f
                                        else -> 12f
                                    }

                                    val animatedInstrBlur by animateFloatAsState(
                                        targetValue = targetInstrBlur,
                                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                                        label = "instrBlur"
                                    )

                                    val animatedInstrScale by animateFloatAsState(
                                        targetValue = if (isActive) 1f else 0.95f,
                                        animationSpec = tween(durationMillis = 166, easing = FastOutSlowInEasing),
                                        label = "instrScale"
                                    )

                                    val animatedInstrAlpha by animateFloatAsState(
                                        targetValue = instrAlpha,
                                        animationSpec = tween(durationMillis = if (isActive) 330 else 500, easing = FastOutSlowInEasing),
                                        label = "instrAlpha"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 8.dp)
                                            .then(
                                                if (animatedInstrBlur > 0f) {
                                                    Modifier.blur(radiusX = animatedInstrBlur.dp, radiusY = animatedInstrBlur.dp, edgeTreatment = androidx.compose.ui.draw.BlurredEdgeTreatment.Unbounded)
                                                } else {
                                                    Modifier
                                                }
                                            )
                                            .graphicsLayer {
                                                scaleX = animatedInstrScale
                                                scaleY = animatedInstrScale
                                                alpha = animatedInstrAlpha
                                            }
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                if (lyricsClick && line.time >= 0L) onLineClick(line.time)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        InstrumentalBreakItem(
                                            durationMs = line.durationMs,
                                            currentPositionMs = currentMsState.value + syncOffset,
                                            startTimeMs = line.time,
                                            textColor = Color.White,
                                            inactiveAlpha = 1f,
                                            modifier = Modifier
                                        )
                                    }
                                } else {
                                    LyricsLineItem(
                                        item = line,
                                        isActive = isActive,
                                        isPast = isPast,
                                        currentPositionMs = currentMsState.value + syncOffset,
                                        textColor = Color.White,
                                        inactiveAlpha = 0.35f,
                                        lyricsTextSize = lyricsTextSize,
                                        lyricsLineSpacing = lyricsLineSpacing,
                                        lyricsClick = lyricsClick,
                                        lyricsFontFamily = lyricsFontFamily,
                                        bounceFactor = bounceFactor,
                                        glowFactor = glowFactor,
                                        fillTransitionWidth = fillTransitionWidth,
                                        onLineClick = onLineClick,
                                        distanceFromActive = distanceFromActive,
                                        isManualScrolling = isManualScrolling,
                                        lyricsLineBlur = lyricsLineBlur,
                                        romanizationPreferences = romanizationPreferences
                                    )
                                }
                            }
                        }

                        if (state.isRefreshingLyrics) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 4.dp,
                                modifier = Modifier
                                    .size(48.dp)
                                    .align(Alignment.Center)
                            )
                        }
                        
                        if (showPlayerControls) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 24.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    modifier = Modifier
                                        .height(72.dp)
                                        .clip(RoundedCornerShape(36.dp))
                                        .background(Color(0x33000000))
                                        .border(BorderStroke(1.dp, Color(0x33FFFFFF)), RoundedCornerShape(36.dp))
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .clickable { onAction(PlayerAction.Previous) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = rememberVectorPainter(Icons.Rounded.SkipPrevious),
                                            contentDescription = "Previous",
                                            tint = Color.White,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .clickable { onAction(PlayerAction.PlayPause) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = rememberVectorPainter(if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow),
                                            contentDescription = "Play/Pause",
                                            tint = Color.Black,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .clickable { onAction(PlayerAction.Next) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = rememberVectorPainter(Icons.Rounded.SkipNext),
                                            contentDescription = "Next",
                                            tint = Color.White,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                    
                                    var sliderPosition by remember { mutableStateOf(0f) }
                                    val isDragging = remember { mutableStateOf(false) }
                                    val interactionSource = remember { MutableInteractionSource() }
                                    val isPressed by interactionSource.collectIsPressedAsState()
                                    val isDragged by interactionSource.collectIsDraggedAsState()
                                    val isInteracting = isPressed || isDragged
                                    
                                    val maxRange = maxOf(1f, state.durationMs.toFloat())
                                    val baseProgress = if (isDragging.value) sliderPosition else currentMsState.value.toFloat()
                                    
                                    val animatedProgress by animateFloatAsState(
                                        targetValue = baseProgress.coerceIn(0f, maxRange),
                                        animationSpec = if (isDragging.value) snap() else tween(durationMillis = 250, easing = LinearEasing),
                                        label = "SliderLineFluidAnimation"
                                    )
                                    
                                    Column(
                                        modifier = Modifier.width(130.dp),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Slider(
                                            value = baseProgress.coerceIn(0f, maxRange),
                                            onValueChange = {
                                                isDragging.value = true
                                                sliderPosition = it
                                                onSeekStarted()
                                            },
                                            onValueChangeFinished = {
                                                isDragging.value = false
                                                onSeek(sliderPosition)
                                            },
                                            valueRange = 0f..maxRange,
                                            interactionSource = interactionSource,
                                            track = { _ ->
                                                val fraction = (animatedProgress / maxRange).coerceIn(0f, 1f)
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(8.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.White.copy(alpha = 0.25f)),
                                                    contentAlignment = Alignment.CenterStart
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth(fraction)
                                                            .fillMaxHeight()
                                                            .clip(CircleShape)
                                                            .background(Color.White)
                                                    )
                                                }
                                            },
                                            thumb = { Box(modifier = Modifier.size(0.dp)) },
                                            colors = SliderDefaults.colors(
                                                thumbColor = Color.Transparent,
                                                activeTickColor = Color.Transparent,
                                                inactiveTickColor = Color.Transparent,
                                                disabledThumbColor = Color.Transparent
                                            ),
                                            modifier = Modifier.fillMaxWidth().height(24.dp)
                                        )
                                        
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = makeTimeString(baseProgress.toLong()),
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                                                color = Color.White.copy(alpha = 0.7f)
                                            )
                                            Text(
                                                text = makeTimeString(state.durationMs),
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                                                color = Color.White.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
