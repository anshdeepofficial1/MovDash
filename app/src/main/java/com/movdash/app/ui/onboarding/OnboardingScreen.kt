package com.movdash.app.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class OnboardingSlide(
    val icon: ImageVector,
    val badge: String,
    val badgeColor: Color,
    val title: String,
    val subtitle: String,
    val description: String,
    val highlights: List<String>,
    val gradientColors: List<Color>
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val slides = remember {
        listOf(
            OnboardingSlide(
                icon = Icons.Default.MovieFilter,
                badge = "NEXT-GEN STREAMING",
                badgeColor = Color(0xFFE50914),
                title = "Welcome to MovDash",
                subtitle = "Your Ultimate Cinematic Hub",
                description = "Stream thousands of blockbuster movies, trending web series, and anime in pristine 4K UHD and 1080p without any paywalls or logins.",
                highlights = listOf("100% Free", "4K Ultra HD", "Zero Sign-up"),
                gradientColors = listOf(Color(0xFF2B0A10), Color(0xFF0F141C))
            ),
            OnboardingSlide(
                icon = Icons.Default.Translate,
                badge = "AUDIO FREEDOM",
                badgeColor = Color(0xFFE9B96E),
                title = "Watch in Your Language",
                subtitle = "Hindi Dubbed & Multi-Audio",
                description = "Never let language be a barrier. Enjoy instant access to Hindi Dubbed releases, Original Audio, Tamil, Telugu, and Dual-Audio streams.",
                highlights = listOf("Hindi Dubbed", "Dual Audio 5.1", "Original Track"),
                gradientColors = listOf(Color(0xFF261D0C), Color(0xFF0F141C))
            ),
            OnboardingSlide(
                icon = Icons.Default.Bolt,
                badge = "BUFFERLESS SPEED",
                badgeColor = Color(0xFF38BDF8),
                title = "Multi-Source Streaming Engine",
                subtitle = "Smart Adaptive CDN Routes",
                description = "Powered by high-speed resilient content delivery pipelines. If any stream slows down, backup sources switch in smoothly.",
                highlights = listOf("Instant Playback", "Auto Failover", "1080p 60fps"),
                gradientColors = listOf(Color(0xFF0C2233), Color(0xFF0F141C))
            ),
            OnboardingSlide(
                icon = Icons.Default.FitScreen,
                badge = "CINEMA CONTROLS",
                badgeColor = Color(0xFFA855F7),
                title = "Pro Auto-Landscape Player",
                subtitle = "Intuitive Gesture & Touch Control",
                description = "Automatically launches into wide landscape mode. Swipe for brightness & volume, double-tap 10s seek, long-press for 2X speed, and lock the screen.",
                highlights = listOf("Auto-Landscape", "Swipe Gestures", "Screen Lock"),
                gradientColors = listOf(Color(0xFF241038), Color(0xFF0F141C))
            ),
            OnboardingSlide(
                icon = Icons.Default.VideoLibrary,
                badge = "BINGE-READY",
                badgeColor = Color(0xFF10B981),
                title = "Detailed Episode Guides",
                subtitle = "Thumbnails, Synopses & Run Times",
                description = "Browse TV shows with visual episode thumbnails, official titles, release dates, and seamless in-player episode switching.",
                highlights = listOf("Episode Stills", "Full Synopses", "In-Player Drawer"),
                gradientColors = listOf(Color(0xFF09291D), Color(0xFF0F141C))
            ),
            OnboardingSlide(
                icon = Icons.Default.BookmarkAdded,
                badge = "LOCAL & PRIVATE",
                badgeColor = Color(0xFFF59E0B),
                title = "Personal Library & History",
                subtitle = "Saved Securely on Your Phone",
                description = "Keep track of your favorite titles, save movies to your watchlist, and resume playback right where you left off. 100% private and offline.",
                highlights = listOf("Watchlist", "Watch History", "No Cloud Tracking"),
                gradientColors = listOf(Color(0xFF2A1C08), Color(0xFF0F141C))
            ),
            OnboardingSlide(
                icon = Icons.Default.HourglassTop,
                badge = "RELEASE RADAR",
                badgeColor = Color(0xFFEC4899),
                title = "Track Upcoming Blockbusters",
                subtitle = "Premiere Dates & Reminders",
                description = "Stay ahead of major releases like Avengers: Doomsday, Vision Quest, and The Batman Part II with countdowns and release alerts.",
                highlights = listOf("Release Radar", "Remind Me", "JustWatch Links"),
                gradientColors = listOf(Color(0xFF2E0D1F), Color(0xFF0F141C))
            ),
            OnboardingSlide(
                icon = Icons.Default.RocketLaunch,
                badge = "READY TO STREAM",
                badgeColor = Color(0xFFE50914),
                title = "100% Free Forever",
                subtitle = "Pure Entertainment, No Strings",
                description = "No subscriptions, no fees, and no account hurdles. Your portal to unlimited movies and series is ready.",
                highlights = listOf("Zero Fees", "No Account", "Instant Cinema"),
                gradientColors = listOf(Color(0xFF33090F), Color(0xFF0F141C))
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090C10))
    ) {
        // TOP SKIP BUTTON
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF141A24)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} of ${slides.size}",
                    color = Color(0xFF8E9BAE),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            if (pagerState.currentPage < slides.size - 1) {
                TextButton(onClick = onFinished) {
                    Text(
                        "Skip Tour",
                        color = Color(0xFF8E9BAE),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Spacer(Modifier.width(1.dp))
            }
        }

        // PAGER
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 70.dp, bottom = 120.dp)
        ) { page ->
            val slide = slides[page]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Card with glowing gradient
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(slide.badgeColor.copy(alpha = 0.35f), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF1A2332), Color(0xFF101622))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = slide.icon,
                            contentDescription = null,
                            tint = slide.badgeColor,
                            modifier = Modifier.size(46.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = slide.badgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = slide.badge,
                        color = slide.badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Title & Subtitle
                Text(
                    text = slide.title,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = slide.subtitle,
                    color = Color(0xFFE9B96E),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(14.dp))

                // Description
                Text(
                    text = slide.description,
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(20.dp))

                // Feature Highlights Pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    slide.highlights.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF161E2C)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = slide.badgeColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = tag,
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // BOTTOM CONTROLS (Dots + Navigation Buttons)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Page Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(slides.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 24.dp else 6.dp,
                        animationSpec = tween(300),
                        label = "dotWidth"
                    )
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color(0xFFE50914) else Color(0xFF2C3748)
                            )
                    )
                }
            }

            // Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                if (pagerState.currentPage > 0) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF161E2C))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous",
                            tint = Color.White
                        )
                    }
                } else {
                    Spacer(Modifier.size(46.dp))
                }

                // Next or Get Started Button
                if (pagerState.currentPage < slides.size - 1) {
                    Button(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE50914),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text("Next", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = onFinished,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE50914),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Get Started • Start Streaming",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
