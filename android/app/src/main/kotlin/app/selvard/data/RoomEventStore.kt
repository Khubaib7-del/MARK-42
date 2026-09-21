package app.selvard.data

import app.selvard.core.domain.crypto.EventPayloadCrypto
import app.selvard.core.domain.event.EventCategory
import app.selvard.core.domain.event.EventJson
import app.selvard.core.domain.event.EventQuery
import app.selvard.core.domain.event.SecurityEvent
import app.selvard.core.domain.store.EventStore

/**
 * Encrypted append-only event store (ADR-005/ADR-006) on Room. Each row holds the
 * canonical event JSON encrypted with the Keystore-backed master key.
 */
class RoomEventStore(
    private val dao: EventDao,
    private val crypto: EventPayloadCrypto,
) : EventStore {

    override suspend fun append(event: SecurityEvent) {
        dao.insert(event.toEntity())
    }

    override suspend fun appendAll(events: List<SecurityEvent>) {
        dao.insertAll(events.map { it.toEntity() })
    }

    override suspend fun query(filter: EventQuery): List<SecurityEvent> {
        // Foundation-scale query: time and category filters are applied in memory
        // after the bounded fetch. Indexes keep the fetch cheap.
        return dao.queryRecent(filter.limit)
            .asSequence()
            .map { it.toEvent() }
            .filter { filter.matches(it) }
            .take(filter.limit)
            .toList()
    }

    override suspend fun purgeOlderThan(category: EventCategory, cutoffMillis: Long): Int =
        dao.purgeOlderThan(category.name, cutoffMillis)

    override suspend fun purgeAll(): Int = dao.purgeAll()

    private fun SecurityEvent.toEntity(): EventEntity = EventEntity(
        eventId = eventId,
        timestampMillis = timestampMillis,
        category = category.name,
        payload = crypto.encrypt(EventJson.encode(this).toByteArray()),
    )

    private fun EventEntity.toEvent(): SecurityEvent =
        EventJson.decode(String(crypto.decrypt(payload)))
}
