package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val UniMultDarkColorScheme = darkColorScheme(
  primary = RubyPrimary,
  onPrimary = Color.White,
  primaryContainer = RubyPrimaryVariant,
  onPrimaryContainer = Color.White,
  secondary = AccentGold,
  onSecondary = Color.Black,
  secondaryContainer = BurgundyGlow,
  onSecondaryContainer = Color.White,
  tertiary = AccentGoldGlow,
  onTertiary = Color.Black,
  background = BurgundyDeep,
  onBackground = TextPrimary,
  surface = DarkSurface,
  onSurface = TextPrimary,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  error = Color(0xFFCF6679),
  onError = Color.Black
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false, // Preserve UniMult signature brand colors
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = UniMultDarkColorScheme,
    typography = Typography,
    content = content
  )
}
