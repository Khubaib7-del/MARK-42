package app.selvard.core.domain.net

data class FilterDecision(
    val blocked: Boolean,
    val listName: String?,
    val category: String?,
) {
    companion object {
        val ALLOW = FilterDecision(false, null, null)
        fun block(listName: String, category: String) = FilterDecision(true, listName, category)
    }
}

enum class FilterTier(val displayName: String) { MALWARE("Malware"), PHISHING("Phishing"), TRACKERS("Trackers") }

/** Tier enablement is user policy; defaults follow docs/product/USER_FLOWS.md F3. */
data class FilterPolicy(
    val malwareEnabled: Boolean = true,
    val phishingEnabled: Boolean = true,
    val trackersEnabled: Boolean = false,
    val failClosed: Boolean = true,
) {
    fun allows(tier: FilterTier): Boolean = when (tier) {
        FilterTier.MALWARE -> malwareEnabled
        FilterTier.PHISHING -> phishingEnabled
        FilterTier.TRACKERS -> trackersEnabled
    }

    companion object { val DEFAULT = FilterPolicy() }
}
