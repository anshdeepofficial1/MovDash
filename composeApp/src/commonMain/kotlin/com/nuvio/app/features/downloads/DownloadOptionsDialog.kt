package com.nuvio.app.features.downloads

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun DownloadOptionsDialog(
    title: String,
    subtitle: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (quality: String, server: String, language: String, includeSubtitles: Boolean, rememberChoice: Boolean) -> Unit,
) {
    val prefs by DownloadPreferencesRepository.preferences.collectAsState()

    var selectedQuality by remember { mutableStateOf(if (prefs.preferredQuality != "ask") prefs.preferredQuality else "1080p") }
    var selectedServer by remember { mutableStateOf(if (prefs.preferredServer != "auto") prefs.preferredServer else "Auto (Best Speed)") }
    var selectedLanguage by remember { mutableStateOf(if (prefs.preferredLanguage != "default") prefs.preferredLanguage else "Default (Multi-Lang)") }
    var includeSubtitles by remember { mutableStateOf(prefs.includeSubtitles) }
    var rememberChoice by remember { mutableStateOf(prefs.rememberChoice) }

    val qualityOptions = listOf("1080p", "720p", "480p")
    val estimatedSizes = mapOf(
        "1080p" to "~1.4 GB",
        "720p" to "~750 MB",
        "480p" to "~380 MB",
    )
    val serverOptions = listOf("Auto (Best Speed)", "Nitro [Multi-Lang]", "Mahalala [Multi-Lang]", "CastVid", "Citadel")
    val languageOptions = listOf("Default (Multi-Lang)", "Hindi", "English", "Tamil", "Telugu")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Download Options",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = if (!subtitle.isNullOrBlank()) "$title • $subtitle" else title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider()

                // Quality selector with estimated size
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Quality & Estimated Size",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        qualityOptions.forEach { quality ->
                            val isSelected = selectedQuality == quality
                            val estimated = estimatedSizes[quality] ?: ""
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedQuality = quality },
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        text = quality,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = estimated,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    )
                                }
                            }
                        }
                    }
                }

                // Server / Source selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Server / Source",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        serverOptions.take(3).forEach { server ->
                            val isSelected = selectedServer == server
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.weight(1f).clickable { selectedServer = server },
                            ) {
                                Text(
                                    text = server.substringBefore(" ["),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(8.dp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }

                // Audio Language selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Preferred Audio",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        languageOptions.take(3).forEach { lang ->
                            val isSelected = selectedLanguage == lang
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.weight(1f).clickable { selectedLanguage = lang },
                            ) {
                                Text(
                                    text = lang.substringBefore(" ("),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(8.dp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }

                // Options: Include Subtitles & Remember Choice
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { includeSubtitles = !includeSubtitles },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = includeSubtitles,
                            onCheckedChange = { includeSubtitles = it },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Download Subtitles",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { rememberChoice = !rememberChoice },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = rememberChoice,
                            onCheckedChange = { rememberChoice = it },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Remember Choice",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "Use these settings automatically next time (can change in Settings)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (rememberChoice) {
                                DownloadPreferencesRepository.updatePreferences(
                                    quality = selectedQuality,
                                    language = selectedLanguage,
                                    includeSubtitles = includeSubtitles,
                                    rememberChoice = true,
                                )
                            }
                            onConfirm(selectedQuality, selectedServer, selectedLanguage, includeSubtitles, rememberChoice)
                        },
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download")
                    }
                }
            }
        }
    }
}
