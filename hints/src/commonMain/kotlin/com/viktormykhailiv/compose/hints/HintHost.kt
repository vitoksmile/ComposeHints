package com.viktormykhailiv.compose.hints

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

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
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        val hostController = rememberHintHostControllerOwner()

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

    @Composable
    fun Content(content: @Composable () -> Unit)
}

private interface HintHostControllerOwner : HintHostController {

    @Composable
    fun ObserveContent()
}

@Composable
private fun rememberHintHostControllerOwner(): HintHostControllerOwner = remember {
    object : HintHostControllerOwner {
        var content = mutableStateOf<(@Composable () -> Unit)?>(null)

        @Composable
        override fun ObserveContent() {
            content.value?.invoke()
        }

        @Composable
        override fun Content(content: @Composable (() -> Unit)) {
            this.content.value = content
        }
    }
}
