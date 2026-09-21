package app.selvard.core.domain.eventbus

import app.selvard.core.domain.event.SecurityEvent
import app.selvard.core.domain.event.testEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class EventBusTest {

    @Test
    fun publishedEventsReachSubscribers() = runTest {
        val bus = InMemoryEventBus()
        val received = mutableListOf<SecurityEvent>()
        // Unconfined: the collector subscribes synchronously before the first publish.
        val collector = backgroundScope.launch(Dispatchers.Unconfined) {
            bus.events.collect { received.add(it) }
        }
        val events = (1..3).map { index -> testEvent(eventId = "evt-$index") }
        events.forEach { bus.publish(it) }
        collector.cancel()
        assertEquals(events, received)
    }

    @Test
    fun everySubscriberReceivesEveryEvent() = runTest {
        val bus = InMemoryEventBus()
        val first = mutableListOf<SecurityEvent>()
        val second = mutableListOf<SecurityEvent>()
        val jobOne = backgroundScope.launch(Dispatchers.Unconfined) {
            bus.events.collect { first.add(it) }
        }
        val jobTwo = backgroundScope.launch(Dispatchers.Unconfined) {
            bus.events.collect { second.add(it) }
        }
        val event = testEvent(eventId = "evt-broadcast")
        bus.publish(event)
        jobOne.cancel()
        jobTwo.cancel()
        assertEquals(listOf(event), first)
        assertEquals(listOf(event), second)
    }
}
