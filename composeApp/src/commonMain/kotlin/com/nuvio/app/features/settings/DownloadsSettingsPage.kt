package com.nuvio.app.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nuvio.app.features.downloads.DownloadPreferencesRepository
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DownloadsSettingsPage(
    isTablet: Boolean,
) {
    val prefs by DownloadPreferencesRepository.preferences.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(if (isTablet) 18.dp else 12.dp)) {
        SettingsSection(
            title = "Download Preferences",
            isTablet = isTablet,
        ) {
            SettingsGroup(isTablet = isTablet) {
                // Preferred Quality
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = if (isTablet) 20.dp else 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Default Download Quality",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (prefs.preferredQuality == "ask") "Ask every time" else prefs.preferredQuality,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf("ask" to "Ask", "1080p" to "1080p", "720p" to "720p", "480p" to "480p").forEach { (key, label) ->
                            val isSelected = prefs.preferredQuality == key
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f).clickable {
                                    DownloadPreferencesRepository.updatePreferences(quality = key)
                                },
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }

                SettingsGroupDivider(isTablet = isTablet)

                // Subtitles toggle
                SettingsSwitchRow(
                    title = "Download Subtitles",
                    description = "Automatically save available subtitles with offline media",
                    checked = prefs.includeSubtitles,
                    isTablet = isTablet,
                    onCheckedChange = { checked ->
                        DownloadPreferencesRepository.updatePreferences(includeSubtitles = checked)
                    },
                )

                SettingsGroupDivider(isTablet = isTablet)

                // Remember Choice & Reset
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = if (isTablet) 20.dp else 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Remembered Choice",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (prefs.rememberChoice) {
                            "Active preference: Quality ${prefs.preferredQuality}, Subtitles ${if (prefs.includeSubtitles) "On" else "Off"}"
                        } else {
                            "No choice remembered. The download options sheet appears every time."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (prefs.rememberChoice) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { DownloadPreferencesRepository.resetRememberedChoice() },
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset Remembered Choice")
                        }
                    }
                }
            }
        }
    }
}
