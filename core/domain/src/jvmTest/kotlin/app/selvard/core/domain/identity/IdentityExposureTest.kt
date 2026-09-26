package app.selvard.core.domain.identity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class IdentityExposureTest {

    @Test
    fun sha1MatchesStandardVectors() {
        assertEquals("DA39A3EE5E6B4B0D3255BFEF95601890AFD80709", HibpSha1.hexUpper(ByteArray(0)))
        assertEquals("A9993E364706816ABA3E25717850C26C9CD0D89D", HibpSha1.hexUpper("abc".toByteArray()))
        assertEquals(
            "567159D622FFBB50B11B0EFD307BE358624A26EE",
            IdentityExposure.addressHash("test@example.com"),
        )
    }

    @Test
    fun normalizationTrimsAndLowercases() {
        assertEquals("user@example.com", IdentityExposure.normalizeEmail("  User@Example.COM "))
        assertFailsWith<IllegalArgumentException> { IdentityExposure.normalizeEmail("not-an-address") }
        assertFailsWith<IllegalArgumentException> { IdentityExposure.normalizeEmail("a@b") }
        assertFailsWith<IllegalArgumentException> { IdentityExposure.normalizeEmail("two@@example.com") }
        assertFailsWith<IllegalArgumentException> { IdentityExposure.normalizeEmail("  ") }
    }

    @Test
    fun maskingKeepsOneChar() {
        assertEquals("u***@example.com", IdentityExposure.maskEmail("user@example.com"))
    }

    @Test
    fun prefixSuffixSplitIsSix() {
        val hash = IdentityExposure.addressHash("test@example.com")
        assertEquals("567159", IdentityExposure.rangePrefix(hash))
        assertEquals(34, IdentityExposure.rangeSuffix(hash).length)
        assertFailsWith<IllegalArgumentException> { IdentityExposure.rangePrefix("xyz") }
    }

    @Test
    fun rangeMatchIsSuffixEquality() {
        val hash = IdentityExposure.addressHash("test@example.com")
        val suffix = IdentityExposure.rangeSuffix(hash)
        val entries = listOf(
            RangeEntry("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", listOf("Decoy")),
            RangeEntry(suffix.lowercase(), listOf("Adobe", "LinkedIn")),
        )
        assertEquals(listOf("Adobe", "LinkedIn"), IdentityExposure.matchRange(hash, entries))
        assertTrue(IdentityExposure.matchRange(hash, entries.take(1)).isEmpty())
    }

    @Test
    fun rangeResponseParses() {
        val raw = """[{"hashSuffix":"ABCDEF1234","websites":["Adobe"]}]"""
        assertEquals(listOf(RangeEntry("ABCDEF1234", listOf("Adobe"))), HibpResponses.parseRange(raw))
    }

    @Test
    fun breachResponseMinimizes() {
        val raw = """[{"Name":"Adobe","Title":"Adobe","BreachDate":"2013-10-04",
            "AddedDate":"2013-12-04T00:00:00Z","ModifiedDate":"2022-01-01T00:00:00Z","PwnCount":153000000,
            "Description":"x","DataClasses":["Email addresses","Password hints"],
            "IsVerified":true,"LogoPath":"https://x/y.png"}]"""
        val breaches = HibpResponses.parseBreaches(raw)
        assertEquals(1, breaches.size)
        assertEquals("Adobe", breaches[0].name)
        assertEquals("2013-10-04", breaches[0].breachDate)
        assertEquals(listOf("Email addresses", "Password hints"), breaches[0].dataClasses)
    }

    @Test
    fun consentTextStatesExactDisclosure() {
        val anon = IdentityExposure.consentDisclosure(HibpQueryMode.K_ANONYMITY_RANGE)
        assertTrue(anon.contains("first 6 characters"))
        assertTrue(anon.contains("never leaves the device"))
        assertTrue(anon.contains("never means the address is safe"))
        val full = IdentityExposure.consentDisclosure(HibpQueryMode.FULL_ADDRESS)
        assertTrue(full.contains("full address"))
        assertTrue(full.contains("will see the complete address"))
    }

    @Test
    fun summaryLinesNeverClaimSafety() {
        val lines = ExposureState.entries.map { IdentityExposure.summaryLine(it, 2) }
        lines.forEach {
            assertTrue(!it.contains("safe", ignoreCase = true) || it.contains("never means"))
            assertTrue(!it.contains("clean", ignoreCase = true))
        }
        assertTrue(lines.any { it.contains("never means safe") })
    }

    @Test
    fun staleDisclosureVersionRejected() {
        assertFailsWith<IllegalArgumentException> {
            DeclaredIdentity("id", IdentityKind.EMAIL, "a@b.co", HibpQueryMode.K_ANONYMITY_RANGE, 1L, 0)
        }
    }
}
