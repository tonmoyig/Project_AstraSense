package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// AstraSense Telemetry and Mission Display Typography
val Typography = Typography(
  displayLarge = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Black,
    fontSize = 34.sp,
    lineHeight = 38.sp,
    letterSpacing = 1.2.sp
  ),
  displayMedium = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 26.sp,
    lineHeight = 30.sp,
    letterSpacing = 0.8.sp
  ),
  headlineLarge = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
    lineHeight = 26.sp,
    letterSpacing = 0.5.sp
  ),
  headlineMedium = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.25.sp
  ),
  titleLarge = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Bold,
    fontSize = 16.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.2.sp
  ),
  titleMedium = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.15.sp
  ),
  bodyLarge = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.2.sp
  ),
  bodyMedium = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.2.sp
  ),
  labelLarge = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Bold,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 1.0.sp
  ),
  labelMedium = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Medium,
    fontSize = 10.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.8.sp
  ),
  labelSmall = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Normal,
    fontSize = 9.sp,
    lineHeight = 12.sp,
    letterSpacing = 0.6.sp
  )
)
