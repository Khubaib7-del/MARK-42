package app.selvard.privacy

import app.selvard.SelvardApplication
import app.selvard.core.domain.event.ActionTaken
import app.selvard.core.domain.event.AffectedAsset
import app.selvard.core.domain.event.AssetType
import app.selvard.core.domain.event.Confidence
import app.selvard.core.domain.event.EventCategory
import app.selvard.core.domain.event.Evidence
import app.selvard.core.domain.event.PrivacyClass
import app.selvard.core.domain.event.SecurityEvent
import app.selvard.core.domain.event.newEventId
import app.selvard.core.domain.privacy.BootHistory
import app.selvard.core.domain.integrity.PatchAge
import app.selvard.core.domain.privacy.PermissionSnapshot
import app.selvard.core.domain.privacy.SnapshotDiff
import app.selvard.core.domain.privacy.diffSnapshots
import kotlinx.coroutines.launch

/**
 * Phase 6 event recording: install/update/remove summaries, snapshot deltas,
 * and boot facts. Best-effort (results render first); boot records carry
 * timestamps only, never inferred causes.
 */
object PrivacyEventRecorder {

    fun recordPackageEvent(
        app: SelvardApplication,
        kind: String,
        packageName: String,
        replacing: Boolean,
    ) {
        // Boot-time delivery of a stale broadcast is a duplicate fact, not a new event.
        if (app.lastPackageEvent(packageName, kind)) return
        val event = SecurityEvent(
            eventId = newEventId(),
            timestampMillis = System.currentTimeMillis(),
            source = "privacy_monitor",
            category = EventCategory.PRIVACY,
            severity = app.selvardSeverity(severityOf(kind)),
            confidence = Confidence.HIGH,
            affectedAsset = AffectedAsset(AssetType.APP, packageName.take(AffectedAsset.MAX_REF_LENGTH)),
            evidence = listOf(
                Evidence(
                    kind,
                    (if (replacing) "updated (reinstall over existing package)" else "recorded") +
                        "; grant state not read, not claimed",
                    "system package broadcast",
                ),
            ),
            actionTaken = ActionTaken.RECORDED,
            privacyClassification = PrivacyClass.SENSITIVE,
        )
        app.scope.launch { runCatching { app.eventBus.publish(event); app.eventStore.append(event) } }
    }

    fun recordSnapshotDiff(app: SelvardApplication, diff: SnapshotDiff) {
        if (diff.isEmpty) return
        // Only low-cardinality metadata is persisted: package names for added/removed
        // are inventory facts the user can already see; per-package permission lists are not stored.
        val changedLines = diff.changed.entries.take(4).map { (pkg, delta) ->
            val adds = delta.added.take(3).joinToString(limit = 3) { it.substringAfterLast('.') }
            val drops = delta.removed.take(3).joinToString(limit = 3) { it.substringAfterLast('.') }
            "$pkg (+$adds / -$drops)".take(Evidence.MAX_VALUE_LENGTH)
        }
        val evidences = listOf(Evidence("permission_snapshot_delta", diff.summaryLine(), "privacy_monitor")) +
            changedLines.map { Evidence("package_changed", it, "privacy_monitor") }
        val event = SecurityEvent(
            eventId = newEventId(),
            timestampMillis = System.currentTimeMillis(),
            source = "privacy_monitor",
            category = EventCategory.PRIVACY,
            severity = app.selvardSeverity(SelvardSeverity.INFO),
            confidence = Confidence.HIGH,
            affectedAsset = AffectedAsset(AssetType.DEVICE, "permission_snapshot"),
            evidence = evidences,
            actionTaken = ActionTaken.RECORDED,
            privacyClassification = PrivacyClass.SENSITIVE,
        )
        app.scope.launch { runCatching { app.eventBus.publish(event); app.eventStore.append(event) } }
    }

    fun recordBoot(app: SelvardApplication, bootAtMillis: Long) {
        val summary = BootHistory.summarize(listOf(app.cachedBoot(bootAtMillis)), bootAtMillis)
        val event = SecurityEvent(
            eventId = newEventId(),
            timestampMillis = bootAtMillis,
            source = "device_integrity",
            category = EventCategory.DEVICE,
            severity = app.selvardSeverity(SelvardSeverity.INFO),
            confidence = Confidence.HIGH,
            affectedAsset = AffectedAsset(AssetType.DEVICE, "device"),
            // Timestamp fact only; the summary line states counts, never causes.
            evidence = listOf(Evidence("boot_recorded", summary.summaryLine(), "system boot broadcast")),
            actionTaken = ActionTaken.RECORDED,
            privacyClassification = PrivacyClass.LOW_SENSITIVITY,
        )
        app.scope.launch { runCatching { app.eventBus.publish(event); app.eventStore.append(event) } }
    }

    fun currentSnapshot(app: SelvardApplication? = null): PermissionSnapshot {
        val entries = app?.selvardPackageEntries() ?: emptyList()
        return PermissionSnapshot(System.currentTimeMillis(), entries)
    }

    /** Patch age is a computed fact; unknown stays unknown (clock skew, unreadable date). */
    fun patchLine(patchLevel: String?, nowMillis: Long): String =
        PatchAge.freshnessLine(patchLevel, PatchAge.ageDays(patchLevel, nowMillis))

    private fun severityOf(kind: String): SelvardSeverity =
        if (kind == PackageEvents.PACKAGE_REMOVED) SelvardSeverity.LOW else SelvardSeverity.INFO
}
