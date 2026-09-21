package app.selvard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand palette (docs/brand/BRAND_IDENTITY.md §4). Restrained, institutional, no gradients.
val Ink = Color(0xFF101A2E)
val Mist = Color(0xFFF6F8FB)
val Slate = Color(0xFF5A6A7E)
val StillwaterTeal = Color(0xFF3E8E9E)
val SurfaceWhite = Color(0xFFFFFFFF)
val SlateContainer = Color(0xFFE8EDF4)

@Composable
fun SelvardTheme(content: @Composable () -> Unit) {
    val colors = lightColorScheme(
        primary = Ink,
        onPrimary = SurfaceWhite,
        secondary = StillwaterTeal,
        onSecondary = SurfaceWhite,
        background = Mist,
        onBackground = Ink,
        surface = SurfaceWhite,
        onSurface = Ink,
        surfaceVariant = SlateContainer,
        onSurfaceVariant = Slate,
        outline = Slate,
    )
    MaterialTheme(
        colorScheme = colors,
        content = content,
    )
}
