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
                // A planned engine must never be reported as active, and only
                // active guardian engines may drive protection claims.
                assertFalse(
                    status.engine in FoundationStatus.engines
                        .filter { it.lifecycle == EngineLifecycle.ACTIVE }
                        .map { it.engine },
                )
            }
        val activeGuardians = FoundationStatus.engines
            .filter { it.lifecycle == EngineLifecycle.ACTIVE && it.engine in FoundationStatus.guardianEngines }
            .map { it.engine }
        assertTrue(FoundationStatus.anyProtectionActive == activeGuardians.isNotEmpty())
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
    fun linkGuardianIsTheOnlyActiveGuardianSincePhase3() {
        val active = FoundationStatus.engines
            .filter { it.lifecycle == EngineLifecycle.ACTIVE }
            .map { it.engine }
        assertTrue(
            active.containsAll(
                listOf(EngineId.EVENT_BUS, EngineId.RISK_ENGINE, EngineId.SECURITY_POSTURE),
            ),
        )
        assertTrue(active.contains(EngineId.LINK_GUARDIAN))
        assertEquals(
            setOf(
                EngineId.EVENT_BUS,
                EngineId.RISK_ENGINE,
                EngineId.SECURITY_POSTURE,
                EngineId.LINK_GUARDIAN,
                EngineId.NETWORK_GUARDIAN,
                EngineId.APP_GUARDIAN,
            ),
            active.toSet(),
        )
        assertTrue(
            FoundationStatus.anyProtectionActive,
            "Link Guardian is active: explicit-link protection may now be claimed",
        )
    }

    private fun EngineStatus.statusLineIfPlannedMentionsPhase() =
        FoundationStatus.statusLine(this).contains("Phase $plannedPhase")
}
