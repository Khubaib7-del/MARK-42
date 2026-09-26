package app.selvard.core.domain.integrity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DeviceIntegrityTest {

    @Test
    fun patchAgeCountsWholeDays() {
        // 2026-09-01 to 2026-09-26 12:00 UTC = 25 days.
        val now = PatchAgeTestClock.millis(2026, 9, 26, 12)
        assertEquals(25, PatchAge.ageDays("2026-09-01", now))
    }

    @Test
    fun unknownMalformedAndFuturePatchDatesAreUnknown() {
        val now = PatchAgeTestClock.millis(2026, 9, 26, 12)
        assertNull(PatchAge.ageDays(null, now))
        assertNull(PatchAge.ageDays("not-a-date", now))
        assertNull(PatchAge.ageDays("2026-13-40", now))
        assertNull(PatchAge.ageDays("2026-10-01", now))
        assertTrue(PatchAge.freshnessLine(null, null).contains("age unknown"))
    }

    @Test
    fun leapDayParses() {
        val now = PatchAgeTestClock.millis(2024, 3, 1, 0)
        assertEquals(1, PatchAge.ageDays("2024-02-29", now))
    }

    @Test
    fun snapshotRequiresReasonsAndPlayNote() {
        assertFailsWith<IllegalArgumentException> {
            snapshot(reasons = emptyList())
        }
        assertFailsWith<IllegalArgumentException> {
            snapshot(playIntegrityNote = "  ")
        }
    }

    private fun snapshot(
        reasons: List<String> = listOf("reason"),
        playIntegrityNote: String = IntegritySnapshot.PLAY_INTEGRITY_DEFERRED,
    ) = IntegritySnapshot(
        patchLevel = "2026-09-01",
        patchAgeDays = 25,
        verifiedBoot = VerifiedBootState.UNKNOWN,
        bootCount7d = 1,
        playIntegrity = PlayIntegrityState.NOT_SUPPORTED,
        playIntegrityNote = playIntegrityNote,
        reasons = reasons,
    )
}

private object PatchAgeTestClock {
    fun millis(year: Int, month: Int, day: Int, hour: Int): Long {
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        cal.clear()
        cal.set(year, month - 1, day, hour, 0, 0)
        return cal.timeInMillis
    }
}
