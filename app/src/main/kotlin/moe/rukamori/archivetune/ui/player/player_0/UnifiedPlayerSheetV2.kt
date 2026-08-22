package moe.rukamori.archivetune.ui.player.player_0

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import moe.rukamori.archivetune.constants.FloatingToolbarBottomPadding
import moe.rukamori.archivetune.constants.MiniPlayerBottomSpacing
import moe.rukamori.archivetune.constants.MiniPlayerHeight
import moe.rukamori.archivetune.ui.player.lyrics_0.LyricsOptionsMenu
import moe.rukamori.archivetune.ui.player.player_0.buttons.PlayerAction
import moe.rukamori.archivetune.ui.player.player_0.scoped.PlayerSheetPredictiveBackHandler
import moe.rukamori.archivetune.ui.player.player_0.scoped.SheetMotionController
import moe.rukamori.archivetune.ui.player.player_0.scoped.SheetVerticalDragGestureHandler
import moe.rukamori.archivetune.ui.player.player_0.scoped.playerSheetVerticalDragGesture
import moe.rukamori.archivetune.ui.player.player_0.scoped.rememberFullPlayerVisualState
import moe.rukamori.archivetune.ui.player.player_0.scoped.rememberSheetVisualState
import moe.rukamori.archivetune.ui.player.player_0.sett.FullPlayerOptionsMenu
import moe.rukamori.archivetune.ui.player.player_0.sett.PlayerMenuScreen
import moe.rukamori.archivetune.ui.state.PlayerSheetState
import moe.rukamori.archivetune.ui.state.PlayerUiState
import moe.rukamori.archivetune.ui.state.UpdateState
import moe.rukamori.archivetune.ui.menu.EqualizerDialog
import moe.rukamori.archivetune.ui.menu.TempoPitchDialog
import moe.rukamori.archivetune.ui.menu.AddToPlaylistDialog
import androidx.activity.compose.BackHandler
import android.media.audiofx.AudioEffect
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import moe.rukamori.archivetune.R
import moe.rukamori.archivetune.LocalPlayerConnection
import kotlin.math.roundToInt

@Composable
fun UnifiedPlayerSheetV2(
    state: PlayerUiState,
    updateState: UpdateState = UpdateState.NoUpdate,
    onAction: (PlayerAction) -> Unit,
    onCloseLyricsClick: () -> Unit,
    onSearchLyricsClick: () -> Unit,
    onSeek: (Float) -> Unit,
    onBackgroundStyleChanged: (Boolean) -> Unit,
    onImmersiveChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
    onSeekStarted: () -> Unit,
    bottomBarHeight: Dp = 0.dp,
    onExpansionFractionChanged: (Float) -> Unit = {},
    onLyricsClick: () -> Unit = {}
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val playerConnection = LocalPlayerConnection.current

    val activityResultLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    var isLyricsMenuVisible by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var menuInitialScreen by remember { mutableStateOf(PlayerMenuScreen.SETTINGS) }
    var showEqualizerDialog by remember { mutableStateOf(false) }
    var showPitchTempoDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenHeightDp = maxHeight
        val screenHeightPx = with(density) { screenHeightDp.toPx() }

        val navigationBarsPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val navigationBarsPx = with(density) { navigationBarsPadding.toPx() }
        val miniHeightPx = with(density) { MiniPlayerHeight.toPx() }

        val expandedY = 0f

        val totalOffsetPx = with(density) {
            val bottomToolbarPadding = if (bottomBarHeight > 0.dp) FloatingToolbarBottomPadding else 0.dp
            (bottomBarHeight + bottomToolbarPadding + MiniPlayerBottomSpacing + MiniPlayerHeight).toPx()
        }

        val collapsedY = if (state.trackUrl.isEmpty()) {
            screenHeightPx
        } else {
            screenHeightPx - navigationBarsPx - totalOffsetPx
        }

        val scope = rememberCoroutineScope()
        val mutatorMutex = remember { MutatorMutex() }
        val velocityTracker = remember { VelocityTracker() }

        var currentSheetState by remember { mutableStateOf(PlayerSheetState.COLLAPSED) }
        val translationY = remember { Animatable(collapsedY) }
        val expansionFraction = remember { Animatable(0f) }
        val visualOvershootScaleY = remember { Animatable(1f) }
        var predictiveBackProgress by remember { mutableStateOf(0f) }

        LaunchedEffect(Unit) {
            snapshotFlow { expansionFraction.value }.collect { fraction ->
                onExpansionFractionChanged(fraction)
            }
        }

        LaunchedEffect(collapsedY) {
            if (currentSheetState == PlayerSheetState.COLLAPSED) {
                if (translationY.value == screenHeightPx && collapsedY < screenHeightPx) {
                    translationY.animateTo(collapsedY, spring(stiffness = Spring.StiffnessMediumLow))
                } else {
                    translationY.snapTo(collapsedY)
                }
            }
        }

        // Анимация открывания/закрывания текстов песен
        val lyricsTransitionFraction = remember { Animatable(0f) }
        LaunchedEffect(state.isLyricsVisible) {
            lyricsTransitionFraction.animateTo(
                targetValue = if (state.isLyricsVisible) 1f else 0f,
                animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
            )
        }

        var lyricsSwipeOffsetY by remember { mutableStateOf(0f) }

        val lyricsFractionProvider = {
            val dragFraction = if (screenHeightPx > 0f) {
                (lyricsSwipeOffsetY / (screenHeightPx * 0.35f)).coerceIn(0f, 1f)
            } else 0f
            lyricsTransitionFraction.value * (1f - dragFraction)
        }

        var wasLyricsFullyOpened by remember { mutableStateOf(false) }

        LaunchedEffect(state.isLyricsVisible) {
            if (!state.isLyricsVisible) {
                wasLyricsFullyOpened = false
            }
        }

        LaunchedEffect(Unit) {
            snapshotFlow { lyricsTransitionFraction.value }.collect { fraction ->
                if (fraction > 0.8f && state.isLyricsVisible) {
                    wasLyricsFullyOpened = true
                }

                if (wasLyricsFullyOpened && fraction < 0.2f && state.isLyricsVisible) {
                    wasLyricsFullyOpened = false
                    onCloseLyricsClick()
                }
            }
        }

        LaunchedEffect(Unit) {
            snapshotFlow { expansionFraction.value }.collect { fraction ->
                if (fraction == 0f && state.isLyricsVisible) {
                    onCloseLyricsClick()
                }
            }
        }

        val motionController = remember(translationY, expansionFraction, mutatorMutex) {
            SheetMotionController(
                translationY = translationY,
                expansionFraction = expansionFraction,
                mutex = mutatorMutex,
                defaultAnimationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow),
                expandedY = expandedY
            )
        }

        // Сворачивание по внешнему запросу из состояния
        LaunchedEffect(state.isSheetCollapseRequested) {
            if (state.isSheetCollapseRequested) {
                if (currentSheetState == PlayerSheetState.EXPANDED || expansionFraction.value > 0.01f) {
                    scope.launch {
                        motionController.animateTo(false, true, collapsedY)
                        currentSheetState = PlayerSheetState.COLLAPSED
                    }
                }
            }
        }

        val sheetVisualState = rememberSheetVisualState(
            showPlayerContentArea = true,
            collapsedStateHorizontalPadding = 12.dp,
            predictiveBackCollapseProgress = predictiveBackProgress,
            currentSheetContentState = currentSheetState,
            playerContentExpansionFraction = expansionFraction,
            containerHeight = screenHeightDp,
            currentSheetTranslationY = translationY,
            sheetCollapsedTargetY = collapsedY,
            isPlaying = state.isPlaying,
            hasCurrentSong = true
        )

        val fullPlayerVisualState = rememberFullPlayerVisualState(
            expansionFraction = expansionFraction,
            initialOffsetY = 150f
        )

        val dragHandler = remember(motionController, sheetVisualState) {
            SheetVerticalDragGestureHandler(
                scope = scope,
                velocityTracker = velocityTracker,
                densityProvider = { density },
                sheetMotionController = motionController,
                playerContentExpansionFraction = expansionFraction,
                currentSheetTranslationY = translationY,
                expandedYProvider = { expandedY },
                collapsedYProvider = { collapsedY },
                miniHeightPxProvider = { miniHeightPx },
                currentSheetStateProvider = { currentSheetState },
                visualOvershootScaleY = visualOvershootScaleY,
                onDraggingChange = {},
                onDraggingPlayerAreaChange = {},
                onAnimateSheet = { targetExpanded, spec, velocity ->
                    motionController.animateTo(
                        targetExpanded = targetExpanded,
                        canExpand = true,
                        collapsedY = collapsedY,
                        animationSpec = spec ?: spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow),
                        initialVelocity = velocity
                    )
                },
                onExpandSheetState = { currentSheetState = PlayerSheetState.EXPANDED },
                onCollapseSheetState = { currentSheetState = PlayerSheetState.COLLAPSED }
            )
        }

        PlayerSheetPredictiveBackHandler(
            enabled = currentSheetState == PlayerSheetState.EXPANDED && !state.isLyricsVisible,
            currentSheetState = currentSheetState,
            predictiveBackFractionValue = predictiveBackProgress,
            onPredictiveBackFractionChanged = { predictiveBackProgress = it },
            sheetCollapsedTargetY = collapsedY,
            sheetExpandedTargetY = expandedY,
            sheetMotionController = motionController,
            animationDurationMs = 300,
            onSwipeEdgeChanged = {},
            onCollapse = {
                scope.launch {
                    motionController.animateTo(false, true, collapsedY)
                    currentSheetState = PlayerSheetState.COLLAPSED
                }
            },
            onExpand = {
                scope.launch {
                    motionController.animateTo(true, true, collapsedY)
                    currentSheetState = PlayerSheetState.EXPANDED
                }
            },
            registrationKey = currentSheetState
        )

        val colorTop = Color(state.gradientColor)
        val colorBottom = Color(0xFF121212)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = (expansionFraction.value * 0.6f).coerceIn(0f, 0.6f)
                }
                .background(Color.Black)
        )

        Box(
            modifier = modifier
                .fillMaxSize()
                .offset {
                    IntOffset(
                        x = 0,
                        y = sheetVisualState.visualSheetTranslationYProvider().roundToInt()
                    )
                }
                .graphicsLayer {
                    scaleY = visualOvershootScaleY.value
                    val paddingX = sheetVisualState.currentHorizontalPaddingStartPxProvider()
                    val currentWidth = size.width - (paddingX * 2)
                    scaleX = currentWidth / size.width
                }
                .playerSheetVerticalDragGesture(
                    enabled = true,
                    handler = dragHandler
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val expansionFractionVal = expansionFraction.value
                        // Фикс скругления при 99%+ раскрытии шторки
                        val radiusTop = if (expansionFractionVal > 0.99f) {
                            0f
                        } else {
                            sheetVisualState.overallSheetTopCornerRadiusProvider().toPx()
                        }
                        val radiusBottom = sheetVisualState.playerContentActualBottomRadiusProvider().toPx()
                        val dynamicHeight = sheetVisualState.playerContentAreaHeightPxProvider()

                        shape = object : Shape {
                            override fun createOutline(
                                size: Size,
                                layoutDirection: LayoutDirection,
                                density: Density
                            ): Outline {
                                val targetSize = if (expansionFractionVal > 0.99f) {
                                    size
                                } else {
                                    Size(size.width, dynamicHeight)
                                }
                                return RoundedCornerShape(
                                    topStart = radiusTop,
                                    topEnd = radiusTop,
                                    bottomStart = radiusBottom,
                                    bottomEnd = radiusBottom
                                ).createOutline(targetSize, layoutDirection, density)
                            }
                        }
                        clip = true
                    }
                    .background(Brush.verticalGradient(listOf(colorTop, colorBottom)))
            ) {
                UnifiedPlayerSheetLayers(
                    state = state,
                    updateState = updateState,
                    expansionFractionProvider = { expansionFraction.value },
                    lyricsFractionProvider = lyricsFractionProvider,
                    fullPlayerVisualState = fullPlayerVisualState,
                    lyricsSwipeOffsetY = lyricsSwipeOffsetY,
                    onLyricsSwipeOffsetChanged = { lyricsSwipeOffsetY = it },
                    onAction = onAction,
                    onCloseLyricsClick = onCloseLyricsClick,
                    onMoreLyricsClick = { isLyricsMenuVisible = true },
                    onSearchLyricsClick = onSearchLyricsClick,
                    onCollapseClick = {
                        scope.launch {
                            motionController.animateTo(false, true, collapsedY)
                            currentSheetState = PlayerSheetState.COLLAPSED
                        }
                    },
                    onExpandClick = {
                        scope.launch {
                            motionController.animateTo(true, true, collapsedY)
                            currentSheetState = PlayerSheetState.EXPANDED
                        }
                    },
                    onSeek = onSeek,
                    onSeekStarted = onSeekStarted,
                    onBackgroundStyleChanged = onBackgroundStyleChanged,
                    onImmersiveChanged = onImmersiveChanged,
                    onOpenSettingsMenu = { screen ->
                        menuInitialScreen = screen
                        showSettingsMenu = true
                    }
                )
            }
        }

        LyricsOptionsMenu(
            isVisible = isLyricsMenuVisible,
            onDismiss = { isLyricsMenuVisible = false },
            onAction = onAction,
            state = state
        )

        FullPlayerOptionsMenu(
            expanded = showSettingsMenu,
            initialScreen = menuInitialScreen,
            onDismissRequest = { showSettingsMenu = false },
            state = state,
            updateState = updateState,
            onBackgroundStyleChanged = onBackgroundStyleChanged,
            onImmersiveChanged = onImmersiveChanged,
            onOpenEqualizer = { showSettingsMenu = false; showEqualizerDialog = true },
            onOpenPlaybackSpeed = { showSettingsMenu = false; showPitchTempoDialog = true },
            onOpenAddToPlaylist = { showSettingsMenu = false; showAddToPlaylistDialog = true },
            onAction = onAction
        )

        // Диалог Эквалайзера
        if (showEqualizerDialog) {
            EqualizerDialog(
                onDismiss = { showEqualizerDialog = false },
                openSystemEqualizer = {
                    try {
                        val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                            playerConnection?.localPlayer?.audioSessionId?.let {
                                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, it)
                            }
                            putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                            putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                        }
                        if (intent.resolveActivity(context.packageManager) != null) {
                            activityResultLauncher.launch(intent)
                        } else {
                            Toast.makeText(context, context.getString(R.string.system_equalizer_not_found), Toast.LENGTH_SHORT).show()
                        }
                    } catch (_: Exception) {
                        Toast.makeText(context, context.getString(R.string.system_equalizer_not_found), Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // Диалог Скорости воспроизведения
        if (showPitchTempoDialog) {
            TempoPitchDialog(onDismiss = { showPitchTempoDialog = false })
        }

        // Диалог Добавления в плейлист
        if (showAddToPlaylistDialog && state.trackUrl.isNotBlank()) {
            AddToPlaylistDialog(
                isVisible = showAddToPlaylistDialog,
                onGetSong = { listOf(state.trackUrl) },
                onDismiss = { showAddToPlaylistDialog = false }
            )
        }
    }
}
