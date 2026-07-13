package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.Movie
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToPlayer: (String, String) -> Unit,
    onNavigateToSeries: (String) -> Unit,
    viewModel: MoviesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
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
            // Liquid background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(AccentOrange.copy(alpha = 0.15f), Color.Transparent),
                                center = Offset(size.width * 0.2f, size.height * 0.3f),
                                radius = size.width * 0.5f
                            )
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(AccentPurple.copy(alpha = 0.15f), Color.Transparent),
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

                                if (searchQuery.isBlank() && state.heroMovie != null) {
                                    item {
                                        HeroBanner(state.heroMovie, onNavigateToPlayer, onNavigateToSeries)
                                    }
                                }

                                state.categories.forEach { (category, movies) ->
                                    item {
                                        CategoryRow(category, movies, onNavigateToPlayer, onNavigateToSeries)
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    HistoryScreen(onNavigateToPlayer, onNavigateToSeries)
                }
                2 -> {
                    ProfileScreen(onNavigateToPlayer, onNavigateToSeries)
                }
            }
        }
    }
}

@Composable
fun NeoflixBottomNav(currentTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = GlassLight.copy(alpha = 0.5f),
        contentColor = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentTab == 0,
            onClick = { onTabSelected(0) },
            icon = { Icon(Icons.Default.Home, "Home") },
            label = { Text("Home", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = AccentOrange,
                unselectedIconColor = TextSlate400,
                unselectedTextColor = TextSlate400,
                indicatorColor = AccentOrange.copy(alpha = 0.5f)
            )
        )
        NavigationBarItem(
            selected = currentTab == 1,
            onClick = { onTabSelected(1) },
            icon = { Icon(Icons.Default.List, "History") },
            label = { Text("History", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = AccentOrange,
                unselectedIconColor = TextSlate400,
                unselectedTextColor = TextSlate400,
                indicatorColor = AccentOrange.copy(alpha = 0.5f)
            )
        )
        NavigationBarItem(
            selected = currentTab == 2,
            onClick = { onTabSelected(2) },
            icon = { Icon(Icons.Default.Person, "Profile") },
            label = { Text("Profile", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = AccentOrange,
                unselectedIconColor = TextSlate400,
                unselectedTextColor = TextSlate400,
                indicatorColor = AccentOrange.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun NeoflixHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
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
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
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
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        placeholder = { Text("Search movies, genres...", color = TextSlate400) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSlate400) },
        shape = RoundedCornerShape(24.dp),
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
fun HeroBanner(movie: Movie, onPlay: (String, String) -> Unit, onNavigateToSeries: (String) -> Unit) {
    val action = {
        if (movie.type == "series") onNavigateToSeries(movie.id) else onPlay(movie.id, movie.videoUrl)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(450.dp)
            .padding(horizontal = 24.dp, vertical = 16.dp)
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
        
        // Gradient Overlay
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

        // Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
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
                Text(
                    text = "${movie.category}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextSlate300
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = movie.title,
                fontSize = 36.sp,
                lineHeight = 36.sp,
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
                    modifier = Modifier.weight(1f).height(48.dp),
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
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassLight)
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", fontSize = 24.sp, color = Color.White)
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
            
            if (movie.isHero) {
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
