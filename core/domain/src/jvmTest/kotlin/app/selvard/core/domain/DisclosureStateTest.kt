package app.selvard.core.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisclosureStateTest {

    @Test
    fun everyStateHasANonBlankUserLabel() {
        DisclosureState.entries.forEach { state ->
            assertTrue(state.userLabel().isNotBlank(), "missing label for $state")
        }
    }

    @Test
    fun honestStateContractNoLabelEverClaimsSafe() {
        // The spec's absolute rule: absence of detection is never presented as safety.
        DisclosureState.entries.forEach { state ->
            assertTrue(
                !state.userLabel().contains("safe", ignoreCase = true),
                "$state label must not contain 'safe': ${state.userLabel()}",
            )
        }
    }

    @Test
    fun labelVocabularyIsTheDocumentedNineStateModel() {
        val expected = setOf(
            "Protected", "Detected", "Blocked", "Unknown", "Not monitored",
            "Not supported", "Permission required", "OS limitation", "Cloud analysis required",
        )
        val actual = DisclosureState.entries.map { it.userLabel() }.toSet()
        assertEquals(expected, actual)
    }
}
