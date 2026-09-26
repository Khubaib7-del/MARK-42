package app.selvard.core.domain.identity

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Identity kinds the vault accepts. Email only: phones and IDs are out of scope. */
enum class IdentityKind { EMAIL }

/** How a breach check discloses the identity to HIBP. */
enum class HibpQueryMode {
    /** Only the first 6 SHA-1 hex chars leave the device (preferred, data-minimizing). */
    K_ANONYMITY_RANGE,
    /** The full address is sent; requires explicit, separately-worded consent. */
    FULL_ADDRESS,
}

/** Lifecycle of one declared identity's exposure knowledge. Never claims safety. */
enum class ExposureState { NOT_CHECKED, NO_BREACHES_FOUND, BREACHED, CHECK_FAILED }

object IdentityExposure {

    const val DISCLOSURE_VERSION = 1
    const val MAX_EMAIL_LENGTH = 254
    const val MAX_BREACH_NAMES_RECORDED = 10
    const val MAX_DATA_CLASSES = 12

    /** Exact disclosure shown at consent time. Changing it bumps DISCLOSURE_VERSION. */
    fun consentDisclosure(mode: HibpQueryMode): String = when (mode) {
        HibpQueryMode.K_ANONYMITY_RANGE ->
            "Selvard will hash this address with SHA-1 and send only the first 6 characters " +
                "of the hash to Have I Been Pwned (haveibeenpwned.com) to download candidate " +
                "breach matches; matching happens on this device. The full address never " +
                "leaves the device for this check. HIBP's index may be incomplete: a check " +
                "with no matches never means the address is safe. Breach data by HIBP; " +
                "an HIBP subscription key is required. The address is stored encrypted on " +
                "this device until you delete it."
        HibpQueryMode.FULL_ADDRESS ->
            "Selvard will send the full address to Have I Been Pwned (haveibeenpwned.com) " +
                "for a direct breach lookup. HIBP will see the complete address. HIBP's " +
                "index may be incomplete: a check with no matches never means the address " +
                "is safe. Breach data by HIBP; an HIBP subscription key is required. " +
                "The address is stored encrypted on this device until you delete it."
    }

    /** Canonical form for hashing and comparison: trimmed, lowercased. */
    fun normalizeEmail(raw: String): String {
        val clean = raw.trim().lowercase()
        require(clean.isNotEmpty()) { "address must not be blank" }
        require(clean.length <= MAX_EMAIL_LENGTH) { "address too long" }
        require(!clean.any { it.isWhitespace() }) { "address must not contain whitespace" }
        val parts = clean.split("@")
        require(parts.size == 2 && parts[0].isNotEmpty() && parts[1].isNotEmpty()) {
            "address must be of the form local@domain"
        }
        require(parts[0].length <= 64) { "local part too long" }
        require(parts[1].contains('.')) { "domain must contain a dot" }
        return clean
    }

    /** Masked display form: keeps no more than the first char of the local part. */
    fun maskEmail(normalized: String): String {
        val (local, domain) = normalized.split("@")
        return "${local.first()}***@$domain"
    }

    /** Uppercase SHA-1 hex of the normalized address (HIBP range format). */
    fun addressHash(normalized: String): String = HibpSha1.hexUpper(normalized.toByteArray(Charsets.UTF_8))

    fun rangePrefix(hashHex: String): String {
        require(hashHex.length == 40 && hashHex.all { it in '0'..'9' || it in 'A'..'F' }) {
            "not an uppercase SHA-1 hex string"
        }
        return hashHex.take(6)
    }

    fun rangeSuffix(hashHex: String): String = hashHex.drop(6)

    /** Device-side k-anonymity match: suffix equality against the range entries. */
    fun matchRange(hashHex: String, entries: List<RangeEntry>): List<String> {
        val suffix = rangeSuffix(hashHex)
        return entries.filter { it.hashSuffix.uppercase() == suffix }.flatMap { it.websites }.distinct().sorted()
    }

    /** Honest one-line summary of a check outcome. Never says safe/clean. */
    fun summaryLine(state: ExposureState, breachCount: Int): String = when (state) {
        ExposureState.NOT_CHECKED -> "Not yet checked against breach intelligence"
        ExposureState.NO_BREACHES_FOUND ->
            "No matches in the HIBP response (covers only HIBP's index; never means safe)"
        ExposureState.BREACHED ->
            "Found in $breachCount breach(es) per HIBP (breach names below; change passwords, enable 2FA)"
        ExposureState.CHECK_FAILED -> "Check failed (reason recorded; nothing inferred)"
    }
}

/** One declared identity: the address itself is encrypted by the vault, never here. */
data class DeclaredIdentity(
    val identityId: String,
    val kind: IdentityKind,
    val normalized: String,
    val mode: HibpQueryMode,
    val consentedAtMillis: Long,
    val disclosureVersion: Int,
) {
    init {
        require(identityId.isNotBlank()) { "identityId must not be blank" }
        require(consentedAtMillis >= 0) { "consent time must be non-negative" }
        require(disclosureVersion == IdentityExposure.DISCLOSURE_VERSION) { "stale disclosure version" }
    }

    val masked: String get() = IdentityExposure.maskEmail(normalized)
}

/** Minimized breach fact: names, dates and data classes only (ADR-010). */
data class BreachRecord(
    val name: String,
    val breachDate: String?,
    val dataClasses: List<String>,
) {
    init {
        require(name.isNotBlank()) { "breach name must not be blank" }
    }
}

/** One k-anonymity range entry from HIBP (hash suffix + breach names). */
@Serializable
data class RangeEntry(
    val hashSuffix: String,
    val websites: List<String> = emptyList(),
)

/** Full breach DTO from HIBP; unknown fields ignored (their schema evolves). */
@Serializable
data class HibpBreachDto(
    @kotlinx.serialization.SerialName("Name") val name: String,
    @kotlinx.serialization.SerialName("Title") val title: String? = null,
    @kotlinx.serialization.SerialName("BreachDate") val breachDate: String? = null,
    @kotlinx.serialization.SerialName("DataClasses") val dataClasses: List<String>? = null,
)

object HibpResponses {

    private val lenient = Json { ignoreUnknownKeys = true }

    fun parseRange(raw: String): List<RangeEntry> =
        lenient.decodeFromString(kotlinx.serialization.builtins.ListSerializer(RangeEntry.serializer()), raw)

    /** Minimizes full breach objects to names, dates and capped data classes. */
    fun parseBreaches(raw: String): List<BreachRecord> =
        lenient.decodeFromString(
            kotlinx.serialization.builtins.ListSerializer(HibpBreachDto.serializer()),
            raw,
        ).map {
            BreachRecord(
                name = it.name,
                breachDate = it.breachDate,
                dataClasses = (it.dataClasses ?: emptyList()).take(IdentityExposure.MAX_DATA_CLASSES),
            )
        }
}

/** Pure-Kotlin SHA-1 (FIPS 180-4): commonMain cannot use java.security. */
object HibpSha1 {

    fun hexUpper(input: ByteArray): String {
        val padded = pad(input)
        var h0 = 0x67452301
        var h1 = 0xEFCDAB89.toInt()
        var h2 = 0x98BADCFE.toInt()
        var h3 = 0x10325476
        var h4 = 0xC3D2E1F0.toInt()
        val w = IntArray(80)
        var off = 0
        while (off < padded.size) {
            for (i in 0 until 16) {
                w[i] = ((padded[off].toInt() and 0xFF) shl 24) or
                    ((padded[off + 1].toInt() and 0xFF) shl 16) or
                    ((padded[off + 2].toInt() and 0xFF) shl 8) or
                    (padded[off + 3].toInt() and 0xFF)
                off += 4
            }
            for (i in 16 until 80) {
                val x = w[i - 3] xor w[i - 8] xor w[i - 14] xor w[i - 16]
                w[i] = (x shl 1) or (x ushr 31)
            }
            var a = h0
            var b = h1
            var c = h2
            var d = h3
            var e = h4
            for (i in 0 until 80) {
                val (f, k) = when (i / 20) {
                    0 -> Pair((b and c) or (b.inv() and d), 0x5A827999)
                    1 -> Pair(b xor c xor d, 0x6ED9EBA1)
                    2 -> Pair((b and c) or (b and d) or (c and d), 0x8F1BBCDC.toInt())
                    else -> Pair(b xor c xor d, 0xCA62C1D6.toInt())
                }
                val tmp = ((a shl 5) or (a ushr 27)) + f + e + k + w[i]
                e = d
                d = c
                c = (b shl 30) or (b ushr 2)
                b = a
                a = tmp
            }
            h0 += a
            h1 += b
            h2 += c
            h3 += d
            h4 += e
        }
        return intHex(h0) + intHex(h1) + intHex(h2) + intHex(h3) + intHex(h4)
    }

    private fun pad(input: ByteArray): ByteArray {
        val bitLen = input.size.toLong() * 8
        val out = mutableListOf<Byte>()
        out.addAll(input.toList())
        out.add(0x80.toByte())
        while (out.size % 64 != 56) out.add(0)
        for (i in 7 downTo 0) out.add(((bitLen ushr (i * 8)) and 0xFF).toByte())
        return out.toByteArray()
    }

    private fun intHex(v: Int): String =
        ((v ushr 28) and 0xF).toString(16).uppercase() +
            ((v ushr 24) and 0xF).toString(16).uppercase() +
            ((v ushr 20) and 0xF).toString(16).uppercase() +
            ((v ushr 16) and 0xF).toString(16).uppercase() +
            ((v ushr 12) and 0xF).toString(16).uppercase() +
            ((v ushr 8) and 0xF).toString(16).uppercase() +
            ((v ushr 4) and 0xF).toString(16).uppercase() +
            (v and 0xF).toString(16).uppercase()
}
