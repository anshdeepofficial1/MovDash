package com.movdash.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.movdash.app.data.ApiFactory
import com.movdash.app.data.Movie
import com.movdash.app.data.MediaDetails
import com.movdash.app.data.JellyfinItem
import com.movdash.app.data.SourceInfo
import com.movdash.app.data.WatchProvider
import com.movdash.app.player.PlayerActivity
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

private val demoMovies = listOf(
    Movie(1, "Dune: Part Two", "A mythic journey across a desert world.", "/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg", "/xOMo8BRK7PfcJv9JCnx7s5hj0PX.jpg", "2024-03-01", 8.2, "preview"),
    Movie(2, "Interstellar", "A journey beyond the limits of our world.", "/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg", "/rAiYTfKGqDCRIIqo664sY9XZIvQ.jpg", "2014-11-07", 8.7, "preview"),
    Movie(3, "Oppenheimer", "The story behind a world-changing moment.", "/ptpr0kGAckfQkJeJIt8st5dglvd.jpg", "/fm6KqXpk3M2HVveHwCrBSSBaO0V.jpg", "2023-07-21", 8.1, "preview"),
    Movie(4, "The Batman", "A detective story set in Gotham City.", "/74xTEgt7R36Fpooo50r9T25onhq.jpg", "/b0PlSFdDwbyK0cf5RxwDpaOJQvQ.jpg", "2022-03-04", 7.7, "preview")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovDashApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("movdash", Context.MODE_PRIVATE) }
    var apiBase by remember {
        mutableStateOf(prefs.getString("api_base", "https://movdash-production.up.railway.app/") ?: "https://movdash-production.up.railway.app/")
    }
    var apiDraft by remember { mutableStateOf(apiBase) }
    val api = remember(apiBase) { ApiFactory.create(apiBase) }
    val scope = rememberCoroutineScope()

    var tab by remember { mutableStateOf(AppTab.HOME) }
    var movies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var series by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var popularMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var popularSeries by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var searchResults by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var sources by remember { mutableStateOf<List<SourceInfo>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var searchType by remember { mutableStateOf("multi") }
    var searchError by remember { mutableStateOf<String?>(null) }
    var selectedMovie by remember { mutableStateOf<Movie?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var backendStatus by remember { mutableStateOf("Connect a MovDash backend to load the catalogue") }
    var isConnected by remember { mutableStateOf(false) }

    val refreshCatalog: suspend () -> Unit = {
        loading = true
        try {
            val health = api.health()
            sources = api.sources()
            val home = api.home()
            movies = home.trendingMovies
            series = home.trendingSeries
            popularMovies = home.popularMovies
            popularSeries = home.popularSeries
            backendStatus = (health.app ?: "MovDash API") + " · catalogue loaded"
            isConnected = true
        } catch (error: Exception) {
            backendStatus = error.message?.substringAfter(" ")?.take(160)
                ?: error.localizedMessage?.take(160)
                ?: "Backend request failed"
            isConnected = false
            movies = emptyList()
            series = emptyList()
            popularMovies = emptyList()
            popularSeries = emptyList()
        } finally {
            loading = false
        }
    }

    LaunchedEffect(apiBase) { refreshCatalog() }

    Scaffold(
        containerColor = Color(0xFF090B10),
        topBar = {
            if (selectedMovie == null) {
                TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(44.dp).clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFFF1C47C), Color(0xFF9D6D32)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("M", color = Color(0xFF1B1309), fontWeight = FontWeight.Black, fontSize = 21.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Welcome back", style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFE9B96E), fontWeight = FontWeight.Medium)
                            Text("MovDash", style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { tab = AppTab.DISCOVER }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }
                    IconButton(onClick = { apiDraft = apiBase; showSettings = true }) {
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
                NavigationBarItem(selected = tab == AppTab.HOME, onClick = { tab = AppTab.HOME },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) }, label = { Text("Home") })
                NavigationBarItem(selected = tab == AppTab.DISCOVER, onClick = { tab = AppTab.DISCOVER },
                    icon = { Icon(Icons.Default.Search, contentDescription = null) }, label = { Text("Discover") })
                NavigationBarItem(selected = tab == AppTab.LIBRARY, onClick = { tab = AppTab.LIBRARY },
                    icon = { Icon(Icons.Default.BookmarkBorder, contentDescription = null) }, label = { Text("Library") })
                NavigationBarItem(selected = tab == AppTab.PROFILE, onClick = { tab = AppTab.PROFILE },
                    icon = { Icon(Icons.Default.PersonOutline, contentDescription = null) }, label = { Text("Profile") })
            }
        }
    ) { padding ->
        if (selectedMovie != null) {
            MovieDetailsScreen(
                movie = selectedMovie!!,
                api = api,
                sources = sources,
                modifier = Modifier.fillMaxSize().padding(padding),
                onBack = { selectedMovie = null }
            )
        } else {
            Box(Modifier.fillMaxSize().padding(padding).background(Color(0xFF090B10))) {
                when (tab) {
                AppTab.HOME -> HomeScreen(
                    movies = movies,
                    series = series,
                    popularMovies = popularMovies,
                    popularSeries = popularSeries,
                    sources = sources,
                    connected = isConnected,
                    status = backendStatus,
                    loading = loading,
                    onMovieClick = { selectedMovie = it },
                    onRefresh = { scope.launch { refreshCatalog() } },
                    onDiscover = { tab = AppTab.DISCOVER }
                )
                AppTab.DISCOVER -> DiscoverScreen(
                    query = query,
                    onQueryChange = { query = it; searchError = null },
                    mediaType = searchType,
                    onMediaTypeChange = { searchType = it; searchResults = emptyList(); searchError = null },
                    results = searchResults,
                    loading = loading,
                    connected = isConnected,
                    error = searchError,
                    onSearch = {
                        if (query.isNotBlank()) scope.launch {
                            loading = true
                            searchError = null
                            try {
                                searchResults = api.search(query.trim(), searchType)
                                if (searchResults.isEmpty()) searchError = "No matching movies or series were found."
                            } catch (error: Exception) {
                                searchResults = emptyList()
                                searchError = error.localizedMessage?.take(180) ?: "Search request failed."
                            } finally {
                                loading = false
                            }
                        }
                    },
                    onMovieClick = { selectedMovie = it }
                )
                AppTab.LIBRARY -> EmptyLibrary()
                AppTab.PROFILE -> ProfileScreen(
                    apiBase, backendStatus, isConnected,
                    onSettings = { apiDraft = apiBase; showSettings = true },
                    onRefresh = { scope.launch { refreshCatalog() } }
                )
                }
            }
        }
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            containerColor = Color(0xFF151D29),
            title = { Text("Backend connection", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "MovDash is preconfigured to use the production Railway API. Change this only when connecting to another MovDash backend.",
                        color = Color(0xFFB6C0CE), style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = apiDraft,
                        onValueChange = { apiDraft = it },
                        label = { Text("API base URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val value = apiDraft.trim()
                    if (value.startsWith("http://") || value.startsWith("https://")) {
                        apiBase = if (value.endsWith("/")) value else "$value/"
                        prefs.edit().putString("api_base", apiBase).apply()
                        showSettings = false
                    }
                }) { Text("Save & connect") }
            },
            dismissButton = { TextButton(onClick = { showSettings = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun HomeScreen(
    movies: List<Movie>,
    series: List<Movie>,
    popularMovies: List<Movie>,
    popularSeries: List<Movie>,
    sources: List<SourceInfo>,
    connected: Boolean,
    status: String,
    loading: Boolean,
    onMovieClick: (Movie) -> Unit,
    onRefresh: () -> Unit,
    onDiscover: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(bottom = 12.dp).verticalScroll(rememberScrollState())) {
        HeroCard(movies = (movies + series).distinctBy { "${it.mediaType}-${it.id}" }.take(5), onExplore = onDiscover)
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(if (connected) "CONNECTED" else "BACKEND STATUS",
                    color = if (connected) Color(0xFF8DD8A6) else Color(0xFFE9B96E),
                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                Text(if (loading) "Loading catalogue…" else status, color = Color(0xFF9DA8B7),
                    style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color(0xFFE9B96E)) }
        }
        if (movies.isNotEmpty()) {
            SectionTitle("Trending movies", "See all", onDiscover)
            LazyRow(contentPadding = PaddingValues(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                items(movies, key = { "movie-${it.id}" }) { movie -> MoviePosterCard(movie) { onMovieClick(movie) } }
            }
        }
        if (series.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            SectionTitle("Trending series", "See all", onDiscover)
            LazyRow(contentPadding = PaddingValues(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                items(series, key = { "tv-${it.id}" }) { item -> MoviePosterCard(item) { onMovieClick(item) } }
            }
        }
        if (popularMovies.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            SectionTitle("Popular movies", "Discover", onDiscover)
            LazyRow(contentPadding = PaddingValues(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                items(popularMovies, key = { "popular-movie-${it.id}" }) { item -> MoviePosterCard(item) { onMovieClick(item) } }
            }
        }
        if (popularSeries.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            SectionTitle("Popular series", "Discover", onDiscover)
            LazyRow(contentPadding = PaddingValues(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                items(popularSeries, key = { "popular-tv-${it.id}" }) { item -> MoviePosterCard(item) { onMovieClick(item) } }
            }
        }
        if (!loading && movies.isEmpty() && series.isEmpty()) {
            Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Movie, contentDescription = null, tint = Color(0xFFE9B96E), modifier = Modifier.size(42.dp))
                Spacer(Modifier.height(8.dp))
                Text("Catalogue unavailable", color = Color.White, fontWeight = FontWeight.Bold)
                Text(status, color = Color(0xFF9AA5B5), style = MaterialTheme.typography.bodySmall)
                Text("Check the backend URL and TMDB configuration in Settings.", color = Color(0xFF9AA5B5), style = MaterialTheme.typography.bodySmall)
                Button(onClick = onRefresh) { Text("Retry") }
            }
        }
        Spacer(Modifier.height(22.dp))
        SectionTitle("Your sources", "Settings", onDiscover)
        if (sources.isEmpty()) {
            SourceCard("TMDB", "Catalogue metadata · configure the backend API key", false)
            SourceCard("Jellyfin", "Authorized personal media library", false)
        } else {
            sources.forEach { source -> SourceCard(source.name, source.note ?: source.type, source.configured) }
        }
        Spacer(Modifier.height(18.dp))
        Text("Discover metadata from TMDB and play media only from providers you are authorized to access.",
            color = Color(0xFF8490A0), style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 20.dp))
    }
}

@Composable
private fun HeroCard(movies: List<Movie>, onExplore: () -> Unit) {
    val spotlightMovies = movies.take(5)
    if (spotlightMovies.isEmpty()) {
        Box(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)
                .height(190.dp).clip(RoundedCornerShape(23.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF273246), Color(0xFF111722)))),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("MOVDASH", color = Color(0xFFE9B96E), fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Text("Your movie and series universe", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Text("Connect the catalogue to load real titles", color = Color(0xFF9AA5B5), style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = onExplore) { Text("Search catalogue", color = Color(0xFFE9B96E)) }
            }
        }
        return
    }
    val pagerState = rememberPagerState(pageCount = { spotlightMovies.size })
    LaunchedEffect(pagerState, spotlightMovies.size) {
        if (spotlightMovies.size > 1) {
            while (true) {
                delay(5500)
                pagerState.animateScrollToPage((pagerState.currentPage + 1) % spotlightMovies.size)
            }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 10.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val movie = spotlightMovies[page]
            Box(
                Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(23.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF273246), Color(0xFF111722), Color(0xFF3B2A1A))))
            ) {
                movie.backdropPath?.let { path ->
                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w1280$path",
                        contentDescription = movie.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
                    colors = listOf(Color(0x22080B11), Color(0x55080B11), Color(0xF5080B11))
                )))
                Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("SPOTLIGHT · ${movie.mediaType.uppercase()}", color = Color(0xFFE9B96E),
                        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp)
                    Text(movie.title, color = Color.White, style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.CenterVertically) {
                        movie.releaseDate?.take(4)?.let { Text(it, color = Color(0xFFD3D8E0), style = MaterialTheme.typography.labelMedium) }
                        movie.voteAverage?.let { Text("★ ${"%.1f".format(it)}", color = Color(0xFFE9B96E),
                            style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) }
                    }
                    Button(onClick = onExplore, colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE9B96E), contentColor = Color(0xFF1A1309)
                    ), contentPadding = PaddingValues(horizontal = 15.dp, vertical = 7.dp)) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Explore titles", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            repeat(spotlightMovies.size) { index ->
                val selected = pagerState.currentPage == index
                Box(Modifier.padding(horizontal = 3.dp).width(if (selected) 19.dp else 6.dp).height(6.dp)
                    .clip(CircleShape).background(if (selected) Color(0xFFE9B96E) else Color(0xFF485364)))
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, action: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        TextButton(onClick = onClick) { Text(action, color = Color(0xFFE9B96E)) }
    }
}

@Composable
private fun MoviePosterCard(movie: Movie, onClick: () -> Unit) {
    Column(Modifier.width(132.dp).clickable(onClick = onClick)) {
        Box(Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF35445D), Color(0xFF171F2C))))) {
            val imageUrl = movie.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
            if (imageUrl != null) {
                AsyncImage(model = imageUrl, contentDescription = movie.title,
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }
            Box(Modifier.align(Alignment.BottomStart).fillMaxWidth().height(60.dp)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xDD080B10)))))
            Text(
                movie.voteAverage?.let { "★ " + "%.1f".format(it) } ?: "MOVDASH",
                color = Color(0xFFE9B96E), style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)
            )
        }
        Spacer(Modifier.height(7.dp))
        Text(movie.title, color = Color.White, style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(movie.releaseDate?.take(4) ?: "Explore", color = Color(0xFF8793A3), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun SourceCard(name: String, description: String, configured: Boolean) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(15.dp)).background(Color(0xFF121A25)).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(38.dp).clip(CircleShape).background(Color(0xFF273447)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Movie, contentDescription = null, tint = Color(0xFFE9B96E))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(name, color = Color.White, fontWeight = FontWeight.Bold)
            Text(description, color = Color(0xFF8D99A9), style = MaterialTheme.typography.labelSmall,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(if (configured) "READY" else "SETUP",
            color = if (configured) Color(0xFF8DD8A6) else Color(0xFFE9B96E),
            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DiscoverScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    mediaType: String,
    onMediaTypeChange: (String) -> Unit,
    results: List<Movie>,
    loading: Boolean,
    connected: Boolean,
    error: String?,
    onSearch: () -> Unit,
    onMovieClick: (Movie) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 12.dp)) {
        Text("Discover", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Search movies and TV series across the catalogue.", color = Color(0xFF9AA5B5), style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = mediaType == "multi", onClick = { onMediaTypeChange("multi") }, label = { Text("All") })
            FilterChip(selected = mediaType == "movie", onClick = { onMediaTypeChange("movie") }, label = { Text("Movies") })
            FilterChip(selected = mediaType == "tv", onClick = { onMediaTypeChange("tv") }, label = { Text("Series") })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query, onValueChange = onQueryChange, modifier = Modifier.weight(1f),
                singleLine = true, placeholder = { Text("Search titles, shows…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = onSearch, enabled = query.isNotBlank() && !loading) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        }
        Spacer(Modifier.height(10.dp))
        when {
            loading -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFFE9B96E), strokeWidth = 2.dp)
                Text("Searching catalogue…", color = Color(0xFFE9B96E))
            }
            error != null -> Text(error, color = Color(0xFFFFA69C), style = MaterialTheme.typography.bodySmall)
            !connected -> Text("Backend is offline. Check the API address in Profile → Connection settings.",
                color = Color(0xFFFFC875), style = MaterialTheme.typography.bodySmall)
            results.isEmpty() && query.isNotBlank() -> Text("No results yet. Run a search to find titles.", color = Color(0xFF9AA5B5))
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 18.dp)) {
            items(results, key = { "${it.mediaType}-${it.id}" }) { movie ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFF121A25))
                        .clickable { onMovieClick(movie) }.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = movie.posterPath?.let { "https://image.tmdb.org/t/p/w185$it" },
                        contentDescription = movie.title, contentScale = ContentScale.Crop,
                        modifier = Modifier.size(width = 58.dp, height = 82.dp).clip(RoundedCornerShape(9.dp))
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(movie.title, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("${movie.mediaType.uppercase()} · ${movie.releaseDate?.take(4) ?: "Release date unknown"}",
                            color = Color(0xFFE9B96E), style = MaterialTheme.typography.labelSmall)
                        Text(movie.overview, color = Color(0xFF9AA5B5), style = MaterialTheme.typography.bodySmall,
                            maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyLibrary() {
    Column(
        Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = Color(0xFFE9B96E), modifier = Modifier.size(54.dp))
        Spacer(Modifier.height(14.dp))
        Text("Your library starts here", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Saved movies and watch history will appear here as those features are connected.",
            color = Color(0xFF98A3B2), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ProfileScreen(
    apiBase: String,
    status: String,
    connected: Boolean,
    onSettings: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Your MovDash", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Independent movie discovery and authorized playback.", color = Color(0xFF9AA5B5))
        SourceCard("Backend API", apiBase, connected)
        Text(status, color = if (connected) Color(0xFF8DD8A6) else Color(0xFFE9B96E), style = MaterialTheme.typography.bodySmall)
        Button(onClick = onSettings, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Connection settings")
        }
        TextButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Check connection")
        }
        Text("Build 0.1.0 · Debug", color = Color(0xFF667282), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun MovieDetailsScreen(
    movie: Movie,
    api: com.movdash.app.data.MovDashApi,
    sources: List<SourceInfo>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var details by remember(movie.id, movie.mediaType) { mutableStateOf<MediaDetails?>(null) }
    var jellyfinMatch by remember(movie.id, movie.mediaType) { mutableStateOf<JellyfinItem?>(null) }
    var seasons by remember(movie.id) { mutableStateOf<List<JellyfinItem>>(emptyList()) }
    var episodes by remember(movie.id) { mutableStateOf<List<JellyfinItem>>(emptyList()) }
    var selectedSeasonId by remember(movie.id) { mutableStateOf<String?>(null) }
    var country by remember(movie.id) { mutableStateOf("IN") }
    var loading by remember { mutableStateOf(true) }
    var libraryLoading by remember { mutableStateOf(false) }
    var playing by remember { mutableStateOf(false) }
    var detailError by remember { mutableStateOf<String?>(null) }
    var libraryError by remember { mutableStateOf<String?>(null) }
    val jellyfinConfigured = sources.any { it.id == "jellyfin" && it.configured }

    LaunchedEffect(movie.id, movie.mediaType) {
        loading = true
        detailError = null
        try {
            details = api.details(movie.mediaType, movie.id)
        } catch (error: Exception) {
            detailError = error.localizedMessage?.take(160) ?: "Could not load title details."
        }
        if (jellyfinConfigured) {
            libraryLoading = true
            try {
                val type = if (movie.mediaType == "tv") "tv" else "movie"
                val candidates = api.jellyfinItems(movie.title, type, 100)
                fun normalize(value: String) = value.lowercase().filter { it.isLetterOrDigit() }
                jellyfinMatch = candidates.firstOrNull { normalize(it.title) == normalize(movie.title) }
                if (jellyfinMatch == null) libraryError = "This title was not found in your Jellyfin library."
            } catch (error: Exception) {
                libraryError = error.localizedMessage?.take(140) ?: "Could not search your Jellyfin library."
            } finally {
                libraryLoading = false
            }
        }
        loading = false
    }

    LaunchedEffect(jellyfinMatch?.id) {
        val match = jellyfinMatch
        if (match != null && match.mediaType.equals("Series", ignoreCase = true)) {
            try {
                seasons = api.jellyfinSeasons(match.id)
                val first = seasons.firstOrNull()
                selectedSeasonId = first?.id
                if (first != null) episodes = api.jellyfinEpisodes(match.id, first.id)
            } catch (error: Exception) {
                libraryError = error.localizedMessage?.take(140) ?: "Could not load episodes."
            }
        }
    }

    fun playItem(item: JellyfinItem) {
        scope.launch {
            playing = true
            libraryError = "Preparing playback from your personal library…"
            try {
                val session = api.playbackSession(item.id)
                context.startActivity(
                    Intent(context, PlayerActivity::class.java)
                        .putExtra(PlayerActivity.EXTRA_MEDIA_URL, session.mediaUrl)
                )
                libraryError = null
            } catch (error: Exception) {
                libraryError = error.localizedMessage?.take(160) ?: "Playback could not be started."
            } finally {
                playing = false
            }
        }
    }

    fun openOfficialAvailability(url: String?) {
        if (url.isNullOrBlank() || !url.startsWith("https://")) return
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }

    val countries = listOf("IN" to "India", "US" to "United States", "GB" to "United Kingdom", "CA" to "Canada")
    val region = details?.watchProviders?.get(country)
    val providerGroups: List<Pair<String, List<WatchProvider>>> = listOf(
        "Subscription" to (region?.streaming ?: emptyList()),
        "Free / with ads" to (region?.free.orEmpty() + region?.ads.orEmpty()).distinctBy {
            it.providerId?.toString() ?: it.providerName
        },
        "Rent" to (region?.rent ?: emptyList()),
        "Buy" to (region?.buy ?: emptyList())
    )
    val hasProviders = providerGroups.any { it.second.isNotEmpty() }
    val availabilityUrl = region?.link?.takeIf { it.startsWith("https://") }
    val backdrop = details?.backdropPath?.let { "https://image.tmdb.org/t/p/w1280" + it }
        ?: movie.backdropPath?.let { "https://image.tmdb.org/t/p/w1280" + it }
        ?: details?.posterPath?.let { "https://image.tmdb.org/t/p/w780" + it }
        ?: movie.posterPath?.let { "https://image.tmdb.org/t/p/w780" + it }

    Column(
        modifier.fillMaxSize()
            .background(Color(0xFF090B10))
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            Modifier.fillMaxWidth().height(330.dp)
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
                    Brush.verticalGradient(listOf(Color(0x88070A10), Color(0x22070A10), Color(0xF5090B10)))
                )
            )
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart).padding(start = 12.dp, top = 12.dp)
                    .size(44.dp).clip(CircleShape).background(Color(0xAA111923))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Column(
                Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    if (movie.mediaType == "tv") "SERIES DETAILS" else "MOVIE DETAILS",
                    color = Color(0xFFE9B96E),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    details?.title ?: movie.title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        details?.releaseDate?.take(4) ?: movie.releaseDate?.take(4) ?: "Release date unknown",
                        color = Color(0xFFE2E7EE),
                        style = MaterialTheme.typography.labelMedium
                    )
                    (details?.voteAverage ?: movie.voteAverage)?.let {
                        Text("★ " + "%.1f".format(it), color = Color(0xFFE9B96E),
                            style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color(0xFFE9B96E))
            details?.tagline?.takeIf { it.isNotBlank() }?.let {
                Text(it, color = Color(0xFFD4DCE6), style = MaterialTheme.typography.titleSmall)
            }
            val genreText = details?.genres.orEmpty().joinToString("  ·  ") { it.name }
            if (genreText.isNotBlank()) Text(genreText, color = Color(0xFFE9B96E), style = MaterialTheme.typography.labelMedium)
            val metadata = details?.let {
                if (it.mediaType == "tv") {
                    (it.numberOfSeasons ?: 0).toString() + " seasons · " +
                        (it.numberOfEpisodes ?: 0).toString() + " episodes"
                } else it.runtime?.let { runtime -> runtime.toString() + " min" } ?: ""
            }.orEmpty()
            if (metadata.isNotBlank()) Text(metadata, color = Color(0xFF9AA5B5), style = MaterialTheme.typography.labelSmall)

            Text("Synopsis", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                details?.overview?.ifBlank { movie.overview } ?: movie.overview.ifBlank { "No synopsis is available." },
                color = Color(0xFFB7C0CD),
                style = MaterialTheme.typography.bodyMedium
            )
            detailError?.let { Text(it, color = Color(0xFFFFA69C), style = MaterialTheme.typography.bodySmall) }

            HorizontalDivider(color = Color(0xFF263244))
            Text("Where to watch", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Official streaming, free, rental and purchase options for the selected country.",
                color = Color(0xFF929EAE),
                style = MaterialTheme.typography.bodySmall
            )
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

            if (loading && details == null) {
                Text("Checking availability…", color = Color(0xFF9AA5B5), style = MaterialTheme.typography.bodySmall)
            } else if (!hasProviders) {
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF121A25)).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "No official listings found for " + (countries.firstOrNull { it.first == country }?.second ?: country),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "There are no provider listings for this title in this region right now. Try another country or check again later.",
                        color = Color(0xFF929EAE),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                providerGroups.forEach { group ->
                    val groupName = group.first
                    val providers = group.second
                    if (providers.isNotEmpty()) {
                        Text(groupName, color = Color(0xFFE9B96E),
                            style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        providers.sortedBy { it.displayPriority ?: Int.MAX_VALUE }.forEach { provider ->
                            WatchProviderRow(
                                provider = provider,
                                category = groupName,
                                onClick = { openOfficialAvailability(availabilityUrl) }
                            )
                        }
                    }
                }
                if (availabilityUrl != null) {
                    Button(
                        onClick = { openOfficialAvailability(availabilityUrl) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE9B96E),
                            contentColor = Color(0xFF17120B)
                        )
                    ) {
                        Text("Open official watch options", fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "Provider listings are availability information, not direct video streams. A subscription or rental may be required.",
                        color = Color(0xFF778496), style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF263244))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Personal library", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Optional · Jellyfin", color = Color(0xFF929EAE), style = MaterialTheme.typography.labelSmall)
                }
                Text(
                    if (jellyfinConfigured) "CONNECTED" else "NOT CONNECTED",
                    color = if (jellyfinConfigured) Color(0xFF8DD8A6) else Color(0xFF8994A3),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            if (!jellyfinConfigured) {
                Text(
                    "Jellyfin is optional. You do not need your own server to browse official watch options.",
                    color = Color(0xFF929EAE), style = MaterialTheme.typography.bodySmall
                )
            } else if (libraryLoading) {
                Text("Checking your personal library…", color = Color(0xFF929EAE), style = MaterialTheme.typography.bodySmall)
            } else if (jellyfinMatch == null) {
                Text(libraryError ?: "This title is not available in your personal library.",
                    color = Color(0xFF929EAE), style = MaterialTheme.typography.bodySmall)
            } else if (jellyfinMatch?.mediaType.equals("Series", ignoreCase = true)) {
                Text("Choose a season", color = Color(0xFFD1D9E3), style = MaterialTheme.typography.bodySmall)
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    seasons.forEach { season ->
                        FilterChip(
                            selected = selectedSeasonId == season.id,
                            onClick = {
                                selectedSeasonId = season.id
                                scope.launch {
                                    try {
                                        episodes = api.jellyfinEpisodes(jellyfinMatch!!.id, season.id)
                                    } catch (error: Exception) {
                                        libraryError = error.localizedMessage?.take(140)
                                    }
                                }
                            },
                            label = { Text(season.title) }
                        )
                    }
                }
                episodes.forEach { episode ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp))
                            .background(Color(0xFF121A25)).padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(episode.title, color = Color.White, modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { playItem(episode) }, enabled = !playing) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Text("Play")
                        }
                    }
                }
            } else {
                Button(onClick = { jellyfinMatch?.let { playItem(it) } }, enabled = !playing,
                    modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(7.dp))
                    Text(if (playing) "Preparing…" else "Play from personal library")
                }
            }
            if (jellyfinConfigured) {
                libraryError?.let { Text(it, color = Color(0xFFFFC875), style = MaterialTheme.typography.bodySmall) }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun WatchProviderRow(
    provider: WatchProvider,
    category: String,
    onClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF121A25))
            .clickable(onClick = onClick)
            .padding(11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!provider.logoPath.isNullOrBlank()) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w92" + provider.logoPath,
                contentDescription = provider.providerName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp))
            )
        } else {
            Box(
                Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF29384B)),
                contentAlignment = Alignment.Center
            ) {
                Text(provider.providerName.take(1), color = Color(0xFFE9B96E), fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(provider.providerName, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(category, color = Color(0xFF8996A8), style = MaterialTheme.typography.labelSmall)
        }
        Text("OPEN", color = Color(0xFFE9B96E), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}
