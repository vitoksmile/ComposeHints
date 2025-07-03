@file:Suppress("RemoveExplicitTypeArguments")

package com.viktormykhailiv.compose.hints

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.coroutines.Continuation
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Stable
class HintController internal constructor() {

    private val queue = mutableStateListOf<HintAnchorState>()

    internal val hints: List<HintAnchorState> get() = queue

    internal var activeAnchorIndex by mutableStateOf<Int>(-1)

    private val pendingRequests = mutableMapOf<HintAnchorState, Continuation<Unit>>()

    suspend fun show(hint: HintAnchorState) {
        suspendCoroutine { continuation ->
            pendingRequests[hint] = continuation
            queue.add(hint)
            activeAnchorIndex = 0
        }
    }

    suspend fun show(vararg hint: HintAnchorState) {
        if (hint.isEmpty()) throw IllegalArgumentException("Nothing to show")

        show(hint.toList())
    }

    suspend fun show(hints: List<HintAnchorState>) {
        if (hints.isEmpty()) throw IllegalArgumentException("Nothing to show")

        suspendCoroutine { continuation ->
            pendingRequests[hints.last()] = continuation
            queue.clear()
            queue.addAll(hints)
            activeAnchorIndex = 0
        }
    }

    internal fun onDismissed() {
        val index = activeAnchorIndex.takeIf { it >= 0 } ?: return
        val hint = queue.getOrNull(index) ?: run {
            activeAnchorIndex = -1
            return
        }
        activeAnchorIndex++
        if (activeAnchorIndex >= queue.size) {
            activeAnchorIndex = -1
        }
        onDismissed(hint)
    }

    internal fun onDismissed(hint: HintAnchorState) {
        pendingRequests[hint]?.let { continuation ->
            continuation.resume(Unit)
            pendingRequests.remove(hint)
        }
        if (pendingRequests.isEmpty()) {
            queue.clear()
        }
    }

    fun dismiss() {
        pendingRequests.values
            .forEach { continuation ->
                continuation.resumeWithException(CancellationException("Hint was dismissed"))
            }
        pendingRequests.clear()
        queue.clear()
        activeAnchorIndex = -1
    }
}

@Composable
fun rememberHintController(
    overlay: Brush,
    overlayEnterTransition: EnterTransition = HintAnimationDefaults.enterTransition(),
    overlayExitTransition: ExitTransition = HintAnimationDefaults.exitTransition(),
): HintController {
    return rememberHintController(
        overlay = LocalHintOverlayBrush provides overlay,
        overlayEnterTransition = overlayEnterTransition,
        overlayExitTransition = overlayExitTransition,
    )
}

@Composable
fun rememberHintController(
    overlay: Color = HintOverlayColorDefault,
    overlayEnterTransition: EnterTransition = HintAnimationDefaults.enterTransition(),
    overlayExitTransition: ExitTransition = HintAnimationDefaults.exitTransition(),
): HintController {
    return rememberHintController(
        overlay = LocalHintOverlayColor provides overlay,
        overlayEnterTransition = overlayEnterTransition,
        overlayExitTransition = overlayExitTransition,
    )
}

@Composable
private fun rememberHintController(
    overlay: ProvidedValue<*>,
    overlayEnterTransition: EnterTransition,
    overlayExitTransition: ExitTransition,
): HintController {
    val controller = remember { HintController() }

    CompositionLocalProvider(
        overlay,
        LocalHintOverlayEnterTransition provides overlayEnterTransition,
        LocalHintOverlayExitTransition provides overlayExitTransition,
    ) {
        HintOverlay(
            anchors = controller.hints,
            activeAnchorIndex = controller.activeAnchorIndex,
            onDismiss = controller::onDismissed,
        )
    }

    return controller
}
