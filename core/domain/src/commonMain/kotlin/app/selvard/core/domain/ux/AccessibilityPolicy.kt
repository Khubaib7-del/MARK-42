package app.selvard.core.domain.ux

import app.selvard.core.domain.risk.SecurityPosture
import kotlin.math.pow

/**
 * Phase 9 accessibility contract, enforced by tests.
 *
 * - Touch targets: Material 3 enforces 48dp minimum interactive size by
 *   default; the app must never disable that enforcement.
 * - Contrast: WCAG 2.1 AA for text pairs the theme actually renders
 *   ([TEXT_CONTRAST_PAIRS]); Stillwater Teal passes large-text/graphics
 *   only, so small body/label text must never render in teal.
 * - Status: posture is always words ([postureLabel]), never color alone.
 * - Motion: the app ships no auto-playing animation; transitions are instant.
 */
object AccessibilityPolicy {

    const val MIN_TOUCH_TARGET_DP: Int = 48

    /** No auto-playing animation ships in the app; transitions are instant. */
    const val NO_AUTO_ANIMATION: Boolean = true

    /** Minimum ratio for normal text; large text and graphics use [LARGE_TEXT_MIN_RATIO]. */
    const val TEXT_MIN_RATIO: Double = 4.5
    const val LARGE_TEXT_MIN_RATIO: Double = 3.0

    data class ContrastPair(val name: String, val foreground: Long, val background: Long, val minRatio: Double)

    /** Every text pair the light theme renders, with the ratio each must meet. */
    val TEXT_CONTRAST_PAIRS: List<ContrastPair> = listOf(
        ContrastPair("ink on mist", BrandPalette.INK, BrandPalette.MIST, TEXT_MIN_RATIO),
        ContrastPair("ink on surface", BrandPalette.INK, BrandPalette.SURFACE_WHITE, TEXT_MIN_RATIO),
        ContrastPair("surface on ink", BrandPalette.SURFACE_WHITE, BrandPalette.INK, TEXT_MIN_RATIO),
        ContrastPair("slate on surface", BrandPalette.SLATE, BrandPalette.SURFACE_WHITE, TEXT_MIN_RATIO),
        ContrastPair("slate on mist", BrandPalette.SLATE, BrandPalette.MIST, TEXT_MIN_RATIO),
        ContrastPair("slate on container", BrandPalette.SLATE, BrandPalette.SLATE_CONTAINER, TEXT_MIN_RATIO),
        ContrastPair("ink on container", BrandPalette.INK, BrandPalette.SLATE_CONTAINER, TEXT_MIN_RATIO),
        // Teal is large-text/graphics only (below 4.5 on light surfaces by design).
        ContrastPair("teal large-only on surface", BrandPalette.STILLWATER_TEAL, BrandPalette.SURFACE_WHITE, LARGE_TEXT_MIN_RATIO),
        ContrastPair("teal large-only on mist", BrandPalette.STILLWATER_TEAL, BrandPalette.MIST, LARGE_TEXT_MIN_RATIO),
    )

    /** WCAG 2.1 contrast ratio of two opaque ARGB colors. */
    fun contrastRatio(foreground: Long, background: Long): Double {
        val fg = relativeLuminance(foreground)
        val bg = relativeLuminance(background)
        val lighter = maxOf(fg, bg)
        val darker = minOf(fg, bg)
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun relativeLuminance(argb: Long): Double {
        fun channel(bits: Int): Double {
            val s = ((argb shr bits) and 0xFF) / 255.0
            return if (s <= 0.03928) s / 12.92 else ((s + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(16) + 0.7152 * channel(8) + 0.0722 * channel(0)
    }

    /**
     * Color-independent posture label. UI must render these words alongside
     * any color or icon; all five are distinct so meaning never rides on hue.
     */
    fun postureLabel(posture: SecurityPosture): String = when (posture) {
        SecurityPosture.NORMAL -> "Normal — no recorded signals above low severity"
        SecurityPosture.ATTENTION_REQUIRED -> "Attention required — medium-severity signals recorded"
        SecurityPosture.ELEVATED_RISK -> "Elevated risk — high-severity signals recorded"
        SecurityPosture.HIGH_RISK -> "High risk — multiple high-severity signals recorded"
        SecurityPosture.CRITICAL -> "Critical — critical-severity signals recorded"
    }
}
