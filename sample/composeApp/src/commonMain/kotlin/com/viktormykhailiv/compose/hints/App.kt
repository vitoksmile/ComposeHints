package com.viktormykhailiv.compose.hints

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationDefaults
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val topAppBarActionHintAnchor = rememberHintAnchorState()
    val actionHintAnchor = rememberHintAnchorState()
    val bottomNavigationHintAnchor = rememberHintAnchorState()

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
                                .hintAnchor(topAppBarActionHintAnchor),
                            onClick = {},
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
                                Modifier.hintAnchor(bottomNavigationHintAnchor)
                            } else {
                                Modifier
                            },
                            icon = { Icon(icon, contentDescription = null) },
                            label = { Text(title) },
                            selected = index == 0,
                            onClick = {},
                        )
                    }
                }
            }
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Button(
                    modifier = Modifier
                        .hintAnchor(actionHintAnchor)
                        .padding(4.dp),
                    onClick = {},
                ) {
                    Text("Action")
                }
            }
        }

        CompositionLocalProvider(
            LocalHintOverlayBrush provides Brush.linearGradient(
                listOf(
                    Color.Blue.copy(alpha = 0.5f),
                    Color.Red.copy(alpha = 0.5f),
                )
            ),
        ) {
            HintOverlay(
                anchors = {
                    listOf(
                        topAppBarActionHintAnchor,
                        actionHintAnchor,
                        bottomNavigationHintAnchor,
                    )
                },
            )
        }
    }
}