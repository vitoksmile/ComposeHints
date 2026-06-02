@file:Suppress("RemoveExplicitTypeArguments")

package com.viktormykhailiv.compose.hints

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Stable
class HintController internal constructor() {

    private val queue = mutableStateListOf<List<HintAnchorState>>()

    internal val steps: List<List<HintAnchorState>> get() = queue

    internal var activeStepIndex by mutableStateOf<Int>(-1)

    /**
     * The index of the currently active step, or -1 if no hints are showing.
     */
    val currentStepIndex: Int get() = activeStepIndex

    /**
     * The total number of steps in the current sequence.
     */
    val totalStepsCount: Int get() = queue.size

    /**
     * Whether there is a next hint in the sequence.
     */
    val hasNext: Boolean get() = activeStepIndex != -1 && activeStepIndex < queue.size - 1

    /**
     * Whether there is a previous hint in the sequence.
     */
    val hasPrevious: Boolean get() = activeStepIndex > 0

    private var currentRequest: CancellableContinuation<Unit>? = null

    private val mutex = Mutex()

    /**
     * Show a single hint with one anchor.
     */
    suspend fun show(anchor: HintAnchorState) {
        show(listOf(listOf(anchor)))
    }

    /**
     * Show a sequence of hints, each with one anchor.
     */
    suspend fun show(vararg anchors: HintAnchorState) {
        if (anchors.isEmpty()) throw IllegalArgumentException("Nothing to show")

        show(anchors.map { listOf(it) })
    }

    /**
     * Show a single hint with multiple anchors simultaneously.
     */
    suspend fun showGroup(anchors: List<HintAnchorState>) {
        show(listOf(anchors))
    }

    /**
     * Show a sequence of steps, where each step can have multiple anchors.
     */
    suspend fun show(steps: List<List<HintAnchorState>>) = mutex.withLock {
        if (steps.isEmpty()) throw IllegalArgumentException("Nothing to show")

        val availableSteps = steps.asSequence()
            .map { s -> s.filter { it.isAttached } }
            .filter { it.isNotEmpty() }
            .toList()

        if (availableSteps.isEmpty()) {
            return@withLock
        }

        try {
            suspendCancellableCoroutine { continuation ->
                currentRequest = continuation
                queue.addAll(availableSteps)
                activeStepIndex = 0

                continuation.invokeOnCancellation {
                    if (currentRequest === continuation) {
                        currentRequest = null
                        queue.clear()
                        activeStepIndex = -1
                    }
                }
            }
        } finally {
            currentRequest = null
            queue.clear()
            activeStepIndex = -1
        }
    }

    /**
     * Move to the next hint in the sequence.
     * If this is the last hint, the sequence will be finished and dismissed.
     */
    fun next() {
        findCurrentStep() ?: return

        activeStepIndex++
        if (activeStepIndex >= queue.size) {
            completeSequence()
        }
    }

    /**
     * Move to the previous hint in the sequence.
     * Does nothing if this is the first hint or no hints are showing.
     */
    fun previous() {
        if (activeStepIndex > 0) {
            activeStepIndex--
        }
    }

    fun dismiss() {
        currentRequest?.cancel(CancellationException("Hint was dismissed"))
    }

    internal fun dismissCurrentHintOnClickOutside() {
        val step = findCurrentStep() ?: return

        // All anchors in a step usually belong to the same hint. 
        // We check the first one for dismiss properties.
        val firstAnchor = step.firstOrNull() ?: return
        if (firstAnchor.hint.properties.dismissOnClickOutside.not()) {
            // Hint not dismissable
            return
        }

        activeStepIndex++
        if (activeStepIndex >= queue.size) {
            completeSequence()
        }
    }

    private fun completeSequence() {
        currentRequest?.resume(Unit)
    }

    internal fun dismissAllHintsOnBackClicked() {
        val step = findCurrentStep()
            ?: run {
                dismiss()
                return
            }

        val firstAnchor = step.firstOrNull() ?: return
        if (firstAnchor.hint.properties.dismissOnBackPress.not()) {
            // Hint not dismissable
            return
        }

        dismiss()
    }

    private fun findCurrentStep(): List<HintAnchorState>? {
        val index = activeStepIndex
            .takeIf { it >= 0 }
            ?: return null

        return queue.getOrNull(index)
            ?: run {
                activeStepIndex = -1
                null
            }
    }
}

@Composable
fun rememberHintController(
    overlay: Brush,
    overlayEnterTransition: EnterTransition = HintAnimationDefaults.enterTransition(),
    overlayExitTransition: ExitTransition = HintAnimationDefaults.exitTransition(),
    anchorAnimationMode: HintAnchorAnimationMode = HintAnimationDefaults.anchorAnimationMode(),
    anchorSizeAnimationSpec: AnimationSpec<Size> = HintAnimationDefaults.anchorSizeAnimationSpec(),
    anchorOffsetAnimationSpec: AnimationSpec<Offset> = HintAnimationDefaults.anchorOffsetAnimationSpec()
): HintController {
    return rememberHintController(
        overlay = LocalHintOverlayBrush provides overlay,
        overlayEnterTransition = overlayEnterTransition,
        overlayExitTransition = overlayExitTransition,
        anchorAnimationMode = anchorAnimationMode,
        anchorSizeAnimationSpec = anchorSizeAnimationSpec,
        anchorOffsetAnimationSpec = anchorOffsetAnimationSpec,
    )
}

@Composable
fun rememberHintController(
    overlay: Color = HintOverlayColorDefault,
    overlayEnterTransition: EnterTransition = HintAnimationDefaults.enterTransition(),
    overlayExitTransition: ExitTransition = HintAnimationDefaults.exitTransition(),
    anchorAnimationMode: HintAnchorAnimationMode = HintAnimationDefaults.anchorAnimationMode(),
    anchorSizeAnimationSpec: AnimationSpec<Size> = HintAnimationDefaults.anchorSizeAnimationSpec(),
    anchorOffsetAnimationSpec: AnimationSpec<Offset> = HintAnimationDefaults.anchorOffsetAnimationSpec(),
): HintController {
    return rememberHintController(
        overlay = LocalHintOverlayColor provides overlay,
        overlayEnterTransition = overlayEnterTransition,
        overlayExitTransition = overlayExitTransition,
        anchorAnimationMode = anchorAnimationMode,
        anchorSizeAnimationSpec = anchorSizeAnimationSpec,
        anchorOffsetAnimationSpec = anchorOffsetAnimationSpec,
    )
}

@Composable
private fun rememberHintController(
    overlay: ProvidedValue<*>,
    overlayEnterTransition: EnterTransition,
    overlayExitTransition: ExitTransition,
    anchorAnimationMode: HintAnchorAnimationMode,
    anchorSizeAnimationSpec: AnimationSpec<Size>,
    anchorOffsetAnimationSpec: AnimationSpec<Offset>,
): HintController {
    requireNotNull(LocalHintHostController.current)

    val controller = remember { HintController() }

    DisposableEffect(controller) {
        onDispose { controller.dismiss() }
    }

    CompositionLocalProvider(
        overlay,
        LocalHintOverlayEnterTransition provides overlayEnterTransition,
        LocalHintOverlayExitTransition provides overlayExitTransition,
        LocalAnchorAnimationMode provides anchorAnimationMode,
        LocalAnchorSizeAnimationSpec provides anchorSizeAnimationSpec,
        LocalAnchorOffsetAnimationSpec provides anchorOffsetAnimationSpec,
    ) {
        LaunchedEffect(controller.activeStepIndex) {
            val currentStep = controller.steps.getOrNull(controller.activeStepIndex)
                ?: return@LaunchedEffect

            // Find the first hint with an auto-advance duration in the current step
            val autoAdvanceDuration = currentStep
                .firstNotNullOfOrNull { it.hint.properties.autoAdvanceDuration }
                ?: return@LaunchedEffect

            delay(autoAdvanceDuration)
            controller.next()
        }

        HintOverlay(
            steps = controller.steps,
            activeStepIndex = controller.activeStepIndex,
            dismissCurrentHintOnClickOutside = controller::dismissCurrentHintOnClickOutside,
            onBackClicked = controller::dismissAllHintsOnBackClicked,
        )
    }

    return controller
}
