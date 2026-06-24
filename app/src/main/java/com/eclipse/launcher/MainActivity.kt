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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eclipse.launcher.presentation.viewmodel.DynamicThemeViewModel
import com.eclipse.launcher.ui.home.HomeScreen
import com.eclipse.launcher.ui.theme.EclipseLauncherTheme
import com.eclipse.launcher.ui.wallpaper.WallpaperScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val dynamicThemeViewModel: DynamicThemeViewModel = hiltViewModel()
            val themeColors by dynamicThemeViewModel.themeColors.collectAsState()

            EclipseLauncherTheme(dynamicThemeColors = themeColors) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                onNavigateToWallpaper = {
                                    navController.navigate("wallpaper")
                                }
                            )
                        }
                        composable("wallpaper") {
                            WallpaperScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
