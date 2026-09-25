package app.selvard.core.domain.link

/** Damerau-Levenshtein (optimal string alignment) for typosquat detection. */
internal fun damerauLevenshtein(a: String, b: String): Int {
    if (a == b) return 0
    if (a.isEmpty()) return b.length
    if (b.isEmpty()) return a.length
    val d = Array(a.length + 1) { IntArray(b.length + 1) }
    for (i in 0..a.length) d[i][0] = i
    for (j in 0..b.length) d[0][j] = j
    for (i in 1..a.length) {
        for (j in 1..b.length) {
            val cost = if (a[i - 1] == b[j - 1]) 0 else 1
            var best = minOf(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + cost)
            val indicesInTransposeRange = i > 1 && j > 1
            val charsTransposed = indicesInTransposeRange &&
                a[i - 1] == b[j - 2] && a[i - 2] == b[j - 1]
            if (charsTransposed) {
                best = minOf(best, d[i - 2][j - 2] + 1)
            }
            d[i][j] = best
        }
    }
    return d[a.length][b.length]
}

/**
 * Deterministic phishing-indicator heuristics (docs/security/THREAT_MODEL.md A/T).
 * Findings carry provenance "heuristic" and are evidence, not verdicts.
 */
class LinkHeuristics {

    fun inspect(url: UrlAnalysis): List<LinkFinding> {
        val findings = mutableListOf<LinkFinding>()

        if (url.scheme !in SAFE_SCHEMES) {
            findings += LinkFinding(
                "dangerous_scheme", "scheme '${url.scheme}' is not a browsable web scheme",
                LinkRiskLevel.MALICIOUS, "scheme policy",
            )
        }
        if (url.hasUserinfo) {
            findings += LinkFinding(
                "userinfo_in_url",
                "credentials embedded in the address (@ trick) hide the real host",
                LinkRiskLevel.MALICIOUS, "url policy",
            )
        }
        val hostNoBrackets = url.host.removePrefix("[").removeSuffix("]")
        if (IPV4_REGEX.matches(hostNoBrackets) || (url.host.startsWith("[") && url.host.contains(':'))) {
            findings += LinkFinding(
                "ip_literal_host", "host is a raw IP address, not a domain name",
                LinkRiskLevel.SUSPICIOUS, "heuristic",
            )
        }
        url.hostLabels.filter { Punycode.hasPrefix(it) }.forEach { label ->
            findings += LinkFinding(
                "punycode_label", "punycode label '$label' (encoded Unicode) in host",
                LinkRiskLevel.SUSPICIOUS, "heuristic",
            )
        }
        url.hostLabels.map { Punycode.revealLabel(it) ?: it }
            .filter { it.any { c -> c.code > 127 } }
            .forEach {
                findings += LinkFinding(
                    "non_ascii_host", "host contains non-ASCII characters (IDN homograph risk)",
                    LinkRiskLevel.SUSPICIOUS, "heuristic",
                )
            }
        brandFindings(url, findings)
        if (url.hostLabels.size >= 5) {
            findings += LinkFinding(
                "deep_subdomain_chain", "${url.hostLabels.size}-level host chain",
                LinkRiskLevel.SUSPICIOUS, "heuristic",
            )
        }
        return findings
    }

    private fun brandFindings(url: UrlAnalysis, findings: MutableList<LinkFinding>) {
        val domainLabel = url.hostLabels.getOrNull(url.hostLabels.size - 2) ?: return
        val tld = url.hostLabels.last()
        val loweredHost = url.host
        // A label that is itself a known brand is not a typosquat of a nearby brand
        // (e.g. "github" is 2 edits from "gitlab" but is the real domain).
        val isKnownBrand = domainLabel in BrandList.brands
        for (brand in BrandList.brands) {
            if (brand.length < 3) continue
            val sensitive = loweredHost.containsAny(BrandList.sensitiveWords)
            if (!isKnownBrand) {
                val distance = damerauLevenshtein(domainLabel, brand)
                if (distance in 1..2 && domainLabel != brand) {
                    findings += LinkFinding(
                        "brand_typosquat",
                        "'$domainLabel' resembles '$brand' ($distance edit${if (distance == 1) "" else "s"})",
                        // SECURITY_TESTING §8: heuristic findings are capped at SUSPICIOUS.
                        LinkRiskLevel.SUSPICIOUS, "heuristic",
                    )
                }
                if (domainLabel != brand && domainLabel.contains(brand)) {
                    findings += LinkFinding(
                        "brand_in_host",
                        "host embeds '$brand' inside '$domainLabel'" +
                            if (sensitive) " with sensitive wording" else "",
                        LinkRiskLevel.SUSPICIOUS, "heuristic",
                    )
                }
            }
            if (domainLabel == brand && tld !in COMMON_TLDS) {
                findings += LinkFinding(
                    "brand_on_unusual_tld",
                    "'$brand' on unusual TLD '.$tld'",
                    LinkRiskLevel.SUSPICIOUS, "heuristic",
                )
            }
        }
    }

    private fun String.containsAny(words: Set<String>): Boolean =
        words.any { contains(it) }

    companion object {
        private val SAFE_SCHEMES = setOf("http", "https")
        private val IPV4_REGEX = Regex("""^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$""")
        private val COMMON_TLDS = setOf(
            "com", "net", "org", "io", "co", "uk", "de", "fr", "in", "jp",
            "ru", "br", "au", "ca", "gov", "edu", "nl", "it", "es", "eu",
        )
    }
}
