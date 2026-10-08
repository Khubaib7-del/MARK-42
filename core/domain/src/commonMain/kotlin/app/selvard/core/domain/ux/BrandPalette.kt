package app.selvard.core.domain.ux

/**
 * Single source of truth for the brand palette (docs/brand/BRAND_IDENTITY.md §4,
 * docs/brand/UI_DIRECTION.md). Android Theme references these; [AccessibilityPolicy]
 * tests their contrast. Values are opaque ARGB longs.
 */
object BrandPalette {
    // Legacy light-surface values: still used for the logo tile and contrast tests.
    const val INK: Long = 0xFF101A2E
    const val MIST: Long = 0xFFF6F8FB
    const val SLATE: Long = 0xFF5A6A7E
    const val STILLWATER_TEAL: Long = 0xFF3E8E9E
    const val SURFACE_WHITE: Long = 0xFFFFFFFF
    const val SLATE_CONTAINER: Long = 0xFFE8EDF4

    // Dark-first UI surfaces. Glass fills are the flattened result of white at
    // ~5.5% / ~9% over DEEP_INK, so contrast can be measured on opaque colors.
    const val DEEP_INK: Long = 0xFF0C1322
    const val GLASS: Long = 0xFF19202E
    const val GLASS_RAISED: Long = 0xFF212938

    const val OFF_WHITE: Long = 0xFFEDF1F7
    const val SLATE_LIGHT: Long = 0xFF9FB0C6

    const val VERDANT: Long = 0xFF0A6C60
    const val SIGNAL_GREEN: Long = 0xFF54E454
    const val SAGE: Long = 0xFFA3C9A8
    const val DEEP_FOREST: Long = 0xFF2F5D50
    const val MUTED_GREEN: Long = 0xFF5A8F6E
    const val CREAM: Long = 0xFFE9F5DB

    // Semantic tones, chosen to hold AA on GLASS and on their own 14% tint.
    const val AMBER: Long = 0xFFE2B93B
    const val CORAL: Long = 0xFFF28C78
}
