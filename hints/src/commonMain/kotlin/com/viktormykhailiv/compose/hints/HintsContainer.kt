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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection

/**
 * Custom layout to measure, place, and draw hints.
 */
@Composable
internal fun HintsContainer(
    modifier: Modifier,
    steps: List<List<HintAnchorState>>,
    activeStepIndex: Int,
    dismissCurrentHintOnClickOutside: () -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val visibleStates = remember {
        mutableStateMapOf<Hint, MutableTransitionState<Boolean>>()
    }

    val allAnchors = steps.flatten()
    val allHints = allAnchors.map { it.hint }.distinct()

    // Initialize states in composition pass to be ready for the first frame
    allHints.forEach { hint ->
        val isActive = steps.getOrNull(activeStepIndex)?.any { it.hint === hint } ?: false
        visibleStates.getOrPut(hint) {
            MutableTransitionState(initialState = isActive)
        }
    }

    val touchInterceptor = LocalHintTouchInterceptor.current
    LaunchedEffect(steps, activeStepIndex) {
        touchInterceptor.interceptTouchEvents = false

        allHints.forEach { hint ->
            val isActive = steps.getOrNull(activeStepIndex)?.any { it.hint === hint } ?: false
            val state = visibleStates.getOrPut(hint) {
                MutableTransitionState(initialState = isActive)
            }
            state.targetState = isActive

            if (isActive) {
                touchInterceptor.interceptTouchEvents = true
            }
        }
    }

    Layout(
        modifier = modifier
            .overlayBackground(steps, activeStepIndex)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = dismissCurrentHintOnClickOutside,
            ),
        content = {
            for (hint in allHints) {
                val state = visibleStates[hint] ?: continue

                AnimatedVisibility(
                    // Tag the layout with the Hint object itself
                    modifier = Modifier.layoutId(hint),
                    visibleState = state,
                    enter = EnterTransition.None,
                    exit = ExitTransition.None,
                ) {
                    hint.content(this)
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
            val currentStep = steps.getOrNull(activeStepIndex) ?: emptyList()

            // Place each hint relatively to it's collective anchors (bounding box)
            for (index in measurables.indices) {
                val measurable = measurables[index]
                val placeable = placeables[index]
                val hint = measurable.layoutId as? Hint ?: continue

                // Find all anchors for this hint in the active step
                val hintAnchors = currentStep.filter { it.hint === hint }

                if (hintAnchors.isNotEmpty()) {
                    // Calculate collective bounding box of all anchors for this hint
                    val minX = hintAnchors.minOf { it.offset.x }
                    val minY = hintAnchors.minOf { it.offset.y }
                    val maxX = hintAnchors.maxOf { it.offset.x + it.size.width }
                    val maxY = hintAnchors.maxOf { it.offset.y + it.size.height }

                    val collectiveOffset = Offset(minX, minY)
                    val collectiveSize = IntSize((maxX - minX).toInt(), (maxY - minY).toInt())

                    val properties = hint.properties
                    val alignment = properties.alignment
                    val paddingPx = properties.padding.roundToPx()
                    val offsetX = properties.offset.x.roundToPx()
                    val offsetY = properties.offset.y.roundToPx()

                    // Calculate anchor's offset relative to the "start" edge.
                    val anchorStartOffset = if (isRtl) {
                        constraints.maxWidth - collectiveOffset.x - collectiveSize.width
                    } else {
                        collectiveOffset.x
                    }

                    var x: Int
                    var y: Int

                    when (alignment) {
                        HintAlignment.Top -> {
                            x = anchorStartOffset.toInt() - (placeable.width - collectiveSize.width) / 2
                            y = collectiveOffset.y.toInt() - placeable.height - paddingPx

                            if (y < 0) {
                                val alternativeY = collectiveOffset.y.toInt() + collectiveSize.height + paddingPx
                                if (alternativeY + placeable.height <= constraints.maxHeight) {
                                    y = alternativeY
                                }
                            }
                        }

                        HintAlignment.Bottom -> {
                            x = anchorStartOffset.toInt() - (placeable.width - collectiveSize.width) / 2
                            y = collectiveOffset.y.toInt() + collectiveSize.height + paddingPx

                            if (y + placeable.height > constraints.maxHeight) {
                                val alternativeY = collectiveOffset.y.toInt() - placeable.height - paddingPx
                                if (alternativeY >= 0) {
                                    y = alternativeY
                                }
                            }
                        }

                        HintAlignment.Start -> {
                            x = anchorStartOffset.toInt() - placeable.width - paddingPx
                            y = collectiveOffset.y.toInt() - (placeable.height - collectiveSize.height) / 2

                            if (x < 0) {
                                val alternativeX = anchorStartOffset.toInt() + collectiveSize.width + paddingPx
                                if (alternativeX + placeable.width <= constraints.maxWidth) {
                                    x = alternativeX
                                }
                            }
                        }

                        HintAlignment.End -> {
                            x = anchorStartOffset.toInt() + collectiveSize.width + paddingPx
                            y = collectiveOffset.y.toInt() - (placeable.height - collectiveSize.height) / 2

                            if (x + placeable.width > constraints.maxWidth) {
                                val alternativeX = anchorStartOffset.toInt() - placeable.width - paddingPx
                                if (alternativeX >= 0) {
                                    x = alternativeX
                                }
                            }
                        }

                        HintAlignment.Center -> {
                            x = anchorStartOffset.toInt() - (placeable.width - collectiveSize.width) / 2
                            y = collectiveOffset.y.toInt() - (placeable.height - collectiveSize.height) / 2
                        }

                        HintAlignment.Overlap -> {
                            x = anchorStartOffset.toInt()
                            y = collectiveOffset.y.toInt()
                        }

                        HintAlignment.TopStart -> {
                            x = anchorStartOffset.toInt()
                            y = collectiveOffset.y.toInt() - placeable.height - paddingPx

                            if (y < 0) {
                                val alternativeY = collectiveOffset.y.toInt() + collectiveSize.height + paddingPx
                                if (alternativeY + placeable.height <= constraints.maxHeight) {
                                    y = alternativeY
                                }
                            }
                        }

                        HintAlignment.TopEnd -> {
                            x = anchorStartOffset.toInt() + collectiveSize.width - placeable.width
                            y = collectiveOffset.y.toInt() - placeable.height - paddingPx

                            if (y < 0) {
                                val alternativeY = collectiveOffset.y.toInt() + collectiveSize.height + paddingPx
                                if (alternativeY + placeable.height <= constraints.maxHeight) {
                                    y = alternativeY
                                }
                            }
                        }

                        HintAlignment.BottomStart -> {
                            x = anchorStartOffset.toInt()
                            y = collectiveOffset.y.toInt() + collectiveSize.height + paddingPx

                            if (y + placeable.height > constraints.maxHeight) {
                                val alternativeY = collectiveOffset.y.toInt() - placeable.height - paddingPx
                                if (alternativeY >= 0) {
                                    y = alternativeY
                                }
                            }
                        }

                        HintAlignment.BottomEnd -> {
                            x = anchorStartOffset.toInt() + collectiveSize.width - placeable.width
                            y = collectiveOffset.y.toInt() + collectiveSize.height + paddingPx

                            if (y + placeable.height > constraints.maxHeight) {
                                val alternativeY = collectiveOffset.y.toInt() - placeable.height - paddingPx
                                if (alternativeY >= 0) {
                                    y = alternativeY
                                }
                            }
                        }

                        HintAlignment.StartTop -> {
                            x = anchorStartOffset.toInt() - placeable.width - paddingPx
                            y = collectiveOffset.y.toInt()

                            if (x < 0) {
                                val alternativeX = anchorStartOffset.toInt() + collectiveSize.width + paddingPx
                                if (alternativeX + placeable.width <= constraints.maxWidth) {
                                    x = alternativeX
                                }
                            }
                        }

                        HintAlignment.StartBottom -> {
                            x = anchorStartOffset.toInt() - placeable.width - paddingPx
                            y = collectiveOffset.y.toInt() + collectiveSize.height - placeable.height

                            if (x < 0) {
                                val alternativeX = anchorStartOffset.toInt() + collectiveSize.width + paddingPx
                                if (alternativeX + placeable.width <= constraints.maxWidth) {
                                    x = alternativeX
                                }
                            }
                        }

                        HintAlignment.EndTop -> {
                            x = anchorStartOffset.toInt() + collectiveSize.width + paddingPx
                            y = collectiveOffset.y.toInt()

                            if (x + placeable.width > constraints.maxWidth) {
                                val alternativeX = anchorStartOffset.toInt() - placeable.width - paddingPx
                                if (alternativeX >= 0) {
                                    x = alternativeX
                                }
                            }
                        }

                        HintAlignment.EndBottom -> {
                            x = anchorStartOffset.toInt() + collectiveSize.width + paddingPx
                            y = collectiveOffset.y.toInt() + collectiveSize.height - placeable.height

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
