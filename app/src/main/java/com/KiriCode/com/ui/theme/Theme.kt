package com.KiriCode.com.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Cyberpunk Light Scheme
val CyberpunkLight = lightColorScheme(
    primary = Color(0xFF8A2BE2),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEBE1FF),
    onPrimaryContainer = Color(0xFF2B0066),
    secondary = Color(0xFFFF007F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFE5F0),
    onSecondaryContainer = Color(0xFF4A0020),
    tertiary = Color(0xFF009EAD),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD2F9FF),
    onTertiaryContainer = Color(0xFF002024),
    background = Color(0xFFFAF9FF),
    onBackground = Color(0xFF100E26),
    surface = Color(0xFFF4F1FD),
    onSurface = Color(0xFF100E26),
    surfaceVariant = Color(0xFFE6E1F4),
    onSurfaceVariant = Color(0xFF48445E),
    outline = Color(0xFF79748E),
    outlineVariant = Color(0xFFC9C4DC),
    scrim = Color(0xFF000000)
)

// Cyberpunk Dark Scheme
val CyberpunkDark = darkColorScheme(
    primary = Color(0xFF00E5FF),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF0A0718),
    onPrimaryContainer = Color(0xFFF0EDFF),
    secondary = Color(0xFFFF007F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF1B153B),
    onSecondaryContainer = Color(0xFFF0EDFF),
    tertiary = Color(0xFF8A2BE2),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF352975),
    onTertiaryContainer = Color(0xFFF0EDFF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0A0718),
    onBackground = Color(0xFFF0EDFF),
    surface = Color(0xFF120E28),
    onSurface = Color(0xFFF0EDFF),
    surfaceVariant = Color(0xFF1B153B),
    onSurfaceVariant = Color(0xFF8C82B5),
    outline = Color(0xFF352975),
    outlineVariant = Color(0xFF120E28),
    scrim = Color(0xFF000000)
)

@Composable
fun CodeQuestTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val targetScheme = if (darkTheme) CyberpunkDark else CyberpunkLight
    val animatedScheme = animateColorScheme(targetScheme)

    MaterialTheme(
        colorScheme = animatedScheme,
        content = content
    )
}

@Composable
fun animateColorScheme(targetScheme: ColorScheme): ColorScheme {
    val animSpec = tween<Color>(durationMillis = 500)
    return ColorScheme(
        primary = animateColorAsState(targetScheme.primary, animSpec, label = "primary").value,
        onPrimary = animateColorAsState(targetScheme.onPrimary, animSpec, label = "onPrimary").value,
        primaryContainer = animateColorAsState(targetScheme.primaryContainer, animSpec, label = "primaryContainer").value,
        onPrimaryContainer = animateColorAsState(targetScheme.onPrimaryContainer, animSpec, label = "onPrimaryContainer").value,
        inversePrimary = animateColorAsState(targetScheme.inversePrimary, animSpec, label = "inversePrimary").value,
        secondary = animateColorAsState(targetScheme.secondary, animSpec, label = "secondary").value,
        onSecondary = animateColorAsState(targetScheme.onSecondary, animSpec, label = "onSecondary").value,
        secondaryContainer = animateColorAsState(targetScheme.secondaryContainer, animSpec, label = "secondaryContainer").value,
        onSecondaryContainer = animateColorAsState(targetScheme.onSecondaryContainer, animSpec, label = "onSecondaryContainer").value,
        tertiary = animateColorAsState(targetScheme.tertiary, animSpec, label = "tertiary").value,
        onTertiary = animateColorAsState(targetScheme.onTertiary, animSpec, label = "onTertiary").value,
        tertiaryContainer = animateColorAsState(targetScheme.tertiaryContainer, animSpec, label = "tertiaryContainer").value,
        onTertiaryContainer = animateColorAsState(targetScheme.onTertiaryContainer, animSpec, label = "onTertiaryContainer").value,
        background = animateColorAsState(targetScheme.background, animSpec, label = "background").value,
        onBackground = animateColorAsState(targetScheme.onBackground, animSpec, label = "onBackground").value,
        surface = animateColorAsState(targetScheme.surface, animSpec, label = "surface").value,
        onSurface = animateColorAsState(targetScheme.onSurface, animSpec, label = "onSurface").value,
        surfaceVariant = animateColorAsState(targetScheme.surfaceVariant, animSpec, label = "surfaceVariant").value,
        onSurfaceVariant = animateColorAsState(targetScheme.onSurfaceVariant, animSpec, label = "onSurfaceVariant").value,
        surfaceTint = animateColorAsState(targetScheme.surfaceTint, animSpec, label = "surfaceTint").value,
        inverseSurface = animateColorAsState(targetScheme.inverseSurface, animSpec, label = "inverseSurface").value,
        inverseOnSurface = animateColorAsState(targetScheme.inverseOnSurface, animSpec, label = "inverseOnSurface").value,
        error = animateColorAsState(targetScheme.error, animSpec, label = "error").value,
        onError = animateColorAsState(targetScheme.onError, animSpec, label = "onError").value,
        errorContainer = animateColorAsState(targetScheme.errorContainer, animSpec, label = "errorContainer").value,
        onErrorContainer = animateColorAsState(targetScheme.onErrorContainer, animSpec, label = "onErrorContainer").value,
        outline = animateColorAsState(targetScheme.outline, animSpec, label = "outline").value,
        outlineVariant = animateColorAsState(targetScheme.outlineVariant, animSpec, label = "outlineVariant").value,
        scrim = animateColorAsState(targetScheme.scrim, animSpec, label = "scrim").value
    )
}
