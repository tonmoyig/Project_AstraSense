package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dark Cosmic Mode (Ultra-Deep Space Astronomical Navy Void)
private val DeepCosmicColorScheme = darkColorScheme(
  primary = NeonCyan,
  onPrimary = AstroVoid,
  primaryContainer = AstroNavyElevated,
  onPrimaryContainer = Color.White,
  secondary = BrightTeal,
  onSecondary = AstroVoid,
  secondaryContainer = Color(0xFF0B2538),
  onSecondaryContainer = BrightTeal,
  tertiary = StarlightBlue,
  onTertiary = AstroVoid,
  background = AstroVoid,
  onBackground = Color(0xFFF8FAFC),
  surface = AstroNavyCardGlass,
  onSurface = Color(0xFFF1F5F9),
  surfaceVariant = AstroNavySurface,
  onSurfaceVariant = Color(0xFF94A3B8),
  error = CriticalRed,
  onError = Color.White,
  outline = GlassBorderStroke
)

// Standard Flight Ops Mode (NASA Mission Control Deep Navy)
private val StandardOpsColorScheme = lightColorScheme(
  primary = Color(0xFF0284C7),
  onPrimary = Color.White,
  primaryContainer = Color(0xFF075985),
  onPrimaryContainer = Color(0xFFE0F2FE),
  secondary = Color(0xFF0D9488),
  onSecondary = Color.White,
  secondaryContainer = Color(0xFF115E59),
  onSecondaryContainer = Color(0xFFCCFBF1),
  tertiary = Color(0xFF2563EB),
  onTertiary = Color.White,
  background = OpsNavyBackground,
  onBackground = OpsTextPrimary,
  surface = OpsNavySurface,
  onSurface = OpsTextPrimary,
  surfaceVariant = Color(0xFF182A4D),
  onSurfaceVariant = OpsTextSecondary,
  error = CriticalRed,
  onError = Color.White,
  outline = OpsCardBorder
)

@Composable
fun AstraSenseTheme(
  isDarkMode: Boolean = true,
  content: @Composable () -> Unit
) {
  val colorScheme = if (isDarkMode) DeepCosmicColorScheme else StandardOpsColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
