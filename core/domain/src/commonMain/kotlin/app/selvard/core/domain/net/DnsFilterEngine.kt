package app.selvard.core.domain.net

import app.selvard.core.domain.link.FeedMatch
import app.selvard.core.domain.link.LocalThreatFeed

/**
 * Network Guardian DNS filter (ADR-003): pure decision core. Hosts are matched
 * exactly and under one trailing dot, mirroring resolver normalization. A
 * throwing feed fails closed by default while the VPN is active (DRD §10).
 */
class DnsFilterEngine(
    private val feeds: List<LocalThreatFeed>,
    private val policy: FilterPolicy = FilterPolicy.DEFAULT,
) {

    fun decide(host: String): FilterDecision {
        require(host.isNotBlank()) { "host must not be blank" }
        val normalized = normalize(host)
        for (feed in feeds) {
            val lookup = runCatching { feed.lookupHost(normalized) }
            val match = if (lookup.isFailure) {
                if (policy.failClosed) {
                    return FilterDecision.block(POLICY_LIST, FAIL_CLOSED_CATEGORY)
                }
                null
            } else {
                lookup.getOrNull()
            }
            if (match == null) continue
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

    companion object {
        const val POLICY_LIST = "selvard-policy"
        const val FAIL_CLOSED_CATEGORY = "intelligence-unavailable (fail-closed default)"
    }
}
