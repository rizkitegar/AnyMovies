package com.movies.anymovies.core.ui.state

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

private const val SHIMMER_DURATION_MS = 1000
private const val SHIMMER_TRAVEL = 400f

/**
 * Skeleton-layout container for a screen's first load. Compose the final layout's
 * shape using [ShimmerBlock] children so the placeholder matches the real content.
 */
@Composable
fun LoadingShimmer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxSize(), content = content)
}

/**
 * A single animated shimmer placeholder shape — a stand-in for text, an image, or a chip
 * while real content loads.
 */
@Composable
fun ShimmerBlock(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(4.dp),
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = SHIMMER_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerTranslate",
    )
    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.colorScheme.surface
    val brush = Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(x = -SHIMMER_TRAVEL + translate * SHIMMER_TRAVEL * 2, y = 0f),
        end = Offset(x = translate * SHIMMER_TRAVEL * 2, y = SHIMMER_TRAVEL / 2),
    )
    Box(modifier = modifier.clip(shape).background(brush))
}
