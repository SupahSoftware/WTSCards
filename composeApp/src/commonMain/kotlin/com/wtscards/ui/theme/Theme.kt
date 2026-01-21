package com.wtscards.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// --- Base Backgrounds ---
val bgPrimary = Color(0xFF121212)
val bgSecondary = Color(0xFF1E1E1E)
val bgSurface = Color(0xFF242424)
val bgHover = Color(0xFF2A2A2A)

// --- Text Colors ---
val textPrimary = Color(0xFFE0E0E0)
val textSecondary = Color(0xFFB0B0B0)
val textTertiary = Color(0xFF7A7A7A)
val textPlaceholder = Color(0xFF999999)

// --- Accent & Interactive ---
val accentPrimary = Color(0xFF4FC3F7)
val accentSecondary = Color(0xFF26A69A)
val accentFocus = Color(0xFF2979FF)

// --- Semantic Colors ---
val successColor = Color(0xFF4CAF50)
val errorColor = Color(0xFFF44336)
val warningColor = Color(0xFFFFB300)
val infoColor = Color(0xFF29B6F6)

// --- Borders & Dividers ---
val borderDivider = Color(0xFF333333)
val borderInput = Color(0xFF555555)

// --- Toolbars & Dialogs ---
val toolbarBg = Color(0xFF1B1B1B)
val dialogBg = Color(0xFF242424)
val modalOverlay = Color(0x99000000)

private val WTSColorScheme = darkColorScheme(
    primary = accentPrimary,
    secondary = accentSecondary,
    tertiary = accentFocus,
    background = bgPrimary,
    surface = bgSurface,
    surfaceVariant = bgSecondary,
    onPrimary = bgPrimary,
    onSecondary = bgPrimary,
    onTertiary = bgPrimary,
    onBackground = textPrimary,
    onSurface = textPrimary,
    onSurfaceVariant = textSecondary,
    error = errorColor,
    onError = textPrimary,
    outline = borderInput,
    outlineVariant = borderDivider
)

@Composable
fun WTSCardsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WTSColorScheme,
        content = content
    )
}
