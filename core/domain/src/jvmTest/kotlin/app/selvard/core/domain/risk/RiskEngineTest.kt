package app.selvard.core.domain.risk

import app.selvard.core.domain.event.Confidence
import app.selvard.core.domain.event.Severity
import app.selvard.core.domain.event.testEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RiskEngineTest {

    private val engine = RiskEngine()

    @Test
    fun assessmentIsDeterministicAndOrderIndependent() {
        val events = listOf(
            testEvent(eventId = "evt-1", severity = Severity.MEDIUM, confidence = Confidence.HIGH),
            testEvent(eventId = "evt-2", severity = Severity.CRITICAL, confidence = Confidence.MEDIUM),
            testEvent(eventId = "evt-3", severity = Severity.INFO, confidence = Confidence.LOW),
        )
        val reference = engine.assess(events)
        assertEquals(reference, engine.assess(events.reversed()))
        assertEquals(reference, engine.assess(events.shuffled()))
    }

    @Test
    fun noEventsIsNotASafetyClaim() {
        val assessment = engine.assess(emptyList())
        assertEquals(SecurityPosture.NORMAL, assessment.posture)
        assertTrue(
            assessment.reasons.any { it.contains("not evidence that the device is safe", ignoreCase = true) },
        )
    }

    @Test
    fun severityMapsToPosture() {
        assertEquals(
            SecurityPosture.CRITICAL,
            engine.assess(listOf(testEvent(severity = Severity.CRITICAL))).posture,
        )
        assertEquals(
            SecurityPosture.HIGH_RISK,
            engine.assess(listOf(testEvent(severity = Severity.HIGH))).posture,
        )
        assertEquals(
            SecurityPosture.ATTENTION_REQUIRED,
            engine.assess(listOf(testEvent(severity = Severity.MEDIUM))).posture,
        )
        assertEquals(
            SecurityPosture.NORMAL,
            engine.assess(listOf(testEvent(severity = Severity.INFO))).posture,
        )
    }

    @Test
    fun confidenceIsTheWeakestContributingConfidence() {
        val assessment = engine.assess(
            listOf(
                testEvent(eventId = "evt-1", severity = Severity.HIGH, confidence = Confidence.HIGH),
                testEvent(eventId = "evt-2", severity = Severity.HIGH, confidence = Confidence.LOW),
            ),
        )
        assertEquals(Confidence.LOW, assessment.confidence)
    }

    @Test
    fun onlyContributingEventsAreListedSortedBySeverityThenId() {
        val assessment = engine.assess(
            listOf(
                testEvent(eventId = "evt-m1", severity = Severity.MEDIUM),
                testEvent(eventId = "evt-c1", severity = Severity.CRITICAL),
                testEvent(eventId = "evt-i1", severity = Severity.INFO),
            ),
        )
        assertEquals(listOf("evt-c1", "evt-m1"), assessment.contributingEventIds)
    }
}
