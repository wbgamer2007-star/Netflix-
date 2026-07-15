package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.Movie
import com.example.ui.theme.*
import java.nio.charset.StandardCharsets
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Person
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToPlayer: (String, String) -> Unit,
    onNavigateToSeries: (String) -> Unit,
    viewModel: MoviesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResultState by viewModel.searchResult.collectAsState()
    var currentTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            if (currentTab == 0) NeoflixHeader()
        },
        bottomBar = {
            NeoflixBottomNav(currentTab) { currentTab = it }
        },
        containerColor = BackgroundDark,
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Liquid visual background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(AccentOrange.copy(alpha = 0.12f), Color.Transparent),
                                center = Offset(size.width * 0.2f, size.height * 0.3f),
                                radius = size.width * 0.5f
                            )
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(AccentPurple.copy(alpha = 0.12f), Color.Transparent),
                                center = Offset(size.width * 0.8f, size.height * 0.7f),
                                radius = size.width * 0.5f
                            )
                        )
                    }
                    .blur(60.dp)
            )

            when (currentTab) {
                0 -> {
                    when (val state = uiState) {
                        is MoviesUiState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = AccentOrange
                            )
                        }
                        is MoviesUiState.Error -> {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        is MoviesUiState.Success -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 32.dp)
                            ) {
                                item {
                                    SearchBarCustom(searchQuery) { viewModel.updateSearchQuery(it) }
                                }

                                if (searchQuery.isBlank()) {
                                    // Default Home Layout
                                    if (state.heroMovies.isNotEmpty()) {
                                        item {
                                            HeroBannerCarousel(state.heroMovies, onNavigateToPlayer, onNavigateToSeries)
                                        }
                                    }

                                    state.categories.forEach { (category, movies) ->
                                        item {
                                            CategoryRow(category, movies, onNavigateToPlayer, onNavigateToSeries)
                                        }
                                    }
                                } else {
                                    // Beautiful Custom Search Layout
                                    searchResultState?.let { results ->
                                        if (results.bestMatch == null && results.otherMatches.isEmpty()) {
                                            item {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(32.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "No results found for \"${results.query}\"",
                                                        color = TextSlate400,
                                                        fontSize = 15.sp,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        } else {
                                            // Best Match section (Show Movie Name and Play Trigger, as requested)
                                            results.bestMatch?.let { best ->
                                                item {
                                                    Text(
                                                        text = "Best Match",
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = AccentOrange,
                                                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                                                    )
                                                    
                                                    // High-contrast, clean name card
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 24.dp, vertical = 6.dp)
                                                            .clip(RoundedCornerShape(16.dp))
                                                            .background(GlassLight)
                                                            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                                                            .clickable {
                                                                if (best.type == "series") {
                                                                    onNavigateToSeries(best.id)
                                                                } else {
                                                                    onNavigateToPlayer(best.id, best.videoUrl)
                                                                }
                                                            }
                                                            .padding(16.dp)
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(48.dp)
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(AccentOrange),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.PlayArrow,
                                                                    contentDescription = "Play",
                                                                    tint = Color.White,
                                                                    modifier = Modifier.size(28.dp)
                                                                )
                                                            }
                                                            
                                                            Column(modifier = Modifier.weight(1f)) {
                                                                Text(
                                                                    text = best.title,
                                                                    fontSize = 18.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color.White
                                                                )
                                                                Spacer(modifier = Modifier.height(2.dp))
                                                                Text(
                                                                    text = "${best.year} • ${best.category}",
                                                                    fontSize = 12.sp,
                                                                    color = TextSlate400
                                                                )
                                                            }
                                                            
                                                            Icon(
                                                                imageVector = Icons.Default.ChevronRight,
                                                                contentDescription = "View",
                                                                tint = TextSlate400
                                                            )
                                                        }
                                                    }
                                                }

                                                // Suggestions underneath best match (7-10 related movies)
                                                if (results.recommendedMovies.isNotEmpty()) {
                                                    item {
                                                        Text(
                                                            text = "Suggested Content (${best.category.split(",").firstOrNull() ?: ""})",
                                                            fontSize = 16.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White,
                                                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                                                        )
                                                    }
                                                    
                                                    item {
                                                        LazyRow(
                                                            contentPadding = PaddingValues(horizontal = 24.dp),
                                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                            modifier = Modifier.padding(bottom = 16.dp)
                                                        ) {
                                                            items(results.recommendedMovies) { movie ->
                                                                MovieCard(movie, onNavigateToPlayer, onNavigateToSeries)
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            // Other matches list
                                            if (results.otherMatches.isNotEmpty()) {
                                                item {
                                                    Text(
                                                        text = "More Matches",
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                                                    )
                                                }
                                                
                                                items(results.otherMatches) { movie ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 24.dp, vertical = 6.dp)
                                                            .clip(RoundedCornerShape(12.dp))
                                                            .background(GlassLight)
                                                            .clickable {
                                                                if (movie.type == "series") {
                                                                    onNavigateToSeries(movie.id)
                                                                } else {
                                                                    onNavigateToPlayer(movie.id, movie.videoUrl)
                                                                }
                                                            }
                                                            .padding(12.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(40.dp)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(Color.White.copy(alpha = 0.1f)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Movie,
                                                                contentDescription = "Movie",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(16.dp))
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = movie.title,
                                                                color = Color.White,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 15.sp
                                                            )
                                                            Spacer(modifier = Modifier.height(2.dp))
                                                            Text(
                                                                text = "${movie.year} • ${movie.category}",
                                                                color = TextSlate400,
                                                                fontSize = 12.sp
                                                            )
                                                        }
                                                        Icon(
                                                            imageVector = Icons.Default.PlayCircleFilled,
                                                            contentDescription = "Play",
                                                            tint = AccentOrange,
                                                            modifier = Modifier.size(28.dp)
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
                }
                1 -> {
                    // Downloads Screen (Under construction as requested)
                    DownloadsScreen()
                }
                2 -> {
                    // Me Profile / History / Liked / Saved Screen
                    ProfileScreen(onNavigateToPlayer, onNavigateToSeries)
                }
            }
        }
    }
}

@Composable
fun DownloadsScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DownloadForOffline,
                contentDescription = "Downloads Under Construction",
                tint = AccentOrange,
                modifier = Modifier.size(96.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Offline Downloads",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Under Construction\nSoon you'll be able to download movies & watch them offline anywhere!",
                color = TextSlate400,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun NeoflixBottomNav(currentTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = BackgroundDark,
        contentColor = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .border(1.dp, GlassBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentTab == 0,
            onClick = { onTabSelected(0) },
            icon = { 
                Icon(
                    imageVector = if (currentTab == 0) Icons.Default.Home else Icons.Outlined.Home, 
                    contentDescription = "Home",
                    modifier = Modifier.size(24.dp)
                ) 
            },
            label = { Text("Home", fontSize = 11.sp, fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentOrange,
                selectedTextColor = AccentOrange,
                unselectedIconColor = TextSlate400,
                unselectedTextColor = TextSlate400,
                indicatorColor = AccentOrange.copy(alpha = 0.15f)
            )
        )
        NavigationBarItem(
            selected = currentTab == 1,
            onClick = { onTabSelected(1) },
            icon = { 
                Icon(
                    imageVector = if (currentTab == 1) Icons.Default.FileDownload else Icons.Outlined.FileDownload, 
                    contentDescription = "Downloads",
                    modifier = Modifier.size(24.dp)
                ) 
            },
            label = { Text("Downloads", fontSize = 11.sp, fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentOrange,
                selectedTextColor = AccentOrange,
                unselectedIconColor = TextSlate400,
                unselectedTextColor = TextSlate400,
                indicatorColor = AccentOrange.copy(alpha = 0.15f)
            )
        )
        NavigationBarItem(
            selected = currentTab == 2,
            onClick = { onTabSelected(2) },
            icon = { 
                Icon(
                    imageVector = if (currentTab == 2) Icons.Default.Person else Icons.Outlined.Person, 
                    contentDescription = "Me",
                    modifier = Modifier.size(24.dp)
                ) 
            },
            label = { Text("Me", fontSize = 11.sp, fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentOrange,
                selectedTextColor = AccentOrange,
                unselectedIconColor = TextSlate400,
                unselectedTextColor = TextSlate400,
                indicatorColor = AccentOrange.copy(alpha = 0.15f)
            )
        )
    }
}

@Composable
fun NeoflixHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AccentOrange),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color.White)
                ) 
            }
            Text(
                text = "NEOFLIX",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                letterSpacing = (-1).sp,
                color = Color.White
            )
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(GlassLight)
                    .border(1.dp, GlassBorder, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(2.dp, AccentOrange.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.DarkGray)
                )
            }
        }
    }
}

@Composable
fun SearchBarCustom(query: String, onQueryChange: (String) -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        placeholder = { Text("Search movies, genres, categories...", color = TextSlate400) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSlate400) },
        shape = RoundedCornerShape(24.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
            }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = GlassBorder,
            focusedBorderColor = AccentOrange,
            unfocusedContainerColor = GlassLight,
            focusedContainerColor = GlassLight,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )
}

@Composable
fun HeroBannerCarousel(
    movies: List<Movie>,
    onPlay: (String, String) -> Unit,
    onNavigateToSeries: (String) -> Unit
) {
    if (movies.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(movies) {
        while (true) {
            delay(5000L)
            currentIndex = (currentIndex + 1) % movies.size
        }
    }

    val currentMovie = movies[currentIndex]

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(450.dp)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        androidx.compose.animation.Crossfade(
            targetState = currentMovie,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 800),
            modifier = Modifier.fillMaxSize()
        ) { movie ->
            HeroBannerItem(
                movie = movie,
                onPlay = onPlay,
                onNavigateToSeries = onNavigateToSeries
            )
        }

        if (movies.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                movies.forEachIndexed { index, _ ->
                    val isSelected = index == currentIndex
                    val width by androidx.compose.animation.core.animateDpAsState(
                        targetValue = if (isSelected) 20.dp else 8.dp,
                        animationSpec = androidx.compose.animation.core.spring()
                    )
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isSelected) AccentOrange else Color.White.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun HeroBannerItem(movie: Movie, onPlay: (String, String) -> Unit, onNavigateToSeries: (String) -> Unit) {
    val action = {
        if (movie.type == "series") onNavigateToSeries(movie.id) else onPlay(movie.id, movie.videoUrl)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            .clickable { action() }
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
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, BackgroundDark.copy(alpha = 0.9f)),
                        startY = 400f
                    )
                )
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(BackgroundDark.copy(alpha = 0.8f), Color.Transparent),
                        endX = 800f
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
                .padding(bottom = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AccentOrange)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "EXCLUSIVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = Color.White
                    )
                }
                
                if (!movie.language.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = movie.language.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Text(
                    text = movie.category,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextSlate300
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = movie.title,
                fontSize = 32.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = movie.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSlate300,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = action,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (movie.type == "series") "View Episodes" else "Play Now", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CategoryRow(category: String, movies: List<Movie>, onPlay: (String, String) -> Unit, onNavigateToSeries: (String) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                color = Color.White
            )
            Text(
                text = "SEE ALL",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                color = AccentOrange
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(movies) { movie ->
                MovieCard(movie, onPlay, onNavigateToSeries)
            }
        }
    }
}

@Composable
fun MovieCard(movie: Movie, onPlay: (String, String) -> Unit, onNavigateToSeries: (String) -> Unit) {
    val action = {
        if (movie.type == "series") onNavigateToSeries(movie.id) else onPlay(movie.id, movie.videoUrl)
    }
    
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { action() },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(16.dp))
                .background(GlassLight)
                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
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
            
            if (!movie.language.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentOrange.copy(alpha = 0.9f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = movie.language.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            } else if (movie.isHero) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .border(1.dp, GlassBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("HD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
        Text(
            text = movie.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1
        )
    }
}
