package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.MainScreen
import com.example.ui.SeriesDetailScreen
import com.example.ui.VideoPlayerScreen
import com.example.ui.theme.MyApplicationTheme
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "main") {
                        composable("main") {
                            MainScreen(
                                onNavigateToPlayer = { videoUrl ->
                                    val encodedUrl = URLEncoder.encode(videoUrl, StandardCharsets.UTF_8.toString())
                                    navController.navigate("player/$encodedUrl")
                                },
                                onNavigateToSeries = { seriesId ->
                                    navController.navigate("series/$seriesId")
                                }
                            )
                        }
                        composable("series/{seriesId}") { backStackEntry ->
                            val seriesId = backStackEntry.arguments?.getString("seriesId") ?: ""
                            SeriesDetailScreen(
                                seriesId = seriesId,
                                onNavigateUp = { navController.navigateUp() },
                                onPlayEpisode = { videoUrl ->
                                    val encodedUrl = URLEncoder.encode(videoUrl, StandardCharsets.UTF_8.toString())
                                    navController.navigate("player/$encodedUrl")
                                }
                            )
                        }
                        composable("player/{videoUrl}") { backStackEntry ->
                            val videoUrl = backStackEntry.arguments?.getString("videoUrl") ?: ""
                            VideoPlayerScreen(
                                videoUrl = videoUrl,
                                onNavigateUp = { navController.navigateUp() }
                            )
                        }
                    }
                }
            }
        }
    }
}
