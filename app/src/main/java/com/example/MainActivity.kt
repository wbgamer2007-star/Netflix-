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
import com.example.ui.ProfileSetupScreen
import com.example.ui.theme.MyApplicationTheme
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

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
                    val context = LocalContext.current
                    val sharedPrefs = remember { context.getSharedPreferences("user_profile_prefs", MODE_PRIVATE) }
                    val isProfileCreated = sharedPrefs.getBoolean("profile_created", false)

                    NavHost(
                        navController = navController, 
                        startDestination = if (isProfileCreated) "main" else "profile_setup"
                    ) {
                        composable("profile_setup") {
                            ProfileSetupScreen(
                                onProfileCreated = {
                                    navController.navigate("main") {
                                        popUpTo("profile_setup") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("main") {
                            MainScreen(
                                onNavigateToPlayer = { movieId, videoUrl ->
                                    val encodedUrl = android.util.Base64.encodeToString(videoUrl.toByteArray(StandardCharsets.UTF_8), android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING)
                                    navController.navigate("player/$movieId/$encodedUrl")
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
                                onPlayEpisode = { episodeTitle, videoUrl ->
                                    val encodedUrl = android.util.Base64.encodeToString(videoUrl.toByteArray(StandardCharsets.UTF_8), android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING)
                                    val encodedTitle = android.util.Base64.encodeToString(episodeTitle.toByteArray(StandardCharsets.UTF_8), android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING)
                                    navController.navigate("player/${seriesId}_$encodedTitle/$encodedUrl")
                                }
                            )
                        }
                        composable("player/{movieId}/{videoUrl}") { backStackEntry ->
                            val encodedUrl = backStackEntry.arguments?.getString("videoUrl") ?: ""
                            val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
                            val videoUrl = try {
                                String(android.util.Base64.decode(encodedUrl, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING), StandardCharsets.UTF_8)
                            } catch (e: Exception) {
                                ""
                            }
                            VideoPlayerScreen(
                                movieId = movieId,
                                videoUrl = videoUrl,
                                onNavigateUp = { navController.navigateUp() },
                                onNavigateToMovie = { id, url ->
                                    val encoded = android.util.Base64.encodeToString(url.toByteArray(StandardCharsets.UTF_8), android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING)
                                    navController.navigate("player/$id/$encoded")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
