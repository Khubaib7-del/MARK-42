package app.selvard.identity

import android.content.Context
import app.selvard.data.KeystoreKeyring
import java.io.File
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Encrypted identity vault. Declared addresses are stored AES-256-GCM encrypted
 * under a per-install data key (itself wrapped by the Keystore master key), in a
 * private file — never in logs, prefs, backups, or events. Masked forms only
 * leave the vault; the plaintext address leaves only inside an HIBP TLS request
 * the user explicitly consented to (full-address mode: never in range mode).
 */
class IdentityVault(context: Context, private val keyring: KeystoreKeyring = KeystoreKeyring()) {

    private val file = File(context.filesDir, VAULT_FILE)
    private val mutex = Mutex()

    suspend fun load(): List<VaultEntry> = mutex.withLock { readAll() }

    suspend fun add(entry: VaultEntry) = mutex.withLock {
        val all = readAll().filterNot { it.identityId == entry.identityId } + entry
        writeAll(all)
    }

    suspend fun remove(identityId: String): Boolean = mutex.withLock {
        val all = readAll()
        val kept = all.filterNot { it.identityId == identityId }
        if (kept.size == all.size) return false
        writeAll(kept)
        true
    }

    /** Deletes the vault file itself: the full deletion flow. */
    suspend fun deleteAll(): Unit = mutex.withLock {
        if (file.exists()) file.delete()
    }

    private fun readAll(): List<VaultEntry> {
        if (!file.exists()) return emptyList()
        val blob = file.readBytes()
        if (blob.isEmpty()) return emptyList()
        val json = String(decrypt(blob), Charsets.UTF_8)
        return VaultCodec.decode(json)
    }

    private fun writeAll(entries: List<VaultEntry>) {
        file.parentFile?.mkdirs()
        file.writeBytes(encrypt(VaultCodec.encode(entries).toByteArray(Charsets.UTF_8)))
    }

    private fun dataKey(): SecretKey {
        val wrapped = File(file.parentFile, KEY_FILE)
        val master = keyring.masterKey()
        if (wrapped.exists()) {
            val blob = wrapped.readBytes()
            val iv = blob.copyOfRange(0, GCM_IV_BYTES)
            val ct = blob.copyOfRange(GCM_IV_BYTES, blob.size)
            return SecretKeySpec(aesGcm(master, iv, ct, decrypt = true), "AES")
        }
        val fresh = ByteArray(32).also { java.security.SecureRandom().nextBytes(it) }
        val enc = Cipher.getInstance(TRANSFORMATION).also {
            it.init(Cipher.ENCRYPT_MODE, master)
        }
        wrapped.parentFile?.mkdirs()
        wrapped.writeBytes(enc.iv + enc.doFinal(fresh))
        return SecretKeySpec(fresh, "AES")
    }

    private fun encrypt(plain: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION).also { it.init(Cipher.ENCRYPT_MODE, dataKey()) }
        return cipher.iv + cipher.doFinal(plain)
    }

    private fun decrypt(blob: ByteArray): ByteArray {
        require(blob.size > GCM_IV_BYTES) { "vault blob too short" }
        val iv = blob.copyOfRange(0, GCM_IV_BYTES)
        val ct = blob.copyOfRange(GCM_IV_BYTES, blob.size)
        return aesGcm(dataKey(), iv, ct, decrypt = true)
    }

    private fun aesGcm(key: SecretKey, iv: ByteArray, input: ByteArray, decrypt: Boolean): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_BITS, iv)
        if (decrypt) cipher.init(Cipher.DECRYPT_MODE, key, spec) else cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(input)
    }

    companion object {
        const val VAULT_FILE = "identity_vault.bin"
        const val KEY_FILE = "identity_vault.key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_IV_BYTES = 12
        const val GCM_TAG_BITS = 128
    }
}

/** One stored declaration: duplicates the core DeclaredIdentity fields for the vault codec. */
data class VaultEntry(
    val identityId: String,
    val normalized: String,
    val mode: String,
    val consentedAtMillis: Long,
    val disclosureVersion: Int,
)

internal object VaultCodec {

    fun encode(entries: List<VaultEntry>): String = buildString {
        append('[')
        entries.forEachIndexed { i, e ->
            if (i > 0) append(',')
            append("{\"id\":${q(e.identityId)},\"n\":${q(e.normalized)},\"m\":${q(e.mode)},")
            append("\"t\":${e.consentedAtMillis},\"v\":${e.disclosureVersion}}")
        }
        append(']')
    }

    fun decode(raw: String): List<VaultEntry> {
        val text = raw.trim()
        if (text == "[]" || text.isEmpty()) return emptyList()
        require(text.startsWith('[') && text.endsWith(']')) { "vault payload malformed" }
        val body = text.substring(1, text.length - 1)
        if (body.isBlank()) return emptyList()
        val out = mutableListOf<VaultEntry>()
        var depth = 0
        var start = 0
        body.forEachIndexed { i, c ->
            if (c == '{') {
                if (depth == 0) start = i
                depth++
            } else if (c == '}') {
                depth--
                require(depth >= 0) { "vault payload malformed" }
                if (depth == 0) out.add(parseOne(body.substring(start, i + 1)))
            }
        }
        require(depth == 0) { "vault payload malformed" }
        require(out.size <= MAX_ENTRIES) { "vault payload too large" }
        return out
    }

    private fun parseOne(obj: String): VaultEntry {
        // Fixed-format record: tampering with the field names breaks the seal
        // (GCM auth) before this runs; structural checks below bound the damage
        // a corrupted-but-authentic record can do.
        require(obj.length <= MAX_ENTRY_CHARS) { "vault entry too large" }
        fun str(key: String): String {
            val k = "\"$key\":"
            val at = obj.indexOf(k)
            require(at >= 0) { "vault entry missing $key" }
            var i = at + k.length
            require(i < obj.length) { "vault entry $key truncated" }
            require(obj[i] == '"') { "vault entry $key not a string" }
            i++
            val sb = StringBuilder()
            while (i < obj.length && obj[i] != '"') {
                if (obj[i] == '\\') {
                    i++
                    require(i < obj.length) { "vault entry $key truncated escape" }
                    sb.append(obj[i])
                } else {
                    sb.append(obj[i])
                }
                i++
            }
            require(i < obj.length) { "vault entry $key unterminated" }
            return sb.toString()
        }
        fun num(key: String): Long {
            val k = "\"$key\":"
            val at = obj.indexOf(k)
            require(at >= 0) { "vault entry missing $key" }
            val rest = obj.substring(at + k.length).takeWhile { it != ',' && it != '}' }
            require(rest.isNotEmpty() && rest.length <= MAX_NUMBER_CHARS && rest.all { it.isDigit() }) {
                "vault entry $key not a number"
            }
            return rest.toLong()
        }
        val id = str("id").also { require(it.length <= MAX_FIELD_CHARS) { "vault entry id too long" } }
        val normalized = str("n")
            .also { require(it.length <= MAX_FIELD_CHARS) { "vault entry address too long" } }
        val mode = str("m").also { require(it.length <= MAX_FIELD_CHARS) { "vault entry mode too long" } }
        return VaultEntry(id, normalized, mode, num("t"), num("v").toInt())
    }

    private fun q(s: String): String = "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
    private const val MAX_ENTRIES = 512
    private const val MAX_ENTRY_CHARS = 4096
    private const val MAX_FIELD_CHARS = 1024
    private const val MAX_NUMBER_CHARS = 20
}
