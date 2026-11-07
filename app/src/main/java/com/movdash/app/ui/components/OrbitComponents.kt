package com.movdash.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movdash.app.R

/**
 * High-performance Shimmer Skeleton modifier for silky smooth loading placeholders.
 */
fun Modifier.shimmerSkeleton(
    shape: Shape = RoundedCornerShape(12.dp)
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = -500f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerColors = listOf(
        Color(0xFF0F1521),
        Color(0xFF1E2A3C),
        Color(0xFF283850),
        Color(0xFF1E2A3C),
        Color(0xFF0F1521)
    )

    this
        .clip(shape)
        .background(
            brush = Brush.linearGradient(
                colors = shimmerColors,
                start = Offset(translateAnim - 400f, translateAnim - 400f),
                end = Offset(translateAnim, translateAnim)
            )
        )
}

/**
 * Cosmic Orbit Animated Loader featuring the new Orbit logo and spinning celestial ring.
 */
@Composable
fun OrbitLoader(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    message: String? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbitLoader")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitRotation"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbitPulse"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            // Rotating Celestial Orbit Ring
            Box(
                modifier = Modifier
                    .size(size)
                    .graphicsLayer { rotationZ = rotation }
                    .border(
                        BorderStroke(
                            2.dp,
                            Brush.sweepGradient(
                                listOf(
                                    Color(0xFFFFFFFF),
                                    Color(0xFFCBD5E1),
                                    Color(0x22FFFFFF),
                                    Color.Transparent,
                                    Color(0xFFFFFFFF)
                                )
                            )
                        ),
                        CircleShape
                    )
            )

            // Center Pulsing Orbit 3D Logo
            Image(
                painter = painterResource(id = R.drawable.ic_logo_transparent),
                contentDescription = "Orbit Loading",
                modifier = Modifier
                    .size(size * 0.72f)
                    .graphicsLayer {
                        scaleX = pulse
                        scaleY = pulse
                    }
            )
        }

        if (message != null) {
            Spacer(Modifier.height(14.dp))
            Text(
                message,
                color = Color(0xFFCBD5E1),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Skeleton placeholder for a single movie poster card.
 */
@Composable
fun MoviePosterCardSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(125.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(185.dp)
                .shimmerSkeleton(RoundedCornerShape(16.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(14.dp)
                .shimmerSkeleton(RoundedCornerShape(4.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(11.dp)
                .shimmerSkeleton(RoundedCornerShape(4.dp))
        )
    }
}

/**
 * Skeleton horizontal row of movie poster cards.
 */
@Composable
fun MovieRowSkeleton(
    title: String? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (title != null) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .width(140.dp)
                    .height(20.dp)
                    .shimmerSkeleton(RoundedCornerShape(6.dp))
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled = false
        ) {
            items(5) {
                MoviePosterCardSkeleton()
            }
        }
    }
}

/**
 * Skeleton for Hero Carousel spotlight.
 */
@Composable
fun HeroSpotlightSkeleton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(260.dp)
            .shimmerSkeleton(RoundedCornerShape(22.dp))
    )
}

/**
 * Skeleton for Search/Discover Results Grid.
 */
@Composable
fun SearchResultsSkeleton() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MoviePosterCardSkeleton(Modifier.weight(1f))
                MoviePosterCardSkeleton(Modifier.weight(1f))
                MoviePosterCardSkeleton(Modifier.weight(1f))
            }
        }
    }
}

/**
 * Skeleton for full Media Details Page.
 */
@Composable
fun MediaDetailsSkeleton() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Backdrop skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .shimmerSkeleton(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
        )
        Spacer(Modifier.height(18.dp))
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(28.dp)
                    .shimmerSkeleton(RoundedCornerShape(6.dp))
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(60.dp, 24.dp).shimmerSkeleton(RoundedCornerShape(6.dp)))
                Box(modifier = Modifier.size(50.dp, 24.dp).shimmerSkeleton(RoundedCornerShape(6.dp)))
                Box(modifier = Modifier.size(70.dp, 24.dp).shimmerSkeleton(RoundedCornerShape(6.dp)))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .shimmerSkeleton(RoundedCornerShape(14.dp))
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .shimmerSkeleton(RoundedCornerShape(8.dp))
            )
        }
    }
}

/**
 * Skeleton for Episode Item.
 */
@Composable
fun EpisodeItemSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(120.dp, 68.dp)
                .shimmerSkeleton(RoundedCornerShape(10.dp))
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth(0.8f).height(16.dp).shimmerSkeleton(RoundedCornerShape(4.dp)))
            Box(modifier = Modifier.fillMaxWidth(0.5f).height(12.dp).shimmerSkeleton(RoundedCornerShape(4.dp)))
        }
    }
}
