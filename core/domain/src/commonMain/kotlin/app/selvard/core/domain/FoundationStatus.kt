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
 * Core processing engines (event bus, risk engine, posture) are active since Phase 2,
 * but no guardian collects signals yet - so no protection may be claimed.
 */
object FoundationStatus {

    val engines: List<EngineStatus> = listOf(
        EngineStatus(EngineId.EVENT_BUS, EngineLifecycle.ACTIVE, 2),
        EngineStatus(EngineId.RISK_ENGINE, EngineLifecycle.ACTIVE, 2),
        EngineStatus(EngineId.LINK_GUARDIAN, EngineLifecycle.ACTIVE, 3),
        EngineStatus(EngineId.NETWORK_GUARDIAN, EngineLifecycle.ACTIVE, 4),
        EngineStatus(EngineId.APP_GUARDIAN, EngineLifecycle.ACTIVE, 5),
        EngineStatus(EngineId.PRIVACY_MONITOR, EngineLifecycle.ACTIVE, 6),
        EngineStatus(EngineId.IDENTITY_EXPOSURE, EngineLifecycle.ACTIVE, 7),
        EngineStatus(EngineId.DEVICE_INTEGRITY, EngineLifecycle.ACTIVE, 6),
        EngineStatus(EngineId.INCIDENT_ENGINE, EngineLifecycle.ACTIVE, 8),
        EngineStatus(EngineId.SECURITY_POSTURE, EngineLifecycle.ACTIVE, 2),
    )

    /**
     * Engines that observe threats or enforce containment. Active since
     * Phases 3-5: Link Guardian (explicit link checks), Network Guardian
     * (opt-in DNS filter), App Guardian (on-demand inventory scans).
     * Privacy/Identity/Integrity/Incident engines remain unimplemented.
     */
    val guardianEngines: Set<EngineId> = setOf(
        EngineId.LINK_GUARDIAN,
        EngineId.NETWORK_GUARDIAN,
        EngineId.APP_GUARDIAN,
        EngineId.PRIVACY_MONITOR,
        EngineId.IDENTITY_EXPOSURE,
        EngineId.DEVICE_INTEGRITY,
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

    /** True only when at least one guardian engine is active. Core engines alone never protect. */
    val anyProtectionActive: Boolean
        get() = engines.any { it.lifecycle == EngineLifecycle.ACTIVE && it.engine in guardianEngines }
}
