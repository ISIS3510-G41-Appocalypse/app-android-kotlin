package com.gn41.appandroidkotlin.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AutumnEmber,
    secondary = DarkCyan,
    tertiary = CoolSteel,
    background = PrussianBlue,
    surface = PrussianBlue,

    onPrimary = BrightSnow,
    onSecondary = BrightSnow,
    onTertiary = BrightSnow,
    onBackground = BrightSnow,
    onSurface = CoolSteel
)
private val LightColorScheme = lightColorScheme(

    primary = AutumnEmber,
    secondary = DarkCyan,
    tertiary = CoolSteel,

    background = BrightSnow,
    surface = BrightSnow,

    onPrimary = BrightSnow,
    onSecondary = BrightSnow,
    onTertiary = PrussianBlue,

    onBackground = PrussianBlue,
    onSurface = PrussianBlue
)

@Composable
fun AppAndroidKotlinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
