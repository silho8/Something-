package com.eclipse.launcher.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.eclipse.launcher.domain.model.Gesture
import com.eclipse.launcher.domain.model.GestureAction
import com.eclipse.launcher.presentation.viewmodel.HomeScreenViewModel
import com.eclipse.launcher.presentation.viewmodel.SettingsViewModel
import com.eclipse.launcher.ui.drawer.AppDrawer
import com.eclipse.launcher.ui.home.components.FloatingDock
import com.eclipse.launcher.ui.home.components.GridEngine
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToWallpaper: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeScreenViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val gestures by settingsViewModel.gestures.collectAsState()

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface)
        }
        return
    }

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { state.totalPages })
    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState)

    val executeGestureAction = { action: GestureAction ->
        when (action) {
            GestureAction.OPEN_APP_DRAWER -> scope.launch { bottomSheetState.expand() }
            GestureAction.OPEN_SEARCH -> onNavigateToSearch()
            GestureAction.OPEN_SETTINGS -> onNavigateToSettings()
            GestureAction.OPEN_WIDGET_GALLERY -> { /* Optional feature */ }
            GestureAction.LOCK_DEVICE -> { /* Requires Device Admin */ }
            GestureAction.TOGGLE_FOCUS_MODE -> { /* Not implemented in OS mock */ }
            GestureAction.EXPAND_NOTIFICATIONS -> { /* Requires Accessibility Service */ }
            GestureAction.EXPAND_QUICK_SETTINGS -> { /* Requires Accessibility Service */ }
            GestureAction.LAUNCH_SELECTED_APP -> { /* Complex arg required */ }
            GestureAction.NONE -> {}
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            AppDrawer()
        },
        sheetPeekHeight = 0.dp,
        sheetContainerColor = Color.Transparent,
        sheetDragHandle = null,
        containerColor = MaterialTheme.colorScheme.background
    ) { _ ->
        DragDropProvider {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Render Wallpaper Background
                if (state.wallpaperPath != null) {
                    Image(
                        painter = rememberAsyncImagePainter(File(state.wallpaperPath!!)),
                        contentDescription = "Custom Wallpaper",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    gestures[Gesture.DOUBLE_TAP]?.let { executeGestureAction(it) }
                                },
                                onLongPress = {
                                    // Fallback if settings gesture is mapped to long press, otherwise default to wallpaper
                                    val action = gestures[Gesture.LONG_PRESS]
                                    if (action != null && action != GestureAction.NONE) {
                                        executeGestureAction(action)
                                    } else {
                                        onNavigateToWallpaper()
                                    }
                                }
                            )
                        }
                        // Detect swipes over workspace
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val changes = event.changes
                                    if (changes.size == 1) {
                                        val change = changes.first()
                                        if (change.pressed && change.previousPressed) {
                                            val dx = change.position.x - change.previousPosition.x
                                            val dy = change.position.y - change.previousPosition.y

                                            // Velocity thresholds
                                            if (abs(dy) > abs(dx) && abs(dy) > 50f) {
                                                if (dy > 0) gestures[Gesture.SWIPE_DOWN]?.let { executeGestureAction(it) }
                                                if (dy < 0) gestures[Gesture.SWIPE_UP]?.let { executeGestureAction(it) }
                                                change.consume()
                                            }
                                        }
                                    } else if (changes.size == 2) {
                                        // 2-finger gestures
                                        val change1 = changes[0]
                                        val change2 = changes[1]
                                        if (change1.pressed && change2.pressed) {
                                            val dy1 = change1.position.y - change1.previousPosition.y
                                            val dy2 = change2.position.y - change2.previousPosition.y
                                            if (dy1 > 20f && dy2 > 20f) {
                                                gestures[Gesture.TWO_FINGER_SWIPE_DOWN]?.let { executeGestureAction(it) }
                                                change1.consume()
                                                change2.consume()
                                            } else if (dy1 < -20f && dy2 < -20f) {
                                                gestures[Gesture.TWO_FINGER_SWIPE_UP]?.let { executeGestureAction(it) }
                                                change1.consume()
                                                change2.consume()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1f)
                    ) { page ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            val itemsOnPage = state.pages[page] ?: emptyList()

                            GridEngine(
                                page = page,
                                items = itemsOnPage,
                                columns = 5,
                                rows = 6,
                                widgetRegistry = viewModel.widgetRegistry,
                                onItemDropped = { item, position ->
                                    viewModel.onItemMoved(item, position)
                                },
                                onAppClick = { packageName ->
                                    viewModel.launchApp(packageName)
                                },
                                onWidgetResize = { widget, dx, dy ->
                                    viewModel.onWidgetResize(widget, dx, dy)
                                }
                            )
                        }
                    }

                    // Floating Dock overlay at the bottom
                    FloatingDock(
                        items = state.dockItems,
                        onItemDropped = { item, position ->
                            viewModel.onItemMoved(item, position)
                        },
                        onAppClick = { packageName ->
                            viewModel.launchApp(packageName)
                        },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
        }
    }
}
