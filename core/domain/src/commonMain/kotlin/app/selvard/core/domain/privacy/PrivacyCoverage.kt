package app.selvard.core.domain.privacy

import app.selvard.core.domain.DisclosureState

/** Privacy-relevant areas Selvard must label honestly, monitored or not. */
enum class PrivacyArea(val title: String) {
    NEW_INSTALLS("New installs, updates and removals"),
    REQUESTED_PERMISSIONS("Requested permissions of installed apps"),
    BOOT_EVENTS("Boots and restarts"),
    DEVICE_PATCH_LEVEL("Android security patch level"),
    REALTIME_SENSOR_USE("Real-time camera, microphone or location use by other apps"),
    GRANTED_PERMISSION_STATE("Runtime grant state of other apps' permissions"),
    TLS_TRAFFIC_CONTENT("Content of encrypted network traffic"),
    MESSAGE_CONTENT("Message, email and notification bodies"),
    PRIVATE_SPACE_APPS("Apps inside Private Space"),
}

/** One area's honest visibility state; unmonitored areas must say why. */
data class CoverageEntry(
    val area: PrivacyArea,
    val state: DisclosureState,
    val coveredBy: String?,
    val note: String,
) {
    init {
        require(note.isNotBlank()) { "every coverage entry must explain itself" }
        if (state == DisclosureState.DETECTED) {
            require(!coveredBy.isNullOrBlank()) { "detected areas must name their source" }
        }
    }
}

/**
 * The Phase 6 honesty matrix. Monitored areas name their source; every
 * NOT_MONITORED / OS_LIMITATION area states the platform reason. No area
 * claims protection it does not provide.
 */
object PrivacyCoverage {

    val entries: List<CoverageEntry> = listOf(
        CoverageEntry(
            PrivacyArea.NEW_INSTALLS,
            DisclosureState.DETECTED,
            "privacy_monitor",
            "Installs, updates and removals are recorded from system broadcasts received while Selvard is installed. " +
                "Events before installation are unknowable and not backfilled.",
        ),
        CoverageEntry(
            PrivacyArea.REQUESTED_PERMISSIONS,
            DisclosureState.DETECTED,
            "privacy_monitor",
            "Requested permissions are manifest facts captured in snapshots; changes between snapshots are reported. " +
                "Runtime grant state is excluded (see below).",
        ),
        CoverageEntry(
            PrivacyArea.BOOT_EVENTS,
            DisclosureState.DETECTED,
            "device_integrity",
            "Boots are recorded with timestamps only. A boot record is a fact, never a cause: no explanation is inferred.",
        ),
        CoverageEntry(
            PrivacyArea.DEVICE_PATCH_LEVEL,
            DisclosureState.DETECTED,
            "device_integrity",
            "The published patch level is read from the OS and its age computed. Age is a fact, not a vulnerability verdict.",
        ),
        CoverageEntry(
            PrivacyArea.REALTIME_SENSOR_USE,
            DisclosureState.NOT_MONITORED,
            null,
            "Android reserves real-time sensor-use visibility for its own indicators (camera/mic dots, Privacy Dashboard). " +
                "Selvard cannot observe when another app uses a sensor and does not try.",
        ),
        CoverageEntry(
            PrivacyArea.GRANTED_PERMISSION_STATE,
            DisclosureState.OS_LIMITATION,
            null,
            "Android does not expose other apps' runtime grant state to normal apps. " +
                "Analyses use requested (manifest) permissions and always say so.",
        ),
        CoverageEntry(
            PrivacyArea.TLS_TRAFFIC_CONTENT,
            DisclosureState.NOT_MONITORED,
            null,
            "By design: Selvard never intercepts encrypted traffic and never will (no MITM in any version).",
        ),
        CoverageEntry(
            PrivacyArea.MESSAGE_CONTENT,
            DisclosureState.NOT_MONITORED,
            null,
            "Only content the user explicitly shares with Selvard is ever analyzed. Nothing is read in the background.",
        ),
        CoverageEntry(
            PrivacyArea.PRIVATE_SPACE_APPS,
            DisclosureState.OS_LIMITATION,
            null,
            "Apps inside Private Space are invisible to package queries. Inventory and snapshots cover the main profile only.",
        ),
    )

    fun entryFor(area: PrivacyArea): CoverageEntry =
        entries.first { it.area == area }
}
