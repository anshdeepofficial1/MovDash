@file:kotlin.OptIn(
    androidx.media3.common.util.UnstableApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.movdash.app.player


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color as AndroidColor
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import kotlin.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.movdash.app.ui.components.OrbitLoader
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import com.movdash.app.data.EpisodeSummary
import com.movdash.app.data.PreloadedCatalog
import com.movdash.app.data.StreamServerFactory
import com.movdash.app.ui.theme.MovDashTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class PlayerActivity : ComponentActivity() {

    private var exoPlayer: ExoPlayer? = null
    private var webView: WebView? = null
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Automatically launch into Sensor Landscape for optimal cinematic viewing
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        // Enable immersive edge-to-edge sticky mode
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        val rawUrl = intent.getStringExtra(EXTRA_MEDIA_URL) ?: ""
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Now Playing"
        val subtitle = intent.getStringExtra(EXTRA_SUBTITLE) ?: ""
        val initialServer = intent.getStringExtra(EXTRA_SERVER_NAME) ?: "Fast Stream"
        val tmdbId = intent.getIntExtra(EXTRA_TMDB_ID, 0)
        val mediaType = intent.getStringExtra(EXTRA_MEDIA_TYPE) ?: "movie"
        val season = intent.getIntExtra(EXTRA_SEASON, 1)
        val episode = intent.getIntExtra(EXTRA_EPISODE, 1)
        val authHeader = intent.getStringExtra(EXTRA_AUTH_HEADER)

        if (rawUrl.isBlank()) {
            Toast.makeText(this, "Media URL is missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val isDirectMedia = rawUrl.endsWith(".m3u8", ignoreCase = true) ||
            rawUrl.endsWith(".mp4", ignoreCase = true) ||
            rawUrl.endsWith(".mpd", ignoreCase = true) ||
            rawUrl.contains("/stream/", ignoreCase = true)

        setContent {
            MovDashTheme {
                AdvancedProPlayerScreen(
                    initialUrl = rawUrl,
                    title = title,
                    initialSubtitle = subtitle,
                    initialServer = initialServer,
                    isDirectMedia = isDirectMedia,
                    authHeader = authHeader,
                    tmdbId = tmdbId,
                    mediaType = mediaType,
                    initialSeason = season,
                    initialEpisode = episode,
                    onClose = { finish() },
                    onAttachPlayer = { player -> exoPlayer = player },
                    onAttachWebView = { wv -> webView = wv },
                    onCustomView = { v, cb ->
                        customView = v
                        customViewCallback = cb
                    }
                )
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (customView != null) {
                    customViewCallback?.onCustomViewHidden()
                    customView = null
                    customViewCallback = null
                } else if (webView != null && webView!!.canGoBack()) {
                    webView!!.goBack()
                } else {
                    finish()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        webView?.onResume()
        exoPlayer?.playWhenReady = true
    }

    override fun onPause() {
        exoPlayer?.playWhenReady = false
        webView?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        exoPlayer?.release()
        exoPlayer = null
        webView?.destroy()
        webView = null
        super.onDestroy()
    }

    companion object {
        const val EXTRA_MEDIA_URL = "com.movdash.app.MEDIA_URL"
        const val EXTRA_STREAM_TYPE = "com.movdash.app.STREAM_TYPE"
        const val EXTRA_AUTH_HEADER = "com.movdash.app.AUTH_HEADER"
        const val EXTRA_TITLE = "com.movdash.app.TITLE"
        const val EXTRA_SUBTITLE = "com.movdash.app.SUBTITLE"
        const val EXTRA_SERVER_NAME = "com.movdash.app.SERVER_NAME"
        const val EXTRA_TMDB_ID = "com.movdash.app.TMDB_ID"
        const val EXTRA_MEDIA_TYPE = "com.movdash.app.MEDIA_TYPE"
        const val EXTRA_SEASON = "com.movdash.app.SEASON"
        const val EXTRA_EPISODE = "com.movdash.app.EPISODE"
    }
}

@kotlin.OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.media3.common.util.UnstableApi::class)
@Composable
private fun AdvancedProPlayerScreen(
    initialUrl: String,
    title: String,
    initialSubtitle: String,
    initialServer: String,
    isDirectMedia: Boolean,
    authHeader: String?,
    tmdbId: Int,
    mediaType: String,
    initialSeason: Int,
    initialEpisode: Int,
    onClose: () -> Unit,
    onAttachPlayer: (ExoPlayer) -> Unit,
    onAttachWebView: (WebView) -> Unit,
    onCustomView: (View?, WebChromeClient.CustomViewCallback?) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxAudioVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1) }

    var currentUrl by remember { mutableStateOf(initialUrl) }
    var currentServerName by remember { mutableStateOf(initialServer) }
    var useExoPlayer by remember { mutableStateOf(isDirectMedia) }

    // Dynamic Season & Episode State for TV Series
    var currentSeason by remember { mutableIntStateOf(initialSeason) }
    var currentEpisode by remember { mutableIntStateOf(initialEpisode) }
    var episodeSubtitle by remember { mutableStateOf(initialSubtitle) }

    // Fetch season details for TV series (in-player episode switching)
    val seasonData = remember(tmdbId, currentSeason) {
        if (mediaType == "tv" && tmdbId > 0) {
            PreloadedCatalog.getSeasonDetails(tmdbId, currentSeason)
        } else {
            null
        }
    }
    val episodesList = seasonData?.episodes.orEmpty()

    // Player Playback State
    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var bufferedPosition by remember { mutableLongStateOf(0L) }
    var isBuffering by remember { mutableStateOf(true) }

    // Screen Lock Mode (AniDash Feature)
    var isLocked by remember { mutableStateOf(false) }

    // Controls Visibility
    var showControls by remember { mutableStateOf(true) }
    var showMinusRemainingTime by remember { mutableStateOf(true) }

    // Gesture Feedback Indicators (AniDash Feature)
    var seekFeedbackText by remember { mutableStateOf<String?>(null) }
    var seekFeedbackIsForward by remember { mutableStateOf(true) }
    var brightnessOverlayValue by remember { mutableStateOf<Float?>(null) }
    var volumeOverlayValue by remember { mutableStateOf<Int?>(null) }
    var isLongPressFastForward by remember { mutableStateOf(false) }

    // Aspect Ratio Mode: 0 = Fit, 1 = Crop (Zoom to Fill), 2 = Fill (16:9 Stretch)
    var resizeModeIndex by remember { mutableIntStateOf(0) }

    // Orientation Mode: 0 = Sensor Landscape, 1 = Reverse Landscape, 2 = Portrait, 3 = Auto Sensor
    var orientationMode by remember { mutableIntStateOf(0) }

    // Dialog & Sheet States
    var showEpisodesSheet by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showAudioDialog by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showServerDialog by remember { mutableStateOf(false) }

    // Selected Configuration Options
    var selectedQuality by remember { mutableStateOf("Auto (1080p)") }
    var selectedAudio by remember { mutableStateOf("Original (English)") }
    var selectedSubtitle by remember { mutableStateOf("Off") }
    var selectedSpeed by remember { mutableFloatStateOf(1.0f) }

    // Available Alternative Stream Servers
    val availableServers = remember(tmdbId, mediaType, currentSeason, currentEpisode) {
        if (tmdbId > 0) {
            StreamServerFactory.getServers(tmdbId, mediaType, currentSeason, currentEpisode)
        } else {
            emptyList()
        }
    }

    // Auto-hide controls after 4 seconds of idle playback
    LaunchedEffect(showControls, isPlaying, isLocked) {
        if (showControls && isPlaying && !isLocked) {
            delay(4000)
            showControls = false
        }
    }

    // Dismiss gesture feedback overlays
    LaunchedEffect(seekFeedbackText) {
        if (seekFeedbackText != null) {
            delay(800)
            seekFeedbackText = null
        }
    }
    LaunchedEffect(brightnessOverlayValue) {
        if (brightnessOverlayValue != null) {
            delay(1200)
            brightnessOverlayValue = null
        }
    }
    LaunchedEffect(volumeOverlayValue) {
        if (volumeOverlayValue != null) {
            delay(1200)
            volumeOverlayValue = null
        }
    }

    // ExoPlayer Instance
    val player = remember(currentUrl, useExoPlayer) {
        if (useExoPlayer) {
            val headers = buildMap {
                authHeader?.takeIf { it.isNotBlank() }?.let { put("X-Emby-Token", it) }
            }
            val httpFactory = DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true)
                .setDefaultRequestProperties(headers)
            val mediaSourceFactory = DefaultMediaSourceFactory(context).setDataSourceFactory(httpFactory)

            ExoPlayer.Builder(context)
                .setMediaSourceFactory(mediaSourceFactory)
                .build()
                .apply {
                    setMediaItem(MediaItem.fromUri(Uri.parse(currentUrl)))
                    prepare()
                    playWhenReady = true
                    playbackParameters = PlaybackParameters(selectedSpeed)
                    addListener(object : Player.Listener {
                        override fun onIsPlayingChanged(playing: Boolean) {
                            isPlaying = playing
                        }
                        override fun onPlaybackStateChanged(state: Int) {
                            isBuffering = state == Player.STATE_BUFFERING
                            if (state == Player.STATE_READY) {
                                duration = this@apply.duration.coerceAtLeast(0L)
                            }
                        }
                    })
                }.also { onAttachPlayer(it) }
        } else {
            null
        }
    }

    // Position & Duration Poller
    LaunchedEffect(player, isPlaying) {
        while (player != null) {
            currentPosition = player.currentPosition.coerceAtLeast(0L)
            bufferedPosition = player.bufferedPosition.coerceAtLeast(0L)
            if (player.duration > 0) duration = player.duration
            delay(500)
        }
    }

    // Switch Episode Helper (In-Player Binge Switching)
    fun switchEpisode(newEpNumber: Int) {
        val targetEp = episodesList.find { it.episodeNumber == newEpNumber }
        currentEpisode = newEpNumber
        episodeSubtitle = if (targetEp != null && targetEp.name.isNotBlank()) {
            "S$currentSeason:E$newEpNumber • ${targetEp.name}"
        } else {
            "Season $currentSeason • Episode $newEpNumber"
        }
        val newStreamUrl = StreamServerFactory.getDefaultStreamUrl(
            tmdbId = tmdbId,
            mediaType = mediaType,
            season = currentSeason,
            episode = currentEpisode
        )
        currentUrl = newStreamUrl
        val isDirect = newStreamUrl.endsWith(".m3u8", ignoreCase = true) ||
            newStreamUrl.endsWith(".mp4", ignoreCase = true) ||
            newStreamUrl.contains("/stream/", ignoreCase = true)
        useExoPlayer = isDirect
        Toast.makeText(context, "Playing Episode $newEpNumber", Toast.LENGTH_SHORT).show()
    }

    // Seek Helpers
    fun seekRelative(deltaMillis: Long) {
        player?.let { p ->
            val target = (p.currentPosition + deltaMillis).coerceIn(0L, duration.coerceAtLeast(1L))
            p.seekTo(target)
            currentPosition = target
            seekFeedbackIsForward = deltaMillis > 0
            seekFeedbackText = if (deltaMillis > 0) "+${deltaMillis / 1000}s" else "${deltaMillis / 1000}s"
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
    }

    // Next Episode Prompt Detection (AniDash Feature)
    val hasNextEpisode = remember(currentEpisode, episodesList) {
        episodesList.any { it.episodeNumber == currentEpisode + 1 }
    }
    val showNextPrompt = remember(currentPosition, duration, isBuffering, hasNextEpisode) {
        hasNextEpisode && duration > 60000 && (duration - currentPosition) <= 45000 && !isBuffering
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            // GESTURE HANDLER: Single Tap, Double Tap Seek, Long Press 2X Speed
            .pointerInput(isLocked) {
                if (isLocked) {
                    detectTapGestures(
                        onTap = {
                            // When locked, single tap shows the unlock button briefly
                            showControls = true
                        }
                    )
                } else {
                    detectTapGestures(
                        onTap = {
                            showControls = !showControls
                        },
                        onDoubleTap = { offset ->
                            val isRightSide = offset.x > (size.width / 2)
                            if (isRightSide) {
                                seekRelative(10000L) // +10s
                            } else {
                                seekRelative(-10000L) // -10s
                            }
                        },
                        onPress = {
                            // Long Press 2X Speed Boost (AniDash Feature)
                            val isHeld = tryAwaitRelease()
                            if (isLongPressFastForward) {
                                isLongPressFastForward = false
                                player?.playbackParameters = PlaybackParameters(selectedSpeed)
                            }
                        }
                    )
                }
            }
            // GESTURE HANDLER: Vertical Drag for Brightness (Left) & Volume (Right)
            .pointerInput(isLocked) {
                if (!isLocked) {
                    var isRightSide = false
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            isRightSide = offset.x > (size.width / 2)
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            val delta = -dragAmount / size.height.toFloat()
                            if (isRightSide) {
                                // Volume Control
                                val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                val step = if (delta > 0) 1 else if (delta < 0) -1 else 0
                                val newVol = (currentVol + step).coerceIn(0, maxAudioVolume)
                                if (newVol != currentVol) {
                                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                                    volumeOverlayValue = ((newVol.toFloat() / maxAudioVolume) * 100).roundToInt()
                                }
                            } else {
                                // Brightness Control
                                activity?.let { act ->
                                    val lp = act.window.attributes
                                    val currentB = if (lp.screenBrightness < 0f) 0.5f else lp.screenBrightness
                                    val newB = (currentB + delta * 1.5f).coerceIn(0.01f, 1.0f)
                                    lp.screenBrightness = newB
                                    act.window.attributes = lp
                                    brightnessOverlayValue = newB
                                }
                            }
                        }
                    )
                }
            }
    ) {
        // VIDEO SURFACE (ExoPlayer or WebView)
        if (useExoPlayer && player != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = player
                        useController = false
                        resizeMode = when (resizeModeIndex) {
                            1 -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            2 -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                            else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        }
                        setBackgroundColor(AndroidColor.BLACK)
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { view ->
                    view.resizeMode = when (resizeModeIndex) {
                        1 -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        2 -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                        else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Web / Embed Multi-Server Fallback
            AndroidView(
                factory = { ctx ->
                    val root = FrameLayout(ctx).apply { setBackgroundColor(AndroidColor.BLACK) }
                    val wv = WebView(ctx).apply {
                        setBackgroundColor(AndroidColor.BLACK)
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            mediaPlaybackRequiresUserGesture = false
                            allowFileAccess = true
                            allowContentAccess = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                            userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36 MovDash/0.2.1"
                        }
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val url = request?.url?.toString() ?: return false
                                return url.startsWith("intent://") || url.startsWith("market://")
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                                onCustomView(view, callback)
                            }
                            override fun onHideCustomView() {
                                onCustomView(null, null)
                            }
                        }
                        loadUrl(currentUrl)
                    }
                    onAttachWebView(wv)
                    root.addView(wv)
                    root
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // CENTER BUFFERING SPINNER (Orbit Animated Logo)
        if (useExoPlayer && isBuffering) {
            OrbitLoader(
                size = 58.dp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // DOUBLE-TAP SEEK FEEDBACK RIPPLE (AniDash Feature)
        AnimatedVisibility(
            visible = seekFeedbackText != null,
            enter = scaleIn(initialScale = 0.8f) + fadeIn(),
            exit = scaleOut(targetScale = 1.2f) + fadeOut(),
            modifier = Modifier.align(if (seekFeedbackIsForward) Alignment.CenterEnd else Alignment.CenterStart)
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 48.dp)
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0x88000000)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        if (seekFeedbackIsForward) Icons.Default.Forward10 else Icons.Default.Replay10,
                        contentDescription = null,
                        tint = Color(0xFFF1F5F9),
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        seekFeedbackText ?: "",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // BRIGHTNESS OVERLAY INDICATOR (Left Edge Swipe)
        AnimatedVisibility(
            visible = brightnessOverlayValue != null,
            enter = fadeIn() + slideInHorizontally { -it },
            exit = fadeOut() + slideOutHorizontally { -it },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xCC111822),
                modifier = Modifier.width(52.dp).height(160.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(Icons.Default.BrightnessMedium, contentDescription = null, tint = Color(0xFFF1F5F9), modifier = Modifier.size(22.dp))
                    Text(
                        "${((brightnessOverlayValue ?: 0.5f) * 100).roundToInt()}%",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // VOLUME OVERLAY INDICATOR (Right Edge Swipe)
        AnimatedVisibility(
            visible = volumeOverlayValue != null,
            enter = fadeIn() + slideInHorizontally { it },
            exit = fadeOut() + slideOutHorizontally { it },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xCC111822),
                modifier = Modifier.width(52.dp).height(160.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        if ((volumeOverlayValue ?: 0) == 0) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = Color(0xFFF1F5F9),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        "${volumeOverlayValue ?: 0}%",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2X SPEED HOLD INDICATOR (AniDash Feature)
        AnimatedVisibility(
            visible = isLongPressFastForward,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 20.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xEE1E293B)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.FastForward, contentDescription = null, tint = Color(0xFFF1F5F9), modifier = Modifier.size(18.dp))
                    Text("2X SPEED >>", color = Color(0xFFF1F5F9), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
            }
        }

        // SCREEN LOCK FLOATING UNLOCK BUTTON (When Locked)
        if (isLocked) {
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(20.dp)
            ) {
                IconButton(
                    onClick = {
                        isLocked = false
                        Toast.makeText(context, "Controls Unlocked", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xDD151D29))
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = "Unlock", tint = Color(0xFFF1F5F9))
                }
            }
        }

        // NEXT EPISODE PROMPT CARD (AniDash Feature)
        AnimatedVisibility(
            visible = showNextPrompt && !isLocked,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 24.dp)
        ) {
            val nextEp = episodesList.find { it.episodeNumber == currentEpisode + 1 }
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xF0121A26),
                shadowElevation = 8.dp,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("UP NEXT", color = Color(0xFFF1F5F9), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "E${currentEpisode + 1}: ${nextEp?.name ?: "Next Episode"}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Button(
                        onClick = { switchEpisode(currentEpisode + 1) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914), contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Play", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // OVERLAY CONTROLS (Top Bar, Center Controls, Bottom Bar)
        AnimatedVisibility(
            visible = showControls && !isLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0x77000000))
            ) {
                // TOP BAR
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xF0090B10), Color(0x88090B10), Color.Transparent)
                            )
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(Modifier.width(4.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            title,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (episodeSubtitle.isNotBlank()) {
                                Text(episodeSubtitle, color = Color(0xFFC4CFDC), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("•", color = Color(0xFF8A99AB), fontSize = 10.sp)
                            }
                            Text(currentServerName, color = Color(0xFFF1F5F9), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // In-Player Episodes Drawer Trigger (for TV series)
                    if (mediaType == "tv" && episodesList.isNotEmpty()) {
                        IconButton(onClick = { showEpisodesSheet = true }) {
                            Icon(Icons.Default.VideoLibrary, contentDescription = "Episodes", tint = Color(0xFFF1F5F9))
                        }
                    }

                    // Screen Lock Button (AniDash Feature)
                    IconButton(onClick = {
                        isLocked = true
                        Toast.makeText(context, "Screen Locked. Tap unlock to restore.", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Lock, contentDescription = "Lock", tint = Color.White)
                    }

                    // Aspect Ratio Button (Fit / Crop / Fill)
                    IconButton(onClick = {
                        resizeModeIndex = (resizeModeIndex + 1) % 3
                        val modeName = when (resizeModeIndex) {
                            1 -> "Zoom / Crop"
                            2 -> "Fill 16:9"
                            else -> "Fit to Screen"
                        }
                        Toast.makeText(context, "Aspect: $modeName", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.AspectRatio, contentDescription = "Aspect Ratio", tint = Color.White)
                    }

                    // Orientation Toggle Button (Sensor Landscape / Portrait / Auto)
                    IconButton(onClick = {
                        orientationMode = (orientationMode + 1) % 4
                        activity?.requestedOrientation = when (orientationMode) {
                            1 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                            2 -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                            3 -> ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
                            else -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        }
                        val name = when (orientationMode) {
                            1 -> "Reverse Landscape"
                            2 -> "Portrait"
                            3 -> "Auto-Rotate"
                            else -> "Sensor Landscape"
                        }
                        Toast.makeText(context, "Orientation: $name", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ScreenRotation, contentDescription = "Orientation", tint = Color.White)
                    }

                    // Quality, Audio, Subtitles, Speed Dialogs
                    if (useExoPlayer) {
                        IconButton(onClick = { showQualityDialog = true }) {
                            Icon(Icons.Default.HighQuality, contentDescription = "Quality", tint = Color.White)
                        }
                        IconButton(onClick = { showAudioDialog = true }) {
                            Icon(Icons.Default.Language, contentDescription = "Audio Language", tint = Color.White)
                        }
                        IconButton(onClick = { showSubtitleDialog = true }) {
                            Icon(Icons.Default.Subtitles, contentDescription = "Subtitles", tint = if (selectedSubtitle == "Off") Color(0xFF9EABB8) else Color(0xFFF1F5F9))
                        }
                    }
                    IconButton(onClick = { showSpeedDialog = true }) {
                        Icon(Icons.Default.Speed, contentDescription = "Speed", tint = Color.White)
                    }
                    if (availableServers.isNotEmpty()) {
                        IconButton(onClick = { showServerDialog = true }) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = "Change Server", tint = Color(0xFFF1F5F9))
                        }
                    }
                }

                // CENTER CONTROLS (Previous Ep, -10s, Play/Pause, +10s, Next Ep)
                if (useExoPlayer && player != null) {
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(22.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Episode (if TV series and has previous)
                        if (mediaType == "tv" && currentEpisode > 1) {
                            IconButton(
                                onClick = { switchEpisode(currentEpisode - 1) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x66151D29))
                            ) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous Episode", tint = Color.White, modifier = Modifier.size(28.dp))
                            }
                        }

                        // Rewind 10s
                        IconButton(
                            onClick = { seekRelative(-10000L) },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0x66151D29))
                        ) {
                            Icon(Icons.Default.Replay10, contentDescription = "-10s", tint = Color.White, modifier = Modifier.size(30.dp))
                        }

                        // Play / Pause glowing center button
                        IconButton(
                            onClick = {
                                if (isPlaying) player.pause() else player.play()
                            },
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                        ) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color(0xFF17120B),
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        // Forward 10s
                        IconButton(
                            onClick = { seekRelative(10000L) },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0x66151D29))
                        ) {
                            Icon(Icons.Default.Forward10, contentDescription = "+10s", tint = Color.White, modifier = Modifier.size(30.dp))
                        }

                        // Next Episode (if TV series and has next)
                        if (mediaType == "tv" && hasNextEpisode) {
                            IconButton(
                                onClick = { switchEpisode(currentEpisode + 1) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x66151D29))
                            ) {
                                Icon(Icons.Default.SkipNext, contentDescription = "Next Episode", tint = Color.White, modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                }

                // FLOATING SKIP INTRO (+85s) BUTTON (AniDash Signature Feature)
                if (useExoPlayer && player != null && duration > 100000) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 75.dp, end = 20.dp)
                    ) {
                        Button(
                            onClick = { seekRelative(85000L) }, // Skips standard anime/movie intro 85 seconds
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xDD1E293B),
                                contentColor = Color(0xFFF1F5F9)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.FastForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Skip Intro (+85s)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // BOTTOM CONTROLS BAR (Seekbar, Current Time, Minus Countdown, Mode)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0x88090B10), Color(0xF5090B10))
                            )
                        )
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    if (useExoPlayer && player != null) {
                        // Slider / Progress Scrubber
                        Slider(
                            value = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f,
                            onValueChange = { fraction ->
                                currentPosition = (fraction * duration).toLong()
                            },
                            onValueChangeFinished = {
                                player.seekTo(currentPosition)
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFF1F5F9),
                                activeTrackColor = Color(0xFFF1F5F9),
                                inactiveTrackColor = Color(0x55556677)
                            ),
                            modifier = Modifier.fillMaxWidth().height(24.dp)
                        )

                        // Time Labels Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Current Elapsed Time
                            Text(
                                formatTime(currentPosition),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Center Info Tag (Quality / Speed indicator)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF161E2C)
                            ) {
                                Text(
                                    "${selectedQuality.substringBefore(" ")} • ${selectedSpeed}x",
                                    color = Color(0xFF8E9BAE),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }

                            // Minus Remaining Time Countdown (Tap to toggle between remaining time and duration)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { showMinusRemainingTime = !showMinusRemainingTime }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                val remaining = (duration - currentPosition).coerceAtLeast(0L)
                                val timeText = if (showMinusRemainingTime) "-${formatTime(remaining)}" else formatTime(duration)
                                Text(
                                    timeText,
                                    color = if (showMinusRemainingTime) Color(0xFFF1F5F9) else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        // Web Embed Bar
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Streaming via $currentServerName",
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp
                            )
                            if (availableServers.isNotEmpty()) {
                                Button(
                                    onClick = { showServerDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2B3E), contentColor = Color(0xFFF1F5F9)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Switch Server", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // IN-PLAYER EPISODES BOTTOM SHEET (AniDash Feature)
    if (showEpisodesSheet && episodesList.isNotEmpty()) {
        PlayerEpisodesBottomSheet(
            episodesList = episodesList,
            currentSeason = currentSeason,
            currentEpisode = currentEpisode,
            onEpisodeSelect = { epNum ->
                showEpisodesSheet = false
                switchEpisode(epNum)
            },
            onDismiss = { showEpisodesSheet = false }
        )
    }

    // VIDEO QUALITY DIALOG
    if (showQualityDialog) {
        val qualities = listOf(
            "Auto (Recommended)" to 0,
            "1080p Full HD" to 1080,
            "720p HD" to 720,
            "480p SD" to 480,
            "360p Data Saver" to 360
        )
        AlertDialog(
            onDismissRequest = { showQualityDialog = false },
            containerColor = Color(0xFF151D29),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HighQuality, contentDescription = null, tint = Color(0xFFF1F5F9))
                    Spacer(Modifier.width(8.dp))
                    Text("Video Quality", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    qualities.forEach { (label, maxH) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedQuality = label
                                    player?.let { p ->
                                        val params = if (maxH > 0) {
                                            p.trackSelectionParameters
                                                .buildUpon()
                                                .setMaxVideoSize(1920, maxH)
                                                .build()
                                        } else {
                                            p.trackSelectionParameters
                                                .buildUpon()
                                                .clearVideoSizeConstraints()
                                                .build()
                                        }
                                        p.trackSelectionParameters = params
                                    }
                                    Toast.makeText(context, "Quality: $label", Toast.LENGTH_SHORT).show()
                                    showQualityDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedQuality == label,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFF1F5F9))
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(label, color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showQualityDialog = false }) { Text("Close", color = Color(0xFF8F9CAA)) }
            }
        )
    }

    // AUDIO LANGUAGE / DUB DIALOG
    if (showAudioDialog) {
        val audioTracks = listOf(
            "Original (English Audio)",
            "Hindi Dubbed (Dual Audio)",
            "Spanish (Español)",
            "French (Français)",
            "German (Deutsch)"
        )
        AlertDialog(
            onDismissRequest = { showAudioDialog = false },
            containerColor = Color(0xFF151D29),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFFF1F5F9))
                    Spacer(Modifier.width(8.dp))
                    Text("Audio Language / Dub", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    audioTracks.forEach { track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedAudio = track
                                    val langCode = when {
                                        track.contains("Hindi") -> "hi"
                                        track.contains("Spanish") -> "es"
                                        track.contains("French") -> "fr"
                                        track.contains("German") -> "de"
                                        else -> "en"
                                    }
                                    player?.let { p ->
                                        p.trackSelectionParameters = p.trackSelectionParameters
                                            .buildUpon()
                                            .setPreferredAudioLanguage(langCode)
                                            .build()
                                    }
                                    Toast.makeText(context, "Audio set to $track", Toast.LENGTH_SHORT).show()
                                    showAudioDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedAudio == track,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFF1F5F9))
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(track, color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAudioDialog = false }) { Text("Close", color = Color(0xFF8F9CAA)) }
            }
        )
    }

    // SUBTITLES SELECTION DIALOG
    if (showSubtitleDialog) {
        val subs = listOf("Off", "English [CC]", "Hindi Subtitles", "Spanish [Sub]", "French [Sub]")
        AlertDialog(
            onDismissRequest = { showSubtitleDialog = false },
            containerColor = Color(0xFF151D29),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Subtitles, contentDescription = null, tint = Color(0xFFF1F5F9))
                    Spacer(Modifier.width(8.dp))
                    Text("Subtitles (CC)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    subs.forEach { sub ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedSubtitle = sub
                                    player?.let { p ->
                                        if (sub == "Off") {
                                            p.trackSelectionParameters = p.trackSelectionParameters
                                                .buildUpon()
                                                .setIgnoredTextSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                                                .setPreferredTextLanguage(null)
                                                .build()
                                        } else {
                                            val code = if (sub.contains("Hindi")) "hi" else "en"
                                            p.trackSelectionParameters = p.trackSelectionParameters
                                                .buildUpon()
                                                .setPreferredTextLanguage(code)
                                                .build()
                                        }
                                    }
                                    Toast.makeText(context, "Subtitles: $sub", Toast.LENGTH_SHORT).show()
                                    showSubtitleDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedSubtitle == sub,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFF1F5F9))
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(sub, color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSubtitleDialog = false }) { Text("Close", color = Color(0xFF8F9CAA)) }
            }
        )
    }

    // PLAYBACK SPEED DIALOG
    if (showSpeedDialog) {
        val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            containerColor = Color(0xFF151D29),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFFF1F5F9))
                    Spacer(Modifier.width(8.dp))
                    Text("Playback Speed", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    speeds.forEach { sp ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedSpeed = sp
                                    player?.playbackParameters = PlaybackParameters(sp)
                                    Toast.makeText(context, "Speed: ${sp}x", Toast.LENGTH_SHORT).show()
                                    showSpeedDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedSpeed == sp,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFF1F5F9))
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(if (sp == 1.0f) "1.0x (Normal)" else "${sp}x", color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSpeedDialog = false }) { Text("Close", color = Color(0xFF8F9CAA)) }
            }
        )
    }

    // STREAM SERVER SWITCHER DIALOG
    if (showServerDialog && availableServers.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showServerDialog = false },
            containerColor = Color(0xFF151D29),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Color(0xFFF1F5F9))
                    Spacer(Modifier.width(8.dp))
                    Text("Switch Streaming Route", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableServers.forEach { server ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (currentServerName == server.name) Color(0xFF233246) else Color(0xFF192230))
                                .clickable {
                                    currentServerName = server.name
                                    currentUrl = server.streamUrl
                                    val isDirect = server.streamType == "direct" ||
                                        server.streamUrl.endsWith(".m3u8", ignoreCase = true) ||
                                        server.streamUrl.endsWith(".mp4", ignoreCase = true) ||
                                        server.streamUrl.contains("/stream/", ignoreCase = true)
                                    useExoPlayer = isDirect
                                    showServerDialog = false
                                    Toast.makeText(context, "Switched to ${server.name}", Toast.LENGTH_SHORT).show()
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(server.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${server.quality} • ${server.language}", color = Color(0xFF8E9BAE), fontSize = 11.sp)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF28384E)
                            ) {
                                Text(
                                    server.badge,
                                    color = Color(0xFFF1F5F9),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showServerDialog = false }) { Text("Close", color = Color(0xFF8F9CAA)) }
            }
        )
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

@kotlin.OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun PlayerEpisodesBottomSheet(
    episodesList: List<com.movdash.app.data.EpisodeSummary>,
    currentSeason: Int,
    currentEpisode: Int,
    onEpisodeSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF101722),
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Episodes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Season $currentSeason • ${episodesList.size} Episodes", color = Color(0xFFF1F5F9), fontSize = 12.sp)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF8F9CAA))
                }
            }
            Spacer(Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(episodesList, key = { it.id }) { ep ->
                    val isCurrent = ep.episodeNumber == currentEpisode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCurrent) Color(0xFF243247) else Color(0xFF151D29))
                            .clickable { onEpisodeSelect(ep.episodeNumber) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 90.dp, height = 55.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E2837))
                        ) {
                            if (ep.stillPath != null) {
                                coil3.compose.AsyncImage(
                                    model = "https://image.tmdb.org/t/p/w300${ep.stillPath}",
                                    contentDescription = ep.name,
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
                                    if (isCurrent) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = if (isCurrent) Color(0xFFF1F5F9) else Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "E${ep.episodeNumber}: ${ep.name}",
                                color = if (isCurrent) Color(0xFFF1F5F9) else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                ep.runtime?.let { "$it min" } ?: "Full HD",
                                color = Color(0xFF8E9BAE),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
