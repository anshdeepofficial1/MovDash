package com.movdash.app.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.movdash.app.R
import kotlinx.coroutines.launch

data class OnboardingFeature(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val tag: String
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val features = remember {
        listOf(
            OnboardingFeature(
                icon = Icons.Default.HighQuality,
                title = "4K Ultra HD & HDR",
                description = "Stream Hollywood blockbusters, Bollywood hits, and South Indian spectacles in pristine 4K quality with zero paywalls.",
                tag = "Cinema Grade"
            ),
            OnboardingFeature(
                icon = Icons.Default.Language,
                title = "Hindi, South & Multi-Audio",
                description = "Enjoy multi-audio tracks including Hindi Dubbed, Tamil, Telugu, and English with synchronized multilingual subtitles.",
                tag = "Multi-Audio"
            ),
            OnboardingFeature(
                icon = Icons.Default.FolderSpecial,
                title = "Private Offline Vault",
                description = "Download TV series neatly organized into dedicated folders, securely sandboxed inside Orbit away from your public gallery.",
                tag = "Private Storage"
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { features.size })
    val scope = rememberCoroutineScope()

    // Poster collage backdrops for authentic streaming atmosphere
    val backdrops = remember {
        listOf(
            "https://image.tmdb.org/t/p/w780/or06FN3Dka5tukK1e9sl16pB3iy.jpg", // Avengers
            "https://image.tmdb.org/t/p/w780/gKkl37BQuKTanygYQG1pyYgLVgf.jpg", // Stranger Things
            "https://image.tmdb.org/t/p/w780/r2J02Z2OpNTctfOSN2Ydgii51I3.jpg", // Interstellar
            "https://image.tmdb.org/t/p/w780/t6HIqrRAclMCA60NsSmeqe9RmNV.jpg"  // Batman
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF06080E))
    ) {
        // TOP CINEMATIC POSTER COLLAGE (Layered with deep obsidian gradient)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                backdrops.forEach { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
            // Multi-stop gradient fade into pitch black
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x8806080E),
                                Color(0xCC06080E),
                                Color(0xF206080E),
                                Color(0xFF06080E)
                            )
                        )
                    )
            )
        }

        // MAIN CONTENT SHELL
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP BAR: Orbit Mark + Skip Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_logo_transparent),
                        contentDescription = "Orbit",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "ORBIT",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                }

                TextButton(
                    onClick = onFinished,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF94A3B8))
                ) {
                    Text(
                        "Skip",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // CENTER CAROUSEL: Headline + Feature Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Calm Space Headline (Unslop principle: sentence case, high contrast)
                Text(
                    "Unlimited movies, TV series, and universes",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Stream anywhere in 4K UHD. 100% free forever.",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(28.dp))

                // Pager Feature Slider
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) { page ->
                    val feature = features[page]
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0E121A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF141923)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        feature.icon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        feature.title,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        feature.tag,
                                        color = Color(0xFF64748B),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                feature.description,
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                maxLines = 2
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Quiet Pill Indicators (4px grid)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(features.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 24.dp else 6.dp,
                            label = "indicatorWidth"
                        )
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White else Color(0xFF28303E))
                                .clickable {
                                    scope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // BOTTOM PRIMARY ACTION (High-contrast pure white button per unslop dark theme token)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onFinished,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Get Started",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    "No credit card required • Cancel never",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}
