@file:Suppress("RemoveExplicitTypeArguments")

package com.viktormykhailiv.compose.hints

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Popup

@Composable
fun HintOverlay(
    anchors: () -> List<HintAnchorState>,
) {
    Popup {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .overlayBackground(anchors)
        )
    }
}
