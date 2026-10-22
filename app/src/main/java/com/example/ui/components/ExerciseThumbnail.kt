package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage

private val ThumbnailBorderColor = Color(0xFF22283A)
private val ElectricBlue = Color(0xFF1877F2)

/**
 * Hevy-style exercise illustration thumbnail with circular white background,
 * subtle border, and graceful Electric Blue barbell icon fallback.
 */
@Composable
fun ExerciseThumbnail(
    imageUrl: String?,
    exerciseName: String,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp
) {
    // Resolve CDN URL if not explicitly provided
    val resolvedUrl = imageUrl?.takeIf { it.isNotBlank() } ?: ExerciseImageProvider.getImageUrl(exerciseName)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, ThumbnailBorderColor, CircleShape)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!resolvedUrl.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = resolvedUrl,
                contentDescription = exerciseName,
                contentScale = ContentScale.Fit,
                loading = {
                    Box(
                        modifier = Modifier.size(size),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = ElectricBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                error = {
                    ExerciseFallbackIcon(size = size)
                },
                modifier = Modifier
                    .size(size - 8.dp)
                    .clip(CircleShape)
            )
        } else {
            ExerciseFallbackIcon(size = size)
        }
    }
}

@Composable
private fun ExerciseFallbackIcon(size: Dp) {
    Icon(
        imageVector = Icons.Default.FitnessCenter,
        contentDescription = "Exercise icon",
        tint = ElectricBlue,
        modifier = Modifier.size(size * 0.52f)
    )
}
