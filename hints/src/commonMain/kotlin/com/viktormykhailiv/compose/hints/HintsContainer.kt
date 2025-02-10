@file:Suppress("NAME_SHADOWING")

package com.viktormykhailiv.compose.hints

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout

/**
 * Custom layout to place and draw hints.
 */
@Composable
internal fun HintsContainer(
    modifier: Modifier,
    anchors: () -> List<HintAnchorState>,
    onDismiss: () -> Unit,
) {
    val anchors = anchors()

    Layout(
        modifier = modifier
            .overlayBackground(anchors)
            .clickable(
                interactionSource = null,
                // Disable ripple
                indication = null,
                onClick = onDismiss,
            ),
        content = {
            anchors.forEach { it.hint.content() }
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
            placeables.forEachIndexed { index, placeable ->
                val anchor = anchors[index]

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
