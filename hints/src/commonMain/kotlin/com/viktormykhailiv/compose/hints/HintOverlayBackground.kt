@file:Suppress("RemoveExplicitTypeArguments")

package com.viktormykhailiv.compose.hints

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    // We use Any as key to allow different keying strategies:
    // Follow mode: index (0, 1, 2...) to reuse animatables across steps.
    // Scale mode: (stepIndex, index) to have fresh animatables for each step.
    val sizes = remember { mutableStateMapOf<Any, Animatable<Size, AnimationVector2D>>() }
    val offsets = remember { mutableStateMapOf<Any, Animatable<Offset, AnimationVector2D>>() }

    var lastActiveStepIndex by remember { mutableStateOf(-1) }
    val currentStep = steps.getOrNull(activeStepIndex) ?: emptyList()

    // Sync maps and initialize animatables in composition to avoid one-frame glitches
    if (activeStepIndex != -1) {
        val currentKeys = currentStep.indices.map { index ->
            if (anchorAnimationMode == HintAnchorAnimationMode.Follow) {
                index
            } else {
                activeStepIndex to index
            }
        }

        // Prune old animatables that are not part of the current step
        val keysToRemove = sizes.keys.filter { it !in currentKeys }
        keysToRemove.forEach {
            sizes.remove(it)
            offsets.remove(it)
        }

        for ((index, anchor) in currentStep.withIndex()) {
            val key = currentKeys[index]
            val holePaddingPx = with(density) { anchor.hint.properties.holePadding.toPx() }
            val targetSize = Size(
                width = anchor.size.width + holePaddingPx * 2,
                height = anchor.size.height + holePaddingPx * 2,
            )
            val targetOffset = Offset(
                x = anchor.offset.x - holePaddingPx,
                y = anchor.offset.y - holePaddingPx,
            )

            sizes.getOrPut(key) {
                Animatable(
                    initialValue = if (isInspectionMode) targetSize else Size.Zero,
                    typeConverter = Size.VectorConverter,
                )
            }
            offsets.getOrPut(key) {
                Animatable(
                    initialValue = if (isInspectionMode) targetOffset else targetOffset.copy(
                        x = targetOffset.x + targetSize.width / 2,
                        y = targetOffset.y + targetSize.height / 2,
                    ),
                    typeConverter = Offset.VectorConverter,
                )
            }
        }
    }

    LaunchedEffect(activeStepIndex, steps, anchorAnimationMode) {
        if (activeStepIndex == -1) {
            lastActiveStepIndex = -1
            sizes.clear()
            offsets.clear()
            return@LaunchedEffect
        }
        val currentStep = steps.getOrNull(activeStepIndex) ?: return@LaunchedEffect
        lastActiveStepIndex = activeStepIndex

        for ((index, anchor) in currentStep.withIndex()) {
            val key = if (anchorAnimationMode == HintAnchorAnimationMode.Follow) {
                index
            } else {
                activeStepIndex to index
            }
            val sizeAnimatable = sizes[key] ?: continue
            val offsetAnimatable = offsets[key] ?: continue

            val holePaddingPx = with(density) { anchor.hint.properties.holePadding.toPx() }
            val targetSize = Size(
                width = anchor.size.width + holePaddingPx * 2,
                height = anchor.size.height + holePaddingPx * 2,
            )
            val targetOffset = Offset(
                x = anchor.offset.x - holePaddingPx,
                y = anchor.offset.y - holePaddingPx,
            )

            launch {
                sizeAnimatable.animateTo(
                    targetValue = targetSize,
                    animationSpec = anchor.sizeAnimationSpec ?: anchorSizeAnimationSpec,
                )
            }

            launch {
                offsetAnimatable.animateTo(
                    targetValue = targetOffset,
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

        for ((index, anchor) in activeStep.withIndex()) {
            val key = if (anchorAnimationMode == HintAnchorAnimationMode.Follow) {
                index
            } else {
                activeStepIndex to index
            }
            val sizeAnim = sizes[key]
            val offsetAnim = offsets[key]

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
