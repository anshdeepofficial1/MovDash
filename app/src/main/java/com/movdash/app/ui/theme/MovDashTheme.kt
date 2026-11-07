package com.movdash.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MovDashColors = darkColorScheme(
    primary = Color(0xFFF8FAFC),
    onPrimary = Color(0xFF06080E),
    secondary = Color(0xFFCBD5E1),
    background = Color(0xFF06080E),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF0E131E),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF17202E),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF2A3649)
)

@Composable
fun MovDashTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = MovDashColors, content = content)
}
