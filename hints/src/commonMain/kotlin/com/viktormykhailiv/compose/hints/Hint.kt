package com.viktormykhailiv.compose.hints

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class Hint internal constructor(
    internal val properties: HintProperties,
) {

    internal var content: @Composable AnimatedVisibilityScope.() -> Unit by mutableStateOf({})
}

@Composable
fun rememberHint(
    properties: HintProperties = HintProperties(),
    content: @Composable AnimatedVisibilityScope.() -> Unit,
): Hint {
    return remember(properties) {
        Hint(properties).also { it.content = content }
    }
}
