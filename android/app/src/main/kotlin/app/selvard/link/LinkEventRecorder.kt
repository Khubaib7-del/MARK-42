package app.selvard.link

import app.selvard.core.domain.event.ActionTaken
import app.selvard.core.domain.event.AffectedAsset
import app.selvard.core.domain.event.AssetType
import app.selvard.core.domain.event.Confidence
import app.selvard.core.domain.event.EventCategory
import app.selvard.core.domain.event.Evidence
import app.selvard.core.domain.event.PrivacyClass
import app.selvard.core.domain.event.SecurityEvent
import app.selvard.core.domain.event.Severity
import app.selvard.core.domain.event.newEventId
import app.selvard.core.domain.link.LinkFinding
import app.selvard.core.domain.link.LinkVerdict
import app.selvard.core.domain.link.LinkVerdictState

/** Records a Link Guardian analysis into the encrypted event store. */
object LinkEventRecorder {

    fun toEvent(url: String, verdict: LinkVerdict, nowMillis: Long): SecurityEvent {
        val host = app.selvard.core.domain.link.UrlAnalyzer.analyze(url)?.host ?: "unparseable"
        return SecurityEvent(
            eventId = newEventId(),
            timestampMillis = nowMillis,
            source = "link_guardian",
            category = EventCategory.LINK,
            severity = severityOf(verdict.state),
            confidence = verdict.confidence,
            affectedAsset = AffectedAsset(AssetType.URL, host.take(AffectedAsset.MAX_REF_LENGTH)),
            evidence = verdict.findings.map { it.toEvidence() },
            actionTaken = actionOf(verdict.state),
            relatedEvents = emptyList(),
            privacyClassification = PrivacyClass.SENSITIVE,
        )
    }

    private fun severityOf(state: LinkVerdictState): Severity = when (state) {
        LinkVerdictState.BLOCK -> Severity.HIGH
        LinkVerdictState.OPEN_WITH_WARNING -> Severity.MEDIUM
        LinkVerdictState.OPEN -> Severity.INFO
        LinkVerdictState.UNKNOWN -> Severity.INFO
    }

    private fun actionOf(state: LinkVerdictState): ActionTaken = when (state) {
        LinkVerdictState.BLOCK -> ActionTaken.BLOCKED
        LinkVerdictState.OPEN_WITH_WARNING -> ActionTaken.WARNED
        LinkVerdictState.OPEN -> ActionTaken.RECORDED
        LinkVerdictState.UNKNOWN -> ActionTaken.RECORDED
    }

    private fun LinkFinding.toEvidence(): Evidence =
        Evidence(kind, detail.take(Evidence.MAX_VALUE_LENGTH), provenance.take(Evidence.MAX_VALUE_LENGTH))

}
