package app.selvard.core.domain.event

import kotlinx.serialization.Serializable

/** Bumping this requires an explicit schema migration; stored events are never rewritten. */
const val EVENT_SCHEMA_VERSION: Int = 1

@Serializable
enum class EventCategory { LINK, NETWORK, APPLICATION, PRIVACY, IDENTITY, DEVICE, USER, SYSTEM }

@Serializable
enum class Severity { INFO, MEDIUM, HIGH, CRITICAL }

@Serializable
enum class Confidence { LOW, MEDIUM, HIGH }

@Serializable
enum class PrivacyClass { PUBLIC, LOW_SENSITIVITY, SENSITIVE, HIGHLY_SENSITIVE }

@Serializable
enum class ActionTaken { NONE, RECORDED, WARNED, BLOCKED }

@Serializable
enum class AssetType { APP, IDENTITY, DEVICE, URL }

@Serializable
data class AffectedAsset(val type: AssetType, val ref: String) {
    init {
        require(ref.isNotBlank()) { "asset reference must not be blank" }
        require(ref.length <= MAX_REF_LENGTH) { "asset reference exceeds data-minimization limit" }
    }

    companion object { const val MAX_REF_LENGTH = 128 }
}

@Serializable
data class Evidence(val kind: String, val value: String, val provenance: String) {
    init {
        require(kind.isNotBlank()) { "evidence kind must not be blank" }
        require(provenance.isNotBlank()) { "evidence provenance must not be blank" }
        require(value.length <= MAX_VALUE_LENGTH) { "evidence value exceeds data-minimization limit" }
    }

    companion object { const val MAX_VALUE_LENGTH = 256 }
}

/**
 * The unified security event (docs/DRD.md §3). There is deliberately no free-text body
 * field: no field of this schema can carry raw message content, page content, or file
 * content. Size limits are enforced at construction.
 */
@Serializable
data class SecurityEvent(
    val eventId: String,
    val timestampMillis: Long,
    val source: String,
    val category: EventCategory,
    val severity: Severity,
    val confidence: Confidence,
    val affectedAsset: AffectedAsset? = null,
    val evidence: List<Evidence> = emptyList(),
    val actionTaken: ActionTaken = ActionTaken.NONE,
    val relatedEvents: List<String> = emptyList(),
    val privacyClassification: PrivacyClass = PrivacyClass.SENSITIVE,
) {
    init {
        require(eventId.isNotBlank()) { "eventId must not be blank" }
        require(source.isNotBlank()) { "source must not be blank" }
        require(timestampMillis >= 0) { "timestamp must be a non-negative epoch-millis value" }
        require(evidence.size <= MAX_EVIDENCE_ITEMS) { "too many evidence items" }
        require(relatedEvents.size <= MAX_RELATED_EVENTS) { "too many related events" }
    }

    companion object {
        const val MAX_EVIDENCE_ITEMS = 32
        const val MAX_RELATED_EVENTS = 64
    }
}
