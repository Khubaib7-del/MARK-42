package app.selvard.core.domain.incident

import app.selvard.core.domain.event.ActionTaken
import app.selvard.core.domain.event.AffectedAsset
import app.selvard.core.domain.event.AssetType
import app.selvard.core.domain.event.Confidence
import app.selvard.core.domain.event.EventCategory
import app.selvard.core.domain.event.PrivacyClass
import app.selvard.core.domain.event.SecurityEvent
import app.selvard.core.domain.event.Severity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class IncidentCorrelationTest {

    @Test
    fun emptyInputCorrelatesToNothing() {
        val result = CorrelationEngine.correlate(emptyList())
        assertTrue(result.incidents.isEmpty())
        assertTrue(result.uncorrelatedEventIds.isEmpty())
    }

    @Test
    fun singleEventIsUncorrelatedNeverAnIncident() {
        val result = CorrelationEngine.correlate(listOf(event("e1", 1_000L, "APP:com.example", Severity.INFO)))
        assertTrue(result.incidents.isEmpty())
        assertEquals(listOf("e1"), result.uncorrelatedEventIds)
    }

    @Test
    fun sameEntityWithinWindowFormsOneIncident() {
        val result = CorrelationEngine.correlate(
            listOf(
                event("e1", 1_000L, "APP:com.example", Severity.INFO),
                event("e2", 61_000L, "APP:com.example", Severity.HIGH),
            ),
        )
        assertEquals(1, result.incidents.size)
        val incident = result.incidents.first()
        assertEquals("incident-e1", incident.incidentId)
        assertEquals(listOf("e1", "e2"), incident.eventIds)
        assertEquals(Severity.HIGH, incident.maxSeverity)
        assertTrue(result.uncorrelatedEventIds.isEmpty())
    }

    @Test
    fun gapBeyondWindowSplitsRuns() {
        val window = 60_000L
        val result = CorrelationEngine.correlate(
            listOf(
                event("e1", 0L, "APP:com.example", Severity.INFO),
                event("e2", 30_000L, "APP:com.example", Severity.INFO),
                event("e3", 30_000L + window + 1, "APP:com.example", Severity.INFO),
            ),
            windowMillis = window,
        )
        assertEquals(1, result.incidents.size)
        assertEquals(listOf("e1", "e2"), result.incidents.first().eventIds)
        assertEquals(listOf("e3"), result.uncorrelatedEventIds)
    }

    @Test
    fun differentEntitiesNeverMerge() {
        val result = CorrelationEngine.correlate(
            listOf(
                event("e1", 1_000L, "APP:com.a", Severity.CRITICAL),
                event("e2", 2_000L, "APP:com.b", Severity.CRITICAL),
            ),
        )
        assertTrue(result.incidents.isEmpty())
        assertEquals(listOf("e1", "e2"), result.uncorrelatedEventIds)
    }

    @Test
    fun correlationIsOrderIndependent() {
        val events = listOf(
            event("e1", 1_000L, "APP:com.example", Severity.MEDIUM),
            event("e2", 2_000L, "APP:com.example", Severity.INFO),
            event("e3", 3_000L, "APP:com.other", Severity.INFO),
        )
        val forward = CorrelationEngine.correlate(events)
        val shuffled = CorrelationEngine.correlate(listOf(events[2], events[0], events[1]))
        assertEquals(forward, shuffled)
    }

    @Test
    fun summaryUsesTemporalLanguageAndPassesCausalLint() {
        val result = CorrelationEngine.correlate(
            listOf(
                event("e1", 0L, "APP:com.example", Severity.INFO),
                event("e2", 120_000L, "APP:com.example", Severity.INFO),
            ),
        )
        val line = result.incidents.first().summaryLine()
        assertTrue(line.contains("occurred shortly after"))
        CausalLanguage.check(line)
    }

    @Test
    fun causalLintRejectsCausalVerbs() {
        assertTrue(CausalLanguage.violations("the alert was caused by the update").isNotEmpty())
        assertTrue(CausalLanguage.violations("the block triggered a rescan").isNotEmpty())
        assertTrue(CausalLanguage.violations("the crash led to data loss").isNotEmpty())
        assertTrue(CausalLanguage.violations("failed due to a timeout").isNotEmpty())
        assertTrue(CausalLanguage.violations("two signals occurred shortly after each other").isEmpty())
        assertFailsWith<IllegalArgumentException> { CausalLanguage.check("root cause found") }
    }

    @Test
    fun incidentsSortBySeverityThenStartTime() {
        val result = CorrelationEngine.correlate(
            listOf(
                event("low1", 10_000L, "APP:com.low", Severity.INFO),
                event("low2", 11_000L, "APP:com.low", Severity.INFO),
                event("high1", 1_000L, "APP:com.high", Severity.CRITICAL),
                event("high2", 2_000L, "APP:com.high", Severity.CRITICAL),
            ),
        )
        assertEquals(listOf("incident-high1", "incident-low1"), result.incidents.map { it.incidentId })
    }

    @Test
    fun nonPositiveWindowRejected() {
        assertFailsWith<IllegalArgumentException> { CorrelationEngine.correlate(emptyList(), 0) }
    }

    private fun event(id: String, at: Long, assetRef: String, severity: Severity): SecurityEvent =
        SecurityEvent(
            eventId = id,
            timestampMillis = at,
            source = "test",
            category = EventCategory.APPLICATION,
            severity = severity,
            confidence = Confidence.HIGH,
            affectedAsset = AffectedAsset(AssetType.APP, assetRef),
            actionTaken = ActionTaken.RECORDED,
            privacyClassification = PrivacyClass.SENSITIVE,
        )
}
