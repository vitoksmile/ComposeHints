@file:Suppress("RemoveExplicitTypeArguments")

package com.viktormykhailiv.compose.hints

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

@Composable
internal fun HintOverlay(
    anchors: () -> List<HintAnchorState>,
    onDismiss: () -> Unit,
) {
    Popup(
        onDismissRequest = onDismiss,
        // Set focusable to handle back press events
        properties = remember { PopupProperties(focusable = true) },
    ) {
        HintsContainer(
            modifier = Modifier.fillMaxSize(),
            anchors = anchors,
            onDismiss = onDismiss,
        )
    }
}

@Composable
internal fun HintOverlay(
    anchor: HintAnchorState,
    onDismiss: () -> Unit,
) {
    HintOverlay(
        anchors = { listOf(anchor) },
        onDismiss = onDismiss,
    )
}
