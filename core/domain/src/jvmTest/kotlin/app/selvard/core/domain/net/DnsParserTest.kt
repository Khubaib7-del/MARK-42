package app.selvard.core.domain.net

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DnsParserTest {

    private fun header(id: Int, flags: Int, qd: Int, an: Int) = byteArrayOf(
        (id shr 8).toByte(), id.toByte(),
        (flags shr 8).toByte(), flags.toByte(),
        (qd shr 8).toByte(), qd.toByte(),
        (an shr 8).toByte(), an.toByte(),
        0, 0, 0, 0,
    )

    private fun nameLabels(vararg labels: String) = buildList {
        labels.forEach { l ->
            add(l.length.toByte())
            l.encodeToByteArray().forEach { add(it) }
        }
        add(0.toByte())
    }.toByteArray()

    @Test
    fun parsesQueryWithSingleQuestion() {
        val packet = header(0x1234, 0x0100, 1, 0) +
            nameLabels("www", "example", "com") +
            byteArrayOf(0, 1, 0, 1) // type A, class IN
        val message = DnsParser.parse(packet)
        assertEquals(0x1234, message.id)
        assertTrue(!message.isResponse)
        assertEquals(1, message.questions.size)
        assertEquals("www.example.com", message.questions[0].name)
        assertEquals(DnsType.A, message.questions[0].type)
    }

    @Test
    fun parsesResponseWithAnswerAndAddresses() {
        val packet = header(0x4242, 0x8180, 1, 1) +
            nameLabels("www", "example", "com") +
            byteArrayOf(0, 1, 0, 1) +
            byteArrayOf(0xC0.toByte(), 0x0C) + // pointer to question name
            byteArrayOf(0, 1, 0, 1, 0, 0, 0, 60, 0, 4) + // A, TTL 60, rdlength 4
            byteArrayOf(93.toByte(), 184.toByte(), 216.toByte(), 34)
        val message = DnsParser.parse(packet)
        assertTrue(message.isResponse)
        val addresses = DnsParser.answerAddressesV4(message)
        assertEquals(1, addresses.size)
        assertEquals(93, addresses[0][0].toInt() and 0xFF)
    }

    @Test
    fun malformedInputThrowsDnsParseExceptionNeverOtherExceptions() {
        assertFailsWith<DnsParseException> { DnsParser.parse(byteArrayOf(1, 2, 3)) }
        // Truncated rdata
        val packet = header(1, 0x8180, 0, 1) +
            byteArrayOf(0xC0.toByte(), 0x0C, 0, 1, 0, 1, 0, 0, 0, 60, 0, 8) +
            byteArrayOf(1, 2, 3) // claims 8, provides 3
        assertFailsWith<DnsParseException> { DnsParser.parse(packet) }
    }

    @Test
    fun implausibleCountsRejected() {
        assertFailsWith<DnsParseException> { DnsParser.parse(header(1, 0, 999, 0)) }
        assertFailsWith<DnsParseException> { DnsParser.parse(header(1, 0, 0, 999)) }
    }

    @Test
    fun namePointerLoopsRejected() {
        // name at offset 12 points to itself
        val packet = header(1, 0x0100, 1, 0) + byteArrayOf(0xC0.toByte(), 0x0C)
        assertFailsWith<DnsParseException> { DnsParser.parse(packet) }
    }

    @Test
    fun fuzzedPacketsNeverThrowUnexpectedExceptions() {
        val rng = java.util.Random(77L)
        repeat(3000) {
            val size = rng.nextInt(200)
            val packet = ByteArray(size)
            rng.nextBytes(packet)
            if (size >= 2) {
                packet[0] = 0
                packet[1] = 1 // plausible id
            }
            val result = runCatching { DnsParser.parse(packet) }
            val exception = result.exceptionOrNull()
            assertTrue(
                exception == null || exception is DnsParseException,
                "unexpected exception type: ${exception?.javaClass}",
            )
        }
    }

    @Test
    fun refusedResponseMirrorsQueryIdAndQuestion() {
        val query = DnsParser.parse(
            header(0x0BAD.toShort().toInt() and 0xFFFF, 0x0100, 1, 0) +
                nameLabels("blocked", "example", "com") +
                byteArrayOf(0, 1, 0, 1),
        )
        val refused = DnsParser.parse(DnsParser.buildRefused(query))
        assertEquals(query.id, refused.id)
        assertTrue(refused.isResponse)
        assertEquals(query.questions, refused.questions)
        assertTrue(refused.answers.isEmpty())
    }
}
