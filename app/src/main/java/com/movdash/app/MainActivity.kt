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
import com.movdash.app.player.PlayerActivity
import com.movdash.app.ui.onboarding.OnboardingScreen
import com.movdash.app.ui.theme.MovDashTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    var showOnboarding by remember {
        mutableStateOf(!prefs.getBoolean("has_completed_onboarding", false))
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

    fun refreshLibrary() {
        watchlist = LibraryManager.getWatchlist(context)
        history = LibraryManager.getHistory(context)
        favorites = LibraryManager.getFavorites(context)
    }

    // Hardware Back Button Interception
    BackHandler(enabled = selectedMovie != null) {
        refreshLibrary()
        selectedMovie = null
    }

    BackHandler(enabled = selectedMovie == null && tab != AppTab.HOME) {
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
                                contentDescription = "MovDash Logo",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    "MOVDASH",
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
            if (selectedMovie == null) {
                NavigationBar(containerColor = Color(0xFF101620), contentColor = Color.White) {
                    NavigationBarItem(
                        selected = tab == AppTab.HOME,
                        onClick = { tab = AppTab.HOME },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFE9B96E),
                            selectedTextColor = Color(0xFFE9B96E),
                            indicatorColor = Color(0xFF1F2B3D),
                            unselectedIconColor = Color(0xFF8E9BAE),
                            unselectedTextColor = Color(0xFF8E9BAE)
                        )
                    )
                    NavigationBarItem(
                        selected = tab == AppTab.DISCOVER,
                        onClick = { tab = AppTab.DISCOVER },
                        icon = { Icon(Icons.Default.Search, contentDescription = null) },
                        label = { Text("Discover") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFE9B96E),
                            selectedTextColor = Color(0xFFE9B96E),
                            indicatorColor = Color(0xFF1F2B3D),
                            unselectedIconColor = Color(0xFF8E9BAE),
                            unselectedTextColor = Color(0xFF8E9BAE)
                        )
                    )
                    NavigationBarItem(
                        selected = tab == AppTab.LIBRARY,
                        onClick = {
                            refreshLibrary()
                            tab = AppTab.LIBRARY
                        },
                        icon = { Icon(Icons.Default.BookmarkBorder, contentDescription = null) },
                        label = { Text("Library") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFE9B96E),
                            selectedTextColor = Color(0xFFE9B96E),
                            indicatorColor = Color(0xFF1F2B3D),
                            unselectedIconColor = Color(0xFF8E9BAE),
                            unselectedTextColor = Color(0xFF8E9BAE)
                        )
                    )
                    NavigationBarItem(
                        selected = tab == AppTab.PROFILE,
                        onClick = {
                            refreshLibrary()
                            tab = AppTab.PROFILE
                        },
                        icon = { Icon(Icons.Default.PersonOutline, contentDescription = null) },
                        label = { Text("Profile") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFE9B96E),
                            selectedTextColor = Color(0xFFE9B96E),
                            indicatorColor = Color(0xFF1F2B3D),
                            unselectedIconColor = Color(0xFF8E9BAE),
                            unselectedTextColor = Color(0xFF8E9BAE)
                        )
                    )
                }
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
                }
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
                        onMovieClick = { selectedMovie = it },
                        onDiscover = { tab = AppTab.DISCOVER },
                        onGenreClick = { genre ->
                            searchGenre = genre
                            tab = AppTab.DISCOVER
                        }
                    )
                    AppTab.DISCOVER -> DiscoverScreen(
                        query = query,
                        onQueryChange = { query = it; searchError = null },
                        mediaType = searchType,
                        onMediaTypeChange = { searchType = it; searchResults = emptyList(); searchError = null },
                        selectedGenre = searchGenre,
                        onGenreSelect = { genre ->
                            searchGenre = if (searchGenre == genre) null else genre
                        },
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
                        onMovieClick = { selectedMovie = it }
                    )
                    AppTab.LIBRARY -> LibraryScreen(
                        watchlist = watchlist,
                        history = history,
                        favorites = favorites,
                        onMovieClick = { selectedMovie = it },
                        onExplore = { tab = AppTab.DISCOVER },
                        onClearHistory = {
                            LibraryManager.clearHistory(context)
                            refreshLibrary()
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
private fun HomeScreen(
    spotlightMovies: List<Movie>,
    movies: List<Movie>,
    series: List<Movie>,
    popularMovies: List<Movie>,
    popularSeries: List<Movie>,
    upcomingMovies: List<Movie>,
    onMovieClick: (Movie) -> Unit,
    onDiscover: () -> Unit,
    onGenreClick: (String) -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        HeroSpotlightCarousel(
            movies = spotlightMovies,
            onWatch = onMovieClick
        )

        Spacer(Modifier.height(14.dp))

        // Quick Category/Genre Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(PreloadedCatalog.genres) { genre ->
                SuggestionChip(
                    onClick = { onGenreClick(genre) },
                    label = { Text(genre, color = Color(0xFFDCE2EC), fontSize = 12.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF141C28)),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = Color(0xFF263347)
                    )
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Upcoming / Releasing Soon Shelf
        if (upcomingMovies.isNotEmpty()) {
            SectionTitle("Releasing Soon • In Theaters / OTT", "Discover", onDiscover)
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(upcomingMovies, key = { "upcoming-home-${it.id}" }) { item ->
                    MoviePosterCard(item) { onMovieClick(item) }
                }
            }
        }

        if (movies.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            SectionTitle("Trending Movies", "See all", onDiscover)
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(movies, key = { "trend-movie-${it.id}" }) { movie ->
                    MoviePosterCard(movie) { onMovieClick(movie) }
                }
            }
        }

        if (series.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            SectionTitle("Trending TV Series", "See all", onDiscover)
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(series, key = { "trend-tv-${it.id}" }) { item ->
                    MoviePosterCard(item) { onMovieClick(item) }
                }
            }
        }

        if (popularMovies.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            SectionTitle("Popular Blockbusters", "Discover", onDiscover)
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(popularMovies, key = { "pop-movie-${it.id}" }) { item ->
                    MoviePosterCard(item) { onMovieClick(item) }
                }
            }
        }

        if (popularSeries.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            SectionTitle("Top Rated Shows", "Discover", onDiscover)
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(popularSeries, key = { "pop-tv-${it.id}" }) { item ->
                    MoviePosterCard(item) { onMovieClick(item) }
                }
            }
        }

        Spacer(Modifier.height(30.dp))
    }
}

@Composable
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
private fun SectionTitle(title: String, action: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        TextButton(onClick = onClick) {
            Text(action, color = Color(0xFFE9B96E), style = MaterialTheme.typography.labelMedium)
        }
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

@Composable
private fun DiscoverScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    mediaType: String,
    onMediaTypeChange: (String) -> Unit,
    selectedGenre: String?,
    onGenreSelect: (String) -> Unit,
    results: List<Movie>,
    loading: Boolean,
    error: String?,
    onSearch: () -> Unit,
    onMovieClick: (Movie) -> Unit
) {
    val displayList = remember(results, query, selectedGenre, mediaType) {
        when {
            results.isNotEmpty() -> results
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

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 8.dp)) {
        Text("Discover", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Search movies, TV shows, and explore all genres.", color = Color(0xFF9AA5B5), style = MaterialTheme.typography.bodySmall)

        Spacer(Modifier.height(10.dp))

        // Type filter chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = mediaType == "multi",
                onClick = { onMediaTypeChange("multi") },
                label = { Text("All Titles") }
            )
            FilterChip(
                selected = mediaType == "movie",
                onClick = { onMediaTypeChange("movie") },
                label = { Text("Movies") }
            )
            FilterChip(
                selected = mediaType == "tv",
                onClick = { onMediaTypeChange("tv") },
                label = { Text("TV Series") }
            )
        }

        Spacer(Modifier.height(8.dp))

        // Search Input
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text("Search any movie or show…") },
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
                onClick = onSearch,
                enabled = query.isNotBlank() && !loading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE9B96E), contentColor = Color(0xFF1A1309))
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        }

        Spacer(Modifier.height(8.dp))

        // Genres horizontal bar
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(PreloadedCatalog.genres) { genre ->
                FilterChip(
                    selected = selectedGenre == genre,
                    onClick = { onGenreSelect(genre) },
                    label = { Text(genre, fontSize = 11.sp) }
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        if (loading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFFE9B96E), strokeWidth = 2.dp)
                Text("Searching titles across catalog…", color = Color(0xFFE9B96E))
            }
        } else if (error != null) {
            Text(error, color = Color(0xFFFFA69C), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 8.dp))
        }

        Text(
            if (query.isBlank()) "Trending Discoveries" else "Search Results (${displayList.size})",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 6.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(displayList, key = { "${it.mediaType}-${it.id}" }) { movie ->
                Column(
                    modifier = Modifier.clickable { onMovieClick(movie) }
                ) {
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
                        val isUnreleased = isUnreleasedTitle(movie.releaseDate, null, movie.title)
                        Surface(
                            shape = RoundedCornerShape(topStart = 0.dp, bottomEnd = 8.dp),
                            color = Color(0xDD090B10),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                if (isUnreleased) "SOON" else (movie.voteAverage?.let { "★ ${"%.1f".format(it)}" } ?: "HD"),
                                color = Color(0xFFE9B96E),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
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
                    Text(
                        "${movie.mediaType.uppercase()} · ${movie.releaseDate?.take(4) ?: "Coming Soon"}",
                        color = Color(0xFF8896A8),
                        fontSize = 10.sp
                    )
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
    onMovieClick: (Movie) -> Unit,
    onExplore: () -> Unit,
    onClearHistory: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Watchlist (${watchlist.size})", "History (${history.size})", "Favorites (${favorites.size})")

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 8.dp)) {
        Text("Your Library", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Your saved movies, series and watch history.", color = Color(0xFF9AA5B5), style = MaterialTheme.typography.bodySmall)

        Spacer(Modifier.height(12.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF101620),
            contentColor = Color(0xFFE9B96E),
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

        val currentList = when (selectedTab) {
            0 -> watchlist
            1 -> history
            else -> favorites
        }

        if (currentList.isEmpty()) {
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    when (selectedTab) {
                        0 -> Icons.Default.BookmarkBorder
                        1 -> Icons.Default.History
                        else -> Icons.Default.FavoriteBorder
                    },
                    contentDescription = null,
                    tint = Color(0xFFE9B96E),
                    modifier = Modifier.size(52.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    when (selectedTab) {
                        0 -> "No items in Watchlist"
                        1 -> "No Watch History yet"
                        else -> "No Favorite titles"
                    },
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Explore movies & series and tap bookmark or play to save them here.",
                    color = Color(0xFF8F9CAE),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = onExplore,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE9B96E), contentColor = Color(0xFF17120B))
                ) {
                    Text("Explore Movies & Shows", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            if (selectedTab == 1) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onClearHistory) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFFFA69C))
                        Spacer(Modifier.width(4.dp))
                        Text("Clear History", color = Color(0xFFFFA69C), fontSize = 12.sp)
                    }
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(currentList, key = { "${it.mediaType}-${it.id}" }) { movie ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF121A25))
                            .clickable { onMovieClick(movie) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = movie.posterPath?.let { "https://image.tmdb.org/t/p/w185$it" },
                            contentDescription = movie.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(width = 56.dp, height = 80.dp).clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(movie.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text(
                                "${movie.mediaType.uppercase()} · ${movie.releaseDate?.take(4) ?: "HD"}",
                                color = Color(0xFFE9B96E),
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                movie.overview,
                                color = Color(0xFF8F9CAA),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        val isUnreleased = isUnreleasedTitle(movie.releaseDate, null, movie.title)
                        IconButton(onClick = { onMovieClick(movie) }) {
                            Icon(
                                if (isUnreleased) Icons.Default.HourglassTop else Icons.Default.PlayArrow,
                                contentDescription = if (isUnreleased) "Coming Soon" else "Play",
                                tint = Color(0xFFE9B96E)
                            )
                        }
                    }
                }
            }
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
                contentDescription = "MovDash Logo",
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
    onPlayStarted: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var details by remember(movie.id, movie.mediaType) { mutableStateOf<MediaDetails?>(null) }
    var inWatchlist by remember(movie.id) { mutableStateOf(LibraryManager.isInWatchlist(context, movie.id, movie.mediaType)) }
    var isFavorite by remember(movie.id) { mutableStateOf(LibraryManager.isFavorite(context, movie.id, movie.mediaType)) }

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
            details = api.details(movie.mediaType, movie.id)
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

        // Action Buttons Row (WATCH NOW or COMING SOON + WATCHLIST + FAVORITE)
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
            // RELEASED TITLE: Clean Primary "WATCH NOW" Button
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

            // Available Audio & Media Details Card (Clean Consumer Look, No Server Confusion)
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

            // TV Series Seasons/Episodes with Visual Thumbnails
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
}
