package app.selvard.core.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FoundationStatusTest {

    @Test
    fun everyEngineIsListedExactlyOnce() {
        val ids = FoundationStatus.engines.map { it.engine }
        assertEquals(EngineId.entries.toSet(), ids.toSet())
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun plannedEnginesNeverClaimProtection() {
        FoundationStatus.engines
            .filter { it.lifecycle == EngineLifecycle.PLANNED }
            .forEach { status ->
                assertTrue(status.statusLineIfPlannedMentionsPhase())
                assertFalse(
                    FoundationStatus.anyProtectionActive,
                    "no engine may report active protection before it is built",
                )
            }
    }

    @Test
    fun statusLinesAreHonestPerLifecycle() {
        val planned = EngineStatus(EngineId.LINK_GUARDIAN, EngineLifecycle.PLANNED, 3)
        assertEquals("Not yet implemented — planned for Phase 3", FoundationStatus.statusLine(planned))

        val inProgress = EngineStatus(EngineId.RISK_ENGINE, EngineLifecycle.IN_PROGRESS, 2)
        assertEquals("In development (Phase 2)", FoundationStatus.statusLine(inProgress))

        val active = EngineStatus(EngineId.EVENT_BUS, EngineLifecycle.ACTIVE, 2)
        assertEquals("Active", FoundationStatus.statusLine(active))
    }

    @Test
    fun phasesAreWithinTheRoadmapRange() {
        FoundationStatus.engines.forEach { status ->
            assertTrue(status.plannedPhase in 2..9, "phase out of roadmap range for ${status.engine}")
        }
    }

    @Test
    fun coreEnginesAreActiveButNoGuardianClaimsProtection() {
        val active = FoundationStatus.engines
            .filter { it.lifecycle == EngineLifecycle.ACTIVE }
            .map { it.engine }
        assertTrue(
            active.containsAll(
                listOf(EngineId.EVENT_BUS, EngineId.RISK_ENGINE, EngineId.SECURITY_POSTURE),
            ),
        )
        assertFalse(
            FoundationStatus.anyProtectionActive,
            "no guardian engine is implemented yet; the product must not claim protection",
        )
    }

    private fun EngineStatus.statusLineIfPlannedMentionsPhase() =
        FoundationStatus.statusLine(this).contains("Phase $plannedPhase")
}
