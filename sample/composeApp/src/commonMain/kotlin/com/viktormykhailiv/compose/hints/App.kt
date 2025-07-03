package com.viktormykhailiv.compose.hints

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationDefaults
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    var hintSettings by remember {
        mutableStateOf(
            HintSettings(
                anchorAnimationMode = HintAnchorAnimationMode.Follow,
            )
        )
    }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val hintController = rememberHintController(
        overlay = Brush.linearGradient(
            listOf(
                Color.Blue.copy(alpha = 0.5f),
                Color.Red.copy(alpha = 0.5f),
            )
        ),
        overlayEnterTransition = fadeIn(tween(durationMillis = 1_000)),
        overlayExitTransition = fadeOut(tween(durationMillis = 1_000)),
        anchorAnimationMode = hintSettings.anchorAnimationMode,
        anchorSizeAnimationSpec = tween(durationMillis = 1_000),
        anchorOffsetAnimationSpec = tween(durationMillis = 1_000),
    )

    val topAppBarHint = rememberHintContainer {
        OutlinedButton(
            onClick = {
                hintController.dismiss()
            }
        ) { Text("Hint for TopAppBar") }
    }
    val topAppBarActionHintAnchor = rememberHintAnchorState(topAppBarHint)

    val actionHint = rememberHintContainer {
        Text("Hint for Action")
    }
    val actionHintAnchor = rememberHintAnchorState(actionHint)

    val bottomNavigationHint = rememberHintContainer {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.size(32.dp).background(Color.Magenta, CircleShape))
            Spacer(Modifier.size(8.dp))
            Text("Hint for BottomNavigation")
        }
    }
    val bottomNavigationHintAnchor = rememberHintAnchorState(bottomNavigationHint)

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    windowInsets = AppBarDefaults.topAppBarWindowInsets,
                    title = {
                        Text("TopAppBar")
                    },
                    actions = {
                        IconButton(
                            modifier = Modifier
                                .hintAnchor(topAppBarActionHintAnchor, CircleShape),
                            onClick = {
                                coroutineScope.launch {
                                    hintController.show(topAppBarActionHintAnchor)
                                }
                            },
                        ) {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = "Localized description",
                            )
                        }
                    }
                )
            },
            bottomBar = {
                BottomNavigation(
                    windowInsets = BottomNavigationDefaults.windowInsets,
                ) {
                    listOf(
                        "Home" to Icons.Filled.Home,
                        "Favourite" to Icons.Outlined.Favorite,
                        "Settings" to Icons.Outlined.Settings,
                    ).forEachIndexed { index, (title, icon) ->
                        BottomNavigationItem(
                            modifier = if (index == 0) {
                                Modifier.hintAnchor(
                                    bottomNavigationHintAnchor,
                                    shape = RoundedCornerShape(50f),
                                )
                            } else {
                                Modifier
                            },
                            icon = { Icon(icon, contentDescription = null) },
                            label = { Text(title) },
                            selected = index == 1,
                            onClick = {
                                if (index == 0) {
                                    coroutineScope.launch {
                                        hintController.show(bottomNavigationHintAnchor)
                                    }
                                }

                                showSettingsDialog = index == 2
                            },
                        )
                    }
                }
            }
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Button(
                    modifier = Modifier
                        .hintAnchor(actionHintAnchor, RoundedCornerShape(16.dp))
                        .padding(4.dp),
                    onClick = {
                        coroutineScope.launch {
                            hintController.show(
                                topAppBarActionHintAnchor,
                                actionHintAnchor,
                                bottomNavigationHintAnchor,
                            )
                        }
                    },
                ) {
                    Text("Action")
                }
            }

            if (showSettingsDialog) {
                HintSettingsDialog(
                    settings = hintSettings,
                    onDismissRequest = {
                        showSettingsDialog = false
                        hintSettings = it
                    },
                )
            }
        }
    }
}