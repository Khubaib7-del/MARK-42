package app.selvard.core.domain.link

/**
 * Tolerant, size-limited URL parse (platform-neutral, no java.net). Returns the
 * components the heuristics need; never throws on malformed input.
 */
data class UrlAnalysis(
    val raw: String,
    val scheme: String,
    val hasUserinfo: Boolean,
    val host: String,
    val port: Int?,
    val path: String,
    val registrableDomain: String,
    val hostLabels: List<String>,
)

object UrlAnalyzer {
    const val MAX_URL_LENGTH = 2048
    const val MAX_HOST_LABELS = 12

    fun analyze(rawInput: String): UrlAnalysis? {
        val raw = rawInput.trim()
        if (raw.isEmpty() || raw.length > MAX_URL_LENGTH) return null
        val scheme = raw.substringBefore("://", missingDelimiterValue = "").lowercase()
        if (scheme.isEmpty()) return null
        val rest = raw.substringAfter("://", "")
        val authorityEnd = rest.indexOfFirst { it == '/' || it == '?' || it == '#' }
        val authority = if (authorityEnd < 0) rest else rest.substring(0, authorityEnd)
        val tail = if (authorityEnd < 0) "" else rest.substring(authorityEnd)
        if (authority.isEmpty()) return null

        val hasUserinfo = authority.contains('@')
        val hostPort = if (hasUserinfo) authority.substringAfterLast('@') else authority
        if (hostPort.isEmpty()) return null
        val (host, port) = parseHostPort(hostPort) ?: return null
        if (host.isEmpty()) return null
        val labels = host.trim('[', ']').split('.').filter { it.isNotBlank() }
        if (labels.size > MAX_HOST_LABELS) return null
        val registrable = registrableOf(labels)
        return UrlAnalysis(
            raw = raw,
            scheme = scheme,
            hasUserinfo = hasUserinfo,
            host = host.lowercase(),
            port = port,
            path = tail.substringBefore('?').substringBefore('#'),
            registrableDomain = registrable.lowercase(),
            hostLabels = labels.map { it.lowercase() },
        )
    }

    /** Minimal public-suffix awareness; the full PSL is a deliberate future dependency decision. */
    private fun registrableOf(labels: List<String>): String {
        if (labels.size < 2) return labels.joinToString(".")
        val lastTwo = labels.takeLast(2).joinToString(".")
        val lastThree = if (labels.size >= 3) labels.takeLast(3).joinToString(".") else null
        val effective = if (lastTwo in MULTI_PART_SUFFIXES && lastThree != null) lastThree else lastTwo
        return effective
    }

    private val MULTI_PART_SUFFIXES = setOf(
        "co.uk", "gov.uk", "ac.uk", "org.uk", "com.au", "net.au", "org.au",
        "co.nz", "co.jp", "co.in", "co.za", "com.br", "com.mx", "com.cn",
    )

    /** Splits an authority (host[:port]) pair; null on a malformed bracketed host. */
    private fun parseHostPort(hostPort: String): Pair<String, Int?>? {
        if (!hostPort.startsWith("[")) {
            val host = hostPort.substringBefore(':')
            val port = hostPort.substringAfter(':', missingDelimiterValue = "")
            return host to port.toIntOrNull()
        }
        val close = hostPort.indexOf(']')
        if (close < 0) return null
        val host = hostPort.substring(0, close + 1)
        val port = hostPort.drop(close + 1).removePrefix(":")
        return host to port.toIntOrNull()
    }
}
