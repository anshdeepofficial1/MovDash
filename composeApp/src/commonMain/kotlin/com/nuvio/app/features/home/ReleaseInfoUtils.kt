package com.nuvio.app.features.home

import com.nuvio.app.core.time.EpisodeReleaseDatePlatform
import com.nuvio.app.core.time.isEpisodeReleaseAired

private val yearRegex = Regex("""\b(19|20)\d{2}\b""")

internal fun MetaPreview.isUnreleased(
    todayIsoDate: String,
    nowEpochMs: Long = EpisodeReleaseDatePlatform.nowEpochMs(),
): Boolean {
    rawReleaseDate
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.let { rawReleased ->
            isEpisodeReleaseAired(rawReleased, nowEpochMs)?.let { hasAired ->
                return !hasAired
            }
        }

    val info = releaseInfo ?: return false
    isEpisodeReleaseAired(info.trim(), nowEpochMs)?.let { hasAired ->
        return !hasAired
    }

    val releaseYear = yearRegex.find(info)?.value?.toIntOrNull() ?: return false
    val currentYear = todayIsoDate.take(4).toIntOrNull() ?: return false
    return releaseYear > currentYear
}

internal fun HomeCatalogSection.filterReleasedItems(
    todayIsoDate: String,
    nowEpochMs: Long = EpisodeReleaseDatePlatform.nowEpochMs(),
): HomeCatalogSection {
    val filteredItems = items.filterReleasedItems(todayIsoDate, nowEpochMs)
    return if (filteredItems.size == items.size) this else copy(items = filteredItems)
}

internal fun List<MetaPreview>.filterReleasedItems(
    todayIsoDate: String,
    nowEpochMs: Long = EpisodeReleaseDatePlatform.nowEpochMs(),
): List<MetaPreview> = filterNot { item -> item.isUnreleased(todayIsoDate, nowEpochMs) }

internal fun com.nuvio.app.features.details.MetaDetails.isUnreleased(
    nowEpochMs: Long = EpisodeReleaseDatePlatform.nowEpochMs(),
    todayIsoDate: String = EpisodeReleaseDatePlatform.localIsoDateAtEpochMs(nowEpochMs).orEmpty(),
): Boolean {
    if (type.equals("series", ignoreCase = true) || videos.isNotEmpty()) {
        val firstEp = videos.firstOrNull { it.season == 1 && it.episode == 1 } ?: videos.firstOrNull()
        if (firstEp?.released != null) {
            isEpisodeReleaseAired(firstEp.released, nowEpochMs)?.let { hasAired ->
                return !hasAired
            }
        }
    }

    val info = releaseInfo?.trim() ?: return false
    isEpisodeReleaseAired(info, nowEpochMs)?.let { hasAired ->
        return !hasAired
    }
    val releaseYear = yearRegex.find(info)?.value?.toIntOrNull() ?: return false
    val currentYear = todayIsoDate.take(4).toIntOrNull() ?: return false
    return releaseYear > currentYear
}
