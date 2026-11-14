package com.nuvio.app.features.downloads

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DownloadPreferences(
    val preferredQuality: String = "ask", // "ask", "1080p", "720p", "480p"
    val preferredServer: String = "auto",
    val preferredLanguage: String = "default",
    val includeSubtitles: Boolean = true,
    val rememberChoice: Boolean = false,
)

object DownloadPreferencesRepository {
    private val _preferences = MutableStateFlow(DownloadPreferences())
    val preferences: StateFlow<DownloadPreferences> = _preferences.asStateFlow()

    fun updatePreferences(
        quality: String? = null,
        server: String? = null,
        language: String? = null,
        includeSubtitles: Boolean? = null,
        rememberChoice: Boolean? = null,
    ) {
        _preferences.update { current ->
            current.copy(
                preferredQuality = quality ?: current.preferredQuality,
                preferredServer = server ?: current.preferredServer,
                preferredLanguage = language ?: current.preferredLanguage,
                includeSubtitles = includeSubtitles ?: current.includeSubtitles,
                rememberChoice = rememberChoice ?: current.rememberChoice,
            )
        }
    }

    fun resetRememberedChoice() {
        _preferences.update { current ->
            current.copy(
                rememberChoice = false,
                preferredQuality = "ask",
            )
        }
    }
}
