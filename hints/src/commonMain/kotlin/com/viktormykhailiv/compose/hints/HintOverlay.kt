@file:Suppress("RemoveExplicitTypeArguments")

package com.viktormykhailiv.compose.hints

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

@Composable
internal fun HintOverlay(
    anchors: List<HintAnchorState>,
    activeAnchorIndex: Int,
    onDismiss: () -> Unit,
) {
    val visibleState = remember { MutableTransitionState<Boolean>(false) }
    LaunchedEffect(activeAnchorIndex) {
        visibleState.targetState = activeAnchorIndex >= 0
    }

    var showPopup by remember { mutableStateOf(false) }
    LaunchedEffect(visibleState.currentState, visibleState.targetState, visibleState.isIdle) {
        showPopup = visibleState.currentState || visibleState.targetState ||
                // Still show popup if exit animation is running
                !visibleState.targetState && !visibleState.isIdle
    }
    if (!showPopup) return

    Popup(
        onDismissRequest = onDismiss,
        // Set focusable to handle back press events
        properties = remember { PopupProperties(focusable = true) },
    ) {
        AnimatedVisibility(
            visibleState = visibleState,
            enter = LocalHintOverlayEnterTransition.current,
            exit = LocalHintOverlayExitTransition.current,
        ) {
            HintsContainer(
                modifier = Modifier.fillMaxSize(),
                anchors = anchors,
                activeAnchorIndex = activeAnchorIndex,
                onDismiss = onDismiss,
            )
        }
    }
}