package app.selvard.core.domain

import kotlin.test.Test
import kotlin.test.assertTrue

class OnboardingContentTest {

    @Test
    fun onboardingHasContentAndEveryPointIsNonBlank() {
        assertTrue(OnboardingContent.pages.isNotEmpty())
        OnboardingContent.pages.forEach { page ->
            assertTrue(page.title.isNotBlank())
            assertTrue(page.points.size >= 2, "page '${page.title}' too thin")
            page.points.forEach { point ->
                assertTrue(point.isNotBlank(), "blank point in '${page.title}'")
            }
        }
    }

    @Test
    fun honestyPanelIsPresentBeforeAnyPromise() {
        // The "cannot do" page must exist and must come before any local-first promise page.
        val cannotIndex = OnboardingContent.pages.indexOfFirst { it.title.contains("cannot do") }
        assertTrue(cannotIndex >= 0, "honesty panel page missing")
        val localFirstIndex = OnboardingContent.pages.indexOfFirst { it.title.contains("Local-first") }
        assertTrue(localFirstIndex > cannotIndex, "limits must be disclosed before promises")
    }

    @Test
    fun noClaimOfUnimplementedCapability() {
        // Foundation-phase contract: onboarding must not claim monitoring/analysis that
        // no engine provides yet (docs/product/MVP_SCOPE.md honest-state contract).
        val presentTenseClaims = listOf("scans your", "blocks malicious", "monitors your apps")
        OnboardingContent.pages.forEach { page ->
            (listOf(page.title) + page.points).forEach { text ->
                presentTenseClaims.forEach { claim ->
                    assertTrue(
                        !text.contains(claim, ignoreCase = true),
                        "unimplemented capability claimed: '$claim' in '$text'",
                    )
                }
            }
        }
    }
}
