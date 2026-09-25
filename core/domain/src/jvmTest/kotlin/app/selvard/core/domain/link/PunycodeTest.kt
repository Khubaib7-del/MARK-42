package app.selvard.core.domain.link

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PunycodeTest {

    @Test
    fun decodesKnownRfc3492Examples() {
        assertEquals("b\u00fccher", Punycode.decodePunycodeLabel("xn--bcher-kva"))
        assertEquals("b\u00fccher", Punycode.decodePunycodeLabel("bcher-kva"))
        assertEquals("caf\u00e9", Punycode.decodePunycodeLabel("xn--caf-dma"))
    }

    @Test
    fun plainAsciiLabelsPassThroughUnchanged() {
        assertEquals("example", Punycode.revealLabel("example"))
    }

    @Test
    fun malformedPunycodeReturnsNullNotException() {
        assertNull(Punycode.decodePunycodeLabel("xn--"))
        assertNull(Punycode.decodePunycodeLabel("xn--%zz"))
    }

    @Test
    fun prefixDetection() {
        assertTrue(Punycode.hasPrefix("xn--anything"))
        assertFalse(Punycode.hasPrefix("example"))
    }
}
