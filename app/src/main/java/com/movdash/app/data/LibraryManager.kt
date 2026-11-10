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
            "Avengers", "Deadpool", "Stranger Things", "Interstellar", "K.G.F", "Salaar"
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
        val list = getWatchProgressList(context).toMutableList()
        list.removeAll { it.id == entry.id && it.mediaType == entry.mediaType }
        list.add(0, entry)
        if (list.size > 50) {
            list.subList(50, list.size).clear()
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_WATCH_PROGRESS, gson.toJson(list)).apply()

        // Also ensure it is present in general history
        addToHistory(
            context,
            Movie(
                id = entry.id,
                title = entry.title,
                posterPath = entry.posterPath,
                backdropPath = entry.backdropPath,
                mediaType = entry.mediaType
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

    private fun saveList(context: Context, key: String, list: List<Movie>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(key, gson.toJson(list)).apply()
    }
}
