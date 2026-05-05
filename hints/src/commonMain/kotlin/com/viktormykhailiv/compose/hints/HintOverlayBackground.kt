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
    anchors: List<HintAnchorState>,
    activeAnchorIndex: Int,
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

    val sizes = remember { mutableStateMapOf<Hint, Animatable<Size, AnimationVector2D>>() }
    val offsets = remember { mutableStateMapOf<Hint, Animatable<Offset, AnimationVector2D>>() }

    var lastActiveAnchorIndex by remember { mutableStateOf<Int>(-1) }
    val currentAnchor = anchors.getOrNull(activeAnchorIndex)

    if (currentAnchor != null) {
        lastActiveAnchorIndex = activeAnchorIndex
        sizes.getOrPut(currentAnchor.hint) {
            Animatable(
                initialValue = if (isInspectionMode) currentAnchor.size.toSize() else Size.Zero,
                typeConverter = Size.VectorConverter,
            )
        }
        offsets.getOrPut(currentAnchor.hint) {
            Animatable(
                initialValue = if (isInspectionMode) currentAnchor.offset else Offset.Zero,
                typeConverter = Offset.VectorConverter,
            )
        }
    }

    // React to anchor changes and trigger animations
    if (currentAnchor != null) {
        val sizeAnimatable = sizes[currentAnchor.hint]
        val offsetAnimatable = offsets[currentAnchor.hint]

        if (sizeAnimatable != null && offsetAnimatable != null) {
            LaunchedEffect(currentAnchor.size, currentAnchor.offset) {
                launch {
                    sizeAnimatable.animateTo(
                        targetValue = currentAnchor.size.toSize(),
                        animationSpec = currentAnchor.sizeAnimationSpec ?: anchorSizeAnimationSpec,
                    )
                }

                launch {
                    offsetAnimatable.animateTo(
                        targetValue = currentAnchor.offset,
                        animationSpec = currentAnchor.offsetAnimationSpec ?: anchorOffsetAnimationSpec,
                    )
                }
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

        val anchorIndex = if (activeAnchorIndex != -1) activeAnchorIndex else lastActiveAnchorIndex
        val anchor = anchors.getOrNull(anchorIndex)
        val sizeAnim = anchor?.let { sizes[it.hint] }
        val offsetAnim = anchor?.let { offsets[it.hint] }

        if (anchor != null && sizeAnim != null && offsetAnim != null) {
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
