package com.movdash.app.player

import android.annotation.SuppressLint
import android.graphics.Color
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
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView

class PlayerActivity : ComponentActivity() {
    private var player: ExoPlayer? = null
    private var webView: WebView? = null
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var fullscreenContainer: FrameLayout? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val rawUrl = intent.getStringExtra(EXTRA_MEDIA_URL)
        val streamType = intent.getStringExtra(EXTRA_STREAM_TYPE) ?: "auto"
        val authHeader = intent.getStringExtra(EXTRA_AUTH_HEADER)

        if (rawUrl.isNullOrBlank()) {
            finish()
            return
        }

        val isDirectMedia = streamType == "direct" ||
            rawUrl.endsWith(".m3u8", ignoreCase = true) ||
            rawUrl.endsWith(".mp4", ignoreCase = true) ||
            rawUrl.endsWith(".mpd", ignoreCase = true) ||
            rawUrl.contains("/stream/", ignoreCase = true)

        if (isDirectMedia) {
            setupExoPlayer(rawUrl, authHeader)
        } else {
            setupWebViewPlayer(rawUrl)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (customView != null) {
                    hideCustomView()
                } else if (webView != null && webView!!.canGoBack()) {
                    webView!!.goBack()
                } else {
                    finish()
                }
            }
        })
    }

    private fun setupExoPlayer(rawUrl: String, authHeader: String?) {
        val headers = buildMap {
            authHeader?.takeIf { it.isNotBlank() }?.let {
                put("X-Emby-Token", it)
            }
        }
        val httpFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setDefaultRequestProperties(headers)
        val mediaSourceFactory = DefaultMediaSourceFactory(this)
            .setDataSourceFactory(httpFactory)
        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .also {
                it.setMediaItem(MediaItem.fromUri(Uri.parse(rawUrl)))
                it.prepare()
                it.playWhenReady = true
            }
        setContentView(PlayerView(this).apply {
            player = this@PlayerActivity.player
            useController = true
            setBackgroundColor(Color.BLACK)
        })
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebViewPlayer(rawUrl: String) {
        val rootLayout = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        fullscreenContainer = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            visibility = View.GONE
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        webView = WebView(this).apply {
            setBackgroundColor(Color.BLACK)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
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
                    // Prevent redirecting away to external app stores or scam redirects
                    return if (url.startsWith("intent://") || url.startsWith("market://")) {
                        true
                    } else {
                        false
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    if (customView != null) {
                        callback?.onCustomViewHidden()
                        return
                    }
                    customView = view
                    customViewCallback = callback
                    webView?.visibility = View.GONE
                    fullscreenContainer?.addView(view)
                    fullscreenContainer?.visibility = View.VISIBLE
                }

                override fun onHideCustomView() {
                    hideCustomView()
                }
            }

            loadUrl(rawUrl)
        }

        rootLayout.addView(webView)
        rootLayout.addView(fullscreenContainer)
        setContentView(rootLayout)
    }

    private fun hideCustomView() {
        if (customView == null) return
        fullscreenContainer?.removeView(customView)
        fullscreenContainer?.visibility = View.GONE
        customView = null
        customViewCallback?.onCustomViewHidden()
        customViewCallback = null
        webView?.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        webView?.onResume()
    }

    override fun onPause() {
        webView?.onPause()
        super.onPause()
    }

    override fun onStop() {
        player?.release()
        player = null
        super.onStop()
    }

    override fun onDestroy() {
        hideCustomView()
        webView?.destroy()
        webView = null
        super.onDestroy()
    }

    companion object {
        const val EXTRA_MEDIA_URL = "com.movdash.app.MEDIA_URL"
        const val EXTRA_STREAM_TYPE = "com.movdash.app.STREAM_TYPE"
        const val EXTRA_AUTH_HEADER = "com.movdash.app.AUTH_HEADER"
        const val EXTRA_TITLE = "com.movdash.app.TITLE"
    }
}
