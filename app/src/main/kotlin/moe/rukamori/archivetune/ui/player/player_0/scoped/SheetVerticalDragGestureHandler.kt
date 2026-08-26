package moe.rukamori.archivetune.ui.player.player_0.scoped

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import moe.rukamori.archivetune.ui.state.PlayerSheetState
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Инкапсулирует состояние жеста вертикального перетаскивания и разрешение целевой точки для шторки плеера.
 * Поведение идентично оригинальной реализации, но отвязано от ViewModel.
 */
internal class SheetVerticalDragGestureHandler(
    private val scope: CoroutineScope,
    private val velocityTracker: VelocityTracker,
    private val densityProvider: () -> Density,
    private val sheetMotionController: SheetMotionController,
    private val playerContentExpansionFraction: Animatable<Float, AnimationVector1D>,
    private val currentSheetTranslationY: Animatable<Float, AnimationVector1D>,
    private val layerTwoFraction: Animatable<Float, AnimationVector1D>,
    private val expandedYProvider: () -> Float,
    private val collapsedYProvider: () -> Float,
    private val miniHeightPxProvider: () -> Float,
    private val screenHeightPxProvider: () -> Float,
    private val screenWidthPxProvider: () -> Float,
    private val currentSheetStateProvider: () -> PlayerSheetState,
    private val visualOvershootScaleY: Animatable<Float, AnimationVector1D>,
    private val onDraggingChange: (Boolean) -> Unit,
    private val onDraggingPlayerAreaChange: (Boolean) -> Unit,
    private val onAnimateSheet: suspend (
        targetExpanded: Boolean,
        animationSpec: AnimationSpec<Float>?,
        initialVelocity: Float
    ) -> Unit,
    private val onExpandSheetState: () -> Unit,
    private val onCollapseSheetState: () -> Unit,
    private val onSelectLayerTwoPage: (Int) -> Unit,
    private val onExpandLayerTwo: () -> Unit,
    private val onCollapseLayerTwo: () -> Unit
) {
    private var initialFractionOnDragStart = 0f
    private var initialYOnDragStart = 0f
    private var initialLayerTwoFractionOnDragStart = 0f
    private var accumulatedDragYSinceStart = 0f
    private var dragSnapJob: Job? = null

    fun onDragStart(position: Offset = Offset.Zero) {
        dragSnapJob?.cancel()
        dragSnapJob = scope.launch(start = CoroutineStart.UNDISPATCHED) {
            sheetMotionController.stop()
            layerTwoFraction.stop()
        }
        onDraggingChange(true)
        onDraggingPlayerAreaChange(true)
        velocityTracker.resetTracking()
        initialFractionOnDragStart = playerContentExpansionFraction.value
        initialYOnDragStart = currentSheetTranslationY.value
        initialLayerTwoFractionOnDragStart = layerTwoFraction.value
        accumulatedDragYSinceStart = 0f

        val expandedY = expandedYProvider()
        if (currentSheetTranslationY.value <= expandedY + 1f && layerTwoFraction.value < 0.05f) {
            val screenWidth = screenWidthPxProvider()
            val targetPage = if (position.x < screenWidth / 2f) 0 else 1
            onSelectLayerTwoPage(targetPage)
        }
    }

    fun onVerticalDrag(
        uptimeMillis: Long,
        position: Offset,
        dragAmount: Float
    ) {
        accumulatedDragYSinceStart += dragAmount
        velocityTracker.addPosition(uptimeMillis, Offset(0f, accumulatedDragYSinceStart))

        val expandedY = expandedYProvider()
        val collapsedY = collapsedYProvider()
        val miniHeightPx = miniHeightPxProvider()
        val layerTwoDistance = (screenHeightPxProvider() * 0.4f).coerceAtLeast(300f)

        val currentY = currentSheetTranslationY.value
        val currentL2 = layerTwoFraction.value

        if (dragAmount < 0) {
            if (currentY > expandedY) {
                val dragFrame = computeSheetVerticalDragFrame(
                    currentTranslationY = currentY,
                    dragAmount = dragAmount,
                    expandedY = expandedY,
                    collapsedY = collapsedY,
                    miniHeightPx = miniHeightPx,
                    initialFractionOnDragStart = initialFractionOnDragStart,
                    initialYOnDragStart = initialYOnDragStart
                )
                if (dragFrame.translationY < expandedY) {
                    val overshootPx = expandedY - dragFrame.translationY
                    val deltaL2 = overshootPx / layerTwoDistance
                    val newL2 = (currentL2 + deltaL2).coerceIn(0f, 1f)
                    dragSnapJob?.cancel()
                    dragSnapJob = scope.launch(start = CoroutineStart.UNDISPATCHED) {
                        sheetMotionController.snapTo(expandedY, 1f)
                        layerTwoFraction.snapTo(newL2)
                    }
                } else {
                    val safeTranslationY = dragFrame.translationY.coerceAtLeast(expandedY)
                    val safeExpansionFraction = dragFrame.expansionFraction.coerceIn(0f, 1f)
                    dragSnapJob?.cancel()
                    dragSnapJob = scope.launch(start = CoroutineStart.UNDISPATCHED) {
                        sheetMotionController.snapTo(safeTranslationY, safeExpansionFraction)
                        if (currentL2 > 0f) {
                            layerTwoFraction.snapTo(0f)
                        }
                    }
                }
            } else {
                val deltaL2 = -dragAmount / layerTwoDistance
                val newL2 = (currentL2 + deltaL2).coerceIn(0f, 1f)
                dragSnapJob?.cancel()
                dragSnapJob = scope.launch(start = CoroutineStart.UNDISPATCHED) {
                    if (currentY != expandedY || playerContentExpansionFraction.value != 1f) {
                        sheetMotionController.snapTo(expandedY, 1f)
                    }
                    layerTwoFraction.snapTo(newL2)
                }
            }
        } else {
            if (currentL2 > 0f) {
                val deltaL2 = dragAmount / layerTwoDistance
                if (currentL2 >= deltaL2) {
                    val newL2 = (currentL2 - deltaL2).coerceIn(0f, 1f)
                    dragSnapJob?.cancel()
                    dragSnapJob = scope.launch(start = CoroutineStart.UNDISPATCHED) {
                        if (currentY != expandedY || playerContentExpansionFraction.value != 1f) {
                            sheetMotionController.snapTo(expandedY, 1f)
                        }
                        layerTwoFraction.snapTo(newL2)
                    }
                } else {
                    val consumedPx = currentL2 * layerTwoDistance
                    val remainingDrag = dragAmount - consumedPx
                    val dragFrame = computeSheetVerticalDragFrame(
                        currentTranslationY = expandedY,
                        dragAmount = remainingDrag,
                        expandedY = expandedY,
                        collapsedY = collapsedY,
                        miniHeightPx = miniHeightPx,
                        initialFractionOnDragStart = 1f,
                        initialYOnDragStart = expandedY
                    )
                    val safeTranslationY = dragFrame.translationY.coerceAtLeast(expandedY)
                    val safeExpansionFraction = dragFrame.expansionFraction.coerceIn(0f, 1f)
                    dragSnapJob?.cancel()
                    dragSnapJob = scope.launch(start = CoroutineStart.UNDISPATCHED) {
                        layerTwoFraction.snapTo(0f)
                        sheetMotionController.snapTo(safeTranslationY, safeExpansionFraction)
                    }
                }
            } else {
                val dragFrame = computeSheetVerticalDragFrame(
                    currentTranslationY = currentY,
                    dragAmount = dragAmount,
                    expandedY = expandedY,
                    collapsedY = collapsedY,
                    miniHeightPx = miniHeightPx,
                    initialFractionOnDragStart = initialFractionOnDragStart,
                    initialYOnDragStart = initialYOnDragStart
                )
                val safeTranslationY = dragFrame.translationY.coerceAtLeast(expandedY)
                val safeExpansionFraction = dragFrame.expansionFraction.coerceIn(0f, 1f)
                dragSnapJob?.cancel()
                dragSnapJob = scope.launch(start = CoroutineStart.UNDISPATCHED) {
                    if (currentL2 > 0f) {
                        layerTwoFraction.snapTo(0f)
                    }
                    sheetMotionController.snapTo(safeTranslationY, safeExpansionFraction)
                }
            }
        }
    }

    fun onDragEnd() {
        dragSnapJob?.cancel()
        dragSnapJob = null
        onDraggingChange(false)
        onDraggingPlayerAreaChange(false)

        val verticalVelocity = velocityTracker.calculateVelocity().y
        val currentFraction = playerContentExpansionFraction.value
        val currentL2 = layerTwoFraction.value
        val minDragThresholdPx = with(densityProvider()) { 5.dp.toPx() }
        val velocityThreshold = 1000f
        val layerTwoDistance = (screenHeightPxProvider() * 0.4f).coerceAtLeast(300f)

        if (currentL2 > 0f) {
            val targetLayerTwoExpanded = when {
                verticalVelocity < -velocityThreshold -> true
                verticalVelocity > velocityThreshold -> false
                initialLayerTwoFractionOnDragStart > 0.5f -> currentL2 > 0.75f
                else -> currentL2 > 0.25f
            }

            scope.launch {
                if (targetLayerTwoExpanded) {
                    launch {
                        visualOvershootScaleY.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = 0.78f,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                    }
                    launch {
                        layerTwoFraction.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = 0.78f,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            initialVelocity = -verticalVelocity / layerTwoDistance
                        )
                    }
                    onExpandSheetState()
                    onExpandLayerTwo()
                } else {
                    launch {
                        layerTwoFraction.animateTo(
                            targetValue = 0f,
                            animationSpec = spring(
                                dampingRatio = 0.78f,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                    }
                    onCollapseLayerTwo()
                    if (currentFraction < 0.99f || currentSheetTranslationY.value > expandedYProvider()) {
                        val targetState = resolveVerticalSheetTargetState(
                            currentSheetContentState = currentSheetStateProvider(),
                            verticalVelocity = verticalVelocity,
                            velocityThreshold = velocityThreshold,
                            currentFraction = currentFraction,
                            accumulatedDragY = accumulatedDragYSinceStart,
                            minDragThresholdPx = minDragThresholdPx
                        )
                        if (targetState == PlayerSheetState.EXPANDED) {
                            launch {
                                onAnimateSheet(
                                    true,
                                    spring(
                                        dampingRatio = 0.78f,
                                        stiffness = Spring.StiffnessMediumLow
                                    ),
                                    verticalVelocity
                                )
                            }
                            onExpandSheetState()
                        } else {
                            val dynamicDamping = collapseSpringDampingForFraction(currentFraction)
                            launch {
                                val initialSquash = collapseInitialSquashForFraction(currentFraction)
                                visualOvershootScaleY.snapTo(initialSquash)
                                visualOvershootScaleY.animateTo(
                                    targetValue = 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessVeryLow
                                    )
                                )
                            }
                            launch {
                                onAnimateSheet(
                                    false,
                                    spring(
                                        dampingRatio = dynamicDamping,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    verticalVelocity
                                )
                            }
                            onCollapseSheetState()
                        }
                    }
                }
            }
        } else {
            val targetState = resolveVerticalSheetTargetState(
                currentSheetContentState = currentSheetStateProvider(),
                verticalVelocity = verticalVelocity,
                velocityThreshold = velocityThreshold,
                currentFraction = currentFraction,
                accumulatedDragY = accumulatedDragYSinceStart,
                minDragThresholdPx = minDragThresholdPx
            )

            scope.launch {
                if (targetState == PlayerSheetState.EXPANDED) {
                    launch {
                        visualOvershootScaleY.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = 0.78f,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                    }
                    launch {
                        onAnimateSheet(
                            true,
                            spring(
                                dampingRatio = 0.78f,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            verticalVelocity
                        )
                    }
                    onExpandSheetState()
                } else {
                    val dynamicDamping = collapseSpringDampingForFraction(currentFraction)
                    launch {
                        val initialSquash = collapseInitialSquashForFraction(currentFraction)
                        visualOvershootScaleY.snapTo(initialSquash)
                        visualOvershootScaleY.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessVeryLow
                            )
                        )
                    }
                    launch {
                        onAnimateSheet(
                            false,
                            spring(
                                dampingRatio = dynamicDamping,
                                stiffness = Spring.StiffnessLow
                            ),
                            verticalVelocity
                        )
                    }
                    onCollapseSheetState()
                    onCollapseLayerTwo()
                }
            }
        }

        accumulatedDragYSinceStart = 0f
    }

    fun onDragCancel() {
        onDragEnd()
    }
}

/**
 * Модификатор для привязки обработчика жестов к UI-компоненту.
 */
internal fun Modifier.playerSheetVerticalDragGesture(
    enabled: Boolean,
    handler: SheetVerticalDragGestureHandler
): Modifier {
    if (!enabled) return this
    return this.pointerInput(enabled, handler) {
        detectVerticalDragGestures(
            onDragStart = { offset -> handler.onDragStart(offset) },
            onVerticalDrag = { change, dragAmount ->
                change.consume()
                handler.onVerticalDrag(
                    uptimeMillis = change.uptimeMillis,
                    position = change.position,
                    dragAmount = dragAmount
                )
            },
            onDragEnd = { handler.onDragEnd() },
            onDragCancel = { handler.onDragCancel() }
        )
    }
}
