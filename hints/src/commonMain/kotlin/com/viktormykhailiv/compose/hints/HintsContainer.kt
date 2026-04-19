@file:Suppress("NAME_SHADOWING")

package com.viktormykhailiv.compose.hints

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

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
    val layoutDirection = LocalLayoutDirection.current
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
                interactionSource = remember { MutableInteractionSource() },
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
            val isRtl = layoutDirection == LayoutDirection.Rtl

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
                    val properties = anchor.hint.properties
                    val alignment = properties.alignment
                    val paddingPx = properties.padding.roundToPx()
                    val offsetX = properties.offset.x.roundToPx()
                    val offsetY = properties.offset.y.roundToPx()

                    // Calculate anchor's offset relative to the "start" edge.
                    // In LTR, start is left. In RTL, start is right.
                    val anchorStartOffset = if (isRtl) {
                        constraints.maxWidth - anchor.offset.x - anchor.size.width
                    } else {
                        anchor.offset.x
                    }

                    var x: Int
                    var y: Int

                    when (alignment) {
                        HintAlignment.Top -> {
                            x = anchorStartOffset.toInt() - (placeable.width - anchor.size.width) / 2
                            y = anchor.offset.y.toInt() - placeable.height - paddingPx

                            if (y < 0) {
                                val alternativeY = anchor.offset.y.toInt() + anchor.size.height + paddingPx
                                if (alternativeY + placeable.height <= constraints.maxHeight) {
                                    y = alternativeY
                                }
                            }
                        }

                        HintAlignment.Bottom -> {
                            x = anchorStartOffset.toInt() - (placeable.width - anchor.size.width) / 2
                            y = anchor.offset.y.toInt() + anchor.size.height + paddingPx

                            if (y + placeable.height > constraints.maxHeight) {
                                val alternativeY = anchor.offset.y.toInt() - placeable.height - paddingPx
                                if (alternativeY >= 0) {
                                    y = alternativeY
                                }
                            }
                        }

                        HintAlignment.Start -> {
                            x = anchorStartOffset.toInt() - placeable.width - paddingPx
                            y = anchor.offset.y.toInt() - (placeable.height - anchor.size.height) / 2

                            if (x < 0) {
                                val alternativeX = anchorStartOffset.toInt() + anchor.size.width + paddingPx
                                if (alternativeX + placeable.width <= constraints.maxWidth) {
                                    x = alternativeX
                                }
                            }
                        }

                        HintAlignment.End -> {
                            x = anchorStartOffset.toInt() + anchor.size.width + paddingPx
                            y = anchor.offset.y.toInt() - (placeable.height - anchor.size.height) / 2

                            if (x + placeable.width > constraints.maxWidth) {
                                val alternativeX = anchorStartOffset.toInt() - placeable.width - paddingPx
                                if (alternativeX >= 0) {
                                    x = alternativeX
                                }
                            }
                        }

                        HintAlignment.Center -> {
                            x = anchorStartOffset.toInt() - (placeable.width - anchor.size.width) / 2
                            y = anchor.offset.y.toInt() - (placeable.height - anchor.size.height) / 2
                        }

                        HintAlignment.Overlap -> {
                            x = anchorStartOffset.toInt()
                            y = anchor.offset.y.toInt()
                        }

                        HintAlignment.TopStart -> {
                            x = anchorStartOffset.toInt()
                            y = anchor.offset.y.toInt() - placeable.height - paddingPx

                            if (y < 0) {
                                val alternativeY = anchor.offset.y.toInt() + anchor.size.height + paddingPx
                                if (alternativeY + placeable.height <= constraints.maxHeight) {
                                    y = alternativeY
                                }
                            }
                        }

                        HintAlignment.TopEnd -> {
                            x = anchorStartOffset.toInt() + anchor.size.width - placeable.width
                            y = anchor.offset.y.toInt() - placeable.height - paddingPx

                            if (y < 0) {
                                val alternativeY = anchor.offset.y.toInt() + anchor.size.height + paddingPx
                                if (alternativeY + placeable.height <= constraints.maxHeight) {
                                    y = alternativeY
                                }
                            }
                        }

                        HintAlignment.BottomStart -> {
                            x = anchorStartOffset.toInt()
                            y = anchor.offset.y.toInt() + anchor.size.height + paddingPx

                            if (y + placeable.height > constraints.maxHeight) {
                                val alternativeY = anchor.offset.y.toInt() - placeable.height - paddingPx
                                if (alternativeY >= 0) {
                                    y = alternativeY
                                }
                            }
                        }

                        HintAlignment.BottomEnd -> {
                            x = anchorStartOffset.toInt() + anchor.size.width - placeable.width
                            y = anchor.offset.y.toInt() + anchor.size.height + paddingPx

                            if (y + placeable.height > constraints.maxHeight) {
                                val alternativeY = anchor.offset.y.toInt() - placeable.height - paddingPx
                                if (alternativeY >= 0) {
                                    y = alternativeY
                                }
                            }
                        }

                        HintAlignment.StartTop -> {
                            x = anchorStartOffset.toInt() - placeable.width - paddingPx
                            y = anchor.offset.y.toInt()

                            if (x < 0) {
                                val alternativeX = anchorStartOffset.toInt() + anchor.size.width + paddingPx
                                if (alternativeX + placeable.width <= constraints.maxWidth) {
                                    x = alternativeX
                                }
                            }
                        }

                        HintAlignment.StartBottom -> {
                            x = anchorStartOffset.toInt() - placeable.width - paddingPx
                            y = anchor.offset.y.toInt() + anchor.size.height - placeable.height

                            if (x < 0) {
                                val alternativeX = anchorStartOffset.toInt() + anchor.size.width + paddingPx
                                if (alternativeX + placeable.width <= constraints.maxWidth) {
                                    x = alternativeX
                                }
                            }
                        }

                        HintAlignment.EndTop -> {
                            x = anchorStartOffset.toInt() + anchor.size.width + paddingPx
                            y = anchor.offset.y.toInt()

                            if (x + placeable.width > constraints.maxWidth) {
                                val alternativeX = anchorStartOffset.toInt() - placeable.width - paddingPx
                                if (alternativeX >= 0) {
                                    x = alternativeX
                                }
                            }
                        }

                        HintAlignment.EndBottom -> {
                            x = anchorStartOffset.toInt() + anchor.size.width + paddingPx
                            y = anchor.offset.y.toInt() + anchor.size.height - placeable.height

                            if (x + placeable.width > constraints.maxWidth) {
                                val alternativeX = anchorStartOffset.toInt() - placeable.width - paddingPx
                                if (alternativeX >= 0) {
                                    x = alternativeX
                                }
                            }
                        }
                    }

                    x += offsetX
                    y += offsetY

                    // Ensure it stays within screen bounds
                    x = x.coerceIn(
                        minimumValue = 0,
                        maximumValue = (constraints.maxWidth - placeable.width)
                            .coerceAtLeast(0),
                    )
                    y = y.coerceIn(
                        minimumValue = 0,
                        maximumValue = (constraints.maxHeight - placeable.height)
                            .coerceAtLeast(0),
                    )

                    placeable.placeRelative(x = x, y = y)
                }
            }
        }
    }
}
