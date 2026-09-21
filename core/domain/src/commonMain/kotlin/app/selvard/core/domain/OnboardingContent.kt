package app.selvard.core.domain

data class OnboardingPage(
    val title: String,
    val points: List<String>,
)

/**
 * Disclosure-first onboarding content (docs/product/USER_FLOWS.md F2).
 * The product states its limits before its promises; no capability is implied
 * that the platform does not permit (docs/research/ANDROID_CAPABILITIES.md).
 */
object OnboardingContent {

    val pages: List<OnboardingPage> = listOf(
        OnboardingPage(
            title = "A security environment — not a security score",
            points = listOf(
                "Selvard is a privacy-first control plane around your device: links, apps, " +
                    "network, identity, privacy events, and incidents.",
                "Every future alert will state what happened, why, what was affected, " +
                    "what Selvard did, and how certain it is.",
                "\u201cNo known threat\u201d is the strongest claim ever made here. " +
                    "Nothing is ever declared \u201csafe\u201d.",
            ),
        ),
        OnboardingPage(
            title = "What Selvard cannot do — stated honestly",
            points = listOf(
                "It cannot read your messages or your email.",
                "It cannot see inside encrypted traffic — and never will.",
                "It cannot intercept links you tap in other apps. Share a link to Selvard to have it checked.",
                "It is not an antivirus, a VPN, or a score app, and it does not pretend to be.",
            ),
        ),
        OnboardingPage(
            title = "Local-first by architecture",
            points = listOf(
                "Sensitive data stays on this device, encrypted with hardware-backed keys.",
                "The future Network Guardian (Phase 4) filters on-device and tunnels nothing to any server.",
                "No accounts. No analytics SDKs. No telemetry.",
            ),
        ),
    )
}
