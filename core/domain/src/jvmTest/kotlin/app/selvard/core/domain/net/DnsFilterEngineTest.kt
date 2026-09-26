package app.selvard.core.domain.net

import app.selvard.core.domain.link.DevelopmentSampleFeed
import app.selvard.core.domain.link.LocalThreatFeed
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DnsFilterEngineTest {

    @Test
    fun blocksListedHostsWithTierPolicyRespected() {
        val engine = DnsFilterEngine(listOf(DevelopmentSampleFeed()))
        // Sample feed categories contain "credential-harvesting" -> PHISHING tier (on by default).
        assertTrue(engine.decide("secure-login-verify-account.com").blocked)
        assertTrue(engine.decide("secure-login-verify-account.com.").blocked) // trailing dot
        assertTrue(engine.decide("SECURE-LOGIN-VERIFY-ACCOUNT.COM").blocked) // case
        val trackersOff = DnsFilterEngine(
            listOf(DevelopmentSampleFeed()),
            FilterPolicy(phishingEnabled = false),
        )
        assertFalse(trackersOff.decide("secure-login-verify-account.com").blocked)
    }

    @Test
    fun unlistedHostsPass() {
        val engine = DnsFilterEngine(listOf(DevelopmentSampleFeed()))
        assertFalse(engine.decide("wikipedia.org").blocked)
        assertFalse(engine.decide("developer.android.com").blocked)
    }

    @Test
    fun unknownCategoryIsBlockedConservatively() {
        val feed = object : LocalThreatFeed {
            override val listName = "test-list"
            override fun lookupHost(host: String) =
                if (host == "weird.example") app.selvard.core.domain.link.FeedMatch("test-list", host, "unclassified") else null
        }
        val engine = DnsFilterEngine(listOf(feed))
        assertTrue(engine.decide("weird.example").blocked)
    }

    @Test
    fun decisionCarriesProvenance() {
        val engine = DnsFilterEngine(listOf(DevelopmentSampleFeed()))
        val decision = engine.decide("update-your-wallet.info")
        assertTrue(decision.blocked)
        assertTrue(decision.listName!!.contains("DEVELOPMENT MOCK"))
        assertTrue(decision.category!!.contains("credential"))
    }

    @Test
    fun determinism() {
        val engine = DnsFilterEngine(listOf(DevelopmentSampleFeed()))
        assertEquals(engine.decide("paypal-account-verify.com"), engine.decide("paypal-account-verify.com"))
    }
}
