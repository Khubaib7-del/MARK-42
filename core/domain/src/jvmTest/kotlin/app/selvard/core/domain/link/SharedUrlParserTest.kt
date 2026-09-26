package app.selvard.core.domain.link

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SharedUrlParserTest {

    @Test
    fun nullAndBlankShareIsRejected() {
        assertNull(SharedUrlParser.extractFirstUrl(null))
        assertNull(SharedUrlParser.extractFirstUrl("   "))
        assertNull(SharedUrlParser.extractFirstUrl("plain text without a link"))
    }

    @Test
    fun firstUrlTokenIsFoundPastLeadingWords() {
        assertEquals(
            "https://example.com/x",
            SharedUrlParser.extractFirstUrl("Check this out https://example.com/x wow"),
        )
    }

    @Test
    fun oversizedSharedTextIsBoundedNeverThrows() {
        val huge = "https://example.com/" + "a".repeat(100_000)
        val result = runCatching { SharedUrlParser.extractFirstUrl(huge) }
        assertTrue(result.isSuccess, "share parsing must not throw")
        // Token exceeds the analyzer limit, so nothing usable is extracted.
        assertNull(result.getOrThrow())
    }

    @Test
    fun fuzzedShareTextNeverThrows() {
        val rng = java.util.Random(99L)
        val chars = "htp:/@?#[]%.-0aZ \u00e9\u4e2d\u0000\n\t"
        repeat(1000) {
            val len = rng.nextInt(256)
            val s = buildString { repeat(len) { append(chars[rng.nextInt(chars.length)]) } }
            assertTrue(runCatching { SharedUrlParser.extractFirstUrl(s) }.isSuccess)
        }
    }
}
