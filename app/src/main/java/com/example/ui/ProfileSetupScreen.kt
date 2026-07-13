package com.example.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    onProfileCreated: () -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("user_profile_prefs", Context.MODE_PRIVATE) }
    
    var username by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var customAvatarUrl by remember { mutableStateOf("") }
    
    val presetAvatars = listOf(
        "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80",
        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&q=80",
        "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?auto=format&fit=crop&w=150&q=80",
        "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=150&q=80",
        "https://images.unsplash.com/photo-1628157582853-a796fa650a6a?auto=format&fit=crop&w=150&q=80"
    )
    
    var selectedAvatar by remember { mutableStateOf(presetAvatars[0]) }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Aesthetic ambient glow backgrounds
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(AccentOrange.copy(alpha = 0.15f), Color.Transparent),
                            center = Offset(size.width * 0.8f, size.height * 0.2f),
                            radius = size.width * 0.6f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(AccentPurple.copy(alpha = 0.12f), Color.Transparent),
                            center = Offset(size.width * 0.2f, size.height * 0.8f),
                            radius = size.width * 0.6f
                        )
                    )
                }
                .blur(80.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(Color.White)
                    )
                }
                Text(
                    text = "NEOFLIX",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    letterSpacing = (-1).sp,
                    color = Color.White
                )
            }

            Text(
                text = "Create Your Profile",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Customize your profile to unlock customized recommendations.",
                fontSize = 14.sp,
                color = TextSlate400,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Profile Avatar Picker
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(GlassLight)
                    .border(2.dp, AccentOrange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val currentImgUrl = if (customAvatarUrl.isNotBlank()) customAvatarUrl else selectedAvatar
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(currentImgUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Selected Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Preset Avatars list
            Text(
                text = "Choose an Avatar",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSlate300,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                items(presetAvatars) { url ->
                    val isSelected = selectedAvatar == url && customAvatarUrl.isBlank()
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(GlassLight)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) AccentOrange else GlassBorder,
                                shape = CircleShape
                            )
                            .clickable {
                                selectedAvatar = url
                                customAvatarUrl = ""
                            }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(url)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Preset Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Input: Custom Avatar URL (Optional)
            OutlinedTextField(
                value = customAvatarUrl,
                onValueChange = { customAvatarUrl = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                label = { Text("Or paste Image URL (Optional)", color = TextSlate400) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = GlassBorder,
                    focusedBorderColor = AccentOrange,
                    unfocusedContainerColor = GlassLight,
                    focusedContainerColor = GlassLight,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            // Input: Username (Required)
            OutlinedTextField(
                value = username,
                onValueChange = { 
                    username = it
                    errorMessage = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                label = { Text("Username", color = TextSlate400) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = GlassBorder,
                    focusedBorderColor = AccentOrange,
                    unfocusedContainerColor = GlassLight,
                    focusedContainerColor = GlassLight,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            // Input: Age (Required)
            OutlinedTextField(
                value = age,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) {
                        age = input
                        errorMessage = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                label = { Text("Age", color = TextSlate400) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = GlassBorder,
                    focusedBorderColor = AccentOrange,
                    unfocusedContainerColor = GlassLight,
                    focusedContainerColor = GlassLight,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            if (errorMessage.isNotBlank()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Create Button
            Button(
                onClick = {
                    if (username.isBlank()) {
                        errorMessage = "Please enter a username"
                        return@Button
                    }
                    if (age.isBlank()) {
                        errorMessage = "Please enter your age"
                        return@Button
                    }
                    val finalAvatar = if (customAvatarUrl.isNotBlank()) customAvatarUrl else selectedAvatar
                    
                    sharedPrefs.edit().apply {
                        putString("username", username.trim())
                        putString("age", age.trim())
                        putString("avatar_url", finalAvatar)
                        putBoolean("profile_created", true)
                        apply()
                    }
                    onProfileCreated()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
            ) {
                Text("Get Started", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = "Enter App")
            }
        }
    }
}
