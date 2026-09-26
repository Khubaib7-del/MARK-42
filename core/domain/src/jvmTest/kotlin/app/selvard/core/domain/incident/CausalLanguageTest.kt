package app.selvard.core.domain.incident

import kotlin.test.Test

/**
 * Phase 8 exit criterion: causal-claim lint on all copy. Every user-facing
 * string the timeline renders (and the summary template) must pass
 * [CausalLanguage.check]. If UI copy changes, mirror it here.
 */
class CausalLanguageTest {

    @Test
    fun timelineUiCopyPassesCausalLint() {
        timelineCopy.forEach { CausalLanguage.check(it) }
    }

    companion object {
        val timelineCopy: List<String> = listOf(
            "Related signals grouped by entity and time window (30 minutes). " +
                "Grouping is temporal proximity only — no link between signals is " +
                "claimed. Singles that match nothing are listed separately.",
            "Nothing analyzed yet — recorded events are not grouped until you analyze them.",
            "No recorded events. There is nothing to group; this is not evidence the device is safe.",
            "single signal(s) with no temporal match — listed separately, not grouped",
            "Tap to expand the ordered timeline",
            "occurred shortly after",
            "single; occurred with no temporal match",
            "Recorded signal (details unavailable)",
        )
    }
}
