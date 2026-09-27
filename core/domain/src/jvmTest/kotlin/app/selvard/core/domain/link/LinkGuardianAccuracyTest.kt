package app.selvard.core.domain.link

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Phase 11 accuracy gate (SECURITY_TESTING §8): a held-out labeled corpus
 * larger than the Phase 3 smoke set. Heuristics-only verdicts are capped at
 * WARNING, so BLOCK precision is measured over MALICIOUS/BLOCK outcomes and
 * recall over feed-indexed plus policy-blocked patterns. Methodology and
 * numbers are published in docs/security/PHASE11_MVP_TEST_REPORT.md.
 */
class LinkGuardianAccuracyTest {

    private val guardian = LinkGuardian(listOf(DevelopmentSampleFeed()))

    private val feedListed = listOf(
        "http://secure-login-verify-account.com/login",
        "https://paypal-account-verify.com",
        "https://appleid-locked-support.net/signin",
        "https://update-your-wallet.info/claim",
    )

    private val policyBlocked = listOf(
        "javascript:alert(1)",
        "data:text/html,phish",
        "ftp://evil.example.com/file",
        "https://google.com@evil.attacker.net/",
        "https://user:pass@bank.example.com/",
    )

    private val heuristicPhishing = listOf(
        "http://g00gle.com/login",
        "https://paypa1.com/verify",
        "https://micros0ft.com/login",
        "https://appl3.com/signin",
        "https://amaz0n.com/order",
        "https://netf1ix.com/billing",
        "https://faceb00k.com/login",
        "https://linked1n.com/auth",
        "https://disc0rd.com/login",
        "https://githu6.com/session",
        "https://paypal-support-verify.com",
        "https://apple-login-secure.com/signin",
        "https://google-drive-share.com/doc",
        "https://microsoft-office-update.com/setup",
        "https://amazon-prime-offer.com/claim",
        "https://chase-bank-alert.com/verify",
        "http://192.168.13.37/login.php",
        "http://10.0.0.9/secure",
        "http://45.148.10.88/verify",
        "http://[2001:db8::1]/login",
        "http://xn--pple-43d.com/",
        "https://xn--ggle-55da.com/login",
        "http://xn--80ak6aa92cm.com/signin",
        "https://a.b.c.d.example-long-name.org/x",
        "https://secure.login.verify.account.update.example.com/",
        "https://paypal.xyz/signin",
        "https://google.top/login",
    )

    private val benign = listOf(
        "https://www.google.com/search?q=test",
        "https://github.com/Khubaib7-del/MARK-42",
        "https://en.wikipedia.org/wiki/Punycode",
        "https://www.wikipedia.org/",
        "https://www.amazon.com/dp/B08N5WRWNW",
        "https://kotlinlang.org/docs/home.html",
        "https://www.mozilla.org/en-US/",
        "https://stackoverflow.com/questions/123",
        "https://www.nytimes.com/2024/01/01/world.html",
        "https://www.bbc.com/news",
        "https://example.com/",
        "https://example.org/papers",
        "https://www.jetbrains.com/idea/",
        "https://gradle.org/guides/",
        "https://medium.com/some-publication/article",
        "https://www.theguardian.com/world",
        "https://weather.example.gov/forecast",
        "https://www.nasa.gov/mission",
        "https://www.nih.gov/health",
        "https://europa.eu/about",
        "https://www.python.org/downloads/",
        "https://www.rust-lang.org/tools",
        "https://nodejs.org/en/docs",
        "https://www.docker.com/products",
        "https://kubernetes.io/docs/home/",
        "https://www.apache.org/foundation/",
        "https://www.linuxfoundation.org/projects",
        "https://www.eff.org/about",
        "https://www.w3.org/standards/",
        "https://www.ietf.org/standards/",
        "https://arxiv.org/abs/2401-00001",
        "https://www.nature.com/articles/sample",
        "https://spring.io/guides",
        "https://developer.android.com/training/package-visibility",
        "https://docs.python.org/3/library/",
        "https://support.microsoft.com/help",
    )

    @Test
    fun feedIndexedRecallIsPerfect() {
        val blocked = feedListed.count { guardian.analyze(it).state == LinkVerdictState.BLOCK }
        assertEquals(feedListed.size, blocked, "every feed-listed URL must BLOCK (recall 100%)")
    }

    @Test
    fun policyViolationsAlwaysBlock() {
        policyBlocked.forEach { url ->
            assertEquals(LinkVerdictState.BLOCK, guardian.analyze(url).state, "policy must BLOCK: $url")
        }
    }

    @Test
    fun heuristicRecallNeverMissesPhishingAsOpenOrUnknown() {
        val missed = heuristicPhishing.filter {
            val state = guardian.analyze(it).state
            state != LinkVerdictState.BLOCK && state != LinkVerdictState.OPEN_WITH_WARNING
        }
        assertTrue(missed.isEmpty(), "heuristics missed phishing patterns as OPEN/UNKNOWN: $missed")
    }

    @Test
    fun benignUrlsNeverBlockPrecisionGate() {
        val blocked = benign.filter { guardian.analyze(it).state == LinkVerdictState.BLOCK }
        assertTrue(blocked.isEmpty(), "benign URLs must never BLOCK (precision gate): $blocked")
    }

    @Test
    fun benignWarningRateIsFieldStudySignal() {
        val warned = benign.filter { guardian.analyze(it).state == LinkVerdictState.OPEN_WITH_WARNING }
        val rate = warned.size.toDouble() / benign.size.toDouble()
        // Published in the Phase 11 report; the gate keeps the field-study
        // false-positive rate in single digits on this held-out benign set.
        assertTrue(rate <= 0.10, "benign warning rate exceeds 10%: $warned")
    }

    @Test
    fun everyVerdictOnCorpusCarriesReasons() {
        (feedListed + policyBlocked + heuristicPhishing + benign).forEach { url ->
            val verdict = guardian.analyze(url)
            assertTrue(verdict.reasons.isNotEmpty(), "verdict without reasons: $url")
            if (verdict.state == LinkVerdictState.OPEN_WITH_WARNING) {
                assertTrue(verdict.findings.isNotEmpty(), "warning without named evidence: $url")
            }
        }
    }
}
