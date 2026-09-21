package app.selvard.core.domain.retention

import app.selvard.core.domain.event.EventCategory
import app.selvard.core.domain.store.EventStore

data class RetentionRule(val category: EventCategory, val autoPurgeDays: Int?) {
    init {
        require(autoPurgeDays == null || autoPurgeDays > 0) { "retention days must be positive or null" }
    }
}

data class PurgeSummary(val purgedByCategory: Map<EventCategory, Int>)

object RetentionPolicies {
    const val VERSION: Int = 1
    const val MILLIS_PER_DAY: Long = 86_400_000L

    /**
     * Retention table from docs/security/DATA_CLASSIFICATION.md.
     * null = retained until the user deletes it (identity vault, consent audit trail).
     */
    val rules: Map<EventCategory, RetentionRule> = mapOf(
        EventCategory.LINK to RetentionRule(EventCategory.LINK, 180),
        EventCategory.NETWORK to RetentionRule(EventCategory.NETWORK, 90),
        EventCategory.APPLICATION to RetentionRule(EventCategory.APPLICATION, 90),
        EventCategory.PRIVACY to RetentionRule(EventCategory.PRIVACY, 180),
        EventCategory.IDENTITY to RetentionRule(EventCategory.IDENTITY, null),
        EventCategory.DEVICE to RetentionRule(EventCategory.DEVICE, 180),
        EventCategory.USER to RetentionRule(EventCategory.USER, null),
        EventCategory.SYSTEM to RetentionRule(EventCategory.SYSTEM, 7),
    )
}

object RetentionEnforcer {
    suspend fun enforce(store: EventStore, nowMillis: Long): PurgeSummary {
        require(nowMillis >= 0) { "nowMillis must be a non-negative epoch-millis value" }
        val purged = mutableMapOf<EventCategory, Int>()
        RetentionPolicies.rules.values.forEach { rule ->
            val days = rule.autoPurgeDays ?: return@forEach
            val cutoff = nowMillis - days * RetentionPolicies.MILLIS_PER_DAY
            val count = store.purgeOlderThan(rule.category, cutoff)
            if (count > 0) purged[rule.category] = count
        }
        return PurgeSummary(purged)
    }
}
