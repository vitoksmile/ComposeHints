package com.viktormykhailiv.compose.hints

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize

@Stable
class HintAnchorState internal constructor() {

    internal var size: IntSize by mutableStateOf(IntSize.Zero)

    internal var offset: Offset by mutableStateOf(Offset.Zero)

    internal var shape: Shape by mutableStateOf(RectangleShape)
}

@Composable
fun rememberHintAnchorState(): HintAnchorState {
    return remember { HintAnchorState() }
}

/**
 * Register [state] to the current UI element as anchor for hints.
 *
 * @param state to hold the anchor's coordinates and size.
 * @param shape desired shape around the anchor.
 */
fun Modifier.hintAnchor(
    state: HintAnchorState,
    shape: Shape = RectangleShape,
): Modifier = composed {
    val statusBarInsets = WindowInsets.statusBars.getTop(LocalDensity.current).toFloat()
    state.shape = shape
    onGloballyPositioned {
        state.size = it.size
        state.offset = it.positionInWindow()
            // To fix WindowInsets for iOS and Android
            .minus(Offset(x = 0f, y = statusBarInsets))
    }
}
