package app.selvard.core.domain

/**
 * System-wide disclosure states (docs/DRD.md §7). Every surfaced capability maps to
 * exactly one of these; the UI renders them 1:1. The word "safe" is deliberately absent:
 * absence of detection is never presented as safety.
 */
enum class DisclosureState {
    PROTECTED,
    DETECTED,
    BLOCKED,
    UNKNOWN,
    NOT_MONITORED,
    NOT_SUPPORTED,
    PERMISSION_REQUIRED,
    OS_LIMITATION,
    CLOUD_ANALYSIS_REQUIRED,
}

/** User-facing label for a disclosure state. Kept honest and unambiguous. */
fun DisclosureState.userLabel(): String = when (this) {
    DisclosureState.PROTECTED -> "Protected"
    DisclosureState.DETECTED -> "Detected"
    DisclosureState.BLOCKED -> "Blocked"
    DisclosureState.UNKNOWN -> "Unknown"
    DisclosureState.NOT_MONITORED -> "Not monitored"
    DisclosureState.NOT_SUPPORTED -> "Not supported"
    DisclosureState.PERMISSION_REQUIRED -> "Permission required"
    DisclosureState.OS_LIMITATION -> "OS limitation"
    DisclosureState.CLOUD_ANALYSIS_REQUIRED -> "Cloud analysis required"
}
