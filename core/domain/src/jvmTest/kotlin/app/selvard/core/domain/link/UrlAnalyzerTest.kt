package app.selvard.core.domain.link

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UrlAnalyzerTest {

    @Test
    fun parsesStandardHttpsUrl() {
        val u = UrlAnalyzer.analyze("https://www.example.co.uk/path/page?query=1#frag")
        assertTrue(u != null)
        assertEquals("https", u!!.scheme)
        assertEquals("www.example.co.uk", u.host)
        assertEquals("example.co.uk", u.registrableDomain)
        assertEquals("/path/page", u.path)
        assertFalse(u.hasUserinfo)
    }

    @Test
    fun parsesPortAndDetectsUserinfo() {
        val u = UrlAnalyzer.analyze("http://user@google.com:8080/x")
        assertTrue(u != null)
        assertTrue(u!!.hasUserinfo)
        assertEquals("google.com", u.host)
        assertEquals(8080, u.port)
    }

    @Test
    fun rejectsMalformedInputsWithoutThrowing() {
        listOf(
            "", "   ", "not a url", "http://", "://host", "ftp://",
            "a".repeat(UrlAnalyzer.MAX_URL_LENGTH + 1),
        ).forEach { input ->
            val result = runCatching { UrlAnalyzer.analyze(input) }
            assertTrue(result.isSuccess, "analyzer must not throw on: '${input.take(20)}'")
        }
        assertNull(UrlAnalyzer.analyze("not a url"))
        assertNull(UrlAnalyzer.analyze("http://"))
    }

    @Test
    fun ipLiteralHostsAreKept() {
        val v4 = UrlAnalyzer.analyze("http://192.168.0.1/x")
        assertEquals("192.168.0.1", v4?.host)
        val v6 = UrlAnalyzer.analyze("http://[2001:db8::1]:8443/x")
        assertEquals("[2001:db8::1]", v6?.host)
        assertEquals(8443, v6?.port)
    }

    @Test
    fun fuzzedInputNeverCrashes() {
        val rng = java.util.Random(1234L)
        val chars = "htp:/@?#[]%.-0aZ\u00e9\u4e2d\u0000 "
        repeat(2000) {
            val len = rng.nextInt(64)
            val s = buildString { repeat(len) { append(chars[rng.nextInt(chars.length)]) } }
            val result = runCatching { UrlAnalyzer.analyze(s) }
            assertTrue(result.isSuccess, "analyzer threw on: ${s.map { it.code }}")
        }
    }
}
