package app.selvard.core.domain.net

import app.selvard.core.domain.link.FeedMatch
import app.selvard.core.domain.link.LocalThreatFeed

/**
 * Network Guardian DNS filter (ADR-003): pure decision core. Hosts are matched
 * exactly and under one trailing dot, mirroring resolver normalization.
 */
class DnsFilterEngine(
    private val feeds: List<LocalThreatFeed>,
    private val policy: FilterPolicy = FilterPolicy.DEFAULT,
) {

    fun decide(host: String): FilterDecision {
        require(host.isNotBlank()) { "host must not be blank" }
        val normalized = normalize(host)
        for (feed in feeds) {
            val match = feed.lookupHost(normalized) ?: continue
            val tier = tierOf(match.category)
            if (tier == null || policy.allows(tier)) {
                return FilterDecision.block(feed.listName, match.category)
            }
        }
        return FilterDecision.ALLOW
    }

    private fun normalize(host: String): String =
        host.removeSuffix(".").lowercase()

    private fun tierOf(category: String): FilterTier? = when {
        category.contains("malware", ignoreCase = true) -> FilterTier.MALWARE
        category.contains("phishing", ignoreCase = true) ||
            category.contains("credential", ignoreCase = true) -> FilterTier.PHISHING
        category.contains("tracker", ignoreCase = true) -> FilterTier.TRACKERS
        else -> null // unknown category: conservative default is to block
    }
}
