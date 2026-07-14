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
                                    val encodedUrl = android.util.Base64.encodeToString(videoUrl.toByteArray(StandardCharsets.UTF_8), android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING)
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
                                    val encodedUrl = android.util.Base64.encodeToString(videoUrl.toByteArray(StandardCharsets.UTF_8), android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING)
                                    navController.navigate("player/$encodedUrl")
                                }
                            )
                        }
                        composable("player/{videoUrl}") { backStackEntry ->
                            val encodedUrl = backStackEntry.arguments?.getString("videoUrl") ?: ""
                            val videoUrl = try {
                                String(android.util.Base64.decode(encodedUrl, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING), StandardCharsets.UTF_8)
                            } catch (e: Exception) {
                                ""
                            }
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
