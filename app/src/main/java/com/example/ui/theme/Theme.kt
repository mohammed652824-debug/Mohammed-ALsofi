package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = SaudiGreenDarkPrimary,
    secondary = SkyAccent,
    tertiary = WarmGold,
    background = DeepSpaceBg,
    surface = DeepNavySurface,
    onPrimary = SlateDarkSecondary,
    onSecondary = SlateDarkSecondary,
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SaudiGreenPrimary,
    secondary = SlateDarkSecondary,
    tertiary = CustomBlueAccent,
    background = SlateLightBg,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = SlateDarkSecondary,
    onSurface = SlateDarkSecondary,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Custom branded theme is prioritized
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
