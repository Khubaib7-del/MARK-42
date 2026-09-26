package app.selvard.core.domain.integrity

/**
 * Verified-boot state read from the OS where available. A factual device
 * state, not a safety verdict: GREEN means the boot chain was verified.
 */
enum class VerifiedBootState {
    GREEN,
    YELLOW,
    ORANGE,
    UNKNOWN,
}

/** Play Integrity verdict availability. Full verification needs a Play-distributed build and backend (Phase 12). */
enum class PlayIntegrityState {
    AVAILABLE,
    NOT_SUPPORTED,
}

/**
 * Point-in-time device-integrity snapshot (Phase 6). Every field is either a
 * locally observed fact or an explicitly stated unknown; nothing is inferred.
 */
data class IntegritySnapshot(
    val patchLevel: String?,
    val patchAgeDays: Int?,
    val verifiedBoot: VerifiedBootState,
    val bootCount7d: Int,
    val playIntegrity: PlayIntegrityState,
    val playIntegrityNote: String,
    val reasons: List<String>,
) {
    init {
        require(reasons.isNotEmpty()) { "a snapshot must always state its reasons" }
        require(playIntegrityNote.isNotBlank()) { "Play Integrity availability must be explained" }
        require(bootCount7d >= 0) { "boot count must be non-negative" }
        if (patchAgeDays != null) {
            require(patchAgeDays >= 0) { "patch age must be non-negative" }
        }
    }

    companion object {
        const val PLAY_INTEGRITY_DEFERRED =
            "Play Integrity verdicts require a Play-distributed build plus backend verification " +
                "(tracked to Phase 12); the verdict API dependency is deliberately not added until then."
    }
}

/** Pure patch-date math (no platform APIs): parses yyyy-MM-dd, returns null when unknown or inconsistent. */
object PatchAge {

    fun ageDays(patchLevel: String?, nowMillis: Long): Int? {
        val date = patchLevel?.let { parsePatchDate(it) } ?: return null
        val days = ((nowMillis - date) / BootWindowMillis.MILLIS_PER_DAY).toInt()
        // A future patch date means device-clock skew, not negative age: unknown, stated.
        return if (days < 0) null else days
    }

    fun freshnessLine(patchLevel: String?, ageDays: Int?): String =
        if (patchLevel == null || ageDays == null) {
            "Security patch level ${patchLevel ?: "unreadable"} (age unknown)"
        } else {
            "Security patch level $patchLevel ($ageDays day(s) old)"
        }

    private fun parsePatchDate(raw: String): Long? {
        val parts = raw.split("-")
        if (parts.size != 3) return null
        val (year, month, day) = parts.map { it.toIntOrNull() ?: return null }
        if (year < 2008 || month !in 1..12 || day !in 1..daysInMonth(year, month)) return null
        // Days-from-civil (Howard Hinnant): days since 1970-01-01, no platform clock.
        val y = if (month <= 2) year - 1 else year
        val era = (if (y >= 0) y else y - 399) / 400
        val yoe = (y - era * 400).toLong()
        val mp = ((month + 9) % 12).toLong()
        val doy = (153 * mp + 2) / 5 + (day - 1)
        val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        val daysSinceEpoch = era * 146097 + doe - 719468
        return daysSinceEpoch * BootWindowMillis.MILLIS_PER_DAY
    }

    private fun daysInMonth(year: Int, month: Int): Int = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        else -> if (isLeap(year)) 29 else 28
    }

    private fun isLeap(year: Int): Boolean = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
}

private object BootWindowMillis {
    const val MILLIS_PER_DAY = 86_400_000L
}

/** Platform source for integrity snapshots (docs/DRD.md §2); Android implements, core defines. */
interface DeviceIntegritySource {
    suspend fun integritySnapshot(): IntegritySnapshot
}
