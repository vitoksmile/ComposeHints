package com.viktormykhailiv.compose.hints

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize

@Stable
class HintAnchorState internal constructor(
    internal val hint: Hint,
) {

    /**
     * Returns true if the corresponding layout is being attached to the hierarchy.
     */
    internal var isAttaching: Boolean by mutableStateOf(false)

    /**
     * Returns false if the corresponding layout was detached from the hierarchy.
     */
    internal var isAttached: Boolean by mutableStateOf(false)

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
 * @param fullScreen if true, the hint will be treated as a full-screen overlay.
 * The specific UI element's position and size will be ignored, and no highlighted
 * hole will be drawn in the background. Useful for introductory or welcome hints.
 */
fun Modifier.hintAnchor(
    state: HintAnchorState,
    shape: Shape = RectangleShape,
    sizeAnimationSpec: AnimationSpec<Size>? = HintAnimationDefaults.anchorSizeAnimationSpec(),
    offsetAnimationSpec: AnimationSpec<Offset>? = HintAnimationDefaults.anchorOffsetAnimationSpec(),
    fullScreen: Boolean = false,
): Modifier = composed {
    state.isAttaching = !state.isAttached

    val hostController = LocalHintHostController.current
    var selfCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    fun updateOffset(self: LayoutCoordinates) {
        if (fullScreen) return

        state.size = self.size

        val hostCoordinates = hostController.hostCoordinates
        if (hostCoordinates != null && hostCoordinates.isAttached) {
            state.offset = hostCoordinates.localPositionOf(self, Offset.Zero)
        } else {
            state.offset = self.positionInRoot()
        }
    }

    LaunchedEffect(selfCoordinates, hostController.hostCoordinates) {
        val self = selfCoordinates ?: return@LaunchedEffect
        if (self.isAttached) {
            updateOffset(self)
        }
        state.isAttaching = false
        state.isAttached = self.isAttached
    }
    DisposableEffect(Unit) {
        onDispose {
            state.isAttaching = false
            state.isAttached = false
        }
    }

    state.shape = shape
    state.sizeAnimationSpec = sizeAnimationSpec
    state.offsetAnimationSpec = offsetAnimationSpec

    onGloballyPositioned {
        selfCoordinates = it
        updateOffset(it)
    }
}
