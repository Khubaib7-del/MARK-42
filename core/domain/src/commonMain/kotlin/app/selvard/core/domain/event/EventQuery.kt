package app.selvard.core.domain.event

data class EventQuery(
    val categories: Set<EventCategory>? = null,
    val sinceMillis: Long? = null,
    val untilMillis: Long? = null,
    val limit: Int = DEFAULT_LIMIT,
) {
    init {
        require(limit in 1..MAX_LIMIT) { "limit must be in 1..$MAX_LIMIT" }
        if (sinceMillis != null && untilMillis != null) {
            require(sinceMillis <= untilMillis) { "sinceMillis must not exceed untilMillis" }
        }
    }

    fun matches(event: SecurityEvent): Boolean {
        if (categories != null && event.category !in categories) return false
        if (sinceMillis != null && event.timestampMillis < sinceMillis) return false
        if (untilMillis != null && event.timestampMillis > untilMillis) return false
        return true
    }

    companion object {
        const val DEFAULT_LIMIT: Int = 500
        const val MAX_LIMIT: Int = 10_000
    }
}
