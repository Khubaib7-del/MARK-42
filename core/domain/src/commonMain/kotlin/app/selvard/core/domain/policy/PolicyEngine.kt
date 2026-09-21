package app.selvard.core.domain.policy

import app.selvard.core.domain.event.Severity

/**
 * User policy (ADR-009). Tightening is always allowed; loosening below the safe
 * default requires explicit informed consent (docs/DRD.md §4).
 */
data class Policy(
    val version: Int = CURRENT_VERSION,
    val notificationSeverityFloor: Severity = DEFAULT_NOTIFICATION_FLOOR,
    val notificationLooseningConsented: Boolean = false,
) {
    init {
        require(version == CURRENT_VERSION) { "unsupported policy version: $version" }
        if (notificationSeverityFloor > DEFAULT_NOTIFICATION_FLOOR) {
            require(notificationLooseningConsented) { LOOSENING_REQUIRES_CONSENT }
        }
    }

    companion object {
        const val CURRENT_VERSION: Int = 1
        val DEFAULT_NOTIFICATION_FLOOR: Severity = Severity.MEDIUM
        const val LOOSENING_REQUIRES_CONSENT =
            "raising the notification floor above the safe default requires explicit informed consent"
    }
}

class PolicyEngine {
    fun policyFor(
        notificationSeverityFloor: Severity = Policy.DEFAULT_NOTIFICATION_FLOOR,
        notificationLooseningConsented: Boolean = false,
    ): Policy = Policy(
        notificationSeverityFloor = notificationSeverityFloor,
        notificationLooseningConsented = notificationLooseningConsented,
    )
}
