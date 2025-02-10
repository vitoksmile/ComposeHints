package com.viktormykhailiv.compose.hints

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Stable
class HintController internal constructor() {

    internal var hint by mutableStateOf<HintAnchorState?>(null)

    fun show(hint: HintAnchorState) {
        this.hint = hint
    }

    fun dismiss() {
        hint = null
    }
}

@Composable
fun rememberHintController(overlay: Brush): HintController {
    return rememberHintController(overlay = LocalHintOverlayBrush provides overlay)
}

@Composable
fun rememberHintController(overlay: Color = HintOverlayColorDefault): HintController {
    return rememberHintController(overlay = LocalHintOverlayColor provides overlay)
}

@Composable
private fun rememberHintController(overlay: ProvidedValue<*>): HintController {
    val controller = remember { HintController() }

    controller.hint?.let { hint ->
        CompositionLocalProvider(overlay) {
            HintOverlay(
                anchor = hint,
                onDismiss = {
                    controller.dismiss()
                },
            )
        }
    }

    return controller
}
