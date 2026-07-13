package com.example.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.Movie
import com.example.ui.theme.*
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
    onNavigateToPlayer: (String, String) -> Unit,
    onNavigateToSeries: (String) -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("user_profile_prefs", android.content.Context.MODE_PRIVATE) }
    val username = remember { sharedPrefs.getString("username", "Neoflix User") ?: "Neoflix User" }
    val age = remember { sharedPrefs.getString("age", "N/A") ?: "N/A" }
    val avatarUrl = remember { sharedPrefs.getString("avatar_url", "") ?: "" }

    val watchHistory by viewModel.watchHistory.collectAsState()
    val likedMovies by viewModel.likedMovies.collectAsState()
    val savedMovies by viewModel.savedMovies.collectAsState()

    var movieToDelete by remember { mutableStateOf<Movie?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog && movieToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                movieToDelete = null
            },
            title = {
                Text(
                    text = "Remove from History",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove \"${movieToDelete?.title}\" from your watch history?",
                    color = Color.White.copy(alpha = 0.8f)
                )
            },
            containerColor = Color(0xFF1C1C1E),
            confirmButton = {
                Button(
                    onClick = {
                        movieToDelete?.let { viewModel.deleteFromHistory(it.id) }
                        showDeleteDialog = false
                        movieToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        movieToDelete = null
                    }
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Me Tab Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Profile",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Profile Avatar Info
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(GlassLight)
                        .border(2.dp, AccentOrange, CircleShape)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(avatarUrl.ifBlank { "https://images.unsplash.com/photo-1628157582853-a796fa650a6a?auto=format&fit=crop&w=150&q=80" })
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = username,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Age: $age • Member",
                    color = AccentOrange,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Edit Profile",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable {
                            sharedPrefs.edit().putBoolean("profile_created", false).apply()
                            // Restart app to trigger onboarding setup
                            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            }
                            context.startActivity(intent)
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Real-Time Stats Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("History", "${watchHistory.size}", Modifier.weight(1f))
                StatCard("My List", "${savedMovies.size}", Modifier.weight(1f))
                StatCard("Liked", "${likedMovies.size}", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Section 1: Saved / My List
        item {
            Text(
                text = "My List",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }
        item {
            if (savedMovies.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassLight)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No movies bookmarked in your list.", color = TextSlate400, fontSize = 13.sp)
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(savedMovies) { movie ->
                        PosterCard(
                            movie = movie,
                            onPlay = {
                                if (movie.type == "series") {
                                    onNavigateToSeries(movie.id)
                                } else {
                                    onNavigateToPlayer(movie.id, movie.videoUrl)
                                }
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Section 2: Liked Content
        item {
            Text(
                text = "Liked Content",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }
        item {
            if (likedMovies.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassLight)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No liked movies yet.", color = TextSlate400, fontSize = 13.sp)
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(likedMovies) { movie ->
                        PosterCard(
                            movie = movie,
                            onPlay = {
                                if (movie.type == "series") {
                                    onNavigateToSeries(movie.id)
                                } else {
                                    onNavigateToPlayer(movie.id, movie.videoUrl)
                                }
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Section 3: Watch History with Long Press deletion
        item {
            Text(
                text = "Watch History",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            Text(
                text = "Long press an item to remove it from history",
                fontSize = 12.sp,
                color = AccentOrange.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp)
            )
        }

        if (watchHistory.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassLight)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No watch history yet.", color = TextSlate400, fontSize = 13.sp)
                }
            }
        } else {
            items(watchHistory) { (movie, activity) ->
                val progressPercent = if (activity.duration > 0) {
                    (activity.progress.toFloat() / activity.duration.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassLight)
                        .combinedClickable(
                            onLongClick = {
                                movieToDelete = movie
                                showDeleteDialog = true
                            },
                            onClick = {
                                if (movie.type == "series") {
                                    // Resume the series episode or show detail
                                    onNavigateToSeries(movie.id)
                                } else {
                                    onNavigateToPlayer(movie.id, movie.videoUrl)
                                }
                            }
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(52.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(movie.posterUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = movie.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = movie.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (activity.duration > 0) {
                            LinearProgressIndicator(
                                progress = { progressPercent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = AccentOrange,
                                trackColor = GlassBorder
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${(progressPercent * 100).toInt()}% watched",
                                color = TextSlate400,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = "Just started",
                                color = TextSlate400,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "Resume",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PosterCard(movie: Movie, onPlay: () -> Unit) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .clickable { onPlay() },
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(12.dp))
                .background(GlassLight)
                .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(movie.posterUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            text = movie.title,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1
        )
    }
}

@Composable
fun StatCard(label: String, count: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GlassLight)
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = count, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = AccentOrange)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSlate400)
    }
}
