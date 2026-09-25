package app.selvard.core.domain.link

import app.selvard.core.domain.event.Confidence
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LinkGuardianTest {

    private val guardian = LinkGuardian(listOf(DevelopmentSampleFeed()))

    // ---- labeled corpus: phishing patterns ----

    @Test
    fun corpusPhishingPatternsGetBlockedOrWarnedWithNamedEvidence() {
        val cases = mapOf(
            "http://secure-login-verify-account.com/login" to LinkVerdictState.BLOCK,
            "https://paypal-account-verify.com" to LinkVerdictState.BLOCK,
            "http://g00gle.com/login" to LinkVerdictState.OPEN_WITH_WARNING,
            // SECURITY_TESTING §8: heuristics alone are capped at WARNING, never BLOCK.
            "https://paypa1.com/verify" to LinkVerdictState.OPEN_WITH_WARNING,
            "https://paypal-support-verify.com" to LinkVerdictState.OPEN_WITH_WARNING,
            "http://192.168.13.37/login.php" to LinkVerdictState.OPEN_WITH_WARNING,
            "https://google.com@evil.attacker.net/" to LinkVerdictState.BLOCK,
            "https://user:pass@bank.example.com/" to LinkVerdictState.BLOCK,
            "javascript:alert(1)" to LinkVerdictState.BLOCK,
            "data:text/html,phish" to LinkVerdictState.BLOCK,
        )
        cases.forEach { (url, expected) ->
            val verdict = guardian.analyze(url)
            assertEquals(expected, verdict.state, "wrong verdict for $url")
            assertTrue(
                verdict.state != LinkVerdictState.OPEN_WITH_WARNING || verdict.findings.isNotEmpty(),
                "a warning must always name its evidence",
            )
        }
    }

    @Test
    fun corpusBenignUrlsStayOpenWithNoSafetyClaim() {
        val benign = listOf(
            "https://www.google.com/search?q=test",
            "https://github.com/Khubaib7-del/MARK-42",
            "https://developer.android.com/training/package-visibility",
            "https://en.wikipedia.org/wiki/Punycode",
            "https://www.amazon.com/dp/B08N5WRWNW",
            "https://kotlinlang.org/docs/home.html",
        )
        benign.forEach { url ->
            val verdict = guardian.analyze(url)
            assertEquals(LinkVerdictState.OPEN, verdict.state, "benign url flagged: $url")
            assertTrue(
                verdict.reasons.any { it.contains("not a safety claim", ignoreCase = true) },
                "OPEN verdicts must carry the no-safety-claim reason: $url",
            )
        }
    }

    @Test
    fun heuristicsAloneNeverExceedWarningWhenNoMaliciousIndicatorFires() {
        // A deep (but benign-looking) host chain: suspicious-class finding only.
        val verdict = guardian.analyze("https://a.b.c.d.example-long-name.org/x")
        assertEquals(LinkVerdictState.OPEN_WITH_WARNING, verdict.state)
        assertTrue(verdict.findings.all { it.risk == LinkRiskLevel.SUSPICIOUS })
    }

    @Test
    fun noFeedConfiguredYieldsUnknownNeverOpen() {
        val offline = LinkGuardian(feeds = emptyList())
        val verdict = offline.analyze("https://www.wikipedia.org/")
        assertEquals(LinkVerdictState.UNKNOWN, verdict.state)
        assertEquals(Confidence.LOW, verdict.confidence)
    }

    @Test
    fun feedMatchBlocksWithHighConfidenceAndNamesTheList() {
        val verdict = guardian.analyze("https://appleid-locked-support.net/signin")
        assertEquals(LinkVerdictState.BLOCK, verdict.state)
        assertEquals(Confidence.HIGH, verdict.confidence)
        val match = verdict.findings.first { it.kind == "matched_intelligence" }
        assertTrue(match.provenance.contains("DEVELOPMENT MOCK"), "sample feed must be labeled as mock")
    }

    @Test
    fun unparseableInputBlocks() {
        assertEquals(LinkVerdictState.BLOCK, guardian.analyze("").state)
        assertEquals(LinkVerdictState.BLOCK, guardian.analyze("nonsense").state)
        assertEquals(
            LinkVerdictState.BLOCK,
            guardian.analyze("https://${"x".repeat(3000)}").state,
        )
    }

    @Test
    fun verdictsAreDeterministic() {
        val url = "https://paypa1-secure-login.com/verify"
        assertEquals(guardian.analyze(url), guardian.analyze(url))
    }

    @Test
    fun fuzzyMutationsNeverCrashAndAlwaysCarryReasons() {
        val rng = java.util.Random(99L)
        val base = "https://paypa1-secure.example.com/verify?a=b"
        repeat(1000) {
            val mutated = buildString {
                base.forEach { c -> append(if (rng.nextInt(8) == 0) '?' else c) }
            }.let { if (rng.nextBoolean()) it.replace("?", "xn--\u0000") else it }
            val verdict = runCatching { guardian.analyze(mutated) }
            assertTrue(verdict.isSuccess)
            assertTrue(verdict.getOrThrow().reasons.isNotEmpty())
        }
    }
}
