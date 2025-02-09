@file:Suppress("RemoveExplicitTypeArguments")

package com.viktormykhailiv.compose.hints

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.window.Popup

@Composable
fun HintOverlay(
    anchors: () -> List<HintAnchorState>,
) {
    Popup {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .overlayBackground(anchors)
        ) {
            anchors().forEach { anchor ->
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = anchor.offset.x
                            translationY = anchor.offset.y + anchor.size.height
                        },
                ) {
                    anchor.hint.content()
                }
            }
        }
    }
}
