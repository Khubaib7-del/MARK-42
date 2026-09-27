package app.selvard.core.domain.policy

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Phase 11 access-control gate (SECURITY_TESTING §4): the manifest permission
 * budget (DRD §9), backup/cleartext lockdown, and the exported-component
 * surface are asserted on every CI run so a permission or export can only be
 * added deliberately.
 */
class ManifestPolicyTest {

    private val allowlisted = setOf(
        "android.permission.INTERNET",
        "android.permission.ACCESS_NETWORK_STATE",
        "android.permission.FOREGROUND_SERVICE",
        "android.permission.FOREGROUND_SERVICE_SPECIAL_USE",
        "android.permission.POST_NOTIFICATIONS",
        "android.permission.RECEIVE_BOOT_COMPLETED",
        "android.permission.QUERY_ALL_PACKAGES",
        "android.permission.USE_BIOMETRIC",
    )

    private val prohibited = setOf(
        "android.permission.READ_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.READ_CONTACTS",
        "android.permission.READ_CALL_LOG",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.ACCESS_BACKGROUND_LOCATION",
        "android.permission.BIND_ACCESSIBILITY_SERVICE",
        "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE",
        "android.permission.SYSTEM_ALERT_WINDOW",
        "android.permission.READ_MEDIA_IMAGES",
        "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO",
    )

    private fun manifestText(): String {
        val here = File(".").absoluteFile
        val found = generateSequence(here) { it.parentFile }
            .map { dir -> File(dir, "android/app/src/main/AndroidManifest.xml") }
            .firstOrNull { it.isFile }
        assertTrue(found != null, "AndroidManifest.xml not found from ${here.path}")
        return found.readText()
    }

    private fun usesPermissions(raw: String): Set<String> =
        Regex("<uses-permission[^>]*android:name=\"([^\"]+)\"")
            .findAll(raw).map { it.groupValues[1] }.toSet()

    @Test
    fun permissionsStayWithinTheAllowlist() {
        val granted = usesPermissions(manifestText())
        val extra = granted - allowlisted
        assertTrue(extra.isEmpty(), "manifest requests permissions outside DRD §9: $extra")
    }

    @Test
    fun prohibitedPermissionsAreAbsent() {
        val granted = usesPermissions(manifestText())
        val overlap = granted intersect prohibited
        assertTrue(overlap.isEmpty(), "prohibited permission requested: $overlap")
    }

    @Test
    fun backupAndCleartextStayLockedDown() {
        val raw = manifestText()
        assertTrue(raw.contains("android:allowBackup=\"false\""), "event store must opt out of cloud backup")
        assertTrue(raw.contains("android:usesCleartextTraffic=\"false\""), "cleartext must stay banned")
        assertTrue(raw.contains("android:networkSecurityConfig="), "network security config must be wired")
    }

    @Test
    fun exportedSurfaceIsMinimal() {
        val raw = manifestText()
        val exportedTrueBlocks = Regex("<(activity|service|receiver)[^>]*android:exported=\"true\"[^>]*>")
            .findAll(raw).toList()
        // Only the launcher entry and the explicit share target are exported.
        assertEquals(2, exportedTrueBlocks.size, "exported components must stay at exactly two")
        assertTrue(raw.contains(".MainActivity"), "launcher activity must remain")
        assertTrue(raw.contains(".link.CheckLinkActivity"), "share target must remain")
        assertTrue(raw.contains("android.permission.BIND_VPN_SERVICE"), "tunnel keeps its system-only guard")
    }
}
