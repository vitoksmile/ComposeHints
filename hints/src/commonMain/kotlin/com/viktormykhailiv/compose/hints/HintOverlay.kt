@file:Suppress("RemoveExplicitTypeArguments")

package com.viktormykhailiv.compose.hints

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Popup

val LocalHintOverlayColor = staticCompositionLocalOf<Color> { Color(0x44000000) }

val LocalHintOverlayBrush = staticCompositionLocalOf<Brush?> { null }

@Composable
fun HintOverlay() {
    Popup {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .overlayBackground()
        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = "Draw hints here",
                color = Color.White,
            )
        }
    }
}

/**
 * Set `background` either from [LocalHintOverlayBrush] or from [LocalHintOverlayColor].
 */
private fun Modifier.overlayBackground(): Modifier = composed {
    LocalHintOverlayBrush.current?.let { background(it) }
        ?: background(LocalHintOverlayColor.current)
}
