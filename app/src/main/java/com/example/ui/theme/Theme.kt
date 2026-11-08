package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = EditorialInkDark,
    onPrimary = EditorialPaperDark,
    primaryContainer = EditorialSurfaceVariantDark,
    onPrimaryContainer = EditorialInkDark,
    secondary = EditorialTerracottaDark,
    onSecondary = EditorialPaperDark,
    secondaryContainer = EditorialSurfaceVariantDark,
    onSecondaryContainer = EditorialTerracottaDark,
    tertiary = EditorialSage,
    onTertiary = EditorialPaperDark,
    background = EditorialPaperDark,
    onBackground = EditorialInkDark,
    surface = EditorialSurfaceDark,
    onSurface = EditorialInkDark,
    surfaceVariant = EditorialSurfaceVariantDark,
    onSurfaceVariant = EditorialMutedDark,
    outline = EditorialBorderDark,
    outlineVariant = EditorialBorderDark.copy(alpha = 0.5f)
)

private val LightColorScheme = lightColorScheme(
    primary = EditorialInkLight,
    onPrimary = EditorialPaperLight,
    primaryContainer = EditorialSurfaceVariantLight,
    onPrimaryContainer = EditorialInkLight,
    secondary = EditorialTerracotta,
    onSecondary = EditorialPaperLight,
    secondaryContainer = EditorialSurfaceVariantLight,
    onSecondaryContainer = EditorialTerracotta,
    tertiary = EditorialSage,
    onTertiary = EditorialPaperLight,
    background = EditorialPaperLight,
    onBackground = EditorialInkLight,
    surface = EditorialSurfaceLight,
    onSurface = EditorialInkLight,
    surfaceVariant = EditorialSurfaceVariantLight,
    onSurfaceVariant = EditorialMutedLight,
    outline = EditorialBorderLight,
    outlineVariant = EditorialBorderLight.copy(alpha = 0.6f)
)

@Composable
fun AuraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AuraTypography,
        content = content
    )
}
