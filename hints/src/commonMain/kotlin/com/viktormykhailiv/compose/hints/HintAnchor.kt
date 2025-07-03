package com.viktormykhailiv.compose.hints

import androidx.compose.animation.core.AnimationSpec
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize

@Stable
class HintAnchorState internal constructor(
    internal val hint: Hint,
) {

    internal var size: IntSize by mutableStateOf(IntSize.Zero)
    internal var offset: Offset by mutableStateOf(Offset.Zero)

    internal var shape: Shape by mutableStateOf(RectangleShape)

    internal var sizeAnimationSpec: AnimationSpec<Size>? = null
    internal var offsetAnimationSpec: AnimationSpec<Offset>? = null
}

@Composable
fun rememberHintAnchorState(hint: Hint): HintAnchorState {
    return remember(hint) {
        HintAnchorState(hint)
    }
}

/**
 * Register [state] to the current UI element as anchor for hints.
 *
 * @param state to hold the anchor's coordinates and size.
 * @param shape desired shape around the anchor.
 * @param sizeAnimationSpec to animate the size of the anchor.
 * If null, animation from [rememberHintController] will be used.
 * @param offsetAnimationSpec to animate the offset of the anchor.
 * If null, animation from [rememberHintController] will be used.
 */
fun Modifier.hintAnchor(
    state: HintAnchorState,
    shape: Shape = RectangleShape,
    sizeAnimationSpec: AnimationSpec<Size>? = HintAnimationDefaults.anchorSizeAnimationSpec(),
    offsetAnimationSpec: AnimationSpec<Offset>? = HintAnimationDefaults.anchorOffsetAnimationSpec(),
): Modifier = composed {
    val statusBarInsets = WindowInsets.statusBars.getTop(LocalDensity.current).toFloat()
    state.shape = shape
    state.sizeAnimationSpec = sizeAnimationSpec
    state.offsetAnimationSpec = offsetAnimationSpec
    onGloballyPositioned {
        state.size = it.size
        state.offset = it.positionInRoot()
            // To fix WindowInsets for iOS and Android
            .minus(Offset(x = 0f, y = statusBarInsets))
    }
}
