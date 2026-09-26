package app.selvard.core.domain.net

/**
 * IPv4/UDP packet codec for the DNS tunnel: detects tunneled DNS queries and
 * mirrors a query's addressing into a valid reply packet (RFC 768/791).
 * Pure ByteArray logic so every branch is unit-testable off-device.
 */
object IpPacketCodec {
    const val UDP_PROTO = 17
    const val DNS_PORT = 53
    private const val IP_HEADER_LEN = 20
    private const val UDP_HEADER_LEN = 8
    private const val MAX_PACKET = 1500

    fun isIpv4UdpToDnsPort(packet: ByteArray): Boolean {
        if (packet.size < IP_HEADER_LEN + UDP_HEADER_LEN) return false
        if (packet[0].toInt() shr 4 != 4) return false
        val ihl = (packet[0].toInt() and 0x0F) * 4
        if (packet.size < ihl + UDP_HEADER_LEN) return false
        if (packet[9].toInt() and 0xFF != UDP_PROTO) return false
        return readU16(packet, ihl + 2) == DNS_PORT
    }

    /** Mirrors the query's addressing into an IPv4/UDP reply carrying dnsPayload. */
    fun buildUdpResponsePacket(query: ByteArray, dnsPayload: ByteArray): ByteArray? {
        if (query.size < IP_HEADER_LEN + UDP_HEADER_LEN) return null
        val ihl = (query[0].toInt() and 0x0F) * 4
        val clientPort = readU16(query, ihl)
        val totalLen = IP_HEADER_LEN + UDP_HEADER_LEN + dnsPayload.size
        if (totalLen > MAX_PACKET) return null
        val out = ByteArray(totalLen)
        out[0] = 0x45
        out[8] = 64 // ttl
        out[9] = UDP_PROTO.toByte()
        System.arraycopy(query, 16, out, 12, 4) // src = original dest (VPN DNS)
        System.arraycopy(query, 12, out, 16, 4) // dst = original src (client)
        writeU16(out, 2, totalLen)
        writeU16(out, 10, ipChecksum(out, IP_HEADER_LEN))
        writeU16(out, IP_HEADER_LEN, DNS_PORT)
        writeU16(out, IP_HEADER_LEN + 2, clientPort)
        writeU16(out, IP_HEADER_LEN + 4, UDP_HEADER_LEN + dnsPayload.size)
        writeU16(out, IP_HEADER_LEN + 6, 0) // UDP checksum 0 is legal for IPv4
        System.arraycopy(dnsPayload, 0, out, IP_HEADER_LEN + UDP_HEADER_LEN, dnsPayload.size)
        return out
    }

    fun ipChecksum(header: ByteArray, length: Int): Int {
        var sum = 0L
        var i = 0
        while (i < length) {
            sum += ((header[i].toLong() and 0xFF) shl 8) or (header[i + 1].toLong() and 0xFF)
            i += 2
        }
        while (sum shr 16 != 0L) sum = (sum and 0xFFFF) + (sum shr 16)
        return (sum.toInt().inv()) and 0xFFFF
    }

    fun readU16(b: ByteArray, offset: Int): Int =
        ((b[offset].toInt() and 0xFF) shl 8) or (b[offset + 1].toInt() and 0xFF)

    private fun writeU16(b: ByteArray, offset: Int, value: Int) {
        b[offset] = ((value shr 8) and 0xFF).toByte()
        b[offset + 1] = (value and 0xFF).toByte()
    }
}
