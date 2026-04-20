@file:Suppress("RemoveExplicitTypeArguments")

package com.viktormykhailiv.compose.hints

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import kotlin.coroutines.Continuation
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Stable
class HintController internal constructor() {

    private val queue = mutableStateListOf<List<HintAnchorState>>()

    internal val steps: List<List<HintAnchorState>> get() = queue

    internal var activeStepIndex by mutableStateOf<Int>(-1)

    private val pendingRequests = mutableMapOf<List<HintAnchorState>, Continuation<Unit>>()

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
    suspend fun show(steps: List<List<HintAnchorState>>) {
        if (steps.isEmpty()) throw IllegalArgumentException("Nothing to show")

        suspendCoroutine { continuation ->
            pendingRequests[steps.last()] = continuation
            queue.clear()
            queue.addAll(steps)
            activeStepIndex = 0
        }
    }

    fun dismiss() {
        pendingRequests.values
            .forEach { continuation ->
                continuation.resumeWithException(CancellationException("Hint was dismissed"))
            }
        pendingRequests.clear()
        queue.clear()
        activeStepIndex = -1
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
            activeStepIndex = -1
        }
        dismissCurrentStep(step)
    }

    private fun dismissCurrentStep(step: List<HintAnchorState>) {
        pendingRequests[step]?.let { continuation ->
            continuation.resume(Unit)
            pendingRequests.remove(step)
        }
        if (pendingRequests.isEmpty()) {
            queue.clear()
        }
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

    CompositionLocalProvider(
        overlay,
        LocalHintOverlayEnterTransition provides overlayEnterTransition,
        LocalHintOverlayExitTransition provides overlayExitTransition,
        LocalAnchorAnimationMode provides anchorAnimationMode,
        LocalAnchorSizeAnimationSpec provides anchorSizeAnimationSpec,
        LocalAnchorOffsetAnimationSpec provides anchorOffsetAnimationSpec,
    ) {
        HintOverlay(
            steps = controller.steps,
            activeStepIndex = controller.activeStepIndex,
            dismissCurrentHintOnClickOutside = controller::dismissCurrentHintOnClickOutside,
            onBackClicked = controller::dismissAllHintsOnBackClicked,
        )
    }

    return controller
}
