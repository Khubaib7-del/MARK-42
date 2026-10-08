package app.selvard.core.domain.ux

import app.selvard.core.domain.risk.SecurityPosture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessibilityPolicyTest {

    @Test
    fun touchTargetFloorIs48dp() {
        assertEquals(48, AccessibilityPolicy.MIN_TOUCH_TARGET_DP)
    }

    @Test
    fun nothingLoopsAndMotionHonorsReducedMotion() {
        assertTrue(AccessibilityPolicy.NO_LOOPING_ANIMATION)
        assertTrue(AccessibilityPolicy.HONORS_REDUCED_MOTION)
    }

    @Test
    fun blendIsExactAtTheEndpoints() {
        val fg = BrandPalette.SIGNAL_GREEN
        val bg = BrandPalette.GLASS
        assertEquals(bg, AccessibilityPolicy.blend(fg, bg, 0.0))
        assertEquals(fg, AccessibilityPolicy.blend(fg, bg, 1.0))
    }

    @Test
    fun everyToneMeetsAaOnItsOwnChipTint() {
        listOf(BrandPalette.SIGNAL_GREEN, BrandPalette.SLATE_LIGHT, BrandPalette.AMBER, BrandPalette.CORAL).forEach { tone ->
            val chipFill = AccessibilityPolicy.blend(tone, BrandPalette.GLASS, AccessibilityPolicy.CHIP_TINT_ALPHA)
            val ratio = AccessibilityPolicy.contrastRatio(tone, chipFill)
            assertTrue(ratio >= AccessibilityPolicy.TEXT_MIN_RATIO, "tone ${tone.toString(16)} chip ratio $ratio")
        }
    }

    @Test
    fun postureHeadlinesAreDistinctAndNeverSafe() {
        val headlines = SecurityPosture.entries.map { AccessibilityPolicy.postureHeadline(it) }
        assertEquals(headlines.size, headlines.toSet().size)
        assertTrue(headlines.none { it.contains("safe", ignoreCase = true) })
    }

    @Test
    fun themeTextPairsMeetWcagAa() {
        AccessibilityPolicy.TEXT_CONTRAST_PAIRS.forEach { pair ->
            val ratio = AccessibilityPolicy.contrastRatio(pair.foreground, pair.background)
            assertTrue(
                ratio >= pair.minRatio,
                "${pair.name} ratio ${"%.2f".format(ratio)} below ${pair.minRatio}",
            )
        }
    }

    @Test
    fun contrastIsSymmetricAndWhiteOnWhiteFails() {
        val ink = BrandPalette.INK
        val mist = BrandPalette.MIST
        assertEquals(
            AccessibilityPolicy.contrastRatio(ink, mist),
            AccessibilityPolicy.contrastRatio(mist, ink),
            0.001,
        )
        assertTrue(
            AccessibilityPolicy.contrastRatio(BrandPalette.SURFACE_WHITE, BrandPalette.SURFACE_WHITE) < 1.5,
            "sanity: identical colors must not pass",
        )
    }

    @Test
    fun tealIsLargeTextOnlyByMeasurement() {
        val tealOnSurface = AccessibilityPolicy.contrastRatio(
            BrandPalette.STILLWATER_TEAL,
            BrandPalette.SURFACE_WHITE,
        )
        assertTrue(tealOnSurface >= AccessibilityPolicy.LARGE_TEXT_MIN_RATIO)
        // Documents the restriction: small body text must never render in teal.
        assertTrue(tealOnSurface < AccessibilityPolicy.TEXT_MIN_RATIO)
    }

    @Test
    fun postureLabelsAreDistinctWordsNeverColorAlone() {
        val labels = SecurityPosture.entries.map { AccessibilityPolicy.postureLabel(it) }
        assertTrue(labels.all { it.isNotBlank() })
        assertEquals(labels.size, labels.toSet().size)
        assertTrue(labels.none { it.equals("red", ignoreCase = true) || it.equals("green", ignoreCase = true) })
    }
}
