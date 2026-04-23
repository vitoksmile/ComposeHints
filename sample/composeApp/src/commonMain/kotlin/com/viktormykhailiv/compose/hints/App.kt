package com.viktormykhailiv.compose.hints

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
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
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    HintHost {
        AppInternal()
    }
}

@Composable
private fun AppInternal() {
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

    val topAppBarHint = rememberHintContainer(
        properties = HintProperties(
            dismissOnBackPress = false,
            alignment = HintAlignment.EndTop,
            padding = 16.dp,
            holePadding = 8.dp,
            autoAdvanceDuration = 5.seconds,
            offset = DpOffset(x = 0.dp, y = 8.dp),
        ),
    ) {
        Column(modifier = Modifier.width(IntrinsicSize.Max)) {
            Text("Hint for TopAppBar")
            Text("* click outside to dismiss current hint")
            Text("* click/swipe back to dismiss is disabled")
            Text("* holePadding = 8.dp (hole is bigger than the icon)")
            Text("* auto-advances in 5 seconds")

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = { hintController.dismiss() },
                ) {
                    Text("Dismiss all")
                }

                Box(modifier = Modifier.weight(1f))

                if (hintController.totalStepsCount > 1) {
                    Button(
                        onClick = { hintController.next() }
                    ) {
                        Text(if (hintController.hasNext) "Next" else "Finish")
                    }
                }
            }
        }
    }
    val topAppBarActionHintAnchor = rememberHintAnchorState(topAppBarHint)

    val actionHint = rememberHintContainer {
        Column(modifier = Modifier.width(IntrinsicSize.Max)) {
            Text("Hint for Action")
            Text("* click outside to dismiss current hint")
            Text("* click/swipe back to dismiss all hints")

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (hintController.hasPrevious) {
                    OutlinedButton(
                        onClick = { hintController.previous() }
                    ) {
                        Text("Back")
                    }
                }

                Box(modifier = Modifier.weight(1f))

                Button(
                    onClick = { hintController.next() }
                ) {
                    Text(if (hintController.hasNext) "Next" else "Finish")
                }
            }
        }
    }
    val actionHintAnchor = rememberHintAnchorState(actionHint)

    val bottomNavigationHint = rememberHintContainer(
        properties = HintProperties(
            dismissOnClickOutside = false,
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(
                modifier = Modifier.size(32.dp)
                    .background(Color.Magenta, CircleShape),
            )

            Column(
                modifier = Modifier.width(IntrinsicSize.Max).padding(horizontal = 8.dp),
            ) {
                Text("Hint for BottomNavigation")
                Text("* click outside is disabled")
                Text("* click/swipe back to dismiss current hint")

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (hintController.hasPrevious) {
                        OutlinedButton(
                            onClick = { hintController.previous() }
                        ) {
                            Text("Back")
                        }
                    }

                    Box(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { hintController.next() }
                    ) {
                        Text(if (hintController.hasNext) "Next" else "Finish")
                    }
                }
            }

            IconButton(
                onClick = { hintController.dismiss() },
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                )
            }
        }
    }
    val bottomNavigationHintAnchor = rememberHintAnchorState(bottomNavigationHint)

    // Alignment showcase hints
    val topHint = rememberHintContainer(
        properties = HintProperties(
            alignment = HintAlignment.Top,
            padding = 8.dp,
        ),
        shape = CutCornerShape(topStart = 16.dp),
    ) {
        Text("HintAlignment.Top")
    }
    val topHintAnchor = rememberHintAnchorState(topHint)

    val bottomHint = rememberHintContainer(
        properties = HintProperties(
            alignment = HintAlignment.Bottom,
            padding = 8.dp,
        ),
        shape = CutCornerShape(bottomEnd = 16.dp),
    ) {
        Text("HintAlignment.Bottom")
    }
    val bottomHintAnchor = rememberHintAnchorState(bottomHint)

    val startHint = rememberHintContainer(
        properties = HintProperties(
            alignment = HintAlignment.Start,
            padding = 8.dp,
        ),
        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
    ) {
        Text("HintAlignment.Start")
    }
    val startHintAnchor = rememberHintAnchorState(startHint)

    val endHint = rememberHintContainer(
        properties = HintProperties(
            alignment = HintAlignment.End,
            padding = 8.dp,
        ),
        shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
    ) {
        Text("HintAlignment.End")
    }
    val endHintAnchor = rememberHintAnchorState(endHint)

    val centerHint = rememberHintContainer(
        properties = HintProperties(alignment = HintAlignment.Center),
        shape = CircleShape,
    ) {
        Text("Center")
    }
    val centerHintAnchor = rememberHintAnchorState(centerHint)

    val overlapHint = rememberHintContainer(
        properties = HintProperties(alignment = HintAlignment.Overlap),
        shape = RoundedCornerShape(0.dp),
    ) {
        Text("Overlap")
    }
    val overlapHintAnchor = rememberHintAnchorState(overlapHint)

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
        ) { innerPadding ->
            Box(
                modifier = Modifier.fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                        Text("Default Tour")
                    }

                    Spacer(modifier = Modifier.size(16.dp))
                    Text("Alignments showcase:", style = MaterialTheme.typography.h6)

                    Row {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Button(
                                modifier = Modifier
                                    .hintAnchor(topHintAnchor, CircleShape)
                                    .padding(4.dp),
                                onClick = {
                                    coroutineScope.launch {
                                        hintController.show(topHintAnchor)
                                    }
                                }
                            ) { Text("Top") }

                            Button(
                                modifier = Modifier
                                    .hintAnchor(bottomHintAnchor, CutCornerShape(8.dp))
                                    .padding(4.dp),
                                onClick = {
                                    coroutineScope.launch {
                                        hintController.show(bottomHintAnchor)
                                    }
                                }
                            ) { Text("Bottom") }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Button(
                                modifier = Modifier
                                    .hintAnchor(
                                        startHintAnchor,
                                        RoundedCornerShape(topStart = 16.dp)
                                    )
                                    .padding(4.dp),
                                onClick = {
                                    coroutineScope.launch {
                                        hintController.show(startHintAnchor)
                                    }
                                }
                            ) { Text("Start") }

                            Button(
                                modifier = Modifier
                                    .hintAnchor(
                                        endHintAnchor,
                                        RoundedCornerShape(bottomEnd = 16.dp)
                                    )
                                    .padding(4.dp),
                                onClick = {
                                    coroutineScope.launch {
                                        hintController.show(endHintAnchor)
                                    }
                                }
                            ) { Text("End") }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Button(
                                modifier = Modifier
                                    .hintAnchor(centerHintAnchor, CircleShape)
                                    .padding(4.dp),
                                onClick = {
                                    coroutineScope.launch {
                                        hintController.show(centerHintAnchor)
                                    }
                                }
                            ) { Text("Center") }

                            Button(
                                modifier = Modifier
                                    .hintAnchor(overlapHintAnchor, RoundedCornerShape(0.dp))
                                    .padding(4.dp),
                                onClick = {
                                    coroutineScope.launch {
                                        hintController.show(overlapHintAnchor)
                                    }
                                }
                            ) { Text("Overlap") }
                        }
                    }

                    Spacer(modifier = Modifier.size(16.dp))
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                hintController.showGroup(listOf(topHintAnchor, bottomHintAnchor))
                            }
                        }
                    ) {
                        Text("Multi-Anchor Showcase (Top + Bottom)")
                    }

                    Spacer(modifier = Modifier.size(16.dp))
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                hintController.show(
                                    topHintAnchor,
                                    bottomHintAnchor,
                                    startHintAnchor,
                                    endHintAnchor,
                                    centerHintAnchor,
                                    overlapHintAnchor
                                )
                            }
                        }
                    ) {
                        Text("Show All Alignments Tour")
                    }
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
