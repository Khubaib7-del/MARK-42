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
    fun noAutoPlayingAnimationShips() {
        assertTrue(AccessibilityPolicy.NO_AUTO_ANIMATION)
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
