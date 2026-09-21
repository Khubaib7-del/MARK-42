package app.selvard.core.domain.store

import app.selvard.core.domain.event.EventCategory
import app.selvard.core.domain.event.EventQuery
import app.selvard.core.domain.event.testEvent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EventStoreTest {

    @Test
    fun queryReturnsNewestFirstDeterministically() = runTest {
        val store = InMemoryEventStore()
        store.appendAll(
            listOf(
                testEvent(eventId = "evt-a", timestampMillis = 100),
                testEvent(eventId = "evt-b", timestampMillis = 300),
                testEvent(eventId = "evt-c", timestampMillis = 300),
            ),
        )
        val ids = store.query().map { it.eventId }
        assertEquals(listOf("evt-b", "evt-c", "evt-a"), ids)
    }

    @Test
    fun categoryFilterAndLimitApply() = runTest {
        val store = InMemoryEventStore()
        store.appendAll(
            (1..5).map { index ->
                testEvent(
                    eventId = "evt-$index",
                    timestampMillis = index.toLong(),
                    category = if (index % 2 == 0) EventCategory.LINK else EventCategory.NETWORK,
                )
            },
        )
        val links = store.query(EventQuery(categories = setOf(EventCategory.LINK)))
        assertEquals(2, links.size)
        assertTrue(links.all { it.category == EventCategory.LINK })

        val limited = store.query(EventQuery(limit = 2))
        assertEquals(2, limited.size)
    }

    @Test
    fun purgeOlderThanRemovesOnlyMatchingCategoryAndAge() = runTest {
        val store = InMemoryEventStore()
        store.appendAll(
            listOf(
                testEvent(eventId = "link-old", timestampMillis = 100, category = EventCategory.LINK),
                testEvent(eventId = "link-new", timestampMillis = 900, category = EventCategory.LINK),
                testEvent(eventId = "net-old", timestampMillis = 100, category = EventCategory.NETWORK),
            ),
        )
        val purged = store.purgeOlderThan(EventCategory.LINK, cutoffMillis = 500)
        assertEquals(1, purged)
        assertEquals(
            setOf("link-new", "net-old"),
            store.query().map { it.eventId }.toSet(),
        )
    }

    @Test
    fun purgeAllClearsEverything() = runTest {
        val store = InMemoryEventStore()
        store.appendAll((1..3).map { testEvent(eventId = "evt-$it") })
        assertEquals(3, store.purgeAll())
        assertTrue(store.query().isEmpty())
    }
}
