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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.movdash.app.R
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
                icon = Icons.Default.AllInclusive,
                badge = "ORBIT MULTIVERSE",
                badgeColor = Color(0xFFF1F5F9),
                title = "Welcome to Orbit",
                subtitle = "Infinite Cinematic Universe",
                description = "Enter Orbit — the next-generation entertainment universe. Stream thousands of Hollywood blockbusters, Bollywood hits, South Indian spectacles, and binge-worthy web series in pristine 4K UHD.",
                highlights = listOf("Infinite Universe", "Pristine 4K UHD", "Zero Paywalls"),
                gradientColors = listOf(Color(0xFF2B1E0A), Color(0xFF090D14))
            ),
            OnboardingSlide(
                icon = Icons.Default.Translate,
                badge = "ORBIT AUDIO",
                badgeColor = Color(0xFF38BDF8),
                title = "Universal Language Orbit",
                subtitle = "Hindi Dubbed & Multi-Language",
                description = "Stream in your preferred tongue with effortless multi-audio switching: Hindi, Tamil, Telugu, English, and dual-audio 5.1 soundtracks with synchronized multilingual subtitles.",
                highlights = listOf("Hindi Dubbed", "Dual Audio 5.1", "Multi-Subtitles"),
                gradientColors = listOf(Color(0xFF0C2438), Color(0xFF090D14))
            ),
            OnboardingSlide(
                icon = Icons.Default.Bolt,
                badge = "ORBIT WARP STREAM",
                badgeColor = Color(0xFF818CF8),
                title = "Warp-Speed Streaming Engine",
                subtitle = "Smart Adaptive Multi-Server CDN",
                description = "Powered by ultra-fast resilient edge server pipelines. Automatic silent failover ensures butter-smooth playback without ever interrupting your viewing experience.",
                highlights = listOf("Zero Buffering", "Auto-Failover", "Adaptive 4K/HD"),
                gradientColors = listOf(Color(0xFF19173B), Color(0xFF090D14))
            ),
            OnboardingSlide(
                icon = Icons.Default.FitScreen,
                badge = "ORBIT CINEMA PLAYER",
                badgeColor = Color(0xFFA855F7),
                title = "Pro Auto-Landscape Player",
                subtitle = "Intuitive Gestures & Touch Controls",
                description = "Launches automatically into immersive wide landscape mode. Swipe for brightness & volume, double-tap 10s seek, long-press for 2X speed boost, and secure screen lock.",
                highlights = listOf("Auto-Landscape", "Fluid Gestures", "Minus Countdown"),
                gradientColors = listOf(Color(0xFF2A0F3D), Color(0xFF090D14))
            ),
            OnboardingSlide(
                icon = Icons.Default.AutoAwesomeMotion,
                badge = "ORBIT SAGAS",
                badgeColor = Color(0xFFF43F5E),
                title = "Franchise Chronological Timelines",
                subtitle = "Official Chronological Watch Orders",
                description = "Track entire cinematic universes in their official storyline order — Marvel MCU, Fast & Furious, Transformers, YRF Spy Universe, Lokesh Cinematic Universe, and Neel Universe.",
                highlights = listOf("Chronological Order", "Phase Timelines", "Watched Tracker"),
                gradientColors = listOf(Color(0xFF3B0E1B), Color(0xFF090D14))
            ),
            OnboardingSlide(
                icon = Icons.Default.FolderSpecial,
                badge = "ORBIT PRIVATE VAULT",
                badgeColor = Color(0xFF10B981),
                title = "Offline Series Folder Vault",
                subtitle = "Private In-App Sandbox Storage",
                description = "Download TV series grouped neatly into dedicated series folders and movie downloads securely isolated in Orbit's private sandbox (hidden from external gallery and files).",
                highlights = listOf("Series Folders", "Private Sandbox", "Offline Cinema"),
                gradientColors = listOf(Color(0xFF0A2B1D), Color(0xFF090D14))
            ),
            OnboardingSlide(
                icon = Icons.Default.HourglassTop,
                badge = "ORBIT RELEASE RADAR",
                badgeColor = Color(0xFFFB923C),
                title = "Theatrical Release Radar",
                subtitle = "Countdowns, Dates & Release Alerts",
                description = "Stay up to date with unreleased blockbusters like Avengers: Doomsday, The Batman Part II, and Vision Quest with official theatrical release dates and reminder alerts.",
                highlights = listOf("Theatrical Radar", "Release Alerts", "Zero CAM Rips"),
                gradientColors = listOf(Color(0xFF381B09), Color(0xFF090D14))
            ),
            OnboardingSlide(
                icon = Icons.Default.RocketLaunch,
                badge = "ENTER ORBIT",
                badgeColor = Color(0xFFF1F5F9),
                title = "Launch Into Orbit",
                subtitle = "100% Free Forever • No Sign-up",
                description = "No subscriptions, no fees, and no account hurdles. Your personal portal to the infinite universe of movies and series is ready for departure.",
                highlights = listOf("Zero Sign-up", "Forever Free", "Instant Cinema"),
                gradientColors = listOf(Color(0xFF332009), Color(0xFF090D14))
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07090E))
    ) {
        // TOP BAR WITH ORBIT BADGE AND SKIP
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF131A26),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222F42))
            ) {
                Text(
                    text = "Orbit • ${pagerState.currentPage + 1} of ${slides.size}",
                    color = Color(0xFFF1F5F9),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
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
                // Orbital Planet & Rings Visual
                Box(
                    modifier = Modifier.size(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer Orbit Celestial Ring
                    Box(
                        modifier = Modifier
                            .size(148.dp)
                            .clip(CircleShape)
                            .border(1.dp, slide.badgeColor.copy(alpha = 0.22f), CircleShape)
                    )

                    // Middle Glowing Nebula Aura
                    Box(
                        modifier = Modifier
                            .size(116.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(slide.badgeColor.copy(alpha = 0.30f), Color.Transparent)
                                )
                            )
                            .border(1.dp, slide.badgeColor.copy(alpha = 0.40f), CircleShape)
                    )

                    // Planet Core with Icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF182232), Color(0xFF0C121D))
                                )
                            )
                            .border(1.5.dp, slide.badgeColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (page == 0) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_logo_transparent),
                                contentDescription = "Orbit Logo",
                                modifier = Modifier.size(52.dp)
                            )
                        } else {
                            Icon(
                                imageVector = slide.icon,
                                contentDescription = null,
                                tint = slide.badgeColor,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Orbit Pill Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = slide.badgeColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, slide.badgeColor.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = slide.badge,
                        color = slide.badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.3.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
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
                    color = Color(0xFFF1F5F9),
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
                            color = Color(0xFF131A26),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2A3A))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
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
                                if (isSelected) Color(0xFFF1F5F9) else Color(0xFF2C3748)
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

                // Next or Get Started Button (Orbit Galactic Gold)
                if (pagerState.currentPage < slides.size - 1) {
                    Button(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF1F5F9),
                            contentColor = Color(0xFF101620)
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
                            containerColor = Color(0xFFF1F5F9),
                            contentColor = Color(0xFF101620)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp)
                    ) {
                        Icon(
                            Icons.Default.RocketLaunch,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Launch Orbit • Start Streaming",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
