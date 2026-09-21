package app.selvard.core.domain.eventbus

import app.selvard.core.domain.event.SecurityEvent
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

interface EventBus {
    val events: Flow<SecurityEvent>
    suspend fun publish(event: SecurityEvent)
}

/** In-process bus (ADR-005): buffered, backpressure-suspending, no replay. */
class InMemoryEventBus : EventBus {
    private val flow = MutableSharedFlow<SecurityEvent>(
        replay = 0,
        extraBufferCapacity = BUFFER_CAPACITY,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )

    override val events: Flow<SecurityEvent> get() = flow

    override suspend fun publish(event: SecurityEvent) {
        flow.emit(event)
    }

    companion object { const val BUFFER_CAPACITY: Int = 256 }
}
