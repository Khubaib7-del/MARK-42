package app.selvard.core.domain.ux

/**
 * Single source of truth for the brand palette (docs/brand/BRAND_IDENTITY.md §4).
 * Android Theme references these; [AccessibilityPolicy] tests their contrast.
 * Values are opaque ARGB longs.
 */
object BrandPalette {
    const val INK: Long = 0xFF101A2E
    const val MIST: Long = 0xFFF6F8FB
    const val SLATE: Long = 0xFF5A6A7E
    const val STILLWATER_TEAL: Long = 0xFF3E8E9E
    const val SURFACE_WHITE: Long = 0xFFFFFFFF
    const val SLATE_CONTAINER: Long = 0xFFE8EDF4
}
