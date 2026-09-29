package app.selvard.core.domain.ux

/**
 * Phase 9 information architecture (docs/product/USER_FLOWS.md F1–F8).
 * Seven fixed sections in display order; routes are stable identifiers.
 * Every section carries a screen-reader description — tabs are never
 * label-only glyphs.
 */
enum class AppSection(
    val route: String,
    val title: String,
    val contentDescription: String,
    /** Rounded Material icon key for the tab bar (resolved in androidApp, never in core). */
    val iconKey: String,
) {
    HOME(
        "home",
        "Home",
        "Home: what Selvard is and is not, current security posture, per-domain state",
        "home",
    ),
    SECURITY(
        "security",
        "Security",
        "Security: check a suspicious link on this device",
        "shield",
    ),
    NETWORK(
        "network",
        "Network",
        "Network: opt-in local DNS filter disclosure and switch",
        "wifi",
    ),
    APPS(
        "apps",
        "Apps",
        "Apps: installed-package inventory scan with capability findings",
        "apps",
    ),
    IDENTITY(
        "identity",
        "Identity",
        "Identity: consented breach checks for declared addresses",
        "person",
    ),
    TIMELINE(
        "timeline",
        "Timeline",
        "Timeline: temporally grouped recorded signals, never causal",
        "history",
    ),
    SETTINGS(
        "settings",
        "Settings",
        "Settings: engine status, recorded-data deletion, diagnostics",
        "settings",
    ),
}
