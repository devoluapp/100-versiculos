package blog.robertotavares.cemversiculos.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Natureza (Sage/Green)
private val NaturezaLight = lightColorScheme(
    primary = Color(0xFF4CE699),
    onPrimary = Color(0xFF112119),
    background = Color(0xFFF6F8F7),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
)

private val NaturezaDark = darkColorScheme(
    primary = Color(0xFF4CE699),
    onPrimary = Color(0xFF112119),
    background = Color(0xFF112119),
    surface = Color(0xFF1E293B),
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9)
)

// Oceano (Blue/Teal)
private val OceanoLight = lightColorScheme(
    primary = Color(0xFF4CACE6),
    onPrimary = Color(0xFF0F1B21),
    background = Color(0xFFF6F7F8),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
)

private val OceanoDark = darkColorScheme(
    primary = Color(0xFF4CACE6),
    onPrimary = Color(0xFF0F1B21),
    background = Color(0xFF0F1B21),
    surface = Color(0xFF1E293B),
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9)
)

// Crepúsculo (Lavender/Purple)
private val CrepusculoLight = lightColorScheme(
    primary = Color(0xFFA594F9),
    onPrimary = Color(0xFF1A1621),
    background = Color(0xFFF8F6F9),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
)

private val CrepusculoDark = darkColorScheme(
    primary = Color(0xFFA594F9),
    onPrimary = Color(0xFF1A1621),
    background = Color(0xFF1A1621),
    surface = Color(0xFF1E293B),
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9)
)

// Areia (Beige/Warm)
private val AreiaLight = lightColorScheme(
    primary = Color(0xFFE6C04C),
    onPrimary = Color(0xFF211D0F),
    background = Color(0xFFF8F7F6),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
)

private val AreiaDark = darkColorScheme(
    primary = Color(0xFFE6C04C),
    onPrimary = Color(0xFF211D0F),
    background = Color(0xFF211D0F),
    surface = Color(0xFF1E293B),
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9)
)

@Composable
fun BaseTheme(
    themeName: String = "Areia",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeName) {
        "Oceano" -> if (darkTheme) OceanoDark else OceanoLight
        "Crepúsculo" -> if (darkTheme) CrepusculoDark else CrepusculoLight
        "Natureza" -> if (darkTheme) NaturezaDark else NaturezaLight
        else -> if (darkTheme) AreiaDark else AreiaLight

    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
