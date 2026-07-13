package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VolumeIndicator(show: Boolean, volumeLevel: Float, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = show,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.background(Color.Black.copy(alpha=0.6f), shape = RoundedCornerShape(8.dp)).padding(16.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text("${(volumeLevel * 100).toInt()}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun BrightnessIndicator(show: Boolean, brightnessLevel: Float, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = show,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.background(Color.Black.copy(alpha=0.6f), shape = RoundedCornerShape(8.dp)).padding(16.dp)
        ) {
            Icon(Icons.Filled.BrightnessMedium, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text("${(brightnessLevel * 100).toInt()}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
