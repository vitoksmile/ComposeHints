@file:Suppress("RemoveExplicitTypeArguments")

package com.viktormykhailiv.compose.hints

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.toSize

val LocalHintOverlayColor = staticCompositionLocalOf<Color> { Color(0x44000000) }

val LocalHintOverlayBrush = staticCompositionLocalOf<Brush?> { null }

/**
 * Set `background` either from [LocalHintOverlayBrush] or from [LocalHintOverlayColor].
 */
internal fun Modifier.overlayBackground(
    anchors: () -> List<HintAnchorState>,
): Modifier = composed {
    val backgroundBrush = LocalHintOverlayBrush.current
    val backgroundColor = LocalHintOverlayColor.current

    drawWithCache {
        onDrawWithContent {
            if (backgroundBrush != null) {
                drawRect(backgroundBrush)
            } else {
                drawRect(backgroundColor)
            }

            anchors().forEach { anchor ->
                drawRect(
                    color = Color.Red,
                    topLeft = anchor.offset,
                    size = anchor.size.toSize(),
                    style = Stroke(width = 5f),
                )
            }

            drawContent()
        }
    }
}
