package com.viktormykhailiv.compose.hints

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

data class HintSettings(
    val anchorAnimationMode: HintAnchorAnimationMode,
)

@Composable
fun HintSettingsDialog(
    settings: HintSettings,
    onDismissRequest: (HintSettings) -> Unit,
) {
    var settings by remember(settings) { mutableStateOf(settings.copy()) }

    Dialog(
        onDismissRequest = { onDismissRequest(settings) },
    ) {
        Column(
            modifier = Modifier
                .background(
                    MaterialTheme.colors.background,
                    RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
        ) {
            HintAnchorAnimationModeSwitch(
                mode = settings.anchorAnimationMode,
                onModeChange = { mode ->
                    settings = settings.copy(anchorAnimationMode = mode)
                }
            )
        }
    }
}

@Composable
private fun HintAnchorAnimationModeSwitch(
    modifier: Modifier = Modifier,
    mode: HintAnchorAnimationMode,
    onModeChange: (HintAnchorAnimationMode) -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        Text("Anchor animation mode:")

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("- Follow")

            Checkbox(
                checked = mode == HintAnchorAnimationMode.Follow,
                onCheckedChange = { checked ->
                    onModeChange(
                        if (checked) {
                            HintAnchorAnimationMode.Follow
                        } else {
                            HintAnchorAnimationMode.Scale
                        }
                    )
                }
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("- Independent")

            Checkbox(
                checked = mode == HintAnchorAnimationMode.Scale,
                onCheckedChange = { checked ->
                    onModeChange(
                        if (checked) {
                            HintAnchorAnimationMode.Scale
                        } else {
                            HintAnchorAnimationMode.Follow
                        }
                    )
                }
            )
        }
    }
}
