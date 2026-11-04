package com.movdash.app.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object LibraryManager {
    private const val PREFS_NAME = "movdash_library"
    private const val KEY_WATCHLIST = "watchlist"
    private const val KEY_HISTORY = "history"
    private const val KEY_FAVORITES = "favorites"
    private val gson = Gson()

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

    private fun saveList(context: Context, key: String, list: List<Movie>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(key, gson.toJson(list)).apply()
    }
}
