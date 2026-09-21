package app.selvard.core.domain.event

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Canonical JSON codec for the versioned event schema. Deterministic, strict. */
object EventJson {
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = false
        prettyPrint = false
    }

    fun encode(event: SecurityEvent): String = json.encodeToString(SecurityEvent.serializer(), event)

    fun decode(raw: String): SecurityEvent = json.decodeFromString(SecurityEvent.serializer(), raw)
}
