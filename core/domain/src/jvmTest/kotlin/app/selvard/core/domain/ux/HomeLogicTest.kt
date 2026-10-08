package app.selvard.core.domain.ux

import app.selvard.core.domain.event.ActionTaken
import app.selvard.core.domain.event.AffectedAsset
import app.selvard.core.domain.event.AssetType
import app.selvard.core.domain.event.Confidence
import app.selvard.core.domain.event.EventCategory
import app.selvard.core.domain.event.SecurityEvent
import app.selvard.core.domain.event.Severity
import app.selvard.core.domain.link.LinkVerdictState
import app.selvard.core.domain.risk.SecurityPosture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeLogicTest {

    private fun event(
        category: EventCategory = EventCategory.LINK,
        severity: Severity = Severity.INFO,
        action: ActionTaken = ActionTaken.RECORDED,
        asset: AffectedAsset? = AffectedAsset(AssetType.URL, "example.com"),
    ) = SecurityEvent(
        eventId = "e1",
        timestampMillis = 1_000,
        source = "link_guardian",
        category = category,
        severity = severity,
        confidence = Confidence.HIGH,
        affectedAsset = asset,
        actionTaken = action,
    )

    @Test
    fun blockedActionWinsOverSeverity() {
        assertEquals(UiStatus.BLOCKED, StatusMapper.forEvent(
            event(category = EventCategory.NETWORK, severity = Severity.INFO, action = ActionTaken.BLOCKED),
        ))
        assertEquals(UiStatus.DO_NOT_OPEN, StatusMapper.forEvent(event(action = ActionTaken.BLOCKED)))
    }

    @Test
    fun severityMapsToWarningThenRisk() {
        assertEquals(UiStatus.OBSERVED, StatusMapper.forEvent(event(severity = Severity.INFO)))
        assertEquals(UiStatus.SUSPICIOUS, StatusMapper.forEvent(event(severity = Severity.MEDIUM)))
        assertEquals(UiStatus.HIGH_RISK, StatusMapper.forEvent(event(severity = Severity.HIGH)))
        assertEquals(UiStatus.HIGH_RISK, StatusMapper.forEvent(event(severity = Severity.CRITICAL)))
    }

    @Test
    fun aQuietEventIsObservedNeverSafe() {
        val status = StatusMapper.forEvent(event())
        assertFalse(status.label.contains("safe", ignoreCase = true))
    }

    @Test
    fun linkVerdictsNeverClaimSafe() {
        assertEquals(UiStatus.NO_KNOWN_THREAT, StatusMapper.forLinkVerdict(LinkVerdictState.OPEN))
        assertEquals(UiStatus.SUSPICIOUS, StatusMapper.forLinkVerdict(LinkVerdictState.OPEN_WITH_WARNING))
        assertEquals(UiStatus.DO_NOT_OPEN, StatusMapper.forLinkVerdict(LinkVerdictState.BLOCK))
        assertEquals(UiStatus.UNKNOWN, StatusMapper.forLinkVerdict(LinkVerdictState.UNKNOWN))
    }

    @Test
    fun statusVocabularyIsDistinctWordsAndNeverSafe() {
        val labels = UiStatus.entries.map { it.label }
        assertEquals(labels.size, labels.toSet().size)
        assertTrue(labels.none { it.equals("safe", ignoreCase = true) || it.contains("secure", ignoreCase = true) })
    }

    @Test
    fun postureNormalIsNeverMappedToAPositiveTone() {
        assertFalse(StatusMapper.forPosture(SecurityPosture.NORMAL).tone == Tone.POSITIVE)
        assertEquals(UiStatus.HIGH_RISK, StatusMapper.forPosture(SecurityPosture.CRITICAL))
    }

    @Test
    fun describeShowsHostsAndMaskedIdentitiesOnly() {
        assertEquals("example.com", EventPresentation.describe(event()).detail)
        val device = event(category = EventCategory.DEVICE, asset = AffectedAsset(AssetType.DEVICE, "permission_snapshot"))
        assertEquals("link guardian", EventPresentation.describe(device).detail)
    }

    @Test
    fun describeTitlesAreFixedPlainLanguage() {
        assertEquals("Link checked", EventPresentation.describe(event()).title)
        assertEquals("Lookup blocked", EventPresentation.describe(event(category = EventCategory.NETWORK)).title)
    }

    @Test
    fun relativeTimeBuckets() {
        val now = 10L * 24 * 60 * 60 * 1000
        assertEquals("just now", RelativeTime.format(now, now - 5_000))
        assertEquals("5 min ago", RelativeTime.format(now, now - 5 * 60_000L))
        assertEquals("3 h ago", RelativeTime.format(now, now - 3 * 3_600_000L))
        assertEquals("yesterday", RelativeTime.format(now, now - 30 * 3_600_000L))
        assertEquals("4 days ago", RelativeTime.format(now, now - 4 * 24 * 3_600_000L))
        assertEquals("just now", RelativeTime.format(now, now + 99_999), "future timestamps clamp, never negative")
    }

    @Test
    fun attentionListsOnlyWhatIsActuallyTrue() {
        val day = 24L * 60 * 60 * 1000
        val now = 100 * day
        val fresh = AttentionPlanner.plan(networkFilterOn = true, lastAppScanMillis = now - day, nowMillis = now)
        assertTrue(fresh.isEmpty(), "nothing to flag must list nothing, not invented items")

        val kinds = AttentionPlanner.plan(false, null, now).map { it.kind }
        assertEquals(listOf(AttentionKind.ENABLE_NETWORK_FILTER, AttentionKind.SCAN_APPS), kinds)

        val stale = AttentionPlanner.plan(true, now - 8 * day, now).map { it.kind }
        assertEquals(listOf(AttentionKind.RESCAN_APPS), stale)

        val boundary = AttentionPlanner.plan(true, now - AttentionPlanner.STALE_SCAN_DAYS * day, now)
        assertTrue(boundary.isEmpty(), "exactly seven days is not yet stale")
    }
}
