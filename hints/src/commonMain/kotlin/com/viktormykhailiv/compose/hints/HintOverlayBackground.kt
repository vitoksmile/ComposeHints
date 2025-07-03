@file:Suppress("RemoveExplicitTypeArguments")

package com.viktormykhailiv.compose.hints

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.LaunchedEffect
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

    // region Animations
    val anchorAnimationMode = LocalAnchorAnimationMode.current
    val anchorSizeAnimationSpec = LocalAnchorSizeAnimationSpec.current
    val anchorOffsetAnimationSpec = LocalAnchorOffsetAnimationSpec.current

    val sizes = anchors.map {
        Animatable<Size, AnimationVector2D>(
            initialValue = Size(0f, 0f),
            typeConverter = Size.VectorConverter,
        )
    }
    val offsets = anchors.map {
        Animatable<Offset, AnimationVector2D>(
            initialValue = Offset.Zero,
            typeConverter = Offset.VectorConverter,
        )
    }
    LaunchedEffect(activeAnchorIndex) {
        val anchor = anchors.getOrNull(activeAnchorIndex) ?: return@LaunchedEffect

        launch {
            if (anchorAnimationMode == HintAnchorAnimationMode.Follow && activeAnchorIndex != 0) {
                val previousAnchor = anchors[activeAnchorIndex - 1]
                sizes[activeAnchorIndex].snapTo(previousAnchor.size.toSize())
            }

            sizes[activeAnchorIndex].animateTo(
                targetValue = anchor.size.toSize(),
                animationSpec = anchor.sizeAnimationSpec ?: anchorSizeAnimationSpec,
            )
        }

        launch {
            when {
                anchorAnimationMode == HintAnchorAnimationMode.Scale -> {
                    offsets[activeAnchorIndex].snapTo(
                        anchor.offset.copy(
                            x = anchor.offset.x + anchor.size.width / 2,
                            y = anchor.offset.y + anchor.size.height / 2,
                        )
                    )
                }

                activeAnchorIndex == 0 -> {
                    offsets[activeAnchorIndex].snapTo(
                        anchor.offset.copy(
                            x = anchor.offset.x + anchor.size.width / 2,
                            y = anchor.offset.y + anchor.size.height / 2,
                        )
                    )
                }

                else -> {
                    val previousAnchor = anchors[activeAnchorIndex - 1]
                    offsets[activeAnchorIndex].snapTo(previousAnchor.offset)
                }
            }

            offsets[activeAnchorIndex].animateTo(
                targetValue = anchor.offset,
                animationSpec = anchor.offsetAnimationSpec ?: anchorOffsetAnimationSpec,
            )
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

        anchors.getOrNull(activeAnchorIndex)?.let { anchor ->
            // Prepare path for the anchor
            val anchorPath = Path()
            anchorPath.addOutline(
                anchor.shape.createOutline(
                    size = sizes[activeAnchorIndex].value,
                    layoutDirection = layoutDirection,
                    density = density,
                )
            )
            anchorPath.translate(offsets[activeAnchorIndex].value)
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
