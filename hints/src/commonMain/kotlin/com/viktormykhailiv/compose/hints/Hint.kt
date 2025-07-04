package com.viktormykhailiv.compose.hints

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class Hint internal constructor() {

    internal var content: @Composable AnimatedVisibilityScope.() -> Unit by mutableStateOf({})
}

@Composable
fun rememberHint(content: @Composable AnimatedVisibilityScope.() -> Unit): Hint {
    return remember {
        Hint().also { it.content = content }
    }
}
