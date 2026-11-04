package com.movdash.app.player

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Bundle
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
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.movdash.app.data.StreamServer
import com.movdash.app.data.StreamServerFactory
import com.movdash.app.ui.theme.MovDashTheme
import kotlinx.coroutines.delay

class PlayerActivity : ComponentActivity() {

    private var exoPlayer: ExoPlayer? = null
    private var webView: WebView? = null
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val rawUrl = intent.getStringExtra(EXTRA_MEDIA_URL) ?: ""
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Now Playing"
        val subtitle = intent.getStringExtra(EXTRA_SUBTITLE) ?: ""
        val initialServer = intent.getStringExtra(EXTRA_SERVER_NAME) ?: "Server 1 (River)"
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
                PlayerScreen(
                    initialUrl = rawUrl,
                    title = title,
                    subtitle = subtitle,
                    initialServer = initialServer,
                    isDirectMedia = isDirectMedia,
                    authHeader = authHeader,
                    tmdbId = tmdbId,
                    mediaType = mediaType,
                    season = season,
                    episode = episode,
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

@OptIn(UnstableApi::class)
@Composable
private fun PlayerScreen(
    initialUrl: String,
    title: String,
    subtitle: String,
    initialServer: String,
    isDirectMedia: Boolean,
    authHeader: String?,
    tmdbId: Int,
    mediaType: String,
    season: Int,
    episode: Int,
    onClose: () -> Unit,
    onAttachPlayer: (ExoPlayer) -> Unit,
    onAttachWebView: (WebView) -> Unit,
    onCustomView: (View?, WebChromeClient.CustomViewCallback?) -> Unit
) {
    val context = LocalContext.current
    var currentUrl by remember { mutableStateOf(initialUrl) }
    var currentServerName by remember { mutableStateOf(initialServer) }
    var useExoPlayer by remember {
        mutableStateOf(isDirectMedia)
    }

    // Player state
    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var bufferedPosition by remember { mutableLongStateOf(0L) }
    var isBuffering by remember { mutableStateOf(true) }

    // Controls visibility
    var showControls by remember { mutableStateOf(true) }
    var showMinusRemainingTime by remember { mutableStateOf(true) } // User requested minus countdown!

    // Aspect ratio: 0 = Fit, 1 = Zoom/Fill, 2 = Fixed 16:9
    var resizeModeIndex by remember { mutableIntStateOf(0) }

    // Dialog sheets
    var showQualityDialog by remember { mutableStateOf(false) }
    var showAudioDialog by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showServerDialog by remember { mutableStateOf(false) }

    // Selected options
    var selectedQuality by remember { mutableStateOf("Auto (1080p)") }
    var selectedAudio by remember { mutableStateOf("Original (English)") }
    var selectedSubtitle by remember { mutableStateOf("Off") }
    var selectedSpeed by remember { mutableFloatStateOf(1.0f) }

    val availableServers = remember(tmdbId, mediaType, season, episode) {
        if (tmdbId > 0) {
            StreamServerFactory.getServers(tmdbId, mediaType, season, episode)
        } else {
            emptyList()
        }
    }

    // Auto-hide controls timer
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(4000)
            showControls = false
        }
    }

    // ExoPlayer initialization
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

    // Position updater ticker
    LaunchedEffect(player, isPlaying) {
        while (player != null) {
            currentPosition = player.currentPosition.coerceAtLeast(0L)
            bufferedPosition = player.bufferedPosition.coerceAtLeast(0L)
            if (player.duration > 0) duration = player.duration
            delay(500)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = !showControls
            }
    ) {
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
            // Web / Multi-server embed stream player
            AndroidView(
                factory = { ctx ->
                    val root = FrameLayout(ctx).apply {
                        setBackgroundColor(AndroidColor.BLACK)
                    }
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
                            userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36 MovDash/0.2.0"
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

        // Center Buffering Indicator
        if (useExoPlayer && isBuffering) {
            CircularProgressIndicator(
                color = Color(0xFFE9B96E),
                strokeWidth = 3.dp,
                modifier = Modifier
                    .size(54.dp)
                    .align(Alignment.Center)
            )
        }

        // OVERLAY CONTROLS
        AnimatedVisibility(
            visible = showControls,
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
                                listOf(Color(0xEE090B10), Color(0x77090B10), Color.Transparent)
                            )
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
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
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (subtitle.isNotBlank()) {
                                Text(subtitle, color = Color(0xFFC4CFDC), fontSize = 11.sp)
                                Text("•", color = Color(0xFF8A99AB), fontSize = 10.sp)
                            }
                            Text(currentServerName, color = Color(0xFFE9B96E), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Top Action Icons
                    if (useExoPlayer) {
                        IconButton(onClick = { showQualityDialog = true }) {
                            Icon(Icons.Default.HighQuality, contentDescription = "Quality", tint = Color.White)
                        }
                        IconButton(onClick = { showAudioDialog = true }) {
                            Icon(Icons.Default.Language, contentDescription = "Audio Language", tint = Color.White)
                        }
                        IconButton(onClick = { showSubtitleDialog = true }) {
                            Icon(Icons.Default.Subtitles, contentDescription = "Subtitles", tint = if (selectedSubtitle == "Off") Color(0xFF9EABB8) else Color(0xFFE9B96E))
                        }
                    }
                    if (availableServers.isNotEmpty()) {
                        IconButton(onClick = { showServerDialog = true }) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = "Change Server", tint = Color(0xFFE9B96E))
                        }
                    }
                    IconButton(onClick = { showSpeedDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                }

                // CENTER CONTROLS (Play/Pause, Rewind 10, FastForward 10)
                if (useExoPlayer && player != null) {
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                player.seekTo((player.currentPosition - 10000).coerceAtLeast(0L))
                            },
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color(0x55151D29))
                        ) {
                            Icon(Icons.Default.Replay10, contentDescription = "-10s", tint = Color.White, modifier = Modifier.size(32.dp))
                        }

                        IconButton(
                            onClick = {
                                if (isPlaying) player.pause() else player.play()
                            },
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE9B96E))
                        ) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color(0xFF17120B),
                                modifier = Modifier.size(42.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                player.seekTo((player.currentPosition + 10000).coerceAtMost(duration))
                            },
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color(0x55151D29))
                        ) {
                            Icon(Icons.Default.Forward10, contentDescription = "+10s", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }
                }

                // BOTTOM BAR (Time, Seek Slider, Remaining Time in Minus Mode, Aspect Ratio)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0x77090B10), Color(0xF5090B10))
                            )
                        )
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    if (useExoPlayer && player != null) {
                        // Slider
                        Slider(
                            value = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f,
                            onValueChange = { fraction ->
                                currentPosition = (fraction * duration).toLong()
                            },
                            onValueChangeFinished = {
                                player.seekTo(currentPosition)
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFE9B96E),
                                activeTrackColor = Color(0xFFE9B96E),
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

                            // Aspect Ratio Mode Button
                            TextButton(
                                onClick = {
                                    resizeModeIndex = (resizeModeIndex + 1) % 3
                                    val modeName = when (resizeModeIndex) {
                                        1 -> "Zoom / Crop"
                                        2 -> "Stretch 16:9"
                                        else -> "Fit to Screen"
                                    }
                                    Toast.makeText(context, "Aspect Ratio: $modeName", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.AspectRatio, contentDescription = null, tint = Color(0xFFE9B96E), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    when (resizeModeIndex) {
                                        1 -> "Crop"
                                        2 -> "Fill"
                                        else -> "Fit"
                                    },
                                    color = Color(0xFFE9B96E),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Remaining Time in MINUS format (Tappable to toggle between minus countdown and total duration)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { showMinusRemainingTime = !showMinusRemainingTime }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                val remaining = (duration - currentPosition).coerceAtLeast(0L)
                                val timeText = if (showMinusRemainingTime) {
                                    "-${formatTime(remaining)}"
                                } else {
                                    formatTime(duration)
                                }
                                Text(
                                    timeText,
                                    color = if (showMinusRemainingTime) Color(0xFFE9B96E) else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        // Web Embed control bar
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
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2B3E), contentColor = Color(0xFFE9B96E)),
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

    // QUALITY SELECTION DIALOG
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
                    Icon(Icons.Default.HighQuality, contentDescription = null, tint = Color(0xFFE9B96E))
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
                                    Toast.makeText(context, "Quality set to $label", Toast.LENGTH_SHORT).show()
                                    showQualityDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedQuality == label,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFE9B96E))
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

    // AUDIO LANGUAGE SELECTION DIALOG
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
                    Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFFE9B96E))
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
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFE9B96E))
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
                    Icon(Icons.Default.Subtitles, contentDescription = null, tint = Color(0xFFE9B96E))
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
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFE9B96E))
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
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFFE9B96E))
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
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFE9B96E))
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

    // SERVER SWITCHER DIALOG
    if (showServerDialog && availableServers.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showServerDialog = false },
            containerColor = Color(0xFF151D29),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Color(0xFFE9B96E))
                    Spacer(Modifier.width(8.dp))
                    Text("Switch Streaming Server", color = Color.White, fontWeight = FontWeight.Bold)
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
                                Text("${server.quality} · ${server.language}", color = Color(0xFF8E9BAE), fontSize = 11.sp)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF28384E)
                            ) {
                                Text(
                                    server.badge,
                                    color = Color(0xFFE9B96E),
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
