package app.selvard.identity

/**
 * HIBP v3 client (ADR-010). Only https://haveibeenpwned.com is ever contacted;
 * the hostname is a private constant and never derived from input. Range mode
 * sends only the hash prefix; full-address mode sends the URL-encoded address
 * and requires explicit consent. No query result is a safety claim.
 */
class HibpClient(
    private val apiKey: String,
    private val fetch: HttpFetch = UrlHttpFetch(),
) {

    init {
        require(apiKey.isNotBlank()) { "HIBP API key required" }
    }

    suspend fun rangeQuery(prefix6: String): HibpResult {
        require(prefix6.length == 6 && prefix6.all { it in '0'..'9' || it in 'A'..'F' }) {
            "prefix must be 6 uppercase hex chars"
        }
        return get("/breachedaccount/range/$prefix6")
    }

    suspend fun fullQuery(urlEncodedAddress: String): HibpResult {
        require(urlEncodedAddress.isNotBlank()) { "address must not be blank" }
        require(!urlEncodedAddress.contains('@')) { "address must be URL-encoded" }
        return get("/breachedaccount/$urlEncodedAddress?truncateResponse=false")
    }

    private suspend fun get(path: String): HibpResult = runCatching {
        when (val r = fetch.get(HOST, path, apiKey, userAgent())) {
            is HttpResponse.Ok -> HibpResult.Ok(r.body)
            is HttpResponse.NotFound -> HibpResult.NoMatch
            is HttpResponse.RateLimited -> HibpResult.RateLimited(r.retryAfterSecs)
            is HttpResponse.Failure -> HibpResult.Failed("HIBP ${r.status}: ${r.message}")
        }
    }.getOrElse { HibpResult.Failed(it.message ?: "network error") }

    private fun userAgent(): String = USER_AGENT_PREFIX + " (consented breach check; contact in app)"

    companion object {
        const val HOST = "haveibeenpwned.com"
        const val USER_AGENT_PREFIX = "Selvard-Android"
    }
}

sealed interface HibpResult {
    data class Ok(val body: String) : HibpResult
    data object NoMatch : HibpResult
    data class RateLimited(val retryAfterSecs: Int?) : HibpResult
    data class Failed(val reason: String) : HibpResult
}

/** Minimal HTTPS fetch so the client is unit-testable without network. */
interface HttpFetch {
    suspend fun get(host: String, path: String, apiKey: String, userAgent: String): HttpResponse
}

sealed interface HttpResponse {
    data class Ok(val body: String) : HttpResponse
    data object NotFound : HttpResponse
    data class RateLimited(val retryAfterSecs: Int?) : HttpResponse
    data class Failure(val status: Int, val message: String) : HttpResponse
}

/** Real HTTPS fetch: host allowlist enforced (HIBP only), timeouts bounded. */
class UrlHttpFetch : HttpFetch {
    override suspend fun get(host: String, path: String, apiKey: String, userAgent: String): HttpResponse =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            require(host == HibpClient.HOST) { "egress allowlist: HIBP only" }
            require(path.startsWith("/")) { "path must be absolute" }
            runCatching {
                val url = java.net.URL("https://$host/api/v3$path")
                val conn = (url.openConnection() as javax.net.ssl.HttpsURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("hibp-api-key", apiKey)
                    setRequestProperty("user-agent", userAgent)
                    setRequestProperty("Accept", "application/json")
                    connectTimeout = TIMEOUT_MS
                    readTimeout = TIMEOUT_MS
                }
                when (conn.responseCode) {
                    200 -> HttpResponse.Ok(conn.inputStream.bufferedReader().readText())
                    404 -> HttpResponse.NotFound
                    429 -> {
                        val retry = conn.getHeaderField("retry-after")?.toIntOrNull()
                        HttpResponse.RateLimited(retry)
                    }
                    else -> {
                        val msg = runCatching { conn.errorStream?.bufferedReader()?.readText() }.getOrNull()
                        HttpResponse.Failure(conn.responseCode, (msg ?: "error").take(256))
                    }
                }
            }.getOrElse { HttpResponse.Failure(-1, (it.message ?: "network error").take(256)) }
        }

    companion object {
        const val TIMEOUT_MS = 15_000
    }
}
