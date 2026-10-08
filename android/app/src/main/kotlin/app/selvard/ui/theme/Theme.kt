package app.selvard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import app.selvard.core.domain.ux.BrandPalette
import app.selvard.ui.design.SelvardShapes
import app.selvard.ui.design.SelvardTypography

// Legacy light-palette aliases remain unchanged for logo tiles and existing callers.
val Ink = Color(BrandPalette.INK)
val Mist = Color(BrandPalette.MIST)
val Slate = Color(BrandPalette.SLATE)
val StillwaterTeal = Color(BrandPalette.STILLWATER_TEAL)
val SurfaceWhite = Color(BrandPalette.SURFACE_WHITE)
val SlateContainer = Color(BrandPalette.SLATE_CONTAINER)

val DeepInk = Color(BrandPalette.DEEP_INK)
val GlassSurface = Color(BrandPalette.GLASS)
val GlassRaised = Color(BrandPalette.GLASS_RAISED)
val OffWhite = Color(BrandPalette.OFF_WHITE)
val SlateLight = Color(BrandPalette.SLATE_LIGHT)
val SignalGreen = Color(BrandPalette.SIGNAL_GREEN)

private val SelvardDarkColors = darkColorScheme(
    primary = SignalGreen,
    onPrimary = DeepInk,
    primaryContainer = Color(BrandPalette.DEEP_FOREST),
    onPrimaryContainer = OffWhite,
    inversePrimary = Color(BrandPalette.VERDANT),
    secondary = Color(BrandPalette.SAGE),
    onSecondary = DeepInk,
    secondaryContainer = GlassRaised,
    onSecondaryContainer = Color(BrandPalette.SAGE),
    tertiary = Color(BrandPalette.AMBER),
    onTertiary = DeepInk,
    tertiaryContainer = GlassRaised,
    onTertiaryContainer = Color(BrandPalette.AMBER),
    background = DeepInk,
    onBackground = OffWhite,
    surface = GlassSurface,
    onSurface = OffWhite,
    surfaceVariant = GlassRaised,
    onSurfaceVariant = SlateLight,
    surfaceTint = Color.Transparent,
    inverseSurface = OffWhite,
    inverseOnSurface = DeepInk,
    error = Color(BrandPalette.CORAL),
    onError = DeepInk,
    errorContainer = GlassRaised,
    onErrorContainer = Color(BrandPalette.CORAL),
    outline = SlateLight,
    outlineVariant = OffWhite.copy(alpha = 0.10f),
    scrim = DeepInk,
    surfaceBright = GlassRaised,
    surfaceDim = DeepInk,
    surfaceContainerLowest = DeepInk,
    surfaceContainerLow = GlassSurface,
    surfaceContainer = GlassSurface,
    surfaceContainerHigh = GlassRaised,
    surfaceContainerHighest = GlassRaised,
)

@Composable
fun SelvardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SelvardDarkColors,
        typography = SelvardTypography,
        shapes = SelvardShapes,
        content = content,
    )
}
