package com.movdash.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MovDashColors = darkColorScheme(
    primary = Color(0xFFE9B96E),
    onPrimary = Color(0xFF21170A),
    secondary = Color(0xFFB8C3D6),
    background = Color(0xFF090B10),
    onBackground = Color(0xFFF4F5F7),
    surface = Color(0xFF111722),
    onSurface = Color(0xFFF4F5F7),
    surfaceVariant = Color(0xFF1B2431),
    onSurfaceVariant = Color(0xFFB2BBC9),
    outline = Color(0xFF354152)
)

@Composable
fun MovDashTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = MovDashColors, content = content)
}
