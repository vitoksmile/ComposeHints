package com.viktormykhailiv.compose.hints

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive

/**
 * Wrap the root Composable of your UI with [HintHost] to define the coordinate space
 * where hints and tooltips will be displayed.
 *
 * It serves as the top-level container that manages the hint overlay layer and
 * provides the default [overlay] color to all [HintController]s within its hierarchy.
 *
 * @param overlay default background color for the hint overlays.
 * Defaults to a semi-transparent black.
 * @param content the UI content that will be wrapped by the hint host.
 *
 * Example:
 * ```
 * HintHost(
 *     overlay = Color.Black.copy(alpha = 0.6f),
 * ) {
 *      MaterialTheme {
 *          Scaffold(...) { ... }
 *      }
 * }
 * ```
 */
@Composable
fun HintHost(
    overlay: Color = HintOverlayColorDefault,
    content: @Composable () -> Unit,
) {
    HintHost(
        overlay = LocalHintOverlayColor provides overlay,
        content = content,
    )
}

/**
 * Wrap the root Composable of your UI with [HintHost] to define the coordinate space
 * where hints and tooltips will be displayed.
 *
 * This overload allows using a [Brush] (e.g., a gradient) for the background overlay.
 *
 * @param overlay default background brush for the hint overlays.
 * @param content the UI content that will be wrapped by the hint host.
 *
 * Example:
 * ```
 * HintHost(
 *     overlay = Brush.verticalGradient(
 *         colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
 *     ),
 * ) {
 *      MaterialTheme {
 *          AppContent()
 *      }
 * }
 * ```
 */
@Composable
fun HintHost(
    overlay: Brush,
    content: @Composable () -> Unit,
) {
    HintHost(
        overlay = LocalHintOverlayBrush provides overlay,
        content = content,
    )
}

@Composable
private fun HintHost(
    overlay: ProvidedValue<*>,
    content: @Composable () -> Unit,
) {
    val hostController = rememberHintHostControllerOwner()
    val touchInterceptor = rememberHintTouchInterceptor()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { hostController.hostCoordinates = it },
    ) {
        CompositionLocalProvider(
            overlay,
            LocalHintHostController provides hostController,
            LocalHintTouchInterceptor provides touchInterceptor,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        coroutineScope {
                            awaitPointerEventScope {
                                while (isActive) {
                                    awaitPointerEvent(PointerEventPass.Initial)
                                        .changes
                                        .forEach { change ->
                                            if (change.pressed && touchInterceptor.interceptTouchEvents) {
                                                change.consume()
                                                touchInterceptor.interceptTouchEvents = false
                                            }
                                        }
                                }
                            }
                        }
                    }
            ) {
                content()
            }

            hostController.ObserveContent()
        }
    }
}

internal val LocalHintHostController = staticCompositionLocalOf<HintHostController> {
    error("HintHost was not set as root Composable")
}

internal interface HintHostController {
    val hostCoordinates: LayoutCoordinates?

    @Composable
    fun Content(content: @Composable () -> Unit)

    fun disposeContent()
}

private interface HintHostControllerOwner : HintHostController {
    override var hostCoordinates: LayoutCoordinates?

    @Composable
    fun ObserveContent()
}

@Composable
private fun rememberHintHostControllerOwner(): HintHostControllerOwner = remember {
    object : HintHostControllerOwner {
        override var hostCoordinates: LayoutCoordinates? by mutableStateOf(null)

        var content = mutableStateOf<(@Composable () -> Unit)?>(null)

        @Composable
        override fun ObserveContent() {
            content.value?.invoke()
        }

        @Composable
        override fun Content(content: @Composable (() -> Unit)) {
            this.content.value = content
        }

        override fun disposeContent() {
            content.value = null
        }
    }
}

/**
 * CompositionLocal to access the [HintTouchInterceptor] from within descendants of [HintHost].
 */
internal val LocalHintTouchInterceptor = staticCompositionLocalOf<HintTouchInterceptor> {
    error("HintHost was not set as root Composable")
}

/**
 * Interface to control whether pointer events should be intercepted and consumed by the [HintHost].
 */
internal interface HintTouchInterceptor {
    /**
     * When set to true, the next pointer press event at the root level is intercepted and consumed,
     * preventing it from propagating to underlying composables.
     */
    var interceptTouchEvents: Boolean
}

@Composable
private fun rememberHintTouchInterceptor(): HintTouchInterceptor = remember {
    object : HintTouchInterceptor {
        override var interceptTouchEvents: Boolean by mutableStateOf(false)
    }
}
