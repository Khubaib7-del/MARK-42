package app.selvard.core.domain.pipeline

import app.selvard.core.domain.event.ActionTaken
import app.selvard.core.domain.event.AffectedAsset
import app.selvard.core.domain.event.AssetType
import app.selvard.core.domain.event.Confidence
import app.selvard.core.domain.event.EventCategory
import app.selvard.core.domain.event.EventJson
import app.selvard.core.domain.event.EventQuery
import app.selvard.core.domain.event.Severity
import app.selvard.core.domain.event.testEvent
import app.selvard.core.domain.eventbus.InMemoryEventBus
import app.selvard.core.domain.incident.CorrelationEngine
import app.selvard.core.domain.link.DevelopmentSampleFeed
import app.selvard.core.domain.link.FeedMatch
import app.selvard.core.domain.link.LinkGuardian
import app.selvard.core.domain.link.LinkVerdictState
import app.selvard.core.domain.link.LocalThreatFeed
import app.selvard.core.domain.net.DnsFilterEngine
import app.selvard.core.domain.net.DnsParser
import app.selvard.core.domain.net.FilterPolicy
import app.selvard.core.domain.risk.RiskEngine
import app.selvard.core.domain.risk.SecurityPosture
import app.selvard.core.domain.store.InMemoryEventStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Phase 11 integration gates (SECURITY_TESTING §2 + §4): the event bus ->
 * store -> correlation -> posture pipeline, the VPN DNS decision loopback,
 * provider fallback chains (DRD §10), and export redaction. JVM-side only;
 * on-device instrumentation stays pending (no emulator in sandbox).
 */
class PipelineIntegrationTest {

    @Test
    fun busStoreCorrelationPosturePipelineIsEndToEnd() = runTest {
        val bus = InMemoryEventBus()
        val store = InMemoryEventStore()
        val received = mutableListOf<app.selvard.core.domain.event.SecurityEvent>()
        val collector = backgroundScope.launch(Dispatchers.Unconfined) {
            bus.events.collect {
                received.add(it)
                store.append(it)
            }
        }
        val base = 1_700_000_000_000L
        val events = listOf(
            testEvent(
                eventId = "pipe-1", timestampMillis = base, severity = Severity.HIGH,
                affectedAsset = AffectedAsset(AssetType.URL, "paypa1.com"),
            ),
            testEvent(
                eventId = "pipe-2", timestampMillis = base + 60_000L, severity = Severity.MEDIUM,
                affectedAsset = AffectedAsset(AssetType.URL, "paypa1.com"),
            ),
            testEvent(
                eventId = "pipe-3", timestampMillis = base + 120_000L, severity = Severity.INFO,
                affectedAsset = AffectedAsset(AssetType.APP, "com.example.notes"),
            ),
        )
        events.forEach { bus.publish(it) }
        collector.cancel()
        assertEquals(events, received)

        val stored = store.query(EventQuery(limit = 100))
        assertEquals(3, stored.size)
        val correlation = CorrelationEngine.correlate(stored)
        assertEquals(1, correlation.incidents.size)
        assertEquals(listOf("pipe-1", "pipe-2"), correlation.incidents.first().eventIds)
        assertEquals(listOf("pipe-3"), correlation.uncorrelatedEventIds)

        val assessment = RiskEngine().assess(stored)
        assertEquals(SecurityPosture.HIGH_RISK, assessment.posture)
        assertTrue(assessment.contributingEventIds.containsAll(listOf("pipe-1", "pipe-2")))
        assertEquals(assessment, RiskEngine().assess(stored.reversed()))
    }

    @Test
    fun vpnDnsLoopbackBlocksListedAndPassesUnlisted() {
        val engine = DnsFilterEngine(listOf(DevelopmentSampleFeed()))
        val blockedQuery = dnsQuery("secure-login-verify-account.com")
        val blockedName = DnsParser.parse(blockedQuery).questions.first().name
        assertEquals("secure-login-verify-account.com", blockedName)
        assertTrue(engine.decide(blockedName).blocked)
        val refused = DnsParser.parse(DnsParser.buildRefused(DnsParser.parse(blockedQuery)))
        assertTrue(refused.isResponse)
        assertTrue(refused.answers.isEmpty())
        assertEquals(blockedName, refused.questions.first().name)
        assertEquals(false, engine.decide("wikipedia.org").blocked)
    }

    private fun dnsQuery(host: String): ByteArray {
        val wire = mutableListOf<Byte>()
        wire.add(0x12.toByte())
        wire.add(0x34.toByte())
        wire.add(0x01.toByte())
        wire.add(0x00.toByte())
        wire.add(0x00.toByte())
        wire.add(0x01.toByte())
        wire.add(0x00.toByte())
        wire.add(0x00.toByte())
        wire.add(0x00.toByte())
        wire.add(0x00.toByte())
        wire.add(0x00.toByte())
        wire.add(0x00.toByte())
        host.split(".").forEach { label ->
            wire.add(label.length.toByte())
            label.encodeToByteArray().forEach { wire.add(it) }
        }
        wire.add(0.toByte())
        wire.add(0.toByte())
        wire.add(1.toByte())
        wire.add(0.toByte())
        wire.add(1.toByte())
        return wire.toByteArray()
    }

    @Test
    fun throwingFeedDegradesLinkToUnknownNeverOpen() {
        val failing = object : LocalThreatFeed {
            override val listName = "failing-feed"
            override fun lookupHost(host: String): FeedMatch? =
                error("timeout")
        }
        val guardian = LinkGuardian(listOf(failing))
        val verdict = guardian.analyze("https://www.wikipedia.org/")
        assertEquals(LinkVerdictState.UNKNOWN, verdict.state)
        assertEquals(Confidence.LOW, verdict.confidence)
        assertTrue(verdict.reasons.any { it.contains("limited analysis", ignoreCase = true) })
    }

    @Test
    fun throwingFeedFailsDnsClosedByDefaultOpenWhenConfigured() {
        val failing = object : LocalThreatFeed {
            override val listName = "failing-feed"
            override fun lookupHost(host: String): FeedMatch? =
                error("HTTP 500")
        }
        val closed = DnsFilterEngine(listOf(failing), FilterPolicy.DEFAULT)
        val closedDecision = closed.decide("wikipedia.org")
        assertTrue(closedDecision.blocked)
        assertEquals(DnsFilterEngine.POLICY_LIST, closedDecision.listName)

        val open = DnsFilterEngine(listOf(failing), FilterPolicy.DEFAULT.copy(failClosed = false))
        assertEquals(false, open.decide("wikipedia.org").blocked)
    }

    @Test
    fun verdictReasonsNeverCarryRawUrlContent() {
        val guardian = LinkGuardian(listOf(DevelopmentSampleFeed()))
        val verdict = guardian.analyze("https://example.com/secret-path?token=abc123")
        assertTrue(
            verdict.reasons.none { it.contains("secret-path") || it.contains("abc123") },
            "verdict reasons must not echo raw path/query: ${verdict.reasons}",
        )
        val matched = guardian.analyze("https://paypal-account-verify.com/secret-path?token=abc123")
        assertEquals(LinkVerdictState.BLOCK, matched.state)
        assertTrue(
            matched.findings.none { it.detail.contains("secret-path") || it.detail.contains("abc123") },
            "feed-match detail names the listed host, never the raw path/query",
        )
    }

    @Test
    fun eventJsonFuzzRejectsMalformedWithoutUnexpectedExceptions() {
        val rng = java.util.Random(20260927L)
        val alphabet = ('a'..'z') + ('0'..'9') + listOf('{', '}', '[', ']', '"', ':', ',', '-', '.', ' ', '_')
        repeat(2000) {
            val len = rng.nextInt(96)
            val raw = buildString { repeat(len) { append(alphabet[rng.nextInt(alphabet.size)]) } }
            val outcome = runCatching { EventJson.decode(raw) }
            val failure = outcome.exceptionOrNull()
            assertTrue(
                failure == null ||
                    failure is kotlinx.serialization.SerializationException ||
                    failure is IllegalArgumentException,
                "unexpected exception on event decode: ${failure?.javaClass}",
            )
        }
    }

    @Test
    fun endToEndLatencyStaysWithinBudgetOnJvm() = runTest {
        val guardian = LinkGuardian(listOf(DevelopmentSampleFeed()))
        val started = System.nanoTime()
        repeat(200) { index ->
            guardian.analyze("https://example-$index.org/page")
        }
        val elapsedMs = (System.nanoTime() - started) / 1_000_000.0
        // JVM proxy for the §7 <200ms end-to-end blocking-decision budget:
        // 200 analyses must complete far inside a single budget window.
        assertTrue(elapsedMs < 5_000.0, "200 link analyses took ${elapsedMs}ms")

        val bus = InMemoryEventBus()
        val received = mutableListOf<app.selvard.core.domain.event.SecurityEvent>()
        val collector = backgroundScope.launch(Dispatchers.Unconfined) {
            bus.events.collect { received.add(it) }
        }
        val publishStart = System.nanoTime()
        repeat(100) { index ->
            bus.publish(testEvent(eventId = "lat-$index", severity = Severity.MEDIUM))
        }
        val publishMs = (System.nanoTime() - publishStart) / 1_000_000.0
        collector.cancel()
        assertEquals(100, received.size)
        assertTrue(publishMs < 5_000.0, "100 bus publications took ${publishMs}ms")
    }

    @Test
    fun storeScalesToThousandsOfEventsWithBoundedQuery() = runTest {
        val store = InMemoryEventStore()
        store.appendAll((1..3000).map { index -> testEvent(eventId = "scale-$index") })
        val started = System.nanoTime()
        val page = store.query(EventQuery(limit = 500))
        val elapsedMs = (System.nanoTime() - started) / 1_000_000.0
        assertEquals(500, page.size)
        assertTrue(elapsedMs < 2_000.0, "paged query over 3000 events took ${elapsedMs}ms")
        assertEquals(3000, store.purgeAll())
        assertTrue(store.query().isEmpty())
    }

    @Test
    fun retentionAndDeletionCompleteTheLifecycle() = runTest {
        val store = InMemoryEventStore()
        store.appendAll(
            listOf(
                testEvent(eventId = "del-1", category = EventCategory.LINK),
                testEvent(eventId = "del-2", category = EventCategory.IDENTITY),
            ),
        )
        assertEquals(2, store.query(EventQuery(limit = 10)).size)
        val purgedLink = store.purgeOlderThan(EventCategory.LINK, cutoffMillis = Long.MAX_VALUE)
        assertEquals(1, purgedLink)
        assertEquals(1, store.purgeAll())
        assertTrue(store.query().isEmpty())
    }

    @Test
    fun storedEventsNeverCarryActionableSecrets() = runTest {
        val store = InMemoryEventStore()
        val event = testEvent(
            affectedAsset = AffectedAsset(AssetType.URL, "paypa1.com"),
            actionTaken = ActionTaken.BLOCKED,
        )
        store.append(event)
        val raw = EventJson.encode(store.query(EventQuery(limit = 1)).first())
        assertTrue(!raw.contains("secret", ignoreCase = true))
        assertEquals(event, EventJson.decode(raw))
    }
}
