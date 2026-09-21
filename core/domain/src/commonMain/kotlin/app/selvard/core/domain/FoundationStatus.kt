package app.selvard.core.domain

/** The engines of the security control plane (docs/ARCHITECTURE.md §4). */
enum class EngineId(val displayName: String) {
    EVENT_BUS("Event Bus"),
    RISK_ENGINE("Risk Engine"),
    LINK_GUARDIAN("Link Guardian"),
    NETWORK_GUARDIAN("Network Guardian"),
    APP_GUARDIAN("App Guardian"),
    PRIVACY_MONITOR("Privacy Monitor"),
    IDENTITY_EXPOSURE("Identity Exposure"),
    DEVICE_INTEGRITY("Device Integrity"),
    INCIDENT_ENGINE("Incident Engine"),
    SECURITY_POSTURE("Security Posture"),
}

/** Honest lifecycle of an engine. Nothing is reported as active before it is built. */
enum class EngineLifecycle {
    PLANNED,
    IN_PROGRESS,
    ACTIVE,
}

data class EngineStatus(
    val engine: EngineId,
    val lifecycle: EngineLifecycle,
    val plannedPhase: Int,
)

/**
 * The single source of truth for what is implemented right now.
 * Phase 1 (foundation): every engine is planned; none may claim protection.
 */
object FoundationStatus {

    val engines: List<EngineStatus> = listOf(
        EngineStatus(EngineId.EVENT_BUS, EngineLifecycle.PLANNED, 2),
        EngineStatus(EngineId.RISK_ENGINE, EngineLifecycle.PLANNED, 2),
        EngineStatus(EngineId.LINK_GUARDIAN, EngineLifecycle.PLANNED, 3),
        EngineStatus(EngineId.NETWORK_GUARDIAN, EngineLifecycle.PLANNED, 4),
        EngineStatus(EngineId.APP_GUARDIAN, EngineLifecycle.PLANNED, 5),
        EngineStatus(EngineId.PRIVACY_MONITOR, EngineLifecycle.PLANNED, 6),
        EngineStatus(EngineId.IDENTITY_EXPOSURE, EngineLifecycle.PLANNED, 7),
        EngineStatus(EngineId.DEVICE_INTEGRITY, EngineLifecycle.PLANNED, 6),
        EngineStatus(EngineId.INCIDENT_ENGINE, EngineLifecycle.PLANNED, 8),
        EngineStatus(EngineId.SECURITY_POSTURE, EngineLifecycle.PLANNED, 2),
    )

    /** Honest, per-engine status line shown to the user. */
    fun statusLine(status: EngineStatus): String = when (status.lifecycle) {
        EngineLifecycle.PLANNED ->
            "Not yet implemented — planned for Phase " + status.plannedPhase
        EngineLifecycle.IN_PROGRESS ->
            "In development (Phase " + status.plannedPhase + ")"
        EngineLifecycle.ACTIVE ->
            "Active"
    }

    /** True only when at least one engine actively protects the user. */
    val anyProtectionActive: Boolean
        get() = engines.any { it.lifecycle == EngineLifecycle.ACTIVE }
}
