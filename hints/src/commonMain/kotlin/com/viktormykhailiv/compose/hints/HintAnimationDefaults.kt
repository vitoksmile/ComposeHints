package com.viktormykhailiv.compose.hints

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

val LocalHintOverlayEnterTransition =
    staticCompositionLocalOf { HintAnimationDefaults.enterTransition() }

val LocalHintOverlayExitTransition =
    staticCompositionLocalOf { HintAnimationDefaults.exitTransition() }

val LocalAnchorAnimationMode =
    staticCompositionLocalOf { HintAnimationDefaults.anchorAnimationMode() }

val LocalAnchorSizeAnimationSpec =
    staticCompositionLocalOf { HintAnimationDefaults.anchorSizeAnimationSpec() }

val LocalAnchorOffsetAnimationSpec =
    staticCompositionLocalOf { HintAnimationDefaults.anchorOffsetAnimationSpec() }

enum class HintAnchorAnimationMode {

    /**
     * Anchor position is animated independently by scale animation.
     */
    Scale,

    /**
     * Anchor position if animated from the previous to next anchors (moving anchor's rect on the screen).
     */
    Follow,
}

/**
 * Default animation values used by [HintController].
 */
object HintAnimationDefaults {

    fun enterTransition(): EnterTransition =
        fadeIn(tween(durationMillis = 300))

    fun exitTransition(): ExitTransition =
        fadeOut(tween(durationMillis = 300))

    fun anchorAnimationMode(): HintAnchorAnimationMode =
        HintAnchorAnimationMode.Scale

    fun anchorSizeAnimationSpec(): AnimationSpec<Size> =
        tween(durationMillis = 300)

    fun anchorOffsetAnimationSpec(): AnimationSpec<Offset> =
        tween(durationMillis = 300)
}
