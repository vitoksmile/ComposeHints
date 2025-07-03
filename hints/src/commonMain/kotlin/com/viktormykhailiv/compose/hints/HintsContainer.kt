@file:Suppress("NAME_SHADOWING")

package com.viktormykhailiv.compose.hints

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId

/**
 * Custom layout to measure, place, and draw hints.
 */
@Composable
internal fun HintsContainer(
    modifier: Modifier,
    anchors: List<HintAnchorState>,
    activeAnchorIndex: Int,
    onDismiss: () -> Unit,
) {
    val visibleStates = remember {
        anchors.map { MutableTransitionState(false) }
    }
    LaunchedEffect(activeAnchorIndex) {
        visibleStates.forEachIndexed { index, state ->
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
                onClick = onDismiss,
            ),
        content = {
            anchors.forEachIndexed { index, anchor ->
                AnimatedVisibility(
                    modifier = Modifier.layoutId(index),
                    visibleState = visibleStates[index],
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
        // We can have many hints displayed
        // e.g. 1st is exiting with animation and 2nd is entering with animation
        val layoutIds = measurables.mapIndexed { index, measurable ->
            placeables[index] to measurable.layoutId as Int
        }.toMap()

        // Set the size of the layout as big as it can
        layout(constraints.maxWidth, constraints.maxHeight) {
            // Place each hint relatively to it's anchor
            placeables.forEach { placeable ->
                val anchor = anchors[layoutIds.getValue(placeable)]

                // Center align this hint
                val x = (anchor.offset.x.toInt() - (placeable.width - anchor.size.width) / 2)
                    // Fix the coordinate if it's out of the screen
                    .coerceAtLeast(0)
                    .coerceAtMost(constraints.maxWidth - placeable.width)

                // Put this hint below its anchor
                var y = (anchor.offset.y.toInt() + anchor.size.height)
                    // Fix y-coordinate if it's out of the screen
                    .coerceAtMost(constraints.maxHeight - placeable.height)
                if (y < anchor.offset.y + anchor.size.height) {
                    // Hint is be overlapping its anchor, put this hint above its anchor
                    y = anchor.offset.y.toInt() - placeable.height
                }

                placeable.placeRelative(x = x, y = y)
            }
        }
    }
}
