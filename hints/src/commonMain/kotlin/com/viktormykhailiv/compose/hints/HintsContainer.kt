@file:Suppress("NAME_SHADOWING")

package com.viktormykhailiv.compose.hints

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalInspectionMode

/**
 * Custom layout to measure, place, and draw hints.
 */
@Composable
internal fun HintsContainer(
    modifier: Modifier,
    anchors: List<HintAnchorState>,
    activeAnchorIndex: Int,
    dismissCurrentHintOnClickOutside: () -> Unit,
) {
    val isInspectionMode = LocalInspectionMode.current
    val visibleStates = remember {
        mutableStateMapOf<Hint, MutableTransitionState<Boolean>>()
    }

    // Initialize states in composition pass to be ready for the first frame
    anchors.forEachIndexed { index, anchor ->
        visibleStates.getOrPut(anchor.hint) {
            MutableTransitionState(initialState = isInspectionMode && index == activeAnchorIndex)
        }
    }

    LaunchedEffect(anchors, activeAnchorIndex) {
        anchors.forEachIndexed { index, anchor ->
            val state = visibleStates.getOrPut(anchor.hint) {
                MutableTransitionState(initialState = isInspectionMode && index == activeAnchorIndex)
            }
            state.targetState = index == activeAnchorIndex
        }
    }

    Layout(
        modifier = modifier
            .overlayBackground(anchors, activeAnchorIndex)
            .clickable(
                interactionSource = null,
                // Disable ripple
                indication = null,
                onClick = dismissCurrentHintOnClickOutside,
            ),
        content = {
            for (anchor in anchors) {
                val state = visibleStates[anchor.hint] ?: continue

                AnimatedVisibility(
                    // Tag the layout with the Hint object itself
                    modifier = Modifier.layoutId(anchor.hint),
                    visibleState = state,
                    enter = EnterTransition.None,
                    exit = ExitTransition.None,
                ) {
                    anchor.hint.content(this)
                }
            }
        },
    ) { measurables, constraints ->
        // Measure each hint
        val placeables = measurables.map { measurable ->
            measurable.measure(
                constraints.copy(minWidth = 0, minHeight = 0)
            )
        }

        // Set the size of the layout as big as it can
        layout(constraints.maxWidth, constraints.maxHeight) {
            // Place each hint relatively to it's anchor
            for (index in measurables.indices) {
                val measurable = measurables[index]
                val placeable = placeables[index]
                val hint = measurable.layoutId as? Hint ?: continue
                val anchor = anchors.find { it.hint === hint }

                // Only place the hint if its anchor still exists in the current list.
                // If it's null, it means the hint is currently playing its exit
                // animation after being removed.
                if (anchor != null) {
                    // Center align this hint
                    val x = (anchor.offset.x.toInt() - (placeable.width - anchor.size.width) / 2)
                        // Fix the coordinate if it's out of the screen
                        .coerceIn(0, constraints.maxWidth - placeable.width)

                    // Put this hint below its anchor
                    var y = (anchor.offset.y.toInt() + anchor.size.height)
                        // Fix y-coordinate if it's out of the screen
                        .coerceAtMost(constraints.maxHeight - placeable.height)
                    if (y < anchor.offset.y + anchor.size.height) {
                        // Hint would overlap its anchor, put it above instead
                        y = anchor.offset.y.toInt() - placeable.height
                    }

                    placeable.placeRelative(x = x, y = y)
                }
            }
        }
    }
}
