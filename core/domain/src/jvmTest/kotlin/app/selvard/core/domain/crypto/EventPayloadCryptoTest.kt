package app.selvard.core.domain.crypto

import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.KeyGenerator
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class EventPayloadCryptoTest {

    private val key = KeyGenerator.getInstance("AES").apply { init(256, SecureRandom()) }.generateKey()
    private val crypto = EventPayloadCrypto(key)

    @Test
    fun encryptDecryptRoundTrips() {
        val plaintext = "canonical-event-json-payload".toByteArray()
        assertTrue(plaintext.contentEquals(crypto.decrypt(crypto.encrypt(plaintext))))
    }

    @Test
    fun tamperedCiphertextFailsAuthentication() {
        val blob = crypto.encrypt("payload".toByteArray())
        blob[blob.size - 1] = (blob[blob.size - 1].toInt() xor 0x01).toByte()
        assertFailsWith<AEADBadTagException> { crypto.decrypt(blob) }
    }

    @Test
    fun everyEncryptionUsesAFreshIv() {
        val first = crypto.encrypt("same-input".toByteArray())
        val second = crypto.encrypt("same-input".toByteArray())
        assertTrue(!first.contentEquals(second), "IV reuse would be a cryptographic defect")
    }

    @Test
    fun truncatedInputIsRejected() {
        val blob = crypto.encrypt("payload".toByteArray())
        assertFailsWith<IllegalArgumentException> {
            crypto.decrypt(blob.copyOf(EventPayloadCrypto.IV_LENGTH_BYTES))
        }
    }
}
