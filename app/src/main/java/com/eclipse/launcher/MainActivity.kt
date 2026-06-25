package com.eclipse.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eclipse.launcher.presentation.viewmodel.DynamicThemeViewModel
import com.eclipse.launcher.ui.home.HomeScreen
import com.eclipse.launcher.ui.search.SearchScreen
import com.eclipse.launcher.ui.settings.SettingsScreen
import com.eclipse.launcher.ui.theme.EclipseLauncherTheme
import com.eclipse.launcher.ui.wallpaper.WallpaperScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.statusBars())

        setContent {
            val dynamicThemeViewModel: DynamicThemeViewModel = hiltViewModel()
            val themeColors by dynamicThemeViewModel.themeColors.collectAsState()
            val iconStyle by dynamicThemeViewModel.iconStyle.collectAsState()
            val typographyStyle by dynamicThemeViewModel.typographyStyle.collectAsState()

            EclipseLauncherTheme(
                dynamicThemeColors = themeColors,
                iconStyle = iconStyle,
                typographyStyle = typographyStyle
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                onNavigateToWallpaper = { navController.navigate("wallpaper") },
                                onNavigateToSearch = { navController.navigate("search") },
                                onNavigateToSettings = { navController.navigate("settings") }
                            )
                        }
                        composable("wallpaper") {
                            val homeViewModel: com.eclipse.launcher.presentation.viewmodel.HomeScreenViewModel = hiltViewModel()
                            val state by homeViewModel.state.collectAsState()
                            WallpaperScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("search") {
                            val homeViewModel: com.eclipse.launcher.presentation.viewmodel.HomeScreenViewModel = hiltViewModel()
                            val state by homeViewModel.state.collectAsState()
                            SearchScreen(
                                wallpaperPath = state.wallpaperPath,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToWallpaper = { navController.navigate("wallpaper") }
                            )
                        }
                    }
                }
            }
        }
    }
}
