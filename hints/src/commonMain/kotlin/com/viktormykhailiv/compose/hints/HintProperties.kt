package com.viktormykhailiv.compose.hints

import androidx.compose.runtime.Immutable

/**
 * Properties used to customize the behavior of a [Hint].
 *
 * @property dismissOnBackPress whether the hint can be dismissed by pressing the back or escape
 *   buttons on Android or the escape key on desktop. If true, pressing the back button will call
 *   onDismissRequest.
 * @property dismissOnClickOutside whether the hint can be dismissed by clicking outside the
 *   hint's bounds.
 */
@Immutable
data class HintProperties(
    val dismissOnBackPress: Boolean = true,
    val dismissOnClickOutside: Boolean = true,
)
