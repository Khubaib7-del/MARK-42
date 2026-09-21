package app.selvard.core.domain.risk

import app.selvard.core.domain.event.Confidence
import app.selvard.core.domain.event.SecurityEvent
import app.selvard.core.domain.event.Severity

enum class SecurityPosture { NORMAL, ATTENTION_REQUIRED, ELEVATED_RISK, HIGH_RISK, CRITICAL }

data class RiskAssessment(
    val posture: SecurityPosture,
    val confidence: Confidence,
    val contributingEventIds: List<String>,
    val reasons: List<String>,
)

/**
 * Deterministic, evidence-based risk engine (ADR-009). A pure function of recorded
 * events: identical input yields identical output, order-independent. It assesses
 * recorded signals only and never claims safety.
 */
class RiskEngine {

    fun assess(events: List<SecurityEvent>): RiskAssessment {
        if (events.isEmpty()) {
            return RiskAssessment(
                posture = SecurityPosture.NORMAL,
                confidence = Confidence.LOW,
                contributingEventIds = emptyList(),
                reasons = listOf(NO_SIGNALS_REASON),
            )
        }

        val contributing = events
            .filter { it.severity >= Severity.MEDIUM }
            .sortedWith(compareByDescending<SecurityEvent> { it.severity }.thenBy { it.eventId })

        if (contributing.isEmpty()) {
            return RiskAssessment(
                posture = SecurityPosture.NORMAL,
                confidence = Confidence.LOW,
                contributingEventIds = emptyList(),
                reasons = listOf(
                    "no events at or above MEDIUM severity in ${events.size} recorded event(s)",
                    NOT_A_SAFETY_CLAIM,
                ),
            )
        }

        val top = contributing.first()
        val posture = when (top.severity) {
            Severity.CRITICAL -> SecurityPosture.CRITICAL
            Severity.HIGH -> SecurityPosture.HIGH_RISK
            Severity.MEDIUM -> SecurityPosture.ATTENTION_REQUIRED
            Severity.INFO -> SecurityPosture.NORMAL
        }
        return RiskAssessment(
            posture = posture,
            confidence = contributing.minOf { it.confidence },
            contributingEventIds = contributing.map { it.eventId },
            reasons = listOf(
                "highest observed severity: ${top.severity} (${contributing.size} contributing event(s))",
                "assessment covers ${events.size} recorded event(s) and reflects recorded signals only",
                NOT_A_SAFETY_CLAIM,
            ),
        )
    }

    companion object {
        const val NO_SIGNALS_REASON =
            "No security signals observed. This is not evidence that the device is safe."
        const val NOT_A_SAFETY_CLAIM =
            "Assessments are not safety claims; absence of signals is not evidence of safety."
    }
}
