package com.movdash.app.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object LibraryManager {
    private const val PREFS_NAME = "movdash_library"
    private const val KEY_WATCHLIST = "watchlist"
    private const val KEY_HISTORY = "history"
    private const val KEY_FAVORITES = "favorites"
    private const val KEY_DOWNLOADS = "downloads"
    private const val KEY_WATCHED_IDS = "watched_timeline_ids"
    private const val KEY_SEARCH_HISTORY = "search_history"
    private const val KEY_WATCH_PROGRESS = "watch_progress"
    private val gson = Gson()

    // WATCHLIST
    fun getWatchlist(context: Context): List<Movie> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_WATCHLIST, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Movie>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addToWatchlist(context: Context, movie: Movie) {
        val list = getWatchlist(context).toMutableList()
        list.removeAll { it.id == movie.id && it.mediaType == movie.mediaType }
        list.add(0, movie)
        saveList(context, KEY_WATCHLIST, list)
    }

    fun removeFromWatchlist(context: Context, movieId: Int, mediaType: String) {
        val list = getWatchlist(context).toMutableList()
        list.removeAll { it.id == movieId && it.mediaType == mediaType }
        saveList(context, KEY_WATCHLIST, list)
    }

    fun isInWatchlist(context: Context, movieId: Int, mediaType: String): Boolean {
        return getWatchlist(context).any { it.id == movieId && it.mediaType == mediaType }
    }

    fun toggleWatchlist(context: Context, movie: Movie): Boolean {
        return if (isInWatchlist(context, movie.id, movie.mediaType)) {
            removeFromWatchlist(context, movie.id, movie.mediaType)
            false
        } else {
            addToWatchlist(context, movie)
            true
        }
    }

    // HISTORY
    fun getHistory(context: Context): List<Movie> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Movie>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addToHistory(context: Context, movie: Movie) {
        val list = getHistory(context).toMutableList()
        list.removeAll { it.id == movie.id && it.mediaType == movie.mediaType }
        list.add(0, movie)
        if (list.size > 50) {
            saveList(context, KEY_HISTORY, list.subList(0, 50))
        } else {
            saveList(context, KEY_HISTORY, list)
        }
    }

    fun clearHistory(context: Context) {
        saveList(context, KEY_HISTORY, emptyList())
    }

    // FAVORITES
    fun getFavorites(context: Context): List<Movie> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_FAVORITES, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Movie>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun toggleFavorite(context: Context, movie: Movie): Boolean {
        val list = getFavorites(context).toMutableList()
        val exists = list.any { it.id == movie.id && it.mediaType == movie.mediaType }
        if (exists) {
            list.removeAll { it.id == movie.id && it.mediaType == movie.mediaType }
        } else {
            list.add(0, movie)
        }
        saveList(context, KEY_FAVORITES, list)
        return !exists
    }

    fun isFavorite(context: Context, movieId: Int, mediaType: String): Boolean {
        return getFavorites(context).any { it.id == movieId && it.mediaType == mediaType }
    }

    // OFFLINE DOWNLOADS
    fun getDownloads(context: Context): List<DownloadedItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_DOWNLOADS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<DownloadedItem>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addDownload(context: Context, item: DownloadedItem) {
        val list = getDownloads(context).toMutableList()
        list.removeAll { it.id == item.id && it.episodeNumber == item.episodeNumber }
        list.add(0, item)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_DOWNLOADS, gson.toJson(list)).apply()
    }

    fun removeDownload(context: Context, id: Int, episodeNumber: Int?) {
        val list = getDownloads(context).toMutableList()
        list.removeAll { it.id == id && it.episodeNumber == episodeNumber }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_DOWNLOADS, gson.toJson(list)).apply()
    }

    fun isDownloaded(context: Context, id: Int, episodeNumber: Int?): Boolean {
        return getDownloads(context).any { it.id == id && it.episodeNumber == episodeNumber }
    }

    fun getTotalDownloadSizeMb(context: Context): Int {
        return getDownloads(context).sumOf { it.fileSizeMb }
    }

    // UNIVERSE WATCHED TIMELINE TRACKING
    fun isWatched(context: Context, movieId: Int): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val watchedSet = prefs.getStringSet(KEY_WATCHED_IDS, emptySet()) ?: emptySet()
        return watchedSet.contains(movieId.toString())
    }

    fun toggleWatched(context: Context, movieId: Int): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val watchedSet = prefs.getStringSet(KEY_WATCHED_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        val idStr = movieId.toString()
        val isNowWatched = if (watchedSet.contains(idStr)) {
            watchedSet.remove(idStr)
            false
        } else {
            watchedSet.add(idStr)
            true
        }
        prefs.edit().putStringSet(KEY_WATCHED_IDS, watchedSet).apply()
        return isNowWatched
    }

    fun getWatchedCountForUniverse(context: Context, universe: Universe): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val watchedSet = prefs.getStringSet(KEY_WATCHED_IDS, emptySet()) ?: emptySet()
        return universe.items.count { watchedSet.contains(it.movie.id.toString()) }
    }

    // SEARCH HISTORY (AniDash Style)
    fun getSearchHistory(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_SEARCH_HISTORY, null) ?: return listOf(
            "Jawan", "RRR", "Stranger Things", "Interstellar", "K.G.F: Chapter 2", "Deadpool", "Avengers", "Pushpa"
        )
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addSearchQuery(context: Context, query: String) {
        val clean = query.trim()
        if (clean.isBlank()) return
        val list = getSearchHistory(context).toMutableList()
        list.removeAll { it.equals(clean, ignoreCase = true) }
        list.add(0, clean)
        if (list.size > 20) {
            list.subList(20, list.size).clear()
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SEARCH_HISTORY, gson.toJson(list)).apply()
    }

    fun removeSearchQuery(context: Context, query: String) {
        val list = getSearchHistory(context).toMutableList()
        list.removeAll { it.equals(query, ignoreCase = true) }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SEARCH_HISTORY, gson.toJson(list)).apply()
    }

    fun clearSearchHistory(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_SEARCH_HISTORY).apply()
    }

    // WATCH PROGRESS (AniDash Continue Watching)
    fun getWatchProgressList(context: Context): List<WatchProgressEntry> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_WATCH_PROGRESS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<WatchProgressEntry>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveWatchProgress(context: Context, entry: WatchProgressEntry) {
        val fallbackMovie = PreloadedCatalog.findMovie(entry.id)
        val finalPoster = if (!entry.posterPath.isNullOrBlank()) entry.posterPath else fallbackMovie?.posterPath
        val finalBackdrop = if (!entry.backdropPath.isNullOrBlank()) entry.backdropPath else fallbackMovie?.backdropPath
        val finalEntry = entry.copy(posterPath = finalPoster, backdropPath = finalBackdrop)

        val list = getWatchProgressList(context).toMutableList()
        list.removeAll { it.id == finalEntry.id && it.mediaType == finalEntry.mediaType }
        list.add(0, finalEntry)
        if (list.size > 50) {
            list.subList(50, list.size).clear()
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_WATCH_PROGRESS, gson.toJson(list)).apply()

        // Also ensure it is present in general history with valid poster
        addToHistory(
            context,
            Movie(
                id = finalEntry.id,
                title = finalEntry.title,
                posterPath = finalEntry.posterPath,
                backdropPath = finalEntry.backdropPath,
                mediaType = finalEntry.mediaType
            )
        )
    }

    fun getWatchProgressForMovie(context: Context, movieId: Int, mediaType: String? = null): WatchProgressEntry? {
        return getWatchProgressList(context).firstOrNull {
            it.id == movieId && (mediaType == null || it.mediaType == mediaType)
        }
    }

    fun removeWatchProgress(context: Context, movieId: Int, mediaType: String? = null) {
        val list = getWatchProgressList(context).toMutableList()
        list.removeAll { it.id == movieId && (mediaType == null || it.mediaType == mediaType) }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_WATCH_PROGRESS, gson.toJson(list)).apply()
    }

    fun clearWatchProgress(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_WATCH_PROGRESS).apply()
    }

    // USER APP PREFERENCES (Settings Persistence)
    fun getPreferredAudio(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("pref_audio_track", "Hindi Dubbed (Dual Audio)") ?: "Hindi Dubbed (Dual Audio)"
    }

    fun setPreferredAudio(context: Context, audio: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString("pref_audio_track", audio).apply()
    }

    fun getPreferredQuality(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("pref_stream_quality", "1080p Ultra HD (Auto)") ?: "1080p Ultra HD (Auto)"
    }

    fun setPreferredQuality(context: Context, quality: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString("pref_stream_quality", quality).apply()
    }

    fun getAppTheme(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("pref_app_theme", "AMOLED Cosmic Black (Pure Deep Black)") ?: "AMOLED Cosmic Black (Pure Deep Black)"
    }

    fun setAppTheme(context: Context, theme: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString("pref_app_theme", theme).apply()
    }

    fun getUserName(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("pref_user_name", "Movie Enthusiast") ?: "Movie Enthusiast"
    }

    fun setUserName(context: Context, name: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString("pref_user_name", name).apply()
    }

    fun getUserAvatarId(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt("pref_user_avatar", 0)
    }

    fun setUserAvatarId(context: Context, avatarId: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt("pref_user_avatar", avatarId).apply()
    }

    private fun saveList(context: Context, key: String, list: List<Movie>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(key, gson.toJson(list)).apply()
    }
}
