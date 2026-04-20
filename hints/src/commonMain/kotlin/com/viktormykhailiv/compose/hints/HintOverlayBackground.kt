@file:Suppress("RemoveExplicitTypeArguments")

package com.viktormykhailiv.compose.hints

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.launch

internal val HintOverlayColorDefault: Color = Color(0x44000000)

val LocalHintOverlayColor = staticCompositionLocalOf<Color> { HintOverlayColorDefault }

val LocalHintOverlayBrush = staticCompositionLocalOf<Brush?> { null }

/**
 * Set `background` either from [LocalHintOverlayBrush] or from [LocalHintOverlayColor].
 */
internal fun Modifier.overlayBackground(
    steps: List<List<HintAnchorState>>,
    activeStepIndex: Int,
): Modifier = composed {
    val backgroundBrush = LocalHintOverlayBrush.current
    val backgroundColor = LocalHintOverlayColor.current
    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current
    val isInspectionMode = LocalInspectionMode.current

    // region Animations
    val anchorAnimationMode = LocalAnchorAnimationMode.current
    val anchorSizeAnimationSpec = LocalAnchorSizeAnimationSpec.current
    val anchorOffsetAnimationSpec = LocalAnchorOffsetAnimationSpec.current

    val sizes = remember { mutableStateMapOf<HintAnchorState, Animatable<Size, AnimationVector2D>>() }
    val offsets = remember { mutableStateMapOf<HintAnchorState, Animatable<Offset, AnimationVector2D>>() }

    val currentStep = steps.getOrNull(activeStepIndex) ?: emptyList()
    val previousStep = steps.getOrNull(activeStepIndex - 1) ?: emptyList()

    for (anchor in currentStep) {
        sizes.getOrPut(anchor) {
            Animatable(
                initialValue = if (isInspectionMode) anchor.size.toSize() else Size.Zero,
                typeConverter = Size.VectorConverter,
            )
        }
        offsets.getOrPut(anchor) {
            Animatable(
                initialValue = if (isInspectionMode) anchor.offset else Offset.Zero,
                typeConverter = Offset.VectorConverter,
            )
        }
    }

    LaunchedEffect(activeStepIndex, steps) {
        val currentStep = steps.getOrNull(activeStepIndex) ?: return@LaunchedEffect
        val previousStep = steps.getOrNull(activeStepIndex - 1) ?: emptyList()

        for ((index, anchor) in currentStep.withIndex()) {
            val sizeAnimatable = sizes[anchor] ?: continue
            val offsetAnimatable = offsets[anchor] ?: continue

            launch {
                if (anchorAnimationMode == HintAnchorAnimationMode.Follow && activeStepIndex != 0) {
                    // Try to follow the corresponding anchor from previous step, or just the first one
                    val previousAnchor = previousStep.getOrNull(index) ?: previousStep.getOrNull(0)
                    if (previousAnchor != null && !sizeAnimatable.isRunning) {
                        sizeAnimatable.snapTo(previousAnchor.size.toSize())
                    }
                }

                sizeAnimatable.animateTo(
                    targetValue = anchor.size.toSize(),
                    animationSpec = anchor.sizeAnimationSpec ?: anchorSizeAnimationSpec,
                )
            }

            launch {
                when {
                    anchorAnimationMode == HintAnchorAnimationMode.Scale -> {
                        if (!offsetAnimatable.isRunning) {
                            offsetAnimatable.snapTo(
                                anchor.offset.copy(
                                    x = anchor.offset.x + anchor.size.width / 2,
                                    y = anchor.offset.y + anchor.size.height / 2,
                                )
                            )
                        }
                    }

                    activeStepIndex == 0 -> {
                        if (!offsetAnimatable.isRunning) {
                            offsetAnimatable.snapTo(
                                anchor.offset.copy(
                                    x = anchor.offset.x + anchor.size.width / 2,
                                    y = anchor.offset.y + anchor.size.height / 2,
                                )
                            )
                        }
                    }

                    else -> {
                        val previousAnchor = previousStep.getOrNull(index) ?: previousStep.getOrNull(0)
                        if (previousAnchor != null && !offsetAnimatable.isRunning) {
                            offsetAnimatable.snapTo(previousAnchor.offset)
                        }
                    }
                }

                offsetAnimatable.animateTo(
                    targetValue = anchor.offset,
                    animationSpec = anchor.offsetAnimationSpec ?: anchorOffsetAnimationSpec,
                )
            }
        }
    }
    // endregion

    drawWithCache {
        // Prepare path for background
        val path = Path().apply {
            lineTo(size.width, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            lineTo(0f, 0f)
            close()
        }

        val activeStep = steps.getOrNull(activeStepIndex) ?: emptyList()

        for (anchor in activeStep) {
            val sizeAnim = sizes[anchor]
            val offsetAnim = offsets[anchor]

            if (sizeAnim != null && offsetAnim != null) {
                // Prepare path for the anchor
                val anchorPath = Path()
                anchorPath.addOutline(
                    anchor.shape.createOutline(
                        size = sizeAnim.value,
                        layoutDirection = layoutDirection,
                        density = density,
                    )
                )
                anchorPath.translate(offsetAnim.value)
                anchorPath.close()

                // Clip out the anchor
                path.op(path, anchorPath, PathOperation.Xor)
            }
        }

        onDrawWithContent {
            if (backgroundBrush != null) {
                drawPath(path, backgroundBrush)
            } else {
                drawPath(path, backgroundColor)
            }

            drawContent()
        }
    }
}
