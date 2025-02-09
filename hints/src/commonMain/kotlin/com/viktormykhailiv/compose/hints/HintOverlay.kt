@file:Suppress("RemoveExplicitTypeArguments")

package com.viktormykhailiv.compose.hints

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Popup

@Composable
fun HintOverlay(
    anchors: () -> List<HintAnchorState>,
) {
    Popup {
        HintsContainer(
            modifier = Modifier.fillMaxSize(),
            anchors = anchors,
        )
    }
}
