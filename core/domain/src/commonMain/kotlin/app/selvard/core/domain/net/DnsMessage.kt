package app.selvard.core.domain.net

/**
 * Minimal, hardening-focused DNS message parser (RFC 1035) for the Network
 * Guardian tunnel. Untrusted-input discipline: every read is bounds-checked,
 * malformed input yields [DnsParseException] - the caller fails closed.
 * Only what filtering needs is parsed: header, question, and A/AAAA answers.
 */
class DnsParseException(message: String) : Exception(message)

data class DnsQuestion(val name: String, val type: Int, val clazz: Int)

data class DnsRecord(val name: String, val type: Int, val data: ByteArray) {
    override fun equals(other: Any?): Boolean =
        other is DnsRecord && name == other.name && type == other.type && data.contentEquals(other.data)

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type
        result = 31 * result + data.contentHashCode()
        return result
    }
}

data class DnsMessage(
    val id: Int,
    val isResponse: Boolean,
    val recursionDesired: Boolean,
    val questions: List<DnsQuestion>,
    val answers: List<DnsRecord>,
)

object DnsType {
    const val A = 1
    const val AAAA = 28
}

object DnsParser {

    fun parse(packet: ByteArray): DnsMessage {
        if (packet.size < 12) throw DnsParseException("packet shorter than DNS header")
        val id = readU16(packet, 0)
        val flags = readU16(packet, 2)
        val qd = readU16(packet, 4)
        val an = readU16(packet, 6)
        if (qd > MAX_QUESTIONS || an > MAX_RECORDS) throw DnsParseException("implausible record counts")
        var offset = 12
        val questions = mutableListOf<DnsQuestion>()
        repeat(qd) {
            val (name, next) = readName(packet, offset)
            if (next + 4 > packet.size) throw DnsParseException("truncated question")
            questions += DnsQuestion(name, readU16(packet, next), readU16(packet, next + 2))
            offset = next + 4
        }
        val answers = mutableListOf<DnsRecord>()
        repeat(an) {
            val (name, next) = readName(packet, offset)
            if (next + 10 > packet.size) throw DnsParseException("truncated answer header")
            val type = readU16(packet, next)
            val rdLength = readU16(packet, next + 8)
            val rdataStart = next + 10
            if (rdataStart + rdLength > packet.size) throw DnsParseException("rdata exceeds packet")
            answers += DnsRecord(name, type, packet.copyOfRange(rdataStart, rdataStart + rdLength))
            offset = rdataStart + rdLength
        }
        return DnsMessage(
            id = id,
            isResponse = (flags and 0x8000) != 0,
            recursionDesired = (flags and 0x0100) != 0,
            questions = questions,
            answers = answers,
        )
    }

    /** True if every A/AAAA answer address is contained in the given 16-byte IPv6-or-IPv4-mapped block. */
    fun answerAddressesV4(message: DnsMessage): List<ByteArray> =
        message.answers.filter { it.type == DnsType.A && it.data.size == 4 }.map { it.data }

    /** Builds a REFUSED response for the given query, mirroring id, question count, and question section. */
    fun buildRefused(query: DnsMessage): ByteArray {
        val out = mutableListOf<Byte>()
        writeU16(out, query.id)
        // QR=1, opcode 0, AA=0, TC=0, RD echoed, RA=0, Z=0, RCODE=5 (REFUSED)
        val flags = 0x8000 or (if (query.recursionDesired) 0x0100 else 0)
        writeU16(out, flags)
        writeU16(out, query.questions.size)
        writeU16(out, 0) // ancount
        writeU16(out, 0) // nscount
        writeU16(out, 0) // arcount
        query.questions.forEach { q ->
            q.name.split('.').filter { it.isNotEmpty() }.forEach { label ->
                require(label.length <= 63) { "label too long" }
                out.add(label.length.toByte())
                label.encodeToByteArray().forEach { out.add(it) }
            }
            out.add(0)
            writeU16(out, q.type)
            writeU16(out, q.clazz)
        }
        return out.toByteArray()
    }

    fun readU16(b: ByteArray, offset: Int): Int =
        if (offset + 2 > b.size) throw DnsParseException("u16 out of bounds")
        else ((b[offset].toInt() and 0xFF) shl 8) or (b[offset + 1].toInt() and 0xFF)

    private fun readName(packet: ByteArray, start: Int): Pair<String, Int> {
        val labels = mutableListOf<String>()
        var offset = start
        var jumps = 0
        var next: Int? = null
        while (true) {
            if (offset >= packet.size) throw DnsParseException("name runs past packet")
            val length = packet[offset].toInt() and 0xFF
            when {
                length == 0 -> {
                    offset++
                    break
                }
                length and 0xC0 == 0xC0 -> {
                    if (offset + 2 > packet.size) throw DnsParseException("truncated pointer")
                    val target = ((length and 0x3F) shl 8) or (packet[offset + 1].toInt() and 0xFF)
                    if (next == null) next = offset + 2
                    jumps++
                    if (jumps > MAX_NAME_JUMPS) throw DnsParseException("name pointer loop")
                    if (target >= offset) throw DnsParseException("forward name pointer")
                    offset = target
                }
                length > MAX_LABEL_LENGTH -> throw DnsParseException("label too long")
                else -> {
                    if (offset + 1 + length > packet.size) throw DnsParseException("label runs past packet")
                    labels += packet.decodeToString(offset + 1, offset + 1 + length)
                    offset += 1 + length
                }
            }
        }
        return labels.joinToString(".").lowercase() to (next ?: offset)
    }

    private fun writeU16(out: MutableList<Byte>, value: Int) {
        out.add(((value shr 8) and 0xFF).toByte())
        out.add((value and 0xFF).toByte())
    }

    private const val MAX_LABEL_LENGTH = 63
    private const val MAX_NAME_JUMPS = 8
    private const val MAX_QUESTIONS = 16
    private const val MAX_RECORDS = 64
}
