package com.eclipse.launcher.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.eclipse.launcher.presentation.viewmodel.HomeScreenViewModel
import com.eclipse.launcher.ui.home.components.GridEngine

@OptIn(ExperimentalFoundationApi::class)
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

    val pagerState = rememberPagerState(pageCount = { state.totalPages })

    DragDropProvider {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black) // AMOLED black UI
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
                    }
                )
            }
        }
    }
}
