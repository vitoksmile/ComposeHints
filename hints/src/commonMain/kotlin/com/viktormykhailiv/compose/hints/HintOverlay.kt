@file:Suppress("RemoveExplicitTypeArguments")

package com.viktormykhailiv.compose.hints

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun HintOverlay(
    anchors: List<HintAnchorState>,
    activeAnchorIndex: Int,
    dismissCurrentHintOnClickOutside: () -> Unit,
    onBackClicked: () -> Unit,
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
    if (!showPopup) {
        LocalHintHostController.current.disposeContent()
        return
    }

    val hintOverlayColor = LocalHintOverlayColor.current
    val hintOverlayBrush = LocalHintOverlayBrush.current
    val overlayEnterTransition = LocalHintOverlayEnterTransition.current
    val overlayExitTransition = LocalHintOverlayExitTransition.current
    val anchorAnimationMode = LocalAnchorAnimationMode.current
    val anchorSizeAnimationSpec = LocalAnchorSizeAnimationSpec.current
    val anchorOffsetAnimationSpec = LocalAnchorOffsetAnimationSpec.current

    LocalHintHostController.current.Content {
        BackHandler { onBackClicked() }

        CompositionLocalProvider(
            LocalHintOverlayColor provides hintOverlayColor,
            LocalHintOverlayBrush provides hintOverlayBrush,
            LocalHintOverlayEnterTransition provides overlayEnterTransition,
            LocalHintOverlayExitTransition provides overlayExitTransition,
            LocalAnchorAnimationMode provides anchorAnimationMode,
            LocalAnchorSizeAnimationSpec provides anchorSizeAnimationSpec,
            LocalAnchorOffsetAnimationSpec provides anchorOffsetAnimationSpec,
        ) {
            AnimatedVisibility(
                visibleState = visibleState,
                enter = overlayEnterTransition,
                exit = overlayExitTransition,
            ) {
                HintsContainer(
                    modifier = Modifier.fillMaxSize(),
                    anchors = anchors,
                    activeAnchorIndex = activeAnchorIndex,
                    dismissCurrentHintOnClickOutside = dismissCurrentHintOnClickOutside,
                )
            }
        }
    }
}