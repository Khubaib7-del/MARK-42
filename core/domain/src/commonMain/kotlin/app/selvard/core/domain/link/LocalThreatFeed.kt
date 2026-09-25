package app.selvard.core.domain.link

/** A matched local intelligence entry. Provenance names the list (never "AI"). */
data class FeedMatch(val listName: String, val matchedHost: String, val category: String)

/**
 * Local intelligence floor (ADR-004): consulted before any remote provider.
 * Absence of a feed yields UNKNOWN verdicts, never "safe".
 */
interface LocalThreatFeed {
    val listName: String
    fun lookupHost(host: String): FeedMatch?
}

/**
 * Bundled DEVELOPMENT SAMPLE feed: a handful of synthetic, clearly-labeled
 * entries so the pipeline is exercisable end-to-end without network access.
 * This is not real threat intelligence; provenance says so explicitly.
 */
class DevelopmentSampleFeed : LocalThreatFeed {
    override val listName = "selvard-dev-sample (DEVELOPMENT MOCK, not real intelligence)"

    private val maliciousHosts = setOf(
        "secure-login-verify-account.com",
        "paypal-account-verify.com",
        "appleid-locked-support.net",
        "update-your-wallet.info",
    )

    override fun lookupHost(host: String): FeedMatch? =
        if (host in maliciousHosts) {
            FeedMatch(listName, host, "credential-harvesting (sample)")
        } else {
            null
        }
}
