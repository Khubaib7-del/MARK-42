package app.selvard.core.domain.store

import app.selvard.core.domain.event.EventCategory
import app.selvard.core.domain.event.EventQuery
import app.selvard.core.domain.event.SecurityEvent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface EventStore {
    suspend fun append(event: SecurityEvent)
    suspend fun appendAll(events: List<SecurityEvent>)
    suspend fun query(filter: EventQuery = EventQuery()): List<SecurityEvent>
    suspend fun purgeOlderThan(category: EventCategory, cutoffMillis: Long): Int
    suspend fun purgeAll(): Int
}

/** In-memory store: JVM-test double and runtime fallback when Room is unavailable. */
class InMemoryEventStore : EventStore {
    private val mutex = Mutex()
    private val events = mutableListOf<SecurityEvent>()

    override suspend fun append(event: SecurityEvent) {
        mutex.withLock { events.add(event) }
    }

    override suspend fun appendAll(events: List<SecurityEvent>) {
        mutex.withLock { this.events.addAll(events) }
    }

    override suspend fun query(filter: EventQuery): List<SecurityEvent> = mutex.withLock {
        events.asSequence()
            .filter { filter.matches(it) }
            .sortedWith(compareByDescending<SecurityEvent> { it.timestampMillis }.thenBy { it.eventId })
            .take(filter.limit)
            .toList()
    }

    override suspend fun purgeOlderThan(category: EventCategory, cutoffMillis: Long): Int = mutex.withLock {
        val expired = events.filter { it.category == category && it.timestampMillis < cutoffMillis }
        events.removeAll(expired)
        expired.size
    }

    override suspend fun purgeAll(): Int = mutex.withLock {
        val count = events.size
        events.clear()
        count
    }
}
