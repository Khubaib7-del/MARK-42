package app.selvard.core.domain.privacy

import app.selvard.core.domain.DisclosureState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PrivacyCoverageTest {

    @Test
    fun everyAreaIsListedExactlyOnce() {
        val areas = PrivacyCoverage.entries.map { it.area }
        assertEquals(PrivacyArea.entries.toSet(), areas.toSet())
        assertEquals(areas.size, areas.toSet().size)
    }

    @Test
    fun monitoredAreasNameTheirSource() {
        PrivacyCoverage.entries
            .filter { it.state == DisclosureState.DETECTED }
            .forEach { assertTrue(!it.coveredBy.isNullOrBlank(), "no source for ${it.area}") }
    }

    @Test
    fun unmonitoredAreasStateWhy() {
        val unmonitored = PrivacyCoverage.entries.filter {
            it.state == DisclosureState.NOT_MONITORED || it.state == DisclosureState.OS_LIMITATION
        }
        assertTrue(unmonitored.isNotEmpty(), "the honesty matrix must contain unmonitored areas")
        unmonitored.forEach { assertTrue(it.note.isNotBlank()) }
    }

    @Test
    fun realtimeSensorUseStaysNotMonitored() {
        // The roadmap's hardest honesty rule: never claim live sensor visibility.
        val entry = PrivacyCoverage.entryFor(PrivacyArea.REALTIME_SENSOR_USE)
        assertEquals(DisclosureState.NOT_MONITORED, entry.state)
    }

    @Test
    fun detectedEntriesRequireASource() {
        assertFailsWith<IllegalArgumentException> {
            CoverageEntry(PrivacyArea.BOOT_EVENTS, DisclosureState.DETECTED, null, "note")
        }
    }
}
