package app.selvard.integrity

import android.content.Context
import android.os.Build
import app.selvard.core.domain.privacy.BootHistory
import app.selvard.core.domain.integrity.DeviceIntegritySource
import app.selvard.core.domain.integrity.IntegritySnapshot
import app.selvard.core.domain.integrity.PatchAge
import app.selvard.core.domain.integrity.PlayIntegrityState
import app.selvard.core.domain.integrity.VerifiedBootState
import app.selvard.core.domain.privacy.BootRecord

/**
 * Phase 6 Android integrity source. Verified-boot uses ro.boot.verifiedbootstate
 * where readable (some OEM builds hide it: UNKNOWN, stated). Patch level comes
 * from Build.VERSION.SECURITY_PATCH. Boot count reads Selvard's own recorded
 * boot events from the encrypted store (no system boot log exists for apps).
 */
class AndroidIntegritySource(
    private val context: Context,
    private val bootCount7d: suspend () -> Int = { 0 },
) : DeviceIntegritySource {

    override suspend fun integritySnapshot(): IntegritySnapshot = runCatching {
        val patch = Build.VERSION.SECURITY_PATCH.ifBlank { null }
        val now = System.currentTimeMillis()
        val age = PatchAge.ageDays(patch, now)
        val state = verifiedBootState()
        val boots = runCatching { bootCount7d() }.getOrDefault(0)
        val reasons = listOf(
            PatchAge.freshnessLine(patch, age),
            "Verified boot: ${bootLine(state)}",
            "Boots recorded by Selvard in the last 7 day(s): $boots (only while installed; no backfill)",
            "Play Integrity: ${integrityLine()}",
        )
        IntegritySnapshot(
            patchLevel = patch,
            patchAgeDays = age,
            verifiedBoot = state,
            bootCount7d = boots,
            playIntegrity = PlayIntegrityState.NOT_SUPPORTED,
            playIntegrityNote = IntegritySnapshot.PLAY_INTEGRITY_DEFERRED,
            reasons = reasons,
        )
    }.getOrElse {
        IntegritySnapshot(
            patchLevel = null,
            patchAgeDays = null,
            verifiedBoot = VerifiedBootState.UNKNOWN,
            bootCount7d = 0,
            playIntegrity = PlayIntegrityState.NOT_SUPPORTED,
            playIntegrityNote = IntegritySnapshot.PLAY_INTEGRITY_DEFERRED,
            reasons = listOf(
                "Integrity signals unreadable on this device (${it.message ?: "unknown error"}); nothing inferred.",
                "Play Integrity: ${integrityLine()}",
            ),
        )
    }

    private fun verifiedBootState(): VerifiedBootState = runCatching {
        when (
            getSystemProperty(VERIFIED_BOOT_PROP).lowercase().ifBlank { getSystemProperty(LEGACY_BOOT_PROP) }
        ) {
            "green" -> VerifiedBootState.GREEN
            "yellow" -> VerifiedBootState.YELLOW
            "orange" -> VerifiedBootState.ORANGE
            else -> VerifiedBootState.UNKNOWN
        }
    }.getOrDefault(VerifiedBootState.UNKNOWN)

    private fun getSystemProperty(key: String): String = runCatching {
        val process = Runtime.getRuntime().exec(arrayOf("getprop", key))
        process.inputStream.bufferedReader().readLine()?.trim().orEmpty()
    }.getOrDefault("")

    private fun bootLine(state: VerifiedBootState): String = when (state) {
        VerifiedBootState.GREEN -> "green (boot chain verified)"
        VerifiedBootState.YELLOW -> "yellow (custom root of trust enrolled by device owner)"
        VerifiedBootState.ORANGE -> "orange (device unlocked; boot chain not enforced)"
        VerifiedBootState.UNKNOWN -> "unreadable on this device (nothing inferred)"
    }

    private fun integrityLine(): String = IntegritySnapshot.PLAY_INTEGRITY_DEFERRED

    companion object {
        const val VERIFIED_BOOT_PROP = "ro.boot.verifiedbootstate"
        const val LEGACY_BOOT_PROP = "ro.boot.flash.locked"

        /** Boot-count reader over our own recorded DEVICE boot events (facts only). */
        fun bootCounter(
            query: suspend (Long) -> List<BootRecord>,
            nowMillis: Long = System.currentTimeMillis(),
        ): suspend () -> Int = {
            val cutoff = nowMillis - BootHistory.DEFAULT_WINDOW_DAYS * BootHistory.MILLIS_PER_DAY
            runCatching { query(cutoff).count { it.timestampMillis in cutoff..nowMillis } }.getOrDefault(0)
        }
    }

    @Suppress("unused")
    private val contextRef: Context = context
}
