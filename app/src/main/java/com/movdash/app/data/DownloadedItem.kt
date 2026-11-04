package com.movdash.app.data

data class DownloadedItem(
    val id: Int,
    val title: String,
    val mediaType: String,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val audioTrack: String = "Hindi Dubbed (Dual Audio)",
    val quality: String = "1080p Full HD",
    val subtitleTrack: String = "English [CC]",
    val fileSizeMb: Int = 1240,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val episodeTitle: String? = null,
    val downloadedAt: Long = System.currentTimeMillis()
)
