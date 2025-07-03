package com.viktormykhailiv.compose.hints

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * App specific hint implementation, with background.
 */
@Composable
fun rememberHintContainer(content: @Composable () -> Unit): Hint {
    return rememberHint {
        Box(
            modifier = Modifier
                .animateEnterExit(
                    enter = fadeIn(tween(500)) + scaleIn(tween(500)),
                    exit = fadeOut(tween(500)) + scaleOut(tween(500))
                )
                .padding(vertical = 8.dp)
                .background(Color.Yellow, shape = RoundedCornerShape(16.dp))
                .padding(16.dp),
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides TextStyle(
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                ),
            ) {
                content()
            }
        }
    }
}
