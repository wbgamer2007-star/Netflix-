package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.ContentRepository
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.GlassLight
import com.example.ui.theme.TextSlate400
import kotlinx.coroutines.delay

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    movieId: String,
    videoUrl: String,
    onNavigateUp: () -> Unit,
    onNavigateToMovie: (String, String) -> Unit = { _, _ -> },
    playerViewModel: PlayerViewModel = viewModel()
) {
    val context = LocalContext.current
    var playWhenReady by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    
    var showBrightnessIndicator by remember { mutableStateOf(false) }
    var brightnessLevel by remember { mutableStateOf(0f) }
    
    var showVolumeIndicator by remember { mutableStateOf(false) }
    var volumeLevel by remember { mutableStateOf(0f) }

    val activity = context as? Activity
    var isFullscreen by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    LaunchedEffect(isLandscape) {
        isFullscreen = isLandscape
    }
    
    val movie = ContentRepository.contentList.find { it.id == movieId }
    val isMovie = movie?.type == "movie"
    
    // Load progress when started
    LaunchedEffect(movieId) {
        playerViewModel.loadProgress(movieId)
    }

    val exoPlayer = remember {
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
            .setAllowCrossProtocolRedirects(true)
        
        val renderersFactory = androidx.media3.exoplayer.DefaultRenderersFactory(context)
            .setEnableDecoderFallback(true)
        
        ExoPlayer.Builder(context, renderersFactory)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .build().apply {
            if (videoUrl.isNotEmpty()) {
                setMediaItem(MediaItem.fromUri(android.net.Uri.parse(videoUrl)))
                prepare()
                this.playWhenReady = playWhenReady
                
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        isBuffering = playbackState == Player.STATE_BUFFERING
                    }
                })
            }
        }
    }
    
    val startPosition = playerViewModel.startPosition.value
    LaunchedEffect(startPosition) {
        if (startPosition > 0) {
            exoPlayer.seekTo(startPosition)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            val currentPos = exoPlayer.currentPosition
            val duration = exoPlayer.duration
            if (currentPos > 0 && duration > 0) {
                playerViewModel.saveProgress(movieId, currentPos, duration)
            }
            exoPlayer.release()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.window?.let { window ->
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
    
    LaunchedEffect(brightnessLevel) {
        if (showBrightnessIndicator) {
            delay(1500)
            showBrightnessIndicator = false
        }
    }
    
    LaunchedEffect(volumeLevel) {
        if (showVolumeIndicator) {
            delay(1500)
            showVolumeIndicator = false
        }
    }

    BackHandler {
        if (isFullscreen) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            isFullscreen = false
        } else {
            onNavigateUp()
        }
    }

    val toggleFullscreen = {
        if (isFullscreen) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    LaunchedEffect(isFullscreen) {
        activity?.window?.let { window ->
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            if (isFullscreen) {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.BackgroundDark)
    ) {
        // Player Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isFullscreen) Modifier.fillMaxHeight() else Modifier.aspectRatio(16f / 9f))
                .background(Color.Black)
                .statusBarsPadding() // Ensures video player doesn't go under status bar
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        setShowSubtitleButton(true) // Enable subtitles
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        keepScreenOn = true
                        
                        setFullscreenButtonClickListener {
                            toggleFullscreen()
                        }
                        
                        if (activity != null) {
                            val gestureDetector = GestureDetector(ctx, GestureHelper(
                                context = ctx,
                                activity = activity,
                                onSeek = {},
                                onVolumeChange = { vol ->
                                    volumeLevel = vol
                                    showVolumeIndicator = true
                                },
                                onBrightnessChange = { bright ->
                                    brightnessLevel = bright
                                    showBrightnessIndicator = true
                                },
                                onSingleTap = {
                                    if (isControllerFullyVisible) {
                                        hideController()
                                    } else {
                                        showController()
                                    }
                                }
                            ))
                            
                            setOnTouchListener { _, event ->
                                gestureDetector.onTouchEvent(event)
                                true 
                            }
                        }
                    }
                }
            )
            
            if (!isFullscreen) {
                IconButton(
                    onClick = onNavigateUp,
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }
            
            if (isBuffering) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AccentOrange
                )
            }
            
            // Volume Indicator
            androidx.compose.animation.AnimatedVisibility(
                visible = showVolumeIndicator,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.CenterEnd).padding(32.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.background(Color.Black.copy(alpha=0.6f), shape = RoundedCornerShape(8.dp)).padding(16.dp)) {
                    Icon(Icons.Filled.VolumeUp, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${(volumeLevel * 100).toInt()}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            
            // Brightness Indicator
            androidx.compose.animation.AnimatedVisibility(
                visible = showBrightnessIndicator,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.CenterStart).padding(32.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.background(Color.Black.copy(alpha=0.6f), shape = RoundedCornerShape(8.dp)).padding(16.dp)) {
                    Icon(Icons.Filled.BrightnessMedium, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${(brightnessLevel * 100).toInt()}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        // Details Section
        if (!isFullscreen) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                if (movie != null) {
                    item {
                        Text(
                            text = movie.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${movie.year} • ${movie.category}",
                            fontSize = 14.sp,
                            color = TextSlate400
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = movie.description,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Divider(color = Color.White.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    if (isMovie) {
                        item {
                            Text(
                                text = "Related Movies",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        
                        val relatedMovies = ContentRepository.contentList.filter { 
                            it.id != movie.id && it.type == "movie" && 
                            it.category.split(",").any { cat -> movie.category.contains(cat.trim()) } 
                        }
                        
                        items(relatedMovies) { related ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GlassLight)
                                    .clickable { onNavigateToMovie(related.id, related.videoUrl) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(related.posterUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = related.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = related.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${related.year} • ${related.category}",
                                        color = TextSlate400,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
