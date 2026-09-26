package app.selvard.core.domain.link

/**
 * Share-intent URL extraction (Phase 10). The sending app controls the shared
 * text, so it is untrusted input: bounded before tokenizing, and the extracted
 * token is capped at the analyzer's own length limit. Never throws.
 */
object SharedUrlParser {
    /** Binder transactions cap near 1 MB; 8 KiB is ample for a shared URL. */
    const val MAX_SHARED_TEXT_LENGTH = 8192

    fun extractFirstUrl(sharedText: String?): String? {
        if (sharedText.isNullOrBlank()) return null
        val bounded = sharedText.take(MAX_SHARED_TEXT_LENGTH)
        val token = bounded.trim().split(Regex("\\s+"))
            .firstOrNull { it.contains("://") }
            ?: return null
        if (token.length > UrlAnalyzer.MAX_URL_LENGTH) return null
        return token
    }
}
