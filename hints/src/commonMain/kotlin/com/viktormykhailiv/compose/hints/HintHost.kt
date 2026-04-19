package com.viktormykhailiv.compose.hints

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned

/**
 * Wrap root Composable with `HintHost` to define a space where Hints will be shown.
 *
 * Example:
 * ```
 * HintHost {
 *      MaterialTheme {
 *          Scaffold()
 *      }
 * }
 * ```
 */
@Composable
fun HintHost(
    content: @Composable () -> Unit,
) {
    val hostController = rememberHintHostControllerOwner()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { hostController.hostCoordinates = it },
    ) {
        CompositionLocalProvider(
            LocalHintHostController provides hostController,
        ) {
            content()

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
