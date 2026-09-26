package app.selvard.core.domain.net

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Phase 10 adversarial bounds: every malformed length field the tunnel can
 * carry must fail closed (false/null/reject), never index out of bounds.
 */
class IpPacketCodecBoundsTest {

    private fun basePacket(): ByteArray {
        val out = ByteArray(28)
        out[0] = 0x45
        out[9] = IpPacketCodec.UDP_PROTO.toByte()
        out[22] = 0
        out[23] = 53.toByte()
        return out
    }

    @Test
    fun absurdIhlValuesFailClosed() {
        // IHL 0 and IHL 15 (60 bytes, beyond this short packet) must not crash.
        val ihlZero = basePacket().also { it[0] = 0x40 }
        assertFalse(IpPacketCodec.isIpv4UdpToDnsPort(ihlZero))
        assertNull(IpPacketCodec.buildUdpResponsePacket(ihlZero, ByteArray(4)))
        val ihlMax = basePacket().also { it[0] = 0x4F }
        assertFalse(IpPacketCodec.isIpv4UdpToDnsPort(ihlMax))
        assertNull(IpPacketCodec.buildUdpResponsePacket(ihlMax, ByteArray(4)))
    }

    @Test
    fun truncatedPacketsFailClosed() {
        assertFalse(IpPacketCodec.isIpv4UdpToDnsPort(ByteArray(0)))
        assertFalse(IpPacketCodec.isIpv4UdpToDnsPort(ByteArray(27)))
        assertNull(IpPacketCodec.buildUdpResponsePacket(ByteArray(10), ByteArray(4)))
    }

    @Test
    fun readU16RejectsOutOfBoundsInsteadOfCrashing() {
        assertFailsWith<IllegalArgumentException> { IpPacketCodec.readU16(ByteArray(4), 3) }
        assertFailsWith<IllegalArgumentException> { IpPacketCodec.readU16(ByteArray(4), -1) }
        assertEquals(0x1234, IpPacketCodec.readU16(byteArrayOf(0x12, 0x34), 0))
    }

    @Test
    fun checksumWindowIsValidated() {
        assertFailsWith<IllegalArgumentException> { IpPacketCodec.ipChecksum(ByteArray(20), 21) }
        assertFailsWith<IllegalArgumentException> { IpPacketCodec.ipChecksum(ByteArray(20), 22) }
        assertTrue(IpPacketCodec.ipChecksum(ByteArray(20), 20) in 0..0xFFFF)
    }
}
