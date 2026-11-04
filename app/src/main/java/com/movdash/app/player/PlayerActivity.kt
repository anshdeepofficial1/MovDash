package com.movdash.app.player

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView

/** Player host for stream URLs supplied by authorized playback-session integrations. */
class PlayerActivity : ComponentActivity() {
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rawUrl = intent.getStringExtra(EXTRA_MEDIA_URL)
        if (rawUrl.isNullOrBlank()) {
            finish()
            return
        }
        val headers = buildMap {
            intent.getStringExtra(EXTRA_AUTH_HEADER)?.takeIf { it.isNotBlank() }?.let {
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
        })
    }

    override fun onStop() {
        player?.release()
        player = null
        super.onStop()
    }

    companion object {
        const val EXTRA_MEDIA_URL = "com.movdash.app.MEDIA_URL"
        const val EXTRA_AUTH_HEADER = "com.movdash.app.AUTH_HEADER"
    }
}
