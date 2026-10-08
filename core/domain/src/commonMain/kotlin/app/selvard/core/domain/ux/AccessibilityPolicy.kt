package app.selvard.core.domain.ux

import app.selvard.core.domain.risk.SecurityPosture
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * Accessibility contract, enforced by tests.
 *
 * - Touch targets: Material 3 enforces 48dp minimum interactive size by
 *   default; the app must never disable that enforcement.
 * - Contrast: WCAG 2.1 AA for every text pair the dark theme renders
 *   ([TEXT_CONTRAST_PAIRS]), including status chips on their own tinted fill.
 * - Status: posture and every status is words plus an icon, never color alone.
 * - Motion: nothing loops or plays continuously (battery and calm). Motion is
 *   limited to one-shot entrances and state transitions, and all of it
 *   collapses to instant when the system animation scale is zero.
 */
object AccessibilityPolicy {

    const val MIN_TOUCH_TARGET_DP: Int = 48

    /** No infinite/looping animation ships in the app. */
    const val NO_LOOPING_ANIMATION: Boolean = true

    /** Every one-shot transition must honor the system "remove animations" setting. */
    const val HONORS_REDUCED_MOTION: Boolean = true

    /** Alpha of the tone tint behind a status chip; contrast is measured on this blend. */
    const val CHIP_TINT_ALPHA: Double = 0.14

    /** Minimum ratio for normal text; large text and graphics use [LARGE_TEXT_MIN_RATIO]. */
    const val TEXT_MIN_RATIO: Double = 4.5
    const val LARGE_TEXT_MIN_RATIO: Double = 3.0

    data class ContrastPair(val name: String, val foreground: Long, val background: Long, val minRatio: Double)

    private fun chipPair(name: String, tone: Long) =
        ContrastPair(
            "$name chip on glass",
            tone,
            blend(tone, BrandPalette.GLASS, CHIP_TINT_ALPHA),
            TEXT_MIN_RATIO,
        )

    /** Every text pair the dark theme renders, with the ratio each must meet. */
    val TEXT_CONTRAST_PAIRS: List<ContrastPair> = listOf(
        ContrastPair("off-white on deep ink", BrandPalette.OFF_WHITE, BrandPalette.DEEP_INK, TEXT_MIN_RATIO),
        ContrastPair("off-white on glass", BrandPalette.OFF_WHITE, BrandPalette.GLASS, TEXT_MIN_RATIO),
        ContrastPair("off-white on raised glass", BrandPalette.OFF_WHITE, BrandPalette.GLASS_RAISED, TEXT_MIN_RATIO),
        ContrastPair("slate on deep ink", BrandPalette.SLATE_LIGHT, BrandPalette.DEEP_INK, TEXT_MIN_RATIO),
        ContrastPair("slate on glass", BrandPalette.SLATE_LIGHT, BrandPalette.GLASS, TEXT_MIN_RATIO),
        ContrastPair("slate on raised glass", BrandPalette.SLATE_LIGHT, BrandPalette.GLASS_RAISED, TEXT_MIN_RATIO),
        ContrastPair("sage on glass", BrandPalette.SAGE, BrandPalette.GLASS, TEXT_MIN_RATIO),
        ContrastPair("white on verdant button", BrandPalette.SURFACE_WHITE, BrandPalette.VERDANT, TEXT_MIN_RATIO),
        ContrastPair("deep ink on signal green", BrandPalette.DEEP_INK, BrandPalette.SIGNAL_GREEN, TEXT_MIN_RATIO),
        ContrastPair("signal green on glass", BrandPalette.SIGNAL_GREEN, BrandPalette.GLASS, TEXT_MIN_RATIO),
        chipPair("positive", BrandPalette.SIGNAL_GREEN),
        chipPair("neutral", BrandPalette.SLATE_LIGHT),
        chipPair("attention", BrandPalette.AMBER),
        chipPair("danger", BrandPalette.CORAL),
        // Logo tile: ink mark details on the light tile the real logo always sits on.
        ContrastPair("ink on mist", BrandPalette.INK, BrandPalette.MIST, TEXT_MIN_RATIO),
        // Teal is large-text/graphics only (below 4.5 on light surfaces by design).
        ContrastPair("teal large-only on surface", BrandPalette.STILLWATER_TEAL, BrandPalette.SURFACE_WHITE, LARGE_TEXT_MIN_RATIO),
    )

    /** WCAG 2.1 contrast ratio of two opaque ARGB colors. */
    fun contrastRatio(foreground: Long, background: Long): Double {
        val fg = relativeLuminance(foreground)
        val bg = relativeLuminance(background)
        val lighter = maxOf(fg, bg)
        val darker = minOf(fg, bg)
        return (lighter + 0.05) / (darker + 0.05)
    }

    /** Opaque result of painting [foreground] at [alpha] over [background]. */
    fun blend(foreground: Long, background: Long, alpha: Double): Long {
        fun channel(bits: Int): Long {
            val f = ((foreground shr bits) and 0xFF).toDouble()
            val b = ((background shr bits) and 0xFF).toDouble()
            return (f * alpha + b * (1.0 - alpha)).roundToLong().coerceIn(0, 255)
        }
        return 0xFF000000L or (channel(16) shl 16) or (channel(8) shl 8) or channel(0)
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

    /** Short headline word for the posture hero; the long label carries the detail. */
    fun postureHeadline(posture: SecurityPosture): String = when (posture) {
        SecurityPosture.NORMAL -> "Normal"
        SecurityPosture.ATTENTION_REQUIRED -> "Attention required"
        SecurityPosture.ELEVATED_RISK -> "Elevated risk"
        SecurityPosture.HIGH_RISK -> "High risk"
        SecurityPosture.CRITICAL -> "Critical"
    }
}
