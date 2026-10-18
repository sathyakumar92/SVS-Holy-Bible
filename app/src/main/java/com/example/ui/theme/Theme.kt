package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val SvsDarkColorScheme = darkColorScheme(
    primary = RichGold,
    onPrimary = DeepNavyBackground,
    primaryContainer = PrimaryRoyalBlue,
    onPrimaryContainer = BrightGoldHighlight,
    secondary = PremiumBlueAccent,
    onSecondary = PrimaryLightText,
    secondaryContainer = ElevatedBlueSurface,
    onSecondaryContainer = PrimaryLightText,
    tertiary = BrightGoldHighlight,
    onTertiary = DeepNavyBackground,
    background = DeepNavyBackground,
    onBackground = PrimaryLightText,
    surface = SecondaryNavySurface,
    onSurface = PrimaryLightText,
    surfaceVariant = ElevatedBlueSurface,
    onSurfaceVariant = SecondaryLightText,
    outline = DividerDark,
    outlineVariant = SecondaryNavySurface,
    error = ErrorRed,
    onError = PrimaryLightText
)

private val SvsLightColorScheme = lightColorScheme(
    primary = PrimaryRoyalBlue,
    onPrimary = LightSurface,
    primaryContainer = BrightGoldHighlight,
    onPrimaryContainer = PrimaryDarkText,
    secondary = PremiumBlueAccent,
    onSecondary = LightSurface,
    secondaryContainer = DividerLight,
    onSecondaryContainer = PrimaryDarkText,
    tertiary = RichGold,
    onTertiary = LightSurface,
    background = PrimaryLightBackground,
    onBackground = PrimaryDarkText,
    surface = LightSurface,
    onSurface = PrimaryDarkText,
    surfaceVariant = DividerLight,
    onSurfaceVariant = SecondaryDarkText,
    outline = DividerLight,
    outlineVariant = DividerLight,
    error = ErrorRed,
    onError = LightSurface
)

@Composable
fun SvsHolyBibleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SvsDarkColorScheme else SvsLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
