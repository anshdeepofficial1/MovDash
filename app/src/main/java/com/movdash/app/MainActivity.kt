package com.movdash.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer

import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.PlayCircleFilled

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.movdash.app.data.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.window.Dialog
import com.movdash.app.data.Universe
import com.movdash.app.data.UniverseData
import com.movdash.app.data.CastMember
import com.movdash.app.data.DownloadedItem

import com.movdash.app.player.PlayerActivity
import com.movdash.app.ui.onboarding.OnboardingScreen
import com.movdash.app.ui.theme.MovDashTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class SectionInfo(
    val title: String,
    val subtitle: String,
    val items: List<Movie>,
    val isRanked: Boolean = false
)

fun getMovieQualityBadge(movie: Movie): String {
    val yr = movie.releaseDate?.take(4)?.toIntOrNull() ?: 2020
    val vote = movie.voteAverage ?: 0.0
    return if (vote >= 7.6 || yr >= 2023) "4K UHD" else "1080p HD"
}

@Composable
private fun OpeningSplashScreen(
    onAnimationFinished: () -> Unit
) {
    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            alpha.animateTo(1f, animationSpec = tween(500, easing = LinearEasing))
        }
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        delay(350)
        launch {
            textAlpha.animateTo(1f, animationSpec = tween(450))
        }
        launch {
            progress.animateTo(1f, animationSpec = tween(1200, easing = FastOutSlowInEasing))
        }
        delay(1600)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07090E)),
        contentAlignment = Alignment.Center
    ) {
        // Subtle Luxury Golden Radial Halo
        Box(
            modifier = Modifier
                .size(340.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x33E9B96E), Color(0x0DE9B96E), Color.Transparent),
                        radius = 450f
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo with Animated Scale, Border and Elevation
            Box(
                modifier = Modifier
                    .size(115.dp)
                    .graphicsLayer(
                        scaleX = scale.value,
                        scaleY = scale.value,
                        alpha = alpha.value
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF101724))
                    .border(BorderStroke(2.dp, Color(0xFFE9B96E)), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = "Orbit Logo",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(22.dp))
                )
            }

            Spacer(Modifier.height(22.dp))

            // Animated App Name
            Text(
                "ORBIT",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp,
                modifier = Modifier.graphicsLayer(alpha = textAlpha.value)
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "MOVIES & SERIES UNIVERSE",
                color = Color(0xFFE9B96E),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.5.sp,
                modifier = Modifier.graphicsLayer(alpha = textAlpha.value)
            )

            Spacer(Modifier.height(36.dp))

            // Cinematic Loading Progress Bar
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(3.5.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0xFF1B2433))
                    .graphicsLayer(alpha = textAlpha.value)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.value)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFE9B96E), Color(0xFFFFD700))
                            )
                        )
                )
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MovDashTheme { MovDashApp() } }
    }
}

private enum class AppTab { HOME, DISCOVER, LIBRARY, PROFILE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovDashApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("movdash", Context.MODE_PRIVATE) }
    var showSplash by remember { mutableStateOf(true) }
    var showOnboarding by remember {
        mutableStateOf(!prefs.getBoolean("has_completed_onboarding", false))
    }

    if (showSplash) {
        OpeningSplashScreen(
            onAnimationFinished = {
                showSplash = false
            }
        )
        return
    }

    if (showOnboarding) {
        OnboardingScreen(
            onFinished = {
                prefs.edit().putBoolean("has_completed_onboarding", true).apply()
                showOnboarding = false
            }
        )
        return
    }

    var apiBase by remember {
        mutableStateOf(prefs.getString("api_base", "https://movdash-production.up.railway.app/") ?: "https://movdash-production.up.railway.app/")
    }
    val api = remember(apiBase) { ApiFactory.create(apiBase) }
    val scope = rememberCoroutineScope()

    var tab by remember { mutableStateOf(AppTab.HOME) }
    var selectedMovie by remember { mutableStateOf<Movie?>(null) }
    var showAppSettings by remember { mutableStateOf(false) }

    // Instant preloaded catalog so the app NEVER opens with a blank screen
    var spotlightList by remember { mutableStateOf(PreloadedCatalog.spotlightItems) }
    var movies by remember { mutableStateOf(PreloadedCatalog.trendingMovies) }
    var series by remember { mutableStateOf(PreloadedCatalog.trendingSeries) }
    var popularMovies by remember { mutableStateOf(PreloadedCatalog.popularMovies) }
    var downloads by remember { mutableStateOf(LibraryManager.getDownloads(context)) }
    var activeUniverseByDiscover by remember { mutableStateOf<Universe?>(null) }
    var popularSeries by remember { mutableStateOf(PreloadedCatalog.popularSeries) }
    var upcomingList by remember { mutableStateOf(PreloadedCatalog.upcomingTitles) }
    var sources by remember { mutableStateOf<List<SourceInfo>>(emptyList()) }

    var searchResults by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var searchType by remember { mutableStateOf("multi") }
    var searchGenre by remember { mutableStateOf<String?>(null) }
    var searchLoading by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }

    // Library state
    var watchlist by remember { mutableStateOf(LibraryManager.getWatchlist(context)) }
    var history by remember { mutableStateOf(LibraryManager.getHistory(context)) }
    var favorites by remember { mutableStateOf(LibraryManager.getFavorites(context)) }
    var activeSection by remember { mutableStateOf<SectionInfo?>(null) }

    fun refreshLibrary() {
        watchlist = LibraryManager.getWatchlist(context)
        history = LibraryManager.getHistory(context)
        favorites = LibraryManager.getFavorites(context)
        downloads = LibraryManager.getDownloads(context)
    }

    // Hardware Back Button Interception (AniDash hierarchy)
    BackHandler(enabled = selectedMovie != null) {
        refreshLibrary()
        selectedMovie = null
    }

    BackHandler(enabled = selectedMovie == null && activeSection != null) {
        activeSection = null
    }

    BackHandler(enabled = selectedMovie == null && activeSection == null && tab != AppTab.HOME) {
        tab = AppTab.HOME
    }

    // Background silent catalog refresh from API
    LaunchedEffect(apiBase) {
        try {
            sources = api.sources()
            val home = api.home()
            if (home.trendingMovies.isNotEmpty()) movies = home.trendingMovies
            if (home.trendingSeries.isNotEmpty()) series = home.trendingSeries
            if (home.popularMovies.isNotEmpty()) popularMovies = home.popularMovies
            if (home.popularSeries.isNotEmpty()) popularSeries = home.popularSeries
            val combined = (movies + series).distinctBy { "${it.mediaType}-${it.id}" }
            if (combined.isNotEmpty()) spotlightList = combined.take(5)
        } catch (_: Exception) {
            // Keep using preloaded catalog seamlessly without disturbing the user
        }
    }

    Scaffold(
        containerColor = Color(0xFF090B10),
        topBar = {
            if (selectedMovie == null) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_logo),
                                contentDescription = "Orbit Logo",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    "ORBIT",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    letterSpacing = 1.2.sp
                                )
                                Text(
                                    "Movies & Series Universe",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFE9B96E),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { tab = AppTab.DISCOVER }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                        }
                        IconButton(onClick = { showAppSettings = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF090B10))
                )
            }
        },
        bottomBar = {
            if (selectedMovie == null && activeSection == null) {
                FloatingPillBottomNav(
                    currentTab = tab,
                    onTabSelected = { newTab ->
                        if (newTab == AppTab.LIBRARY || newTab == AppTab.PROFILE) {
                            refreshLibrary()
                        }
                        tab = newTab
                    }
                )
            }
        }
    ) { padding ->
        if (selectedMovie != null) {
            MovieDetailsScreen(
                movie = selectedMovie!!,
                api = api,
                sources = sources,
                modifier = Modifier.fillMaxSize().padding(padding),
                onBack = {
                    refreshLibrary()
                    selectedMovie = null
                },
                onPlayStarted = {
                    LibraryManager.addToHistory(context, selectedMovie!!)
                    refreshLibrary()
                },
                onSelectMovie = { movie ->
                    selectedMovie = movie
                }
            )
        } else if (activeSection != null) {
            SectionScreen(
                section = activeSection!!,
                onBack = { activeSection = null },
                onMovieClick = { selectedMovie = it }
            )
        } else {
            Box(Modifier.fillMaxSize().padding(padding).background(Color(0xFF090B10))) {
                when (tab) {
                    AppTab.HOME -> HomeScreen(
                        spotlightMovies = spotlightList,
                        movies = movies,
                        series = series,
                        popularMovies = popularMovies,
                        popularSeries = popularSeries,
                        upcomingMovies = upcomingList,
                        history = history,
                        onMovieClick = { selectedMovie = it },
                        onDiscover = { tab = AppTab.DISCOVER },
                        onGenreClick = { genre ->
                            searchGenre = genre
                            tab = AppTab.DISCOVER
                        },
                        onOpenSection = { section ->
                            activeSection = section
                        }
                    )
                    AppTab.DISCOVER -> DiscoverScreen(
                        query = query,
                        onQueryChange = { query = it; searchError = null },
                        mediaType = searchType,
                        onMediaTypeChange = { searchType = it; searchResults = emptyList(); searchError = null },
                        results = searchResults,
                        loading = searchLoading,
                        error = searchError,
                        onSearch = {
                            if (query.isNotBlank()) scope.launch {
                                searchLoading = true
                                searchError = null
                                try {
                                    val res = api.search(query.trim(), searchType)
                                    searchResults = if (res.isNotEmpty()) res else {
                                        PreloadedCatalog.allDiscover.filter {
                                            it.title.contains(query.trim(), ignoreCase = true)
                                        }
                                    }
                                    if (searchResults.isEmpty()) {
                                        searchError = "No titles found matching \"$query\"."
                                    }
                                } catch (error: Exception) {
                                    searchResults = PreloadedCatalog.allDiscover.filter {
                                        it.title.contains(query.trim(), ignoreCase = true)
                                    }
                                    if (searchResults.isEmpty()) {
                                        searchError = "No titles found. Try searching another title."
                                    }
                                } finally {
                                    searchLoading = false
                                }
                            }
                        },
                        onMovieClick = { selectedMovie = it },
                        activeUniverse = activeUniverseByDiscover,
                        onUniverseSelected = { activeUniverseByDiscover = it },
                        searchGenre = searchGenre,
                        onGenreSelected = { searchGenre = it }
                    )
                    AppTab.LIBRARY -> LibraryScreen(
                        watchlist = watchlist,
                        history = history,
                        favorites = favorites,
                        downloads = downloads,
                        onMovieClick = { selectedMovie = it },
                        onExplore = { tab = AppTab.DISCOVER },
                        onClearHistory = {
                            LibraryManager.clearHistory(context)
                            refreshLibrary()
                        },
                        onPlayDownloaded = { downloadedItem ->
                            val streamUrl = StreamServerFactory.getDefaultStreamUrl(
                                downloadedItem.id,
                                downloadedItem.mediaType,
                                downloadedItem.seasonNumber,
                                downloadedItem.episodeNumber
                            )
                            val intent = Intent(context, PlayerActivity::class.java).apply {
                                putExtra(PlayerActivity.EXTRA_MEDIA_URL, streamUrl)
                                putExtra(PlayerActivity.EXTRA_STREAM_TYPE, "embed")
                                putExtra(PlayerActivity.EXTRA_TITLE, downloadedItem.title)
                                putExtra(
                                    PlayerActivity.EXTRA_SUBTITLE,
                                    downloadedItem.episodeTitle ?: "${downloadedItem.audioTrack} • ${downloadedItem.quality}"
                                )
                                putExtra(PlayerActivity.EXTRA_SERVER_NAME, "Offline Local")
                                putExtra(PlayerActivity.EXTRA_TMDB_ID, downloadedItem.id)
                                putExtra(PlayerActivity.EXTRA_MEDIA_TYPE, downloadedItem.mediaType)
                                putExtra(PlayerActivity.EXTRA_SEASON, downloadedItem.seasonNumber ?: 1)
                                putExtra(PlayerActivity.EXTRA_EPISODE, downloadedItem.episodeNumber ?: 1)
                            }
                            context.startActivity(intent)
                        },
                        onDeleteDownload = { item ->
                            LibraryManager.removeDownload(context, item.id, item.episodeNumber)
                            refreshLibrary()
                        },
                        onSelectUniverse = { u ->
                            activeUniverseByDiscover = u
                            tab = AppTab.DISCOVER
                        }
                    )
                    AppTab.PROFILE -> ProfileScreen(
                        watchlistCount = watchlist.size,
                        historyCount = history.size,
                        onOpenSettings = { showAppSettings = true },
                        onOpenOnboarding = { showOnboarding = true }
                    )
                }
            }
        }
    }

    if (showAppSettings) {
        AppSettingsDialog(
            onDismiss = { showAppSettings = false }
        )
    }
}

@Composable
private fun FloatingPillBottomNav(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .height(64.dp),
        shape = RoundedCornerShape(100.dp),
        color = Color(0xEE0E1522),
        border = BorderStroke(1.dp, Color(0x33FFFFFF)),
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                Triple(AppTab.HOME, "Home", Icons.Default.Home),
                Triple(AppTab.DISCOVER, "Discover", Icons.Default.Search),
                Triple(AppTab.LIBRARY, "Vault", Icons.Default.BookmarkBorder),
                Triple(AppTab.PROFILE, "Profile", Icons.Default.PersonOutline)
            )

            tabs.forEach { (tab, label, icon) ->
                val selected = currentTab == tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (selected) Color(0x33E9B96E) else Color.Transparent)
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = if (selected) 16.dp else 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (selected) Color(0xFFE9B96E) else Color(0xFF8E9BAE),
                            modifier = Modifier.size(20.dp)
                        )
                        if (selected) {
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = label,
                                color = Color(0xFFE9B96E),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SectionScreen(
    section: SectionInfo,
    onBack: () -> Unit,
    onMovieClick: (Movie) -> Unit
) {
    Scaffold(
        containerColor = Color(0xFF090B10),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            section.title,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${section.items.size} Titles • ${section.subtitle}",
                            color = Color(0xFFE9B96E),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF090B10))
            )
        }
    ) { padding ->
        if (section.isRanked) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                itemsIndexed(section.items, key = { index, movie -> "ranked-${movie.id}-$index" }) { index, movie ->
                    val rank = index + 1
                    val (badgeColor, bgColor, borderColor) = when (rank) {
                        1 -> Triple(Color(0xFFFFD700), Color(0x22FFD700), Color(0x88FFD700))
                        2 -> Triple(Color(0xFFD6D6D6), Color(0x1CD6D6D6), Color(0x66D6D6D6))
                        3 -> Triple(Color(0xFFCD7F32), Color(0x22CD7F32), Color(0x77CD7F32))
                        else -> Triple(Color(0xFF94A3B8), Color(0xFF131A26), Color(0xFF263347))
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMovieClick(movie) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(badgeColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "#$rank",
                                    color = badgeColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Box(
                                modifier = Modifier
                                    .size(width = 65.dp, height = 90.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E2837))
                            ) {
                                val imageUrl = movie.posterPath?.let { "https://image.tmdb.org/t/p/w200$it" }
                                if (imageUrl != null) {
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = movie.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(Modifier.weight(1f)) {
                                Text(
                                    movie.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        "★ " + String.format("%.1f", movie.voteAverage ?: 0.0),
                                        color = Color(0xFFE9B96E),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        movie.releaseDate?.take(4) ?: "",
                                        color = Color(0xFF8E9BAE),
                                        fontSize = 12.sp
                                    )
                                    val badge = getMovieQualityBadge(movie)
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (badge == "4K UHD") Color(0xFFE9B96E) else Color(0xFF2563EB)
                                    ) {
                                        Text(
                                            badge,
                                            color = if (badge == "4K UHD") Color(0xFF101620) else Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    movie.overview,
                                    color = Color(0xFF9AA5B5),
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(section.items, key = { "section-${it.id}" }) { item ->
                    MoviePosterCard(item) { onMovieClick(item) }
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    spotlightMovies: List<Movie>,
    movies: List<Movie>,
    series: List<Movie>,
    popularMovies: List<Movie>,
    popularSeries: List<Movie>,
    upcomingMovies: List<Movie>,
    history: List<Movie>,
    onMovieClick: (Movie) -> Unit,
    onDiscover: () -> Unit,
    onGenreClick: (String) -> Unit,
    onOpenSection: (SectionInfo) -> Unit
) {
    var selectedNxshaCategory by remember { mutableStateOf("All") }
    val nxshaCategories = listOf("All", "Hollywood", "Bollywood", "South Cinema", "TV Series", "Movies")

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        HeroSpotlightCarousel(
            movies = spotlightMovies,
            onWatch = onMovieClick
        )

        // Continue Watching Shelf (AniDash style)
        if (history.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            SectionTitle(
                title = "Continue Watching",
                action = "History",
                onClick = {
                    onOpenSection(
                        SectionInfo(
                            title = "Continue Watching",
                            subtitle = "Recently streamed titles",
                            items = history
                        )
                    )
                }
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history.take(10), key = { "continue-${it.id}" }) { movie ->
                    Column(
                        Modifier
                            .width(150.dp)
                            .clickable { onMovieClick(movie) }
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(95.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF151D29))
                        ) {
                            val bg = movie.backdropPath ?: movie.posterPath
                            if (bg != null) {
                                AsyncImage(
                                    model = "https://image.tmdb.org/t/p/w300$bg",
                                    contentDescription = movie.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(Color(0x44000000)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.PlayCircleFilled,
                                    contentDescription = "Resume",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            // Progress bar
                            Box(
                                Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(Color(0x55FFFFFF))
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxWidth(0.65f)
                                        .fillMaxHeight()
                                        .background(Color(0xFFE50914))
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            movie.title,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // NXSHA-STYLE CATEGORY TABS (Hollywood, Bollywood, South Cinema, TV Series, Movies)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(nxshaCategories) { category ->
                val isSelected = selectedNxshaCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedNxshaCategory = category },
                    label = {
                        Text(
                            category,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE9B96E),
                        selectedLabelColor = Color(0xFF101620),
                        containerColor = Color(0xFF131A26),
                        labelColor = Color(0xFFCBD5E1)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) Color(0xFFE9B96E) else Color(0xFF263347)
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // 1. RELEASING SOON • IN THEATERS / OTT (Verified Official Upcoming Titles)
        if (selectedNxshaCategory in listOf("All", "Hollywood", "Movies")) {
            Spacer(Modifier.height(10.dp))
            SectionTitle("Releasing Soon • In Theaters / OTT", "See All") {
                onOpenSection(
                    SectionInfo(
                        title = "Releasing Soon • In Theaters / OTT",
                        subtitle = "Official upcoming theatrical releases",
                        items = upcomingMovies
                    )
                )
            }
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(upcomingMovies, key = { "upcoming-home-${it.id}" }) { item ->
                    MoviePosterCard(item) { onMovieClick(item) }
                }
            }
        }

        // 2. HOLLYWOOD BLOCKBUSTERS (Nxsha Section)
        if (selectedNxshaCategory in listOf("All", "Hollywood", "Movies")) {
            Spacer(Modifier.height(18.dp))
            SectionTitle("Hollywood Blockbusters", "See All") {
                onOpenSection(
                    SectionInfo(
                        title = "Hollywood Blockbusters",
                        subtitle = "Global box-office Hollywood hits",
                        items = PreloadedCatalog.hollywoodMovies
                    )
                )
            }
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(PreloadedCatalog.hollywoodMovies, key = { "hollywood-movie-${it.id}" }) { movie ->
                    MoviePosterCard(movie) { onMovieClick(movie) }
                }
            }
        }

        // 3. BOLLYWOOD & HINDI HITS (Nxsha Section)
        if (selectedNxshaCategory in listOf("All", "Bollywood", "Movies")) {
            Spacer(Modifier.height(18.dp))
            SectionTitle("Bollywood Hits & Hindi Cinema", "See All") {
                onOpenSection(
                    SectionInfo(
                        title = "Bollywood Hits & Hindi Cinema",
                        subtitle = "Massive blockbuster Hindi cinema",
                        items = PreloadedCatalog.bollywoodHits
                    )
                )
            }
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(PreloadedCatalog.bollywoodHits, key = { "bolly-${it.id}" }) { item ->
                    MoviePosterCard(item) { onMovieClick(item) }
                }
            }
        }

        // 4. TRENDING TV & WEB SERIES (Nxsha Section)
        if (selectedNxshaCategory in listOf("All", "TV Series")) {
            Spacer(Modifier.height(18.dp))
            SectionTitle("Trending TV & Web Series", "See All") {
                onOpenSection(
                    SectionInfo(
                        title = "Trending TV & Web Series",
                        subtitle = "Binge-worthy shows & serials",
                        items = series
                    )
                )
            }
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(series, key = { "trend-tv-${it.id}" }) { item ->
                    MoviePosterCard(item) { onMovieClick(item) }
                }
            }
        }

        // HOLLYWOOD TV SERIES (Nxsha Section)
        if (selectedNxshaCategory in listOf("All", "Hollywood", "TV Series")) {
            Spacer(Modifier.height(18.dp))
            SectionTitle("Hollywood TV Series", "See All") {
                onOpenSection(
                    SectionInfo(
                        title = "Hollywood TV Series",
                        subtitle = "Top-rated global television masterworks",
                        items = PreloadedCatalog.hollywoodSeries
                    )
                )
            }
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(PreloadedCatalog.hollywoodSeries, key = { "hollywood-series-${it.id}" }) { item ->
                    MoviePosterCard(item) { onMovieClick(item) }
                }
            }
        }

        // 5. SOUTH INDIAN EPIC SPECTACLES (Nxsha Section)
        if (selectedNxshaCategory in listOf("All", "South Cinema", "Movies")) {
            Spacer(Modifier.height(18.dp))
            SectionTitle("South Indian Epic Cinema", "See All") {
                onOpenSection(
                    SectionInfo(
                        title = "South Indian Epic Cinema",
                        subtitle = "Pan-Indian action and drama spectacles",
                        items = PreloadedCatalog.southIndianCinema
                    )
                )
            }
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(PreloadedCatalog.southIndianCinema, key = { "south-${it.id}" }) { item ->
                    MoviePosterCard(item) { onMovieClick(item) }
                }
            }
        }

        // 6. TOP RATED MASTERPIECES (IMDb 8.5+ Ranked Leaderboard!)
        if (selectedNxshaCategory in listOf("All", "Hollywood", "Movies")) {
            Spacer(Modifier.height(18.dp))
            SectionTitle("Top Rated Masterpieces", "Ranked #1-10") {
                onOpenSection(
                    SectionInfo(
                        title = "Top Rated Masterpieces",
                        subtitle = "Ranked #1 to #10 all-time cinema",
                        items = PreloadedCatalog.topRatedMasterpieces,
                        isRanked = true
                    )
                )
            }
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(PreloadedCatalog.topRatedMasterpieces, key = { "top-rated-${it.id}" }) { item ->
                    MoviePosterCard(item) { onMovieClick(item) }
                }
            }
        }

        // 7. BOLLYWOOD WEB SERIES (Nxsha Section)
        if (selectedNxshaCategory in listOf("All", "Bollywood", "TV Series")) {
            Spacer(Modifier.height(18.dp))
            SectionTitle("Bollywood & Hindi Web Series", "See All") {
                onOpenSection(
                    SectionInfo(
                        title = "Bollywood & Hindi Web Series",
                        subtitle = "Acclaimed Indian crime and thriller sagas",
                        items = PreloadedCatalog.bollywoodSeries
                    )
                )
            }
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(PreloadedCatalog.bollywoodSeries, key = { "bolly-series-${it.id}" }) { item ->
                    MoviePosterCard(item) { onMovieClick(item) }
                }
            }
        }

        // 8. POPULAR BLOCKBUSTERS
        if (selectedNxshaCategory in listOf("All", "Movies")) {
            Spacer(Modifier.height(18.dp))
            SectionTitle("Popular Blockbusters", "See All") {
                onOpenSection(
                    SectionInfo(
                        title = "Popular Blockbusters",
                        subtitle = "All-time viewer favorites",
                        items = popularMovies
                    )
                )
            }
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(popularMovies, key = { "pop-movie-${it.id}" }) { item ->
                    MoviePosterCard(item) { onMovieClick(item) }
                }
            }
        }

        Spacer(Modifier.height(30.dp))
    }
}
private fun HeroSpotlightCarousel(
    movies: List<Movie>,
    onWatch: (Movie) -> Unit
) {
    val items = movies.take(5)
    if (items.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { items.size })
    LaunchedEffect(pagerState, items.size) {
        if (items.size > 1) {
            while (true) {
                delay(5500)
                pagerState.animateScrollToPage((pagerState.currentPage + 1) % items.size)
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 10.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val movie = items[page]
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF273246), Color(0xFF111722))))
                    .clickable { onWatch(movie) }
            ) {
                val backdrop = movie.backdropPath ?: movie.posterPath
                if (backdrop != null) {
                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w1280$backdrop",
                        contentDescription = movie.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            listOf(Color(0x33080B11), Color(0x77080B11), Color(0xF8080B11))
                        )
                    )
                )

                Column(
                    Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE9B96E)
                        ) {
                            Text(
                                movie.mediaType.uppercase(),
                                color = Color(0xFF1B1309),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        movie.voteAverage?.let {
                            Text(
                                "★ " + "%.1f".format(it),
                                color = Color(0xFFE9B96E),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        movie.releaseDate?.take(4)?.let {
                            Text("· $it", color = Color(0xFFCBD5E1), style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Text(
                        movie.title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        movie.overview,
                        color = Color(0xFFB5C0CE),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isUnreleased = isUnreleasedTitle(movie.releaseDate, null, movie.title)
                        Button(
                            onClick = { onWatch(movie) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isUnreleased) Color(0xFF233246) else Color(0xFFE9B96E),
                                contentColor = if (isUnreleased) Color(0xFFE9B96E) else Color(0xFF1A1309)
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                if (isUnreleased) Icons.Default.HourglassTop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(if (isUnreleased) "Coming Soon" else "Watch Now", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(items.size) { index ->
                val selected = pagerState.currentPage == index
                Box(
                    Modifier
                        .padding(horizontal = 3.dp)
                        .width(if (selected) 20.dp else 6.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(if (selected) Color(0xFFE9B96E) else Color(0xFF455062))
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, action: String = "See All", onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFE9B96E), modifier = Modifier.size(18.dp))
        }
        Text(action, color = Color(0xFFE9B96E), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MoviePosterCard(movie: Movie, onClick: () -> Unit) {
    Column(Modifier.width(132.dp).clickable(onClick = onClick)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF2B374A), Color(0xFF141C28))))
        ) {
            val imageUrl = movie.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            // Quality Badge (4K UHD in luxurious gold / 1080p HD in electric blue)
            val qualityBadge = getMovieQualityBadge(movie)
            Surface(
                shape = RoundedCornerShape(bottomStart = 8.dp, topEnd = 14.dp),
                color = if (qualityBadge == "4K UHD") Color(0xFFE9B96E) else Color(0xFF2563EB),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(
                    qualityBadge,
                    color = if (qualityBadge == "4K UHD") Color(0xFF101620) else Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }

            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xEE080B10))))
            )
            Row(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isUnreleased = isUnreleasedTitle(movie.releaseDate, null, movie.title)
                Text(
                    if (isUnreleased) "SOON" else (movie.voteAverage?.let { "★ " + "%.1f".format(it) } ?: "HD"),
                    color = Color(0xFFE9B96E),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xBB121A24)
                ) {
                    Text(
                        movie.mediaType.uppercase(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            movie.title,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            movie.releaseDate?.take(4) ?: "Upcoming",
            color = Color(0xFF8793A3),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

private fun DiscoverScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    mediaType: String,
    onMediaTypeChange: (String) -> Unit,
    results: List<Movie>,
    loading: Boolean,
    error: String?,
    onSearch: () -> Unit,
    onMovieClick: (Movie) -> Unit,
    activeUniverse: Universe?,
    onUniverseSelected: (Universe?) -> Unit,
    searchGenre: String? = null,
    onGenreSelected: (String?) -> Unit = {}
) {
    val context = LocalContext.current
    var timelineWatchVersion by remember { mutableIntStateOf(0) }
    var selectedGenre by remember(searchGenre) { mutableStateOf(searchGenre) }
    var searchHistory by remember { mutableStateOf(LibraryManager.getSearchHistory(context)) }

    // If a universe is open, display the Chronological Watch Order Timeline
    if (activeUniverse != null) {
        UniverseWatchOrderScreen(
            universe = activeUniverse,
            onBack = { onUniverseSelected(null) },
            onMovieClick = onMovieClick
        )
        return
    }

    val displayList = remember(results, query, mediaType, selectedGenre) {
        when {
            results.isNotEmpty() -> results
            selectedGenre != null -> PreloadedCatalog.getMoviesForGenre(selectedGenre!!)
            query.isBlank() -> {
                var list = PreloadedCatalog.allDiscover
                if (mediaType != "multi") {
                    list = list.filter { it.mediaType == mediaType }
                }
                list
            }
            else -> emptyList()
        }
    }

    val searchSuggestions = remember(query) {
        if (query.trim().isNotEmpty()) {
            PreloadedCatalog.allDiscover
                .filter { it.title.contains(query.trim(), ignoreCase = true) }
                .take(5)
        } else {
            emptyList()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Discover & Universes", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Search all movies, series, explore genres or franchise watch orders.", color = Color(0xFF9AA5B5), style = MaterialTheme.typography.bodySmall)

        Spacer(Modifier.height(10.dp))

        // Type filter chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = mediaType == "multi",
                onClick = { onMediaTypeChange("multi") },
                label = { Text("All") }
            )
            FilterChip(
                selected = mediaType == "movie",
                onClick = { onMediaTypeChange("movie") },
                label = { Text("Movies") }
            )
            FilterChip(
                selected = mediaType == "tv",
                onClick = { onMediaTypeChange("tv") },
                label = { Text("Series") }
            )
        }

        Spacer(Modifier.height(10.dp))

        // Search Input Bar (AniDash style)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text("Search movies, stars, universes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFE9B96E)) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE9B96E),
                    unfocusedBorderColor = Color(0xFF263347),
                    focusedContainerColor = Color(0xFF121A25),
                    unfocusedContainerColor = Color(0xFF121A25)
                ),
                shape = RoundedCornerShape(14.dp)
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    if (query.isNotBlank()) {
                        LibraryManager.addSearchQuery(context, query.trim())
                        searchHistory = LibraryManager.getSearchHistory(context)
                    }
                    onSearch()
                },
                enabled = query.isNotBlank() && !loading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE9B96E), contentColor = Color(0xFF1A1309))
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        }

        // Live Auto-complete Suggestions Dropdown
        if (query.isNotBlank() && searchSuggestions.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF151D29),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(vertical = 4.dp)) {
                    searchSuggestions.forEach { suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onQueryChange(suggestion.title)
                                    LibraryManager.addSearchQuery(context, suggestion.title)
                                    searchHistory = LibraryManager.getSearchHistory(context)
                                    onMovieClick(suggestion)
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (suggestion.mediaType == "tv") Icons.Default.Tv else Icons.Default.Movie,
                                contentDescription = null,
                                tint = Color(0xFFE9B96E),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                suggestion.title,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                suggestion.releaseDate?.take(4) ?: "",
                                color = Color(0xFF8E9BAE),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Recent Searches (AniDash Style)
        if (query.isBlank() && searchHistory.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFFE9B96E), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Recent Searches", color = Color(0xFFCBD5E1), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                TextButton(
                    onClick = {
                        LibraryManager.clearSearchHistory(context)
                        searchHistory = emptyList()
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Clear All", color = Color(0xFF8E9BAE), fontSize = 11.sp)
                }
            }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(searchHistory) { item ->
                    SuggestionChip(
                        onClick = {
                            onQueryChange(item)
                            onSearch()
                        },
                        label = { Text(item, color = Color(0xFFDCE2EC), fontSize = 12.sp) },
                        icon = {
                            Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF8E9BAE), modifier = Modifier.size(14.dp))
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF141C28)),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = Color(0xFF263347)
                        )
                    )
                }
            }
        }

        // Restored Genre Filter Chips Row (Action, Adventure, Sci-Fi, etc.)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Browse by Genre", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (selectedGenre != null) {
                Spacer(Modifier.width(8.dp))
                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFE9B96E)) {
                    Text(
                        selectedGenre!!,
                        color = Color(0xFF101620),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedGenre == null,
                    onClick = {
                        selectedGenre = null
                        onGenreSelected(null)
                    },
                    label = { Text("All Genres") }
                )
            }
            items(PreloadedCatalog.genres) { genre ->
                FilterChip(
                    selected = selectedGenre == genre,
                    onClick = {
                        val newG = if (selectedGenre == genre) null else genre
                        selectedGenre = newG
                        onGenreSelected(newG)
                    },
                    label = { Text(genre) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // CINEMATIC UNIVERSES SECTION
        Text("Cinematic Universes & Sagas", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Select a universe to track its official chronological watch order", color = Color(0xFF8F9CAA), fontSize = 11.sp)

        Spacer(Modifier.height(10.dp))

        // Rectangular Universe Cards
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(UniverseData.allUniverses, key = { it.id }) { u ->
                val watchedCount = remember(u.id, timelineWatchVersion) {
                    LibraryManager.getWatchedCountForUniverse(context, u)
                }
                val totalCount = u.items.size
                val percent = if (totalCount > 0) (watchedCount.toFloat() / totalCount.toFloat()) else 0f

                Card(
                    modifier = Modifier
                        .width(220.dp)
                        .height(130.dp)
                        .clickable { onUniverseSelected(u) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141C28))
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(u.accentColor).copy(alpha = 0.35f), Color(0xFF101722))
                                )
                            )
                            .padding(12.dp)
                    ) {
                        Column(
                            Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(u.accentColor)
                                ) {
                                    Text(
                                        u.logoTag,
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    "$totalCount Titles",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Column {
                                Text(
                                    u.name,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "Chronological Order",
                                    color = Color(0xFFE9B96E),
                                    fontSize = 10.sp
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Watched: $watchedCount / $totalCount",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        "${(percent * 100).toInt()}%",
                                        color = Color(u.accentColor),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { percent },
                                    color = Color(u.accentColor),
                                    trackColor = Color(0xFF263347),
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (loading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFFE9B96E), strokeWidth = 2.dp)
                Text("Searching titles across catalog...", color = Color(0xFFE9B96E))
            }
        } else if (error != null) {
            Text(error, color = Color(0xFFFFA69C), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 8.dp))
        }

        // Search Empty State with Fuzzy Suggestions (AniDash style)
        if (!loading && displayList.isEmpty() && query.isNotBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.SearchOff,
                    contentDescription = null,
                    tint = Color(0xFFE9B96E),
                    modifier = Modifier.size(54.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "No matches found for \"$query\"",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Try searching for popular blockbusters or explore genres above.",
                    color = Color(0xFF8E9BAE),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Text("Did you mean:", color = Color(0xFFE9B96E), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Avengers", "Interstellar", "K.G.F", "Stranger Things").forEach { sug ->
                        SuggestionChip(
                            onClick = {
                                onQueryChange(sug)
                                onSearch()
                            },
                            label = { Text(sug, color = Color.White, fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF1F2B3D))
                        )
                    }
                }
            }
        }

        Text(
            if (query.isBlank()) (if (selectedGenre != null) "$selectedGenre Cinema" else "Trending Discoveries") else "Search Results (${displayList.size})",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 6.dp)
        )

        // Grid of movies
        val rows = displayList.chunked(3)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            rows.forEach { rowMovies ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowMovies.forEach { movie ->
                        Box(Modifier.weight(1f)) {
                            MoviePosterCard(movie = movie, onClick = { onMovieClick(movie) })
                        }
                    }
                    repeat(3 - rowMovies.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private fun UniverseWatchOrderScreen(
    universe: Universe,
    onBack: () -> Unit,
    onMovieClick: (Movie) -> Unit
) {
    val context = LocalContext.current
    var watchVersion by remember { mutableIntStateOf(0) }
    val watchedCount = remember(watchVersion) { LibraryManager.getWatchedCountForUniverse(context, universe) }
    val totalCount = universe.items.size
    val percent = if (totalCount > 0) (watchedCount.toFloat() / totalCount.toFloat()) else 0f

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Back Button & Tag
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onBack,
                contentPadding = PaddingValues(horizontal = 0.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFFE9B96E))
                Spacer(Modifier.width(6.dp))
                Text("Back to Discover", color = Color(0xFFE9B96E), fontWeight = FontWeight.Bold)
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(universe.accentColor)
            ) {
                Text(
                    universe.logoTag,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Hero Banner
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141C28)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(universe.accentColor).copy(alpha = 0.35f), Color(0xFF0F1722))
                        )
                    )
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(universe.name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    Text(universe.subtitle, color = Color(0xFFE9B96E), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(universe.description, color = Color(0xFF94A3B8), fontSize = 12.sp, lineHeight = 18.sp)

                    Spacer(Modifier.height(4.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Watch Progress: $watchedCount of $totalCount Movies Watched",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${(percent * 100).roundToInt()}%",
                            color = Color(universe.accentColor),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    LinearProgressIndicator(
                        progress = { percent },
                        color = Color(universe.accentColor),
                        trackColor = Color(0xFF263347),
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "Chronological Timeline Watch Order",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Mark movies as watched as you complete each chapter in the timeline.",
            color = Color(0xFF8F9CAA),
            fontSize = 11.sp
        )

        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            universe.items.forEach { item ->
                val isWatched = remember(item.movie.id, watchVersion) {
                    LibraryManager.isWatched(context, item.movie.id)
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isWatched) Color(0xFF14202E) else Color(0xFF121824))
                        .clickable { onMovieClick(item.movie) }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isWatched) Color(0xFF10B981) else Color(0xFF1F2B3E)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isWatched) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        } else {
                            Text(
                                "#${item.timelineOrder}",
                                color = Color(0xFFE9B96E),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .size(width = 65.dp, height = 90.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1C2738))
                    ) {
                        item.movie.posterPath?.let {
                            AsyncImage(
                                model = "https://image.tmdb.org/t/p/w185$it",
                                contentDescription = item.movie.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF222F42)
                        ) {
                            Text(
                                item.phaseOrEra,
                                color = Color(0xFFE9B96E),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            item.movie.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${item.releaseYear} • ${item.movie.voteAverage?.let { "★ $it" } ?: "HD"}",
                            color = Color(0xFF8F9CAA),
                            fontSize = 11.sp
                        )
                        if (item.note.isNotBlank()) {
                            Text(
                                item.note,
                                color = Color(0xFF6B7A90),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            LibraryManager.toggleWatched(context, item.movie.id)
                            watchVersion++
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isWatched) Color(0x3310B981) else Color(0xFF1B2433))
                    ) {
                        Icon(
                            if (isWatched) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Mark as Watched",
                            tint = if (isWatched) Color(0xFF10B981) else Color(0xFF8F9CAA),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryScreen(
    watchlist: List<Movie>,
    history: List<Movie>,
    favorites: List<Movie>,
    downloads: List<DownloadedItem>,
    onMovieClick: (Movie) -> Unit,
    onExplore: () -> Unit,
    onClearHistory: () -> Unit,
    onPlayDownloaded: (DownloadedItem) -> Unit,
    onDeleteDownload: (DownloadedItem) -> Unit,
    onSelectUniverse: (Universe) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "Watchlist (${watchlist.size})",
        "History (${history.size})",
        "Downloads (${downloads.size})",
        "Favorites (${favorites.size})",
        "Universes"
    )

    val totalStorageMb = remember(downloads) { downloads.sumOf { it.fileSizeMb } }

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 8.dp)) {
        // Luxury Vault Header
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Personal Cinema Vault", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Offline media, universes progress & your personal library.", color = Color(0xFF9AA5B5), style = MaterialTheme.typography.bodySmall)
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF172333)
            ) {
                Text(
                    "$totalStorageMb MB Offline",
                    color = Color(0xFFE9B96E),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Segmented Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF101620),
            contentColor = Color(0xFFE9B96E),
            edgePadding = 0.dp,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) Color(0xFFE9B96E) else Color(0xFF8E9BAE),
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        when (selectedTab) {
            // DOWNLOADS TAB
            2 -> {
                if (downloads.isEmpty()) {
                    EmptyVaultState(
                        icon = Icons.Default.DownloadDone,
                        title = "No Offline Downloads",
                        message = "Download movies or series episodes with your preferred Hindi/English audio and subtitles to watch offline without internet.",
                        actionText = "Explore Titles to Download",
                        onAction = onExplore
                    )
                } else {
                    val tvDownloads = downloads.filter { it.mediaType == "tv" }
                    val movieDownloads = downloads.filter { it.mediaType != "tv" }
                    val seriesFolders = tvDownloads.groupBy { it.id }
                    var expandedSeriesIds by remember { mutableStateOf(setOf<Int>()) }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            // In-App Sandbox Notice Banner
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF0F1824),
                                border = BorderStroke(1.dp, Color(0xFF1E2D42)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Security,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            "Secure In-App Sandbox Storage",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "All downloads stay strictly inside Orbit's private vault (hidden from phone gallery & external file managers).",
                                            color = Color(0xFF8E9BAE),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        // SERIES FOLDERS
                        seriesFolders.forEach { (seriesId, epList) ->
                            val firstEp = epList.first()
                            val isExpanded = expandedSeriesIds.contains(seriesId)
                            val folderSizeMb = epList.sumOf { it.fileSizeMb }

                            item(key = "series-folder-$seriesId") {
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26)),
                                    border = BorderStroke(1.dp, if (isExpanded) Color(0xFFE9B96E) else Color(0xFF1E2B3D)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    expandedSeriesIds = if (isExpanded) {
                                                        expandedSeriesIds - seriesId
                                                    } else {
                                                        expandedSeriesIds + seriesId
                                                    }
                                                },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Folder Icon Badge
                                            Box(
                                                modifier = Modifier
                                                    .size(46.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color(0xFF1A2637)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                                                    contentDescription = "Series Folder",
                                                    tint = Color(0xFFE9B96E),
                                                    modifier = Modifier.size(26.dp)
                                                )
                                            }

                                            Spacer(Modifier.width(12.dp))

                                            Column(Modifier.weight(1f)) {
                                                Text(
                                                    firstEp.title,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    "Series Folder • ${epList.size} Episodes • $folderSizeMb MB",
                                                    color = Color(0xFFE9B96E),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Spacer(Modifier.height(4.dp))
                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF1E2D42)) {
                                                        Text(
                                                            "PRIVATE VAULT",
                                                            color = Color(0xFF10B981),
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF1E2D42)) {
                                                        Text(
                                                            firstEp.quality,
                                                            color = Color(0xFFE9B96E),
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            IconButton(
                                                onClick = {
                                                    expandedSeriesIds = if (isExpanded) {
                                                        expandedSeriesIds - seriesId
                                                    } else {
                                                        expandedSeriesIds + seriesId
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                    contentDescription = "Expand",
                                                    tint = Color(0xFFE9B96E)
                                                )
                                            }
                                        }

                                        // Expanded episodes list
                                        if (isExpanded) {
                                            Spacer(Modifier.height(10.dp))
                                            HorizontalDivider(color = Color(0xFF1F2D40))
                                            Spacer(Modifier.height(10.dp))

                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                epList.forEach { ep ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(10.dp))
                                                            .background(Color(0xFF0D141F))
                                                            .padding(8.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(Modifier.weight(1f)) {
                                                            Text(
                                                                ep.episodeTitle ?: "Season ${ep.seasonNumber ?: 1} Episode ${ep.episodeNumber ?: 1}",
                                                                color = Color.White,
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.SemiBold,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                            Spacer(Modifier.height(2.dp))
                                                            Text(
                                                                "S${ep.seasonNumber ?: 1} E${ep.episodeNumber ?: 1} • ${ep.audioTrack} • ${ep.fileSizeMb} MB",
                                                                color = Color(0xFF8E9BAE),
                                                                fontSize = 10.sp
                                                            )
                                                        }

                                                        Button(
                                                            onClick = { onPlayDownloaded(ep) },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914), contentColor = Color.White),
                                                            shape = RoundedCornerShape(8.dp),
                                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                            modifier = Modifier.height(28.dp)
                                                        ) {
                                                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                                            Spacer(Modifier.width(4.dp))
                                                            Text("Play", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                        }

                                                        Spacer(Modifier.width(6.dp))

                                                        IconButton(
                                                            onClick = { onDeleteDownload(ep) },
                                                            modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFF241519))
                                                        ) {
                                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFF87171), modifier = Modifier.size(14.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // MOVIE DOWNLOADS
                        items(movieDownloads, key = { "movie-dl-${it.id}" }) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF131A26))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 75.dp, height = 100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E2837))
                                ) {
                                    item.posterPath?.let {
                                        AsyncImage(
                                            model = "https://image.tmdb.org/t/p/w185$it",
                                            contentDescription = item.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xDD10B981),
                                        modifier = Modifier.align(Alignment.TopStart).padding(4.dp)
                                    ) {
                                        Text("OFFLINE", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(Modifier.weight(1f)) {
                                    Text(
                                        item.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text("Movie Download • Private Sandbox", color = Color(0xFF8E9BAE), fontSize = 11.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF1F2C3E)) {
                                            Text(item.audioTrack.substringBefore(" "), color = Color(0xFFCBD5E1), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF1F2C3E)) {
                                            Text(item.quality, color = Color(0xFFE9B96E), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF1F2C3E)) {
                                            Text("${item.fileSizeMb} MB", color = Color(0xFF94A3B8), fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { onPlayDownloaded(item) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914), contentColor = Color.White),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Play Offline", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        IconButton(
                                            onClick = { onDeleteDownload(item) },
                                            modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFF241519))
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFF87171), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // UNIVERSES PROGRESS TAB
            4 -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(UniverseData.allUniverses, key = { it.id }) { u ->
                        val watched = LibraryManager.getWatchedCountForUniverse(context, u)
                        val total = u.items.size
                        val percent = if (total > 0) (watched.toFloat() / total.toFloat()) else 0f

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectUniverse(u) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26))
                        ) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = Color(u.accentColor)) {
                                        Text(u.logoTag, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                    Text("${(percent * 100).roundToInt()}% Completed", color = Color(u.accentColor), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(u.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(u.subtitle, color = Color(0xFF94A3B8), fontSize = 11.sp)

                                LinearProgressIndicator(
                                    progress = { percent },
                                    color = Color(u.accentColor),
                                    trackColor = Color(0xFF263347),
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
                                )

                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("$watched of $total Movies Watched", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                    TextButton(onClick = { onSelectUniverse(u) }) {
                                        Text("Continue Watch Order", color = Color(0xFFE9B96E), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Spacer(Modifier.width(4.dp))
                                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFFE9B96E), modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // WATCHLIST (0), HISTORY (1), FAVORITES (3)
            else -> {
                val currentList = when (selectedTab) {
                    0 -> watchlist
                    1 -> history
                    else -> favorites
                }

                if (currentList.isEmpty()) {
                    EmptyVaultState(
                        icon = when (selectedTab) {
                            0 -> Icons.Default.BookmarkBorder
                            1 -> Icons.Default.History
                            else -> Icons.Default.FavoriteBorder
                        },
                        title = when (selectedTab) {
                            0 -> "No items in Watchlist"
                            1 -> "No Watch History yet"
                            else -> "No Favorite titles"
                        },
                        message = "Explore blockbusters & web series and tap bookmark or play to save them here.",
                        actionText = "Explore Trending Titles",
                        onAction = onExplore
                    )
                } else {
                    Column(Modifier.fillMaxSize()) {
                        if (selectedTab == 1 && history.isNotEmpty()) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = onClearHistory) {
                                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Clear All History", color = Color(0xFFF87171), fontSize = 11.sp)
                                }
                            }
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 20.dp)
                        ) {
                            items(currentList, key = { "${it.mediaType}-${it.id}" }) { movie ->
                                Column(modifier = Modifier.clickable { onMovieClick(movie) }) {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF17202D))
                                    ) {
                                        movie.posterPath?.let {
                                            AsyncImage(
                                                model = "https://image.tmdb.org/t/p/w342$it",
                                                contentDescription = movie.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        movie.title,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        movie.releaseDate?.take(4) ?: "HD",
                                        color = Color(0xFF8793A3),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyVaultState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    actionText: String,
    onAction: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFF17202D)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFFE9B96E), modifier = Modifier.size(42.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(message, color = Color(0xFF8F9CAE), textAlign = TextAlign.Center, fontSize = 12.sp, lineHeight = 18.sp)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onAction,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE9B96E), contentColor = Color(0xFF1A1309)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(actionText, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProfileScreen(
    watchlistCount: Int,
    historyCount: Int,
    onOpenSettings: () -> Unit,
    onOpenOnboarding: () -> Unit
) {
    val context = LocalContext.current
    var userName by remember { mutableStateOf("Movie Enthusiast") }
    var showEditName by remember { mutableStateOf(false) }
    var nameDraft by remember { mutableStateOf(userName) }
    var preferredAudio by remember { mutableStateOf("Hindi Dubbed (Dual Audio)") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Header Card with Transparent Logo
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF1C2738), Color(0xFF101722))))
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "Orbit Logo",
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(14.dp))
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(userName, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { nameDraft = userName; showEditName = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFFE9B96E), modifier = Modifier.size(16.dp))
                    }
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF26354A),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        "PREMIUM VIP ACTIVE",
                        color = Color(0xFFE9B96E),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Stats Row
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard("Watchlist", "$watchlistCount", Modifier.weight(1f))
            StatCard("Watched", "$historyCount", Modifier.weight(1f))
            StatCard("Quality", "1080p Ultra", Modifier.weight(1f))
        }

        Text("Audio & Content Preferences", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121A25)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PreferenceRow("Preferred Audio", preferredAudio)
                Divider(color = Color(0xFF1E2A3A))
                PreferenceRow("Default Quality", "1080p Full HD (Adaptive)")
                Divider(color = Color(0xFF1E2A3A))
                PreferenceRow("Subtitles", "English & Hindi (Auto)")
            }
        }

        Button(
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2B3D), contentColor = Color.White)
        ) {
            Icon(Icons.Default.Tune, contentDescription = null, tint = Color(0xFFE9B96E))
            Spacer(Modifier.width(8.dp))
            Text("Customize Playback & Audio Preferences")
        }

        Button(
            onClick = onOpenOnboarding,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF161F2E), contentColor = Color(0xFFE9B96E))
        ) {
            Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Color(0xFFE9B96E))
            Spacer(Modifier.width(8.dp))
            Text("Revisit Welcome Tour & Features")
        }

        // Cache & About
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121A25)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("App Cache", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("Temporary poster and stream cache: 14.8 MB", color = Color(0xFF8F9CAA), fontSize = 11.sp)
                    }
                    TextButton(onClick = {
                        Toast.makeText(context, "Cache cleared successfully!", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Clear", color = Color(0xFFE9B96E))
                    }
                }
                Divider(color = Color(0xFF1E2A3A))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("App Version", color = Color(0xFF8F9CAA), fontSize = 12.sp)
                    Text("v0.2.1 (Official Update)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showEditName) {
        AlertDialog(
            onDismissRequest = { showEditName = false },
            containerColor = Color(0xFF151D29),
            title = { Text("Edit Profile Name", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = nameDraft,
                    onValueChange = { nameDraft = it },
                    singleLine = true,
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (nameDraft.isNotBlank()) userName = nameDraft.trim()
                    showEditName = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditName = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF121A25))
            .padding(vertical = 12.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = Color(0xFFE9B96E), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            Text(title, color = Color(0xFF8E9BAE), fontSize = 11.sp)
        }
    }
}

@Composable
private fun PreferenceRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color(0xFFC0CCD9), fontSize = 13.sp)
        Text(value, color = Color(0xFFE9B96E), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AppSettingsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var preferredAudio by remember { mutableStateOf("Hindi Dubbed (Dual Audio)") }
    var preferredQuality by remember { mutableStateOf("1080p Ultra HD (Auto)") }
    var autoPlayNext by remember { mutableStateOf(true) }
    var hardwareAccel by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF151D29),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFFE9B96E))
                Spacer(Modifier.width(8.dp))
                Text("App Preferences", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("AUDIO & LANGUAGE PREFERENCE", color = Color(0xFFE9B96E), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        "Hindi Dubbed (Dual Audio) [Preferred]",
                        "Original (English Audio)",
                        "Multi-Audio (All Tracks)"
                    ).forEach { audioOption ->
                        Row(
                            Modifier.fillMaxWidth().clickable { preferredAudio = audioOption },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = preferredAudio == audioOption,
                                onClick = { preferredAudio = audioOption },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFE9B96E))
                            )
                            Text(audioOption, color = Color(0xFFCBD5E1), fontSize = 12.sp)
                        }
                    }
                }

                Divider(color = Color(0xFF263347))

                Text("STREAMING QUALITY", color = Color(0xFFE9B96E), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        "1080p Ultra HD (Auto Recommended)",
                        "720p HD (Balanced)",
                        "480p Data Saver"
                    ).forEach { quality ->
                        Row(
                            Modifier.fillMaxWidth().clickable { preferredQuality = quality },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = preferredQuality == quality,
                                onClick = { preferredQuality = quality },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFE9B96E))
                            )
                            Text(quality, color = Color(0xFFCBD5E1), fontSize = 12.sp)
                        }
                    }
                }

                Divider(color = Color(0xFF263347))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Auto-play Next Episode", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Seamlessly queues upcoming episodes", color = Color(0xFF8F9CAA), fontSize = 11.sp)
                    }
                    Switch(
                        checked = autoPlayNext,
                        onCheckedChange = { autoPlayNext = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFE9B96E), checkedTrackColor = Color(0xFF38465B))
                    )
                }

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Hardware Acceleration", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Smooth 60 FPS cinema rendering", color = Color(0xFF8F9CAA), fontSize = 11.sp)
                    }
                    Switch(
                        checked = hardwareAccel,
                        onCheckedChange = { hardwareAccel = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFE9B96E), checkedTrackColor = Color(0xFF38465B))
                    )
                }

                Divider(color = Color(0xFF263347))

                Button(
                    onClick = {
                        Toast.makeText(context, "Storage and image cache cleared!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF243042), contentColor = Color(0xFFE9B96E))
                ) {
                    Text("Clear Temporary Cache", fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    Toast.makeText(context, "Preferences saved", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE9B96E), contentColor = Color(0xFF17120B))
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun MovieDetailsScreen(
    movie: Movie,
    api: MovDashApi,
    sources: List<SourceInfo>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onPlayStarted: () -> Unit,
    onSelectMovie: (Movie) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var details by remember(movie.id, movie.mediaType) { mutableStateOf<MediaDetails?>(null) }
    var inWatchlist by remember(movie.id) { mutableStateOf(LibraryManager.isInWatchlist(context, movie.id, movie.mediaType)) }
    var isFavorite by remember(movie.id) { mutableStateOf(LibraryManager.isFavorite(context, movie.id, movie.mediaType)) }
    var isDownloaded by remember(movie.id) { mutableStateOf(LibraryManager.isDownloaded(context, movie.id, null)) }
    var showDownloadDialog by remember { mutableStateOf(false) }
    var downloadingEpisode by remember { mutableStateOf<Episode?>(null) }
    var selectedActor by remember { mutableStateOf<CastMember?>(null) }

    var selectedSeasonNumber by remember { mutableStateOf(1) }
    var seasonDetailsData by remember(movie.id, selectedSeasonNumber) {
        mutableStateOf(PreloadedCatalog.getSeasonDetails(movie.id, selectedSeasonNumber))
    }
    var seasonDetailsLoading by remember(movie.id, selectedSeasonNumber) {
        mutableStateOf(seasonDetailsData == null && movie.mediaType == "tv")
    }

    var country by remember(movie.id) { mutableStateOf("IN") }
    var loading by remember { mutableStateOf(true) }

    val isUnreleased = remember(movie.id, details?.status, details?.releaseDate, movie.title) {
        isUnreleasedTitle(details?.releaseDate ?: movie.releaseDate, details?.status, details?.title ?: movie.title)
    }

    LaunchedEffect(movie.id, movie.mediaType) {
        loading = true
        try {
            // If it is a known upcoming title, prevent wrong API id collision or overwrite
            if (!isUnreleasedTitle(movie.releaseDate, null, movie.title)) {
                details = api.details(movie.mediaType, movie.id)
            }
        } catch (_: Exception) {
            // Keep preview info
        }
        loading = false
    }

    // Fetch actual episode details with thumbnails when TV
    LaunchedEffect(movie.id, selectedSeasonNumber, movie.mediaType) {
        if (movie.mediaType == "tv") {
            try {
                val remote = api.seasonDetails(movie.id, selectedSeasonNumber)
                if (remote.episodes.isNotEmpty()) {
                    seasonDetailsData = remote
                }
            } catch (_: Exception) {
                // Keep preloaded or current
            } finally {
                seasonDetailsLoading = false
            }
        }
    }

    fun playTitle(season: Int? = null, episode: Int? = null) {
        onPlayStarted()
        val s = season ?: selectedSeasonNumber
        val e = episode ?: 1
        val streamUrl = StreamServerFactory.getDefaultStreamUrl(movie.id, movie.mediaType, s, e)

        val intent = Intent(context, PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_MEDIA_URL, streamUrl)
            putExtra(PlayerActivity.EXTRA_STREAM_TYPE, "embed")
            putExtra(PlayerActivity.EXTRA_TITLE, movie.title)
            putExtra(
                PlayerActivity.EXTRA_SUBTITLE,
                if (movie.mediaType == "tv") "Season $s · Episode $e" else details?.releaseDate?.take(4) ?: movie.releaseDate?.take(4) ?: "HD Movie"
            )
            putExtra(PlayerActivity.EXTRA_SERVER_NAME, "Fast Stream")
            putExtra(PlayerActivity.EXTRA_TMDB_ID, movie.id)
            putExtra(PlayerActivity.EXTRA_MEDIA_TYPE, movie.mediaType)
            putExtra(PlayerActivity.EXTRA_SEASON, s)
            putExtra(PlayerActivity.EXTRA_EPISODE, e)
        }
        context.startActivity(intent)
    }

    val countries = listOf("IN" to "India", "US" to "United States", "GB" to "United Kingdom", "CA" to "Canada")
    val region = details?.watchProviders?.get(country)
    val providerGroups = listOf(
        "Subscription" to (region?.streaming ?: emptyList()),
        "Free / with ads" to (region?.free.orEmpty() + region?.ads.orEmpty()).distinctBy { it.providerId?.toString() ?: it.providerName },
        "Rent" to (region?.rent ?: emptyList()),
        "Buy" to (region?.buy ?: emptyList())
    )
    val hasProviders = providerGroups.any { it.second.isNotEmpty() }
    val defaultJustWatchSearch = "https://www.justwatch.com/in/search?q=" + Uri.encode(details?.title ?: movie.title)
    val availabilityUrl = region?.link?.takeIf { it.startsWith("https://") } ?: defaultJustWatchSearch

    val backdrop = details?.backdropPath?.let { "https://image.tmdb.org/t/p/w1280$it" }
        ?: movie.backdropPath?.let { "https://image.tmdb.org/t/p/w1280$it" }
        ?: details?.posterPath?.let { "https://image.tmdb.org/t/p/w780$it" }
        ?: movie.posterPath?.let { "https://image.tmdb.org/t/p/w780$it" }

    Column(
        modifier
            .fillMaxSize()
            .background(Color(0xFF090B10))
            .verticalScroll(rememberScrollState())
    ) {
        // Hero Header
        Box(
            Modifier
                .fillMaxWidth()
                .height(340.dp)
                .background(Brush.linearGradient(listOf(Color(0xFF243248), Color(0xFF101722))))
        ) {
            if (backdrop != null) {
                AsyncImage(
                    model = backdrop,
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        listOf(Color(0x77070A10), Color(0x33070A10), Color(0xF8090B10))
                    )
                )
            )

            // Back button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp, top = 12.dp)
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xCC111923))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            // Title & Meta
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFE9B96E)
                ) {
                    Text(
                        if (isUnreleased) "RELEASING SOON" else (if (movie.mediaType == "tv") "SERIES" else "MOVIE"),
                        color = Color(0xFF1B1309),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    details?.title ?: movie.title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        details?.releaseDate ?: movie.releaseDate ?: "Coming Soon",
                        color = Color(0xFFE2E7EE),
                        style = MaterialTheme.typography.labelMedium
                    )
                    (details?.voteAverage ?: movie.voteAverage)?.let {
                        Text(
                            "★ " + "%.1f".format(it),
                            color = Color(0xFFE9B96E),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Action Buttons Row (WATCH NOW or COMING SOON + DOWNLOAD + WATCHLIST + FAVORITE)
        if (isUnreleased) {
            // UNRELEASED / UPCOMING CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF182230))
            ) {
                Column(
                    Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.HourglassTop, contentDescription = null, tint = Color(0xFFE9B96E), modifier = Modifier.size(22.dp))
                        Text(
                            "AWAITING THEATRICAL / DIGITAL RELEASE",
                            color = Color(0xFFE9B96E),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        "Official Release Date: " + (details?.releaseDate ?: movie.releaseDate ?: "Coming Soon"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "This title is currently in production or awaiting release. Playback will be unlocked automatically on its official release date.",
                        color = Color(0xFF9AA8BA),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = {
                            inWatchlist = LibraryManager.toggleWatchlist(context, movie)
                            Toast.makeText(
                                context,
                                if (inWatchlist) "Added to Reminders & Watchlist!" else "Removed from Watchlist",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (inWatchlist) Color(0xFF26354B) else Color(0xFFE9B96E),
                            contentColor = if (inWatchlist) Color(0xFFE9B96E) else Color(0xFF17120B)
                        )
                    ) {
                        Icon(if (inWatchlist) Icons.Default.Check else Icons.Default.NotificationsActive, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (inWatchlist) "REMINDER SET (IN WATCHLIST)" else "REMIND ME ON RELEASE", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // RELEASED TITLE: Clean Primary "WATCH NOW" + "DOWNLOAD" + "WATCHLIST" + "FAVORITE" Row
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { playTitle() },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE9B96E),
                        contentColor = Color(0xFF17120B)
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("WATCH NOW", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                }

                // Download Button with Offline Vault Status
                IconButton(
                    onClick = {
                        if (isDownloaded) {
                            Toast.makeText(context, "Already saved in Offline Vault!", Toast.LENGTH_SHORT).show()
                        } else {
                            showDownloadDialog = true
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isDownloaded) Color(0xFF1E3A24) else Color(0xFF16202E))
                ) {
                    Icon(
                        if (isDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                        contentDescription = "Download Offline",
                        tint = if (isDownloaded) Color(0xFF4CAF50) else Color(0xFFE9B96E)
                    )
                }

                IconButton(
                    onClick = {
                        inWatchlist = LibraryManager.toggleWatchlist(context, movie)
                        Toast.makeText(
                            context,
                            if (inWatchlist) "Added to Watchlist" else "Removed from Watchlist",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF16202E))
                ) {
                    Icon(
                        if (inWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Watchlist",
                        tint = if (inWatchlist) Color(0xFFE9B96E) else Color.White
                    )
                }

                IconButton(
                    onClick = {
                        isFavorite = LibraryManager.toggleFavorite(context, movie)
                        Toast.makeText(
                            context,
                            if (isFavorite) "Added to Favorites" else "Removed from Favorites",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF16202E))
                ) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFFF5252) else Color.White
                    )
                }
            }
        }

        // Details Content
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color(0xFFE9B96E))

            details?.tagline?.takeIf { it.isNotBlank() }?.let {
                Text(it, color = Color(0xFFCBD5E1), style = MaterialTheme.typography.titleSmall)
            }

            val genreText = details?.genres.orEmpty().joinToString("  ·  ") { it.name }
            if (genreText.isNotBlank()) {
                Text(genreText, color = Color(0xFFE9B96E), style = MaterialTheme.typography.labelMedium)
            }

            Text("Synopsis", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                details?.overview?.ifBlank { movie.overview } ?: movie.overview.ifBlank { "No synopsis available." },
                color = Color(0xFFB7C0CD),
                style = MaterialTheme.typography.bodyMedium
            )

            // Available Audio & Media Details Card (Clean Consumer Look)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121A25))
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("AUDIO & MEDIA SPECIFICATIONS", color = Color(0xFFE9B96E), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Available Audio", color = Color(0xFFB4C0CE), fontSize = 12.sp)
                        Text("Hindi Dubbed · English · Multi", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Divider(color = Color(0xFF1E2837))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Quality & Bitrate", color = Color(0xFFB4C0CE), fontSize = 12.sp)
                        Text("1080p Ultra HD · 60 FPS HDR", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Divider(color = Color(0xFF1E2837))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Subtitles", color = Color(0xFFB4C0CE), fontSize = 12.sp)
                        Text("English [CC] · Hindi Subtitles", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // TOP CAST & STARRING ACTORS ROW
            val castList = remember(movie.id) { UniverseData.getCastForMovie(movie.id) }
            if (castList.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Top Cast & Starring Actors",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${castList.size} Stars",
                        color = Color(0xFFE9B96E),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    "Tap any actor to explore full filmography & other titles",
                    color = Color(0xFF8E9BAE),
                    fontSize = 11.sp
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(castList) { actor ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(82.dp)
                                .clickable { selectedActor = actor }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color(0xFFE9B96E), CircleShape)
                                    .background(Color(0xFF1E2837)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!actor.profilePath.isNullOrBlank()) {
                                    AsyncImage(
                                        model = "https://image.tmdb.org/t/p/w185${actor.profilePath}",
                                        contentDescription = actor.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        actor.name.take(1),
                                        color = Color(0xFFE9B96E),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                actor.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                actor.character,
                                color = Color(0xFF8E9BAE),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // TV Series Seasons/Episodes with Visual Thumbnails & Offline Download Buttons
            if (movie.mediaType == "tv") {
                Spacer(Modifier.height(6.dp))
                Text("Seasons & Episodes", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                val totalSeasons = details?.numberOfSeasons ?: 1
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (s in 1..totalSeasons.coerceAtLeast(1)) {
                        FilterChip(
                            selected = selectedSeasonNumber == s,
                            onClick = { selectedSeasonNumber = s },
                            label = { Text("Season $s") }
                        )
                    }
                }

                // Episode List with Thumbnails & Real Episode Names
                if (seasonDetailsLoading && seasonDetailsData?.episodes.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFFE50914),
                                strokeWidth = 2.dp
                            )
                            Text(
                                "Loading episode titles & descriptions...",
                                color = Color(0xFF8E9BAE),
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    val episodesList = seasonDetailsData?.episodes.orEmpty()
                    if (episodesList.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            episodesList.forEach { episode ->
                                val epDownloaded = LibraryManager.isDownloaded(context, movie.id, episode.episodeNumber)
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF121A25))
                                        .clickable {
                                            if (!isUnreleased) playTitle(selectedSeasonNumber, episode.episodeNumber)
                                        }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Episode Thumbnail with play badge
                                    Box(
                                        modifier = Modifier
                                            .size(width = 110.dp, height = 70.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF1E2837))
                                    ) {
                                        val thumbPath = episode.stillPath ?: details?.backdropPath ?: movie.backdropPath
                                        if (thumbPath != null) {
                                            AsyncImage(
                                                model = "https://image.tmdb.org/t/p/w300$thumbPath",
                                                contentDescription = episode.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        Box(
                                            Modifier
                                                .fillMaxSize()
                                                .background(Color(0x44000000)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.PlayArrow,
                                                contentDescription = "Play",
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xDD090B10),
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(4.dp)
                                        ) {
                                            Text(
                                                "EP ${episode.episodeNumber}",
                                                color = Color(0xFFE9B96E),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }

                                    Spacer(Modifier.width(12.dp))

                                    Column(Modifier.weight(1f)) {
                                        val epTitle = if (episode.name.isNotBlank() && !episode.name.equals("Episode ${episode.episodeNumber}", ignoreCase = true)) {
                                            "E${episode.episodeNumber}: ${episode.name}"
                                        } else {
                                            "Episode ${episode.episodeNumber}"
                                        }
                                        Text(
                                            epTitle,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                episode.runtime?.let { "$it min" } ?: "Full HD",
                                                color = Color(0xFFE9B96E),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            if (!episode.airDate.isNullOrBlank()) {
                                                Text("•", color = Color(0xFF4A5568), fontSize = 10.sp)
                                                Text(
                                                    episode.airDate,
                                                    color = Color(0xFF8E9BAE),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                        if (episode.overview.isNotBlank()) {
                                            Text(
                                                episode.overview,
                                                color = Color(0xFF8E9BAE),
                                                fontSize = 11.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    // Episode Download Button
                                    IconButton(
                                        onClick = {
                                            if (epDownloaded) {
                                                Toast.makeText(context, "Episode already saved offline!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                downloadingEpisode = episode
                                            }
                                        },
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Icon(
                                            if (epDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                                            contentDescription = "Download Episode",
                                            tint = if (epDownloaded) Color(0xFF4CAF50) else Color(0xFFE9B96E),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else if (isUnreleased) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF131A26),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = "Upcoming",
                                    tint = Color(0xFFE9B96E),
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        "Episodes will be listed upon official premiere.",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        "Add to Watchlist to get notified when Season $selectedSeasonNumber airs.",
                                        color = Color(0xFF8E9BAE),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Official Availability / Where to Watch (JustWatch) - ALWAYS WORKING
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF263244))
            Text("Official Availability (JustWatch)", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                countries.forEach { item ->
                    FilterChip(
                        selected = country == item.first,
                        onClick = { country = item.first },
                        label = { Text(item.second) }
                    )
                }
            }

            if (hasProviders) {
                providerGroups.forEach { (category, providers) ->
                    if (providers.isNotEmpty()) {
                        Text(category, color = Color(0xFFE9B96E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        providers.forEach { provider ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF121A25))
                                    .clickable {
                                        runCatching {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(availabilityUrl)))
                                        }
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!provider.logoPath.isNullOrBlank()) {
                                    AsyncImage(
                                        model = "https://image.tmdb.org/t/p/w92" + provider.logoPath,
                                        contentDescription = provider.providerName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                                    )
                                } else {
                                    Box(
                                        Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF243248)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(provider.providerName.take(1), color = Color(0xFFE9B96E), fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(provider.providerName, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text(category, color = Color(0xFF8996A8), fontSize = 11.sp)
                                }
                                Text("OPEN", color = Color(0xFFE9B96E), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(availabilityUrl)))
                    }
                },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2837), contentColor = Color(0xFFE9B96E))
            ) {
                Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Check Official Watch Options on JustWatch", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(Modifier.height(30.dp))
        }
    }

    // DOWNLOAD DIALOG FOR MOVIE
    if (showDownloadDialog) {
        DownloadDialog(
            title = movie.title,
            mediaType = movie.mediaType,
            posterPath = movie.posterPath,
            onDismiss = { showDownloadDialog = false },
            onStartDownload = { audio, quality, subs, sizeMb ->
                LibraryManager.addDownload(
                    context,
                    DownloadedItem(
                        id = movie.id,
                        title = movie.title,
                        mediaType = movie.mediaType,
                        posterPath = movie.posterPath,
                        backdropPath = movie.backdropPath,
                        releaseDate = movie.releaseDate,
                        audioTrack = audio,
                        quality = quality,
                        subtitles = subs,
                        fileSizeMb = sizeMb,
                        seasonNumber = null,
                        episodeNumber = null,
                        episodeTitle = null
                    )
                )
                isDownloaded = true
                showDownloadDialog = false
                Toast.makeText(context, "Downloading in background! Added to Offline Library Vault.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // DOWNLOAD DIALOG FOR EPISODE
    if (downloadingEpisode != null) {
        val ep = downloadingEpisode!!
        DownloadDialog(
            title = "${movie.title} • S$selectedSeasonNumber E${ep.episodeNumber}",
            mediaType = "tv",
            posterPath = ep.stillPath ?: movie.posterPath,
            onDismiss = { downloadingEpisode = null },
            onStartDownload = { audio, quality, subs, sizeMb ->
                LibraryManager.addDownload(
                    context,
                    DownloadedItem(
                        id = movie.id,
                        title = movie.title,
                        mediaType = "tv",
                        posterPath = ep.stillPath ?: movie.posterPath,
                        backdropPath = movie.backdropPath,
                        releaseDate = ep.airDate ?: movie.releaseDate,
                        audioTrack = audio,
                        quality = quality,
                        subtitles = subs,
                        fileSizeMb = sizeMb / 3,
                        seasonNumber = selectedSeasonNumber,
                        episodeNumber = ep.episodeNumber,
                        episodeTitle = "S$selectedSeasonNumber E${ep.episodeNumber}: ${ep.name}"
                    )
                )
                downloadingEpisode = null
                Toast.makeText(context, "Episode downloading! Saved to Offline Library.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ACTOR FILMOGRAPHY DIALOG
    if (selectedActor != null) {
        ActorFilmographyDialog(
            actor = selectedActor!!,
            onDismiss = { selectedActor = null },
            onSelectMovie = { filmMovie ->
                selectedActor = null
                onSelectMovie(filmMovie)
            }
        )
    }
}

@Composable
private fun DownloadDialog(
    title: String,
    mediaType: String,
    posterPath: String?,
    onDismiss: () -> Unit,
    onStartDownload: (audio: String, quality: String, subs: String, sizeMb: Int) -> Unit
) {
    val audioOptions = listOf("Hindi Dubbed (Clean 5.1)", "Original English (Dolby Atmos)", "Tamil Dubbed", "Telugu Dubbed")
    val qualityOptions = listOf(
        Triple("1080p Ultra HD", "1080p", 1850),
        Triple("720p HD", "720p", 950),
        Triple("480p Data Saver", "480p", 420)
    )
    val subtitleOptions = listOf("English [CC]", "Hindi Subtitles", "None")

    var selectedAudio by remember { mutableStateOf(audioOptions[0]) }
    var selectedQuality by remember { mutableStateOf(qualityOptions[0]) }
    var selectedSubs by remember { mutableStateOf(subtitleOptions[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF101722),
        titleContentColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE9B96E)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF17120B))
                }
                Column {
                    Text("Download Offline", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Color.White)
                    Text(title, fontSize = 12.sp, color = Color(0xFFE9B96E), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        },
        text = {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Audio Selection
                Text("Select Audio Track", color = Color(0xFFB4C0CE), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    audioOptions.forEach { audio ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedAudio == audio) Color(0xFF24354D) else Color(0xFF161F2C),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedAudio = audio }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(audio, color = if (selectedAudio == audio) Color(0xFFE9B96E) else Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                if (selectedAudio == audio) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFFE9B96E), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // Quality Selection
                Text("Select Download Quality", color = Color(0xFFB4C0CE), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    qualityOptions.forEach { (label, q, size) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedQuality.second == q) Color(0xFF24354D) else Color(0xFF161F2C),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedQuality = Triple(label, q, size) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(label, color = if (selectedQuality.second == q) Color(0xFFE9B96E) else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Est. ~${size} MB • Offline Playback", color = Color(0xFF8E9BAE), fontSize = 10.sp)
                                }
                                if (selectedQuality.second == q) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFFE9B96E), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // Subtitle Selection
                Text("Select Subtitles", color = Color(0xFFB4C0CE), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subtitleOptions.forEach { sub ->
                        FilterChip(
                            selected = selectedSubs == sub,
                            onClick = { selectedSubs = sub },
                            label = { Text(sub, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onStartDownload(selectedAudio, selectedQuality.first, selectedSubs, selectedQuality.third)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE9B96E), contentColor = Color(0xFF17120B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Start Download (${selectedQuality.third} MB)", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF8E9BAE))
            }
        }
    )
}

@Composable
private fun ActorFilmographyDialog(
    actor: CastMember,
    onDismiss: () -> Unit,
    onSelectMovie: (Movie) -> Unit
) {
    val actorTitles = remember(actor.name) { UniverseData.getMoviesForActor(actor) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF0F1520),
            modifier = Modifier.fillMaxWidth().heightIn(max = 580.dp)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Actor Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFFE9B96E), CircleShape)
                            .background(Color(0xFF1E2837)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!actor.profilePath.isNullOrBlank()) {
                            AsyncImage(
                                model = "https://image.tmdb.org/t/p/w185${actor.profilePath}",
                                contentDescription = actor.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(actor.name.take(1), color = Color(0xFFE9B96E), fontWeight = FontWeight.Bold, fontSize = 28.sp)
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            actor.name,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                        Text(
                            "Starring as ${actor.character}",
                            color = Color(0xFFE9B96E),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${actorTitles.size} Titles Available in Orbit",
                            color = Color(0xFF8E9BAE),
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFF1E2837))
                Spacer(Modifier.height(10.dp))

                Text(
                    "FILMOGRAPHY IN ORBIT",
                    color = Color(0xFFE9B96E),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(Modifier.height(8.dp))

                // Scrollable List of Titles Starring this Actor
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(actorTitles) { titleMovie ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF151E2B))
                                .clickable { onSelectMovie(titleMovie) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 54.dp, height = 76.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E2837))
                            ) {
                                val poster = titleMovie.posterPath?.let { "https://image.tmdb.org/t/p/w185$it" }
                                if (poster != null) {
                                    AsyncImage(
                                        model = poster,
                                        contentDescription = titleMovie.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(Modifier.weight(1f)) {
                                Text(
                                    titleMovie.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        titleMovie.releaseDate?.take(4) ?: "Movie",
                                        color = Color(0xFF8E9BAE),
                                        fontSize = 11.sp
                                    )
                                    Text("•", color = Color(0xFF4A5568), fontSize = 10.sp)
                                    Text(
                                        titleMovie.mediaType.uppercase(),
                                        color = Color(0xFFE9B96E),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    titleMovie.voteAverage?.let {
                                        Text("•", color = Color(0xFF4A5568), fontSize = 10.sp)
                                        Text("★ %.1f".format(it), color = Color(0xFFE9B96E), fontSize = 11.sp)
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    titleMovie.overview,
                                    color = Color(0xFF8E9BAE),
                                    fontSize = 10.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(Modifier.width(8.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF243248)
                            ) {
                                Text(
                                    "EXPLORE",
                                    color = Color(0xFFE9B96E),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
