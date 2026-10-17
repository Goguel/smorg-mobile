package br.ufrn.smorg.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightColorScheme =
    lightColorScheme(
        primary = Color(0xFF00668B),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFC7E7FF),
        onPrimaryContainer = Color(0xFF001E2E),
        secondary = Color(0xFF4F616E),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD2E5F5),
        onSecondaryContainer = Color(0xFF0B1D29),
        tertiary = Color(0xFF63597C),
        onTertiary = Color(0xFFFFFFFF),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFFBFDFE),
        onBackground = Color(0xFF191C1E),
        surface = Color(0xFFFBFDFE),
        onSurface = Color(0xFF191C1E),
        surfaceVariant = Color(0xFFDDE3EA),
        onSurfaceVariant = Color(0xFF41474D),
        outline = Color(0xFF71787E),
    )

val DarkColorScheme =
    darkColorScheme(
        primary = Color(0xFF7CD0FF),
        onPrimary = Color(0xFF00344A),
        primaryContainer = Color(0xFF004C6A),
        onPrimaryContainer = Color(0xFFC7E7FF),
        secondary = Color(0xFFB6C9D8),
        onSecondary = Color(0xFF21323E),
        secondaryContainer = Color(0xFF374955),
        onSecondaryContainer = Color(0xFFD2E5F5),
        tertiary = Color(0xFFCDC0E9),
        onTertiary = Color(0xFF342B4B),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF191C1E),
        onBackground = Color(0xFFE1E2E5),
        surface = Color(0xFF191C1E),
        onSurface = Color(0xFFE1E2E5),
        surfaceVariant = Color(0xFF41474D),
        onSurfaceVariant = Color(0xFFC1C7CE),
        outline = Color(0xFF8B9198),
    )

@Composable
fun SmOrgTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
