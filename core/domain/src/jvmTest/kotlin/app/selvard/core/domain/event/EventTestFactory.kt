package app.selvard.core.domain.event

/** Deterministic factory for tests. */
fun testEvent(
    eventId: String = "evt-test",
    timestampMillis: Long = 1_000L,
    source: String = "test",
    category: EventCategory = EventCategory.LINK,
    severity: Severity = Severity.INFO,
    confidence: Confidence = Confidence.HIGH,
    affectedAsset: AffectedAsset? = null,
    evidence: List<Evidence> = listOf(Evidence("test-kind", "test-value", "test-provenance")),
    actionTaken: ActionTaken = ActionTaken.RECORDED,
    relatedEvents: List<String> = emptyList(),
    privacyClassification: PrivacyClass = PrivacyClass.SENSITIVE,
): SecurityEvent = SecurityEvent(
    eventId = eventId,
    timestampMillis = timestampMillis,
    source = source,
    category = category,
    severity = severity,
    confidence = confidence,
    affectedAsset = affectedAsset,
    evidence = evidence,
    actionTaken = actionTaken,
    relatedEvents = relatedEvents,
    privacyClassification = privacyClassification,
)
