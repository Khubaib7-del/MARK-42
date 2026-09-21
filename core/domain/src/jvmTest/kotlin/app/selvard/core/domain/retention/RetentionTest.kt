package app.selvard.core.domain.retention

import app.selvard.core.domain.event.EventCategory
import app.selvard.core.domain.event.EventQuery
import app.selvard.core.domain.event.testEvent
import app.selvard.core.domain.store.InMemoryEventStore
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RetentionTest {

    @Test
    fun rulesMatchTheDocumentedRetentionTable() {
        // Mirrors docs/security/DATA_CLASSIFICATION.md; drift here must be a deliberate doc change.
        assertEquals(180, RetentionPolicies.rules[EventCategory.LINK]?.autoPurgeDays)
        assertEquals(90, RetentionPolicies.rules[EventCategory.NETWORK]?.autoPurgeDays)
        assertEquals(90, RetentionPolicies.rules[EventCategory.APPLICATION]?.autoPurgeDays)
        assertEquals(180, RetentionPolicies.rules[EventCategory.PRIVACY]?.autoPurgeDays)
        assertEquals(180, RetentionPolicies.rules[EventCategory.DEVICE]?.autoPurgeDays)
        assertEquals(7, RetentionPolicies.rules[EventCategory.SYSTEM]?.autoPurgeDays)
        assertNull(RetentionPolicies.rules[EventCategory.IDENTITY]?.autoPurgeDays)
        assertNull(RetentionPolicies.rules[EventCategory.USER]?.autoPurgeDays)
    }

    @Test
    fun enforcePurgesOnlyExpiredCategoriesAndKeepsUserControlledData() = runTest {
        val store = InMemoryEventStore()
        val now = 100_000_000_000_000L
        fun daysAgo(days: Long) = now - days * RetentionPolicies.MILLIS_PER_DAY
        store.appendAll(
            listOf(
                testEvent(eventId = "link-old", timestampMillis = daysAgo(181), category = EventCategory.LINK),
                testEvent(eventId = "link-boundary", timestampMillis = daysAgo(180), category = EventCategory.LINK),
                testEvent(eventId = "net-old", timestampMillis = daysAgo(91), category = EventCategory.NETWORK),
                testEvent(eventId = "identity-ancient", timestampMillis = daysAgo(365), category = EventCategory.IDENTITY),
                testEvent(eventId = "user-ancient", timestampMillis = daysAgo(365), category = EventCategory.USER),
                testEvent(eventId = "system-old", timestampMillis = daysAgo(8), category = EventCategory.SYSTEM),
            ),
        )

        val summary = RetentionEnforcer.enforce(store, now)

        assertEquals(
            mapOf(EventCategory.LINK to 1, EventCategory.NETWORK to 1, EventCategory.SYSTEM to 1),
            summary.purgedByCategory,
        )
        assertEquals(
            setOf("link-boundary", "identity-ancient", "user-ancient"),
            store.query(EventQuery(limit = 100)).map { it.eventId }.toSet(),
        )
    }
}
