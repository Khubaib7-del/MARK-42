package app.selvard.core.domain.privacy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class BootHistoryTest {

    @Test
    fun countsOnlyBootsInsideWindow() {
        val now = 40L * BootHistory.MILLIS_PER_DAY
        val records = listOf(
            BootRecord(now - 1L),
            BootRecord(now - 6L * BootHistory.MILLIS_PER_DAY),
            BootRecord(now - 30L * BootHistory.MILLIS_PER_DAY),
        )
        val summary = BootHistory.summarize(records, now)
        assertEquals(2, summary.bootCount)
        assertEquals(now - 1L, summary.mostRecentBootMillis)
        assertTrue(summary.summaryLine().contains("2 boot(s)"))
    }

    @Test
    fun emptyHistorySaysSoPlainly() {
        val summary = BootHistory.summarize(emptyList(), 1_000L)
        assertEquals(0, summary.bootCount)
        assertTrue(summary.summaryLine().startsWith("No boots recorded"))
    }

    @Test
    fun futureBootsAreExcluded() {
        val summary = BootHistory.summarize(listOf(BootRecord(9_999_999_999_999L)), 1_000L)
        assertEquals(0, summary.bootCount)
    }

    @Test
    fun guardsRejectBadInput() {
        assertFailsWith<IllegalArgumentException> { BootRecord(-1L) }
        assertFailsWith<IllegalArgumentException> { BootHistory.summarize(emptyList(), 0L, windowDays = 0) }
    }
}
