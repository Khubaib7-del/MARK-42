package app.selvard.core.domain.privacy

/** One recorded boot: a timestamp fact, never an inferred cause. */
data class BootRecord(val timestampMillis: Long) {
    init {
        require(timestampMillis >= 0) { "boot time must be non-negative" }
    }
}

/** Factual summary of boots inside a trailing window. */
data class BootSummary(
    val windowDays: Int,
    val bootCount: Int,
    val mostRecentBootMillis: Long?,
) {
    /** Plain-language fact line; states counts and times, never causes. */
    fun summaryLine(): String =
        if (bootCount == 0) {
            "No boots recorded in the last $windowDays day(s)"
        } else {
            "$bootCount boot(s) in the last $windowDays day(s), most recent at $mostRecentBootMillis"
        }
}

object BootHistory {

    const val DEFAULT_WINDOW_DAYS = 7
    const val MILLIS_PER_DAY = 86_400_000L

    fun summarize(
        records: List<BootRecord>,
        nowMillis: Long,
        windowDays: Int = DEFAULT_WINDOW_DAYS,
    ): BootSummary {
        require(windowDays > 0) { "window must be positive" }
        val cutoff = nowMillis - windowDays * MILLIS_PER_DAY
        val inWindow = records.filter { it.timestampMillis in cutoff..nowMillis }.map { it.timestampMillis }
        return BootSummary(
            windowDays = windowDays,
            bootCount = inWindow.size,
            mostRecentBootMillis = inWindow.maxOrNull(),
        )
    }
}
