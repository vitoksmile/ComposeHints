package com.viktormykhailiv.compose.hints

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.staticCompositionLocalOf

val LocalHintOverlayEnterTransition =
    staticCompositionLocalOf { HintAnimationDefaults.enterTransition() }

val LocalHintOverlayExitTransition =
    staticCompositionLocalOf { HintAnimationDefaults.exitTransition() }

/**
 * Default animation values used by [HintController].
 */
object HintAnimationDefaults {

    fun enterTransition(): EnterTransition = fadeIn(tween(durationMillis = 300))

    fun exitTransition(): ExitTransition = fadeOut(tween(durationMillis = 300))
}
