package dev.andock.ui.theme

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

private val DarkCocoaScheme = darkColorScheme(
    primary = Color(0xFFF5B0DE),
    onPrimary = Color(0xFF5A1552),
    primaryContainer = Color(0xFF772A6B),
    onPrimaryContainer = Color(0xFFFFD7F0),
    secondary = Color(0xFFE7BDAE),
    onSecondary = Color(0xFF482923),
    secondaryContainer = Color(0xFF614039),
    onSecondaryContainer = Color(0xFFFFDBD1),
    tertiary = Color(0xFFF2C987),
    onTertiary = Color(0xFF422C05),
    tertiaryContainer = Color(0xFF5C4318),
    onTertiaryContainer = Color(0xFFFFE5B5),
    background = Color(0xFF1B1018),
    onBackground = Color(0xFFF3DFED),
    surface = Color(0xFF251720),
    onSurface = Color(0xFFF3DFED),
    surfaceVariant = Color(0xFF40303D),
    onSurfaceVariant = Color(0xFFE3CBDC),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightCocoaScheme = lightColorScheme(
    primary = Color(0xFF8D367E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD7F0),
    onPrimaryContainer = Color(0xFF381030),
    secondary = Color(0xFF79554D),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDBD1),
    onSecondaryContainer = Color(0xFF2E150F),
    tertiary = Color(0xFF765B22),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE5B5),
    onTertiaryContainer = Color(0xFF271900),
    background = Color(0xFFFFF7FA),
    onBackground = Color(0xFF211820),
    surface = Color(0xFFFFF7FA),
    onSurface = Color(0xFF211820),
    surfaceVariant = Color(0xFFF1DEE9),
    onSurfaceVariant = Color(0xFF51434E)
)

@Composable
fun AndockTheme(
    useDynamicColor: Boolean,
    content: @Composable () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val colors = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        dark -> DarkCocoaScheme
        else -> LightCocoaScheme
    }
    MaterialTheme(colorScheme = colors, content = content)
}
