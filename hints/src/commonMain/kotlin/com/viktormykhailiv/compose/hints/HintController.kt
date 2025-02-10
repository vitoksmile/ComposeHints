package com.viktormykhailiv.compose.hints

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.coroutines.Continuation
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Stable
class HintController internal constructor() {

    private var queue = mutableStateListOf<HintAnchorState>()

    internal val hint: HintAnchorState? get() = queue.firstOrNull()

    private val pendingRequests = mutableMapOf<HintAnchorState, Continuation<Unit>>()

    suspend fun show(hint: HintAnchorState) {
        suspendCoroutine { continuation ->
            pendingRequests[hint] = continuation
            queue.add(hint)
        }
    }

    suspend fun show(vararg hint: HintAnchorState) {
        show(hint.toList())
    }

    suspend fun show(hints: List<HintAnchorState>) {
        suspendCoroutine { continuation ->
            pendingRequests[hints.last()] = continuation
            queue.addAll(hints)
        }
    }

    internal fun onDismissed(hint: HintAnchorState) {
        pendingRequests[hint]?.let { continuation ->
            continuation.resume(Unit)
            pendingRequests.remove(hint)
        }
        queue.remove(hint)
    }

    fun dismiss() {
        pendingRequests.values
            .forEach { continuation ->
                continuation.resumeWithException(CancellationException("Hint was dismissed"))
            }
        pendingRequests.clear()
        queue.clear()
    }
}

@Composable
fun rememberHintController(overlay: Brush): HintController {
    return rememberHintController(overlay = LocalHintOverlayBrush provides overlay)
}

@Composable
fun rememberHintController(overlay: Color = HintOverlayColorDefault): HintController {
    return rememberHintController(overlay = LocalHintOverlayColor provides overlay)
}

@Composable
private fun rememberHintController(overlay: ProvidedValue<*>): HintController {
    val controller = remember { HintController() }

    controller.hint?.let { hint ->
        CompositionLocalProvider(overlay) {
            HintOverlay(
                anchor = hint,
                onDismiss = {
                    controller.onDismissed(hint)
                },
            )
        }
    }

    return controller
}
