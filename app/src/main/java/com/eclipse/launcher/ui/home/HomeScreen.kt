package com.eclipse.launcher.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eclipse.launcher.presentation.viewmodel.HomeScreenViewModel
import com.eclipse.launcher.ui.drawer.AppDrawer
import com.eclipse.launcher.ui.home.components.GridEngine
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
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
        sheetPeekHeight = 0.dp, // Fully hidden until swiped up
        sheetContainerColor = Color.Transparent,
        sheetDragHandle = null,
        containerColor = Color.Black // AMOLED black UI
    ) { paddingValues ->
        DragDropProvider {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    // Detect vertical swipe to open the app drawer
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            // Optional: handle partial dragging if needed
                        },
                        onDragStopped = { velocity ->
                            if (velocity < -500f) { // Swipe up
                                scope.launch {
                                    bottomSheetState.expand()
                                }
                            } else if (velocity > 500f) { // Swipe down
                                scope.launch {
                                    bottomSheetState.hide()
                                }
                            }
                        }
                    )
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
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
