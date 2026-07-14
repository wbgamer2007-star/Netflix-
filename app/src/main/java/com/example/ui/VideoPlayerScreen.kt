package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.media3.ui.PlayerView
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MergingMediaSource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.ContentRepository
import com.example.data.Movie
import com.example.data.AudioTrack
import com.example.data.NetworkModule
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.nio.charset.StandardCharsets

private val NetflixRed = Color(0xFFE50914)

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

    val activity = context as? Activity
    var isFullscreen by remember { mutableStateOf(false) }
    var videoResizeMode by remember { mutableStateOf(androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    // Parse series and episodes: if movieId has an underscore, it's a series episode
    val isSeriesEpisode = movieId.contains("_")
    val baseMovieId = if (isSeriesEpisode) movieId.substringBefore("_") else movieId
    val decodedEpisodeTitle = if (isSeriesEpisode) {
        try {
            val encodedTitle = movieId.substringAfter("_")
            String(android.util.Base64.decode(encodedTitle, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING), StandardCharsets.UTF_8)
        } catch (e: Exception) {
            "Episode"
        }
    } else {
        ""
    }

    var currentMovie by remember(baseMovieId) {
        mutableStateOf(ContentRepository.contentList.find { it.id == baseMovieId })
    }
    
    LaunchedEffect(baseMovieId) {
        if (currentMovie == null) {
            try {
                val response = NetworkModule.apiService.getContent()
                if (response != null) {
                    val moviesList = response.map { (key, value) -> value.apply { id = key } }.sortedByDescending { it.timestamp }
                    ContentRepository.contentList = moviesList
                    currentMovie = moviesList.find { it.id == baseMovieId }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val movie = currentMovie

    val relatedContent = remember(baseMovieId, movie) {
        if (movie == null) {
            emptyList()
        } else {
            val currentCats = movie.category.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
            val categoryMatched = ContentRepository.contentList.filter { other ->
                other.id != baseMovieId &&
                other.category.split(",").any { cat ->
                    val trimmedCat = cat.trim().lowercase()
                    trimmedCat.isNotEmpty() && currentCats.contains(trimmedCat)
                }
            }
            if (categoryMatched.isNotEmpty()) {
                categoryMatched.take(10)
            } else {
                ContentRepository.contentList.filter { other -> other.id != baseMovieId }.take(10)
            }
        }
    }

    val currentEpisode = remember(movie, decodedEpisodeTitle) {
        if (isSeriesEpisode && movie != null) {
            movie.seasons?.flatMap { it.episodes ?: emptyList() }?.find { it.title == decodedEpisodeTitle }
        } else {
            null
        }
    }

    val audioTracks = remember(movie, currentEpisode, isSeriesEpisode) {
        if (isSeriesEpisode) {
            currentEpisode?.audioTracks ?: emptyList()
        } else {
            movie?.audioTracks ?: emptyList()
        }
    }

    var selectedAudioTrackIndex by remember { mutableIntStateOf(-1) }
    var showAudioSelector by remember { mutableStateOf(false) }
    val activeActivity by playerViewModel.activeActivity.collectAsState()
    val isLiked = activeActivity?.isLiked ?: false
    val isSaved = activeActivity?.isSaved ?: false

    LaunchedEffect(isLandscape) {
        isFullscreen = isLandscape
    }
    
    // Load local progress & activity on launch or change
    LaunchedEffect(movieId) {
        playerViewModel.loadProgress(movieId)
    }

    val exoPlayer = remember(movieId) {
        val renderersFactory = androidx.media3.exoplayer.DefaultRenderersFactory(context)
            .setEnableDecoderFallback(true)
        
        ExoPlayer.Builder(context, renderersFactory)
            .build().apply {
                this.playWhenReady = playWhenReady
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        isBuffering = playbackState == Player.STATE_BUFFERING
                    }
                })
            }
    }

    // Dynamic prepare or reload on videoUrl or custom audio track change
    LaunchedEffect(exoPlayer, videoUrl, selectedAudioTrackIndex, audioTracks) {
        if (videoUrl.isEmpty()) return@LaunchedEffect
        
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
            .setAllowCrossProtocolRedirects(true)
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
        
        val videoUri = android.net.Uri.parse(videoUrl)
        val videoSource = mediaSourceFactory.createMediaSource(MediaItem.fromUri(videoUri))
        
        if (selectedAudioTrackIndex >= 0 && selectedAudioTrackIndex < audioTracks.size) {
            val audioTrack = audioTracks[selectedAudioTrackIndex]
            val audioUri = android.net.Uri.parse(audioTrack.audioUrl)
            val audioSource = mediaSourceFactory.createMediaSource(MediaItem.fromUri(audioUri))
            val mergedSource = MergingMediaSource(videoSource, audioSource)
            
            val currentPos = exoPlayer.currentPosition
            exoPlayer.setMediaSource(mergedSource)
            exoPlayer.prepare()
            if (currentPos > 0) {
                exoPlayer.seekTo(currentPos)
            }
        } else {
            val currentPos = exoPlayer.currentPosition
            exoPlayer.setMediaSource(videoSource)
            exoPlayer.prepare()
            if (currentPos > 0) {
                exoPlayer.seekTo(currentPos)
            }
        }
    }
    
    // Seek to last saved progress once loaded
    val startPosition = playerViewModel.startPosition.collectAsState().value
    LaunchedEffect(startPosition) {
        if (startPosition > 0 && exoPlayer.currentPosition < startPosition) {
            exoPlayer.seekTo(startPosition)
        }
    }

    // Auto-save progress periodically (every 5 seconds)
    LaunchedEffect(exoPlayer, isFullscreen) {
        while (true) {
            val currentPos = exoPlayer.currentPosition
            val duration = exoPlayer.duration
            if (currentPos > 0 && duration > 0) {
                playerViewModel.saveProgress(movieId, currentPos, duration)
            }
            delay(5000)
        }
    }

    // 1. Reset activity orientation and system bars ONLY when this screen is completely exited/disposed
    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.window?.let { window ->
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // 2. Manage ExoPlayer saving and release cleanly on player instance change or disposal
    DisposableEffect(exoPlayer) {
        onDispose {
            val currentPos = exoPlayer.currentPosition
            val duration = exoPlayer.duration
            if (currentPos > 0 && duration > 0) {
                playerViewModel.saveProgress(movieId, currentPos, duration)
            }
            exoPlayer.release()
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
            .background(BackgroundDark)
    ) {
        // Player Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isFullscreen) Modifier.fillMaxHeight() else Modifier.aspectRatio(16f / 9f))
                .background(Color.Black)
                .then(if (!isFullscreen) Modifier.statusBarsPadding() else Modifier)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    androidx.media3.ui.PlayerView(ctx).apply {
                        useController = false
                        resizeMode = videoResizeMode
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        keepScreenOn = true
                    }
                },
                update = { playerView ->
                    if (playerView.player != exoPlayer) {
                        playerView.player = exoPlayer
                    }
                    if (playerView.resizeMode != videoResizeMode) {
                        playerView.resizeMode = videoResizeMode
                    }
                }
            )

            H5PlayerControls(
                player = exoPlayer,
                title = if (isSeriesEpisode) "${movie?.title ?: "Series"} - $decodedEpisodeTitle" else (movie?.title ?: "Video"),
                onBack = {
                    if (isFullscreen) {
                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        isFullscreen = false
                    } else {
                        onNavigateUp()
                    }
                },
                onShowSettings = {
                    if (audioTracks.isNotEmpty()) {
                        showAudioSelector = true
                    } else {
                        val dialogContext = activity ?: context
                        val builder = androidx.media3.ui.TrackSelectionDialogBuilder(
                            dialogContext, "Audio & Subtitle Tracks", exoPlayer, androidx.media3.common.C.TRACK_TYPE_AUDIO
                        )
                        builder.build().show()
                    }
                },
                isFullscreen = isFullscreen,
                onToggleFullscreen = {
                    if (isFullscreen) {
                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        isFullscreen = false
                    } else {
                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        isFullscreen = true
                    }
                },
                videoResizeMode = videoResizeMode,
                onToggleResizeMode = {
                    videoResizeMode = when (videoResizeMode) {
                        androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT -> androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
                        else -> androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (isBuffering) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = NetflixRed
                )
            }
        }

        // Details Section below the Player (Portrait Mode only)
        if (!isFullscreen) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
            ) {
                if (movie != null) {
                    item {
                        // Title
                        Text(
                            text = movie.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        
                        // Active Episode Indicator if Series
                        if (isSeriesEpisode) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Currently Watching: $decodedEpisodeTitle",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NetflixRed
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Year & Categories
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${movie.year}",
                                fontSize = 13.sp,
                                color = TextSlate400,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(GlassLight)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = movie.type.uppercase(),
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = movie.category,
                                fontSize = 13.sp,
                                color = TextSlate400,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // Interactive Action Buttons Row (Like, My List, Share)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Like Button
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GlassLight)
                                    .clickable { playerViewModel.toggleLiked(movieId, !isLiked) }
                                    .padding(vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isLiked) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                                    contentDescription = "Like",
                                    tint = if (isLiked) NetflixRed else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isLiked) "Liked" else "Like",
                                    color = if (isLiked) NetflixRed else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // My List/Save Button
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GlassLight)
                                    .clickable { playerViewModel.toggleSaved(movieId, !isSaved) }
                                    .padding(vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Default.Check else Icons.Default.Add,
                                    contentDescription = "My List",
                                    tint = if (isSaved) NetflixRed else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isSaved) "In My List" else "My List",
                                    color = if (isSaved) NetflixRed else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Description
                        Text(
                            text = movie.description,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 20.sp
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 8.dp))
                    }

                    // Series Episode Guide (Only if movie type is series)
                    if (movie.type == "series" && movie.seasons != null) {
                        movie.seasons.forEach { season ->
                            item {
                                Text(
                                    text = season.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }
                            items(season.episodes ?: emptyList()) { episode ->
                                val encodedEpisodeTitle = android.util.Base64.encodeToString(episode.title.toByteArray(StandardCharsets.UTF_8), android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING)
                                val epId = "${movie.id}_$encodedEpisodeTitle"
                                val isCurrentEpisode = movieId == epId

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isCurrentEpisode) NetflixRed.copy(alpha = 0.15f) else GlassLight)
                                        .border(
                                            width = 1.dp,
                                            color = if (isCurrentEpisode) NetflixRed else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { 
                                            if (!isCurrentEpisode) {
                                                onNavigateToMovie(epId, episode.videoUrl)
                                            }
                                        }
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isCurrentEpisode) NetflixRed else Color.White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isCurrentEpisode) Icons.Default.VolumeUp else Icons.Default.PlayArrow,
                                            contentDescription = "Play Episode",
                                            tint = if (isCurrentEpisode) Color.White else Color.Black,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = episode.title,
                                            color = Color.White,
                                            fontWeight = if (isCurrentEpisode) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 15.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (isCurrentEpisode) {
                                            Text(
                                                text = "Now Playing",
                                                color = NetflixRed,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }

                    // Suggestions Header
                    item {
                        Text(
                            text = "More Like This",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }

                    if (relatedContent.isEmpty()) {
                        item {
                            Text(
                                text = "No suggested content available.",
                                color = TextSlate400,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(relatedContent) { related ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GlassLight)
                                    .clickable { 
                                        if (related.type == "series") {
                                            val firstEp = related.seasons?.firstOrNull()?.episodes?.firstOrNull()
                                            if (firstEp != null) {
                                                val encTitle = android.util.Base64.encodeToString(firstEp.title.toByteArray(StandardCharsets.UTF_8), android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING)
                                                onNavigateToMovie("${related.id}_$encTitle", firstEp.videoUrl)
                                            } else {
                                                onNavigateToMovie(related.id, related.videoUrl)
                                            }
                                        } else {
                                            onNavigateToMovie(related.id, related.videoUrl)
                                        }
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(related.posterUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = related.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = related.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${related.year} • ${related.category}",
                                        color = TextSlate400,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = NetflixRed)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading details...",
                                color = TextSlate400,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAudioSelector) {
        AlertDialog(
            onDismissRequest = { showAudioSelector = false },
            title = {
                Text(
                    text = "Select Audio Language",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Default Track Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedAudioTrackIndex == -1) GlassLight else Color.Transparent)
                            .clickable {
                                selectedAudioTrackIndex = -1
                                showAudioSelector = false
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Default / Video's Audio",
                            color = Color.White,
                            fontWeight = if (selectedAudioTrackIndex == -1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        if (selectedAudioTrackIndex == -1) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = NetflixRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    // Custom Audio Track Options
                    audioTracks.forEachIndexed { index, track ->
                        val isSelected = selectedAudioTrackIndex == index
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) GlassLight else Color.Transparent)
                                .clickable {
                                    selectedAudioTrackIndex = index
                                    showAudioSelector = false
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = track.language,
                                color = Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = NetflixRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAudioSelector = false }) {
                    Text("Close", color = NetflixRed, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = BackgroundDark,
            titleContentColor = Color.White,
            textContentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
        )
    }
}
