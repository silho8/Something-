package com.eclipse.launcher.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import com.eclipse.launcher.presentation.viewmodel.HomeScreenViewModel
import com.eclipse.launcher.ui.drawer.AppDrawer
import com.eclipse.launcher.ui.home.components.GridEngine
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToWallpaper: () -> Unit,
    viewModel: HomeScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

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

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            AppDrawer()
        },
        sheetPeekHeight = 0.dp,
        sheetContainerColor = Color.Transparent,
        sheetDragHandle = null,
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
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

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    onNavigateToWallpaper()
                                }
                            )
                        }
                        .draggable(
                            orientation = Orientation.Vertical,
                            state = rememberDraggableState { delta -> },
                            onDragStopped = { velocity ->
                                if (velocity < -500f) {
                                    scope.launch { bottomSheetState.expand() }
                                } else if (velocity > 500f) {
                                    scope.launch { bottomSheetState.hide() }
                                }
                            }
                        )
                ) { page ->
                    val itemsOnPage = state.pages[page] ?: emptyList()

                    GridEngine(
                        page = page,
                        items = itemsOnPage,
                        columns = 5,
                        rows = 6,
                        onItemDropped = { item, position ->
                            viewModel.onItemMoved(item, position)
                        },
                        onAppClick = { packageName ->
                            viewModel.launchApp(packageName)
                        }
                    )
                }
            }
        }
    }
}
