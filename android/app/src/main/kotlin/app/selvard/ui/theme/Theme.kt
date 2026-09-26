package app.selvard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import app.selvard.core.domain.ux.BrandPalette

/**
 * Brand palette (docs/brand/BRAND_IDENTITY.md §4). Values live in
 * [BrandPalette] (single truth, contrast-tested); these aliases keep
 * existing references compiling. Teal renders large text and graphics
 * only — never small body/label text (AccessibilityPolicy).
 */
val Ink = Color(BrandPalette.INK)
val Mist = Color(BrandPalette.MIST)
val Slate = Color(BrandPalette.SLATE)
val StillwaterTeal = Color(BrandPalette.STILLWATER_TEAL)
val SurfaceWhite = Color(BrandPalette.SURFACE_WHITE)
val SlateContainer = Color(BrandPalette.SLATE_CONTAINER)

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
