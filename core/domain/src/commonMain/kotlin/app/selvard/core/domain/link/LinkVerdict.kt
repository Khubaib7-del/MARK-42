package app.selvard.core.domain.link

/** Link Guardian outcomes (docs/PRD.md §7). OPEN is always "no known threat", never "safe". */
enum class LinkVerdictState { OPEN, OPEN_WITH_WARNING, BLOCK, UNKNOWN }

/** Heuristic findings are capped at WARNING; only matched intelligence may BLOCK (SECURITY_TESTING §8). */
enum class LinkRiskLevel { NONE, SUSPICIOUS, MALICIOUS }

data class LinkFinding(
    val kind: String,
    val detail: String,
    val risk: LinkRiskLevel,
    val provenance: String,
) {
    init {
        require(kind.isNotBlank()) { "finding kind must not be blank" }
        require(detail.length <= MAX_DETAIL_LENGTH) { "finding detail exceeds limit" }
        require(provenance.isNotBlank()) { "finding provenance must not be blank" }
    }

    companion object { const val MAX_DETAIL_LENGTH = 256 }
}

data class LinkVerdict(
    val state: LinkVerdictState,
    val confidence: app.selvard.core.domain.event.Confidence,
    val findings: List<LinkFinding>,
    val reasons: List<String>,
) {
    init {
        require(reasons.isNotEmpty()) { "a verdict must always state its reasons" }
        require(findings.size <= MAX_FINDINGS) { "too many findings" }
    }

    companion object { const val MAX_FINDINGS = 16 }
}
