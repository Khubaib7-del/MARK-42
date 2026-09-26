package app.selvard.appguard

import app.selvard.SelvardApplication
import app.selvard.core.domain.appguard.AppAnalysis
import app.selvard.core.domain.appguard.AppRiskBand
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
import kotlinx.coroutines.launch

/**
 * Data-minimizing event recording for app scans: one summary event (counts
 * only, no package names of low-risk apps), and per-app events only for
 * HIGH/CRITICAL capability scores. The full inventory is never persisted.
 *
 * Best-effort by design: persistence must never crash a scan. Results are
 * computed and shown first; a store failure only loses the audit copy.
 */
object AppGuardianEventRecorder {

    fun record(app: SelvardApplication, analyses: List<AppAnalysis>) {
        val now = System.currentTimeMillis()
        val highOrCritical = analyses.filter {
            it.band == AppRiskBand.HIGH || it.band == AppRiskBand.CRITICAL
        }
        val summary = SecurityEvent(
            eventId = newEventId(),
            timestampMillis = now,
            source = "app_guardian",
            category = EventCategory.APPLICATION,
            severity = if (highOrCritical.isNotEmpty()) Severity.MEDIUM else Severity.INFO,
            confidence = Confidence.HIGH,
            affectedAsset = AffectedAsset(AssetType.DEVICE, "app_inventory"),
            evidence = listOf(
                Evidence(
                    "scan_summary",
                    "${analyses.size} apps analyzed: " +
                        "${highOrCritical.size} high/critical capability score(s), " +
                        "${analyses.count { it.band == AppRiskBand.ELEVATED }} elevated, " +
                        "${analyses.count { it.band == AppRiskBand.LOW }} low",
                    "local package inventory",
                ),
            ),
            actionTaken = ActionTaken.RECORDED,
            privacyClassification = PrivacyClass.SENSITIVE,
        )
        app.scope.launch {
            runCatching {
                app.eventBus.publish(summary)
                app.eventStore.append(summary)
            }
        }
        highOrCritical.take(MAX_PERSISTED_APP_EVENTS).forEach { analysis ->
            val event = SecurityEvent(
                eventId = newEventId(),
                timestampMillis = now,
                source = "app_guardian",
                category = EventCategory.APPLICATION,
                severity = if (analysis.band == AppRiskBand.CRITICAL) Severity.HIGH else Severity.MEDIUM,
                confidence = Confidence.MEDIUM,
                affectedAsset = AffectedAsset(AssetType.APP, analysis.packageName),
                evidence = analysis.findings.take(4).map {
                    Evidence(it.kind, it.detail.take(Evidence.MAX_VALUE_LENGTH), it.provenance)
                },
                actionTaken = ActionTaken.RECORDED,
                privacyClassification = PrivacyClass.SENSITIVE,
            )
            app.scope.launch {
                runCatching {
                    app.eventBus.publish(event)
                    app.eventStore.append(event)
                }
            }
        }
    }

    /** Per-scan cap so one scan cannot flood the encrypted store. */
    const val MAX_PERSISTED_APP_EVENTS = 8
}
