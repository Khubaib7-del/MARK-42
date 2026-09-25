package app.selvard.core.domain.link

import app.selvard.core.domain.event.Confidence

/**
 * Link Guardian (Phase 3): URL -> evidence-backed verdict. Deterministic pure
 * function of (url, feeds). Heuristics alone never exceed OPEN_WITH_WARNING;
 * only matched intelligence or scheme policy can BLOCK (SECURITY_TESTING §8).
 */
class LinkGuardian(private val feeds: List<LocalThreatFeed> = emptyList()) {

    private val heuristics = LinkHeuristics()

    fun analyze(rawUrl: String): LinkVerdict {
        val url = UrlAnalyzer.analyze(rawUrl)
        if (url == null) {
            return LinkVerdict(
                state = LinkVerdictState.BLOCK,
                confidence = Confidence.HIGH,
                findings = emptyList(),
                reasons = listOf(
                    "not a parseable web URL (empty, oversized, or malformed) — refusing to open",
                ),
            )
        }

        val findings = heuristics.inspect(url)
        val policyBlock = findings.any { it.risk == LinkRiskLevel.MALICIOUS }
        val policyKinds = findings.filter { it.risk == LinkRiskLevel.MALICIOUS }.map { it.kind }

        val feedMatches = feeds.mapNotNull { feed -> feed.lookupHost(url.host) }
        if (feedMatches.isNotEmpty()) {
            val match = feedMatches.first()
            return LinkVerdict(
                state = LinkVerdictState.BLOCK,
                confidence = Confidence.HIGH,
                findings = findings + LinkFinding(
                    "matched_intelligence",
                    "host '${match.matchedHost}' is listed: ${match.category}",
                    LinkRiskLevel.MALICIOUS, match.listName,
                ),
                reasons = listOf(
                    "matched known-bad intelligence: ${match.category}",
                    "source: ${match.listName}",
                ),
            )
        }

        if (policyBlock) {
            return LinkVerdict(
                state = LinkVerdictState.BLOCK,
                confidence = Confidence.MEDIUM,
                findings = findings,
                reasons = listOf(
                    "deterministic URL policy violation (${policyKinds.joinToString()})",
                    "policy-based verdict; no intelligence match was required",
                    BLOCK_EXPLANATION,
                ),
            )
        }

        if (findings.isNotEmpty()) {
            return LinkVerdict(
                state = LinkVerdictState.OPEN_WITH_WARNING,
                confidence = Confidence.MEDIUM,
                findings = findings,
                reasons = listOf(
                    "suspicious indicators present: ${findings.joinToString { it.kind }}",
                    NO_SAFETY_CLAIM,
                ),
            )
        }

        if (feeds.isEmpty()) {
            return LinkVerdict(
                state = LinkVerdictState.UNKNOWN,
                confidence = Confidence.LOW,
                findings = emptyList(),
                reasons = listOf(
                    "limited analysis: no threat intelligence available (offline or not configured)",
                    NO_SAFETY_CLAIM,
                ),
            )
        }

        return LinkVerdict(
            state = LinkVerdictState.OPEN,
            confidence = Confidence.LOW,
            findings = emptyList(),
            reasons = listOf(
                "no known threat in available intelligence; heuristics found no indicators",
                NO_SAFETY_CLAIM,
            ),
        )
    }

    companion object {
        const val NO_SAFETY_CLAIM =
            "\u201cNo known threat\u201d is not a safety claim; absence of detection is not evidence of safety."
        const val BLOCK_EXPLANATION =
            "Blocked as a precaution: you can override after reviewing the evidence."
    }
}
