package com.movdash.app.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class CosmicStar(
    val x: Float,
    val y: Float,
    val radius: Float,
    val speed: Float,
    val phase: Float
)

data class OnboardingSlide(
    val icon: ImageVector,
    val tag: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val backdropUrl: String,
    val badgeColor: Long
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val slides = remember {
        listOf(
            OnboardingSlide(
                icon = Icons.Default.HighQuality,
                tag = "4K ULTRA HD • HDR",
                title = "Stream Without Limits",
                subtitle = "Unlimited Movies, TV Shows & Sagas",
                description = "Experience Hollywood blockbusters, Bollywood hits, and South Indian spectacles in pristine 4K resolution with zero paywalls.",
                backdropUrl = "https://image.tmdb.org/t/p/w1280/or06FN3Dka5tukK1e9sl16pB3iy.jpg", // Avengers Endgame
                badgeColor = 0xFFE50914
            ),
            OnboardingSlide(
                icon = Icons.Default.Language,
                tag = "MULTI-AUDIO • DUAL AUDIO",
                title = "Hindi & Regional Dubs",
                subtitle = "Watch in Your Preferred Language",
                description = "Enjoy crystal-clear Hindi dubbed dual-audio, Tamil, Telugu, and English audio tracks with synchronized multilingual subtitles.",
                backdropUrl = "https://image.tmdb.org/t/p/w1280/nsV5Mfi9FAV4w8eDsdr7uqVswOk.jpg", // KGF / South Epic
                badgeColor = 0xFFF59E0B
            ),
            OnboardingSlide(
                icon = Icons.Default.Timeline,
                tag = "CHRONOLOGICAL WATCH ORDERS",
                title = "Cinematic Universes",
                subtitle = "Track Every Phase in Order",
                description = "Follow Marvel Phase 1 to Phase 6, Fast Saga, Transformers, and YRF Spy Universe in exact chronological timeline order.",
                backdropUrl = "https://image.tmdb.org/t/p/w1280/7RyHsO4yDXtBv1zUU3mTpHeQ0d5.jpg", // Marvel
                badgeColor = 0xFF8B5CF6
            ),
            OnboardingSlide(
                icon = Icons.Default.FolderSpecial,
                tag = "OFFLINE VAULT • PRIVATE",
                title = "Private Media Vault",
                subtitle = "Organized Series Folders",
                description = "Download entire series seasons organized into neat folders, securely sandboxed inside Orbit and hidden from your gallery.",
                backdropUrl = "https://image.tmdb.org/t/p/w1280/gKkl37BQuKTanygYQG1pyYgLVgf.jpg", // Stranger Things
                badgeColor = 0xFF10B981
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()

    // Edge-to-edge cosmic star particles
    val stars = remember {
        val rand = java.util.Random(1337)
        List(120) {
            CosmicStar(
                x = rand.nextFloat(),
                y = rand.nextFloat(),
                radius = 0.8f + rand.nextFloat() * 1.6f,
                speed = 0.6f + rand.nextFloat() * 1.4f,
                phase = rand.nextFloat() * 6.28f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "onboardStars")
    val twinkleTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "twinkle"
    )

    // Smooth auto-rotation
    LaunchedEffect(pagerState) {
        while (true) {
            delay(5000)
            if (!pagerState.isScrollInProgress) {
                val next = (pagerState.currentPage + 1) % slides.size
                pagerState.animateScrollToPage(
                    next,
                    animationSpec = tween(650, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030509))
    ) {
        // FULL EDGE-TO-EDGE COSMIC STARFIELD
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Deep cosmic nebula cloud
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x301E3A8A), Color(0x187C3AED), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.35f),
                    radius = w * 0.9f
                ),
                radius = w * 0.9f,
                center = Offset(w * 0.5f, h * 0.35f)
            )

            // Twinkling stars
            for (s in stars) {
                val starAlpha = (0.25f + 0.75f * kotlin.math.sin(twinkleTime * s.speed + s.phase).toFloat().coerceIn(0f, 1f))
                drawCircle(
                    color = Color.White.copy(alpha = starAlpha),
                    radius = s.radius,
                    center = Offset(s.x * w, s.y * h)
                )
            }
        }

        // SLIDER PAGES
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val slide = slides[page]

            Box(modifier = Modifier.fillMaxSize()) {
                // High-resolution Cinematic Hero Backdrop
                AsyncImage(
                    model = slide.backdropUrl,
                    contentDescription = slide.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.65f)
                )

                // Multi-Stop Obsidian Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x33030509),
                                    Color(0x77030509),
                                    Color(0xCC030509),
                                    Color(0xF5030509),
                                    Color(0xFF030509)
                                ),
                                startY = 100f
                            )
                        )
                )

                // Slide Content Card (Clean spacing above bottom controls)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 195.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Feature Tag Pill
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = Color(slide.badgeColor).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(slide.badgeColor).copy(alpha = 0.5f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                slide.icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                slide.tag,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        slide.title,
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp,
                        lineHeight = 38.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        slide.subtitle,
                        color = Color(0xFFF1F5F9),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        slide.description,
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }

        // TOP BAR: Orbit Brand + Skip Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo_transparent),
                    contentDescription = "Orbit",
                    modifier = Modifier.size(34.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "ORBIT",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
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
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // BOTTOM CONTROLS: Animated Page Indicators + Primary "Get Started" Action
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Interactive Pill Page Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(slides.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 28.dp else 7.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "pillWidth"
                    )
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White else Color(0xFF334155))
                            .clickable {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                    )
                }
            }

            // High-Contrast Primary Button (Directly opens setup / app)
            Button(
                onClick = onFinished,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Get Started",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                "100% Free Forever • Zero Subscription • No Sign-up Required",
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
