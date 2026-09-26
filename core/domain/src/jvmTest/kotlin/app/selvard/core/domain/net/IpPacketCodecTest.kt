package app.selvard.core.domain.net

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IpPacketCodecTest {

    /** A valid IPv4/UDP query to 10.111.222.1:53 from 10.111.222.3:54321. */
    private fun dnsQueryPacket(payload: ByteArray): ByteArray {
        val out = ByteArray(28 + payload.size)
        out[0] = 0x45
        out[2] = 0
        out[3] = (28 + payload.size).toByte()
        out[8] = 64
        out[9] = IpPacketCodec.UDP_PROTO.toByte()
        byteArrayOf(10, 111.toByte(), 222.toByte(), 3).copyInto(out, 12) // client
        byteArrayOf(10, 111.toByte(), 222.toByte(), 1).copyInto(out, 16) // VPN DNS
        out[20] = (54321 shr 8).toByte()
        out[21] = 54321.toByte()
        out[22] = 0
        out[23] = 53.toByte()
        payload.copyInto(out, 28)
        return out
    }

    @Test
    fun detectsIpv4UdpDnsQueries() {
        val packet = dnsQueryPacket(byteArrayOf(0, 1, 0, 0, 0, 0, 0, 0))
        assertTrue(IpPacketCodec.isIpv4UdpToDnsPort(packet))
    }

    @Test
    fun rejectsNonDnsPackets() {
        // Too short
        assertFalse(IpPacketCodec.isIpv4UdpToDnsPort(ByteArray(10)))
        // IPv6
        val v6 = dnsQueryPacket(ByteArray(8)).also { it[0] = 0x60 }
        assertFalse(IpPacketCodec.isIpv4UdpToDnsPort(v6))
        // UDP but wrong port
        val otherPort = dnsQueryPacket(ByteArray(8)).also { it[23] = 80.toByte() }
        assertFalse(IpPacketCodec.isIpv4UdpToDnsPort(otherPort))
        // TCP (proto 6)
        val tcp = dnsQueryPacket(ByteArray(8)).also { it[9] = 6.toByte() }
        assertFalse(IpPacketCodec.isIpv4UdpToDnsPort(tcp))
    }

    @Test
    fun responseMirrorsAddressingAndCarriesPayload() {
        val dns = byteArrayOf(0xAB.toByte(), 0xCD.toByte(), 0x81.toByte(), 0x80.toByte(), 0, 0, 0, 0, 0, 0, 0, 0)
        val query = dnsQueryPacket(dns)
        val response = IpPacketCodec.buildUdpResponsePacket(query, dns)!!

        assertEquals(40, response.size)
        // Source/destination swapped
        assertEquals(10, response[12].toInt())
        assertEquals(111, response[13].toInt() and 0xFF)
        assertEquals(222, response[14].toInt() and 0xFF)
        assertEquals(1, response[15].toInt())
        assertEquals(10, response[16].toInt())
        assertEquals(3, response[19].toInt())
        // Ports mirrored
        assertEquals(53, IpPacketCodec.readU16(response, 20))
        assertEquals(54321, IpPacketCodec.readU16(response, 22))
        // UDP length field
        assertEquals(8 + dns.size, IpPacketCodec.readU16(response, 24))
        // Payload intact
        assertTrue(dns.contentEquals(response.copyOfRange(28, response.size)))
    }

    @Test
    fun ipHeaderChecksumIsValid() {
        val dns = ByteArray(12)
        val response = IpPacketCodec.buildUdpResponsePacket(dnsQueryPacket(dns), dns)!!
        // Property: sum of all 16-bit header words (checksum included) folds to 0xFFFF.
        var sum = 0L
        for (i in 0 until 20 step 2) {
            sum += ((response[i].toLong() and 0xFF) shl 8) or (response[i + 1].toLong() and 0xFF)
        }
        while (sum shr 16 != 0L) sum = (sum and 0xFFFF) + (sum shr 16)
        assertEquals(0xFFFF, sum.toInt(), "header checksum must validate to 0xFFFF")
    }

    @Test
    fun checksumMatchesIndependentlyComputedGoldenValue() {
        // Golden value computed independently (Python one's-complement sum):
        // 0x45 00 00 28 00 00 00 00 40 11 00 00 | 10.111.222.1 -> 10.111.222.3
        val dns = ByteArray(12)
        val header = IpPacketCodec.buildUdpResponsePacket(dnsQueryPacket(dns), dns)!!
            .copyOfRange(0, 20).also { it[10] = 0; it[11] = 0 }
        assertEquals(0xA9E2, IpPacketCodec.ipChecksum(header, 20))
    }

    @Test
    fun rejectsMalformedQueries() {
        assertNull(IpPacketCodec.buildUdpResponsePacket(ByteArray(10), ByteArray(4)))
        assertNull(IpPacketCodec.buildUdpResponsePacket(ByteArray(28), ByteArray(1500)))
    }
}
