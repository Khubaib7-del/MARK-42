package app.selvard.core.domain.incident

import app.selvard.core.domain.event.SecurityEvent
import app.selvard.core.domain.event.Severity

/**
 * One correlated incident: recorded events sharing an entity key whose
 * consecutive timestamps fall within the correlation window. Correlation is
 * temporal proximity only — it never claims one event caused another.
 */
data class Incident(
    val incidentId: String,
    val entityKey: String,
    val eventIds: List<String>,
    val startedAtMillis: Long,
    val endedAtMillis: Long,
    val maxSeverity: Severity,
) {
    init {
        require(incidentId.isNotBlank()) { "incidentId must not be blank" }
        require(eventIds.size >= 2) { "an incident needs at least two correlated events" }
        require(startedAtMillis <= endedAtMillis) { "incident start must not exceed end" }
    }

    val eventCount: Int get() = eventIds.size

    /** Honest summary in temporal language only. Must always pass [CausalLanguage.check]. */
    fun summaryLine(): String {
        val minutes = (endedAtMillis - startedAtMillis) / 60_000L
        return "$eventCount related signals for $entityKey within $minutes minute(s); " +
            "each occurred shortly after the previous. Temporal proximity only; " +
            "no causal relationship is claimed."
    }
}

/** Correlation result: incidents plus the ids left uncorrelated (shown separately, never dropped). */
data class CorrelationResult(
    val incidents: List<Incident>,
    val uncorrelatedEventIds: List<String>,
)

/**
 * Deterministic incident correlation (order-independent): groups by entity,
 * then splits each group into runs where consecutive timestamps are within
 * the window. Single events are never incidents; they are reported as
 * uncorrelated. Pure function — identical input yields identical output.
 */
object CorrelationEngine {

    const val DEFAULT_WINDOW_MILLIS: Long = 30 * 60_000L

    /** Entity key: the affected asset when known, else the recording source. */
    fun entityKey(event: SecurityEvent): String =
        event.affectedAsset?.let { "${it.type}:${it.ref}" } ?: "source:${event.source}"

    fun correlate(
        events: List<SecurityEvent>,
        windowMillis: Long = DEFAULT_WINDOW_MILLIS,
    ): CorrelationResult {
        require(windowMillis > 0) { "window must be positive" }
        if (events.isEmpty()) return CorrelationResult(emptyList(), emptyList())
        val incidents = mutableListOf<Incident>()
        val uncorrelated = mutableListOf<String>()
        events.groupBy(::entityKey).forEach { (key, group) ->
            splitRuns(key, group.sortedBy { it.timestampMillis }, windowMillis, incidents, uncorrelated)
        }
        incidents.sortWith(compareByDescending<Incident> { it.maxSeverity }.thenBy { it.startedAtMillis })
        return CorrelationResult(incidents, uncorrelated.sorted())
    }

    private fun splitRuns(
        key: String,
        ordered: List<SecurityEvent>,
        windowMillis: Long,
        incidents: MutableList<Incident>,
        uncorrelated: MutableList<String>,
    ) {
        var run = mutableListOf<SecurityEvent>()
        fun flush() {
            if (run.size >= 2) incidents.add(toIncident(key, run))
            else if (run.size == 1) uncorrelated.add(run.first().eventId)
            run = mutableListOf()
        }
        ordered.forEach { event ->
            val gap = if (run.isEmpty()) 0 else event.timestampMillis - run.last().timestampMillis
            if (run.isNotEmpty() && gap > windowMillis) flush()
            run.add(event)
        }
        flush()
    }

    private fun toIncident(key: String, run: List<SecurityEvent>): Incident {
        val orderedIds = run.sortedBy { it.timestampMillis }.map { it.eventId }
        return Incident(
            incidentId = "incident-" + run.minBy { it.timestampMillis }.eventId,
            entityKey = key,
            eventIds = orderedIds,
            startedAtMillis = run.minOf { it.timestampMillis },
            endedAtMillis = run.maxOf { it.timestampMillis },
            maxSeverity = run.maxOf { it.severity },
        )
    }
}

/**
 * Causal-claim lint for user-facing copy. Correlation output and timeline UI
 * must use temporal language ("occurred shortly after"); causal verbs imply a
 * proven link Selvard cannot establish, so they are rejected.
 */
object CausalLanguage {

    val bannedPhrases: List<String> = listOf(
        "caused by",
        "caused",
        "triggered by",
        "triggered",
        "led to",
        "resulted in",
        "because of",
        "due to",
        "root cause",
    )

    fun violations(text: String): List<String> {
        val lower = text.lowercase()
        return bannedPhrases.filter { lower.contains(it) }
    }

    fun check(text: String) {
        val found = violations(text)
        require(found.isEmpty()) { "causal language not allowed: $found" }
    }
}
