package app.selvard.core.domain.event

import java.util.UUID

/** Collision-resistant event ids (UUIDv4). Tests may supply deterministic ids instead. */
fun newEventId(): String = UUID.randomUUID().toString()
