package app.selvard.core.domain.ux

import app.selvard.core.domain.event.ActionTaken
import app.selvard.core.domain.event.EventCategory
import app.selvard.core.domain.event.SecurityEvent
import app.selvard.core.domain.event.Severity
import app.selvard.core.domain.link.LinkVerdictState
import app.selvard.core.domain.risk.SecurityPosture

/** Visual tone of a status. Tone never carries meaning alone: [UiStatus.label] always does. */
enum class Tone { POSITIVE, NEUTRAL, ATTENTION, DANGER }

/**
 * The fixed status vocabulary (spec §44). There is deliberately no "Safe":
 * the strongest positive claim is [NO_KNOWN_THREAT], and the UI pairs every
 * status with an icon and its label, never color alone.
 */
enum class UiStatus(val label: String, val tone: Tone) {
    PROTECTED("Protected", Tone.POSITIVE),
    NO_KNOWN_THREAT("No known threat", Tone.POSITIVE),
    OBSERVED("Observed", Tone.NEUTRAL),
    NOT_RUN("Not run yet", Tone.NEUTRAL),
    UNKNOWN("Unknown", Tone.NEUTRAL),
    NOT_MONITORED("Not monitored", Tone.NEUTRAL),
    OS_LIMITATION("OS limitation", Tone.NEUTRAL),
    PERMISSION_REQUIRED("Permission required", Tone.ATTENTION),
    SUSPICIOUS("Suspicious", Tone.ATTENTION),
    ATTENTION("Attention", Tone.ATTENTION),
    HIGH_RISK("High risk", Tone.DANGER),
    BLOCKED("Blocked", Tone.DANGER),
    DO_NOT_OPEN("Do not open", Tone.DANGER),
}

object StatusMapper {

    /** A recorded event's status, from what was done and how severe it was — never "safe". */
    fun forEvent(event: SecurityEvent): UiStatus = when {
        event.category == EventCategory.LINK && event.actionTaken == ActionTaken.BLOCKED -> UiStatus.DO_NOT_OPEN
        event.actionTaken == ActionTaken.BLOCKED -> UiStatus.BLOCKED
        event.severity >= Severity.HIGH -> UiStatus.HIGH_RISK
        event.severity == Severity.MEDIUM -> UiStatus.SUSPICIOUS
        else -> UiStatus.OBSERVED
    }

    fun forLinkVerdict(state: LinkVerdictState): UiStatus = when (state) {
        LinkVerdictState.OPEN -> UiStatus.NO_KNOWN_THREAT
        LinkVerdictState.OPEN_WITH_WARNING -> UiStatus.SUSPICIOUS
        LinkVerdictState.BLOCK -> UiStatus.DO_NOT_OPEN
        LinkVerdictState.UNKNOWN -> UiStatus.UNKNOWN
    }

    fun forPosture(posture: SecurityPosture): UiStatus = when (posture) {
        SecurityPosture.NORMAL -> UiStatus.OBSERVED
        SecurityPosture.ATTENTION_REQUIRED -> UiStatus.ATTENTION
        SecurityPosture.ELEVATED_RISK, SecurityPosture.HIGH_RISK, SecurityPosture.CRITICAL -> UiStatus.HIGH_RISK
    }
}
