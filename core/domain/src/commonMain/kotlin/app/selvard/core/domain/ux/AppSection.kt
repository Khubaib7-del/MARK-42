package app.selvard.core.domain.ux

/**
 * Information architecture (docs/brand/UI_DIRECTION.md): five bottom tabs.
 * The guardians (links, network, apps, privacy) live as panes inside SHIELD
 * so the bar stays calm. Routes are stable identifiers; every section carries
 * a screen-reader description — tabs are never label-only glyphs.
 */
enum class AppSection(
    val route: String,
    val title: String,
    val contentDescription: String,
    /** Icon key for the tab bar (resolved in the Android module, never in core). */
    val iconKey: String,
) {
    HOME(
        "home",
        "Home",
        "Home: current security posture, what needs attention, and recent activity",
        "home",
    ),
    SHIELD(
        "shield",
        "Shield",
        "Shield: check links, the network filter, installed apps, and privacy facts",
        "shield",
    ),
    TIMELINE(
        "timeline",
        "Timeline",
        "Timeline: recorded signals grouped by time, never claimed as cause and effect",
        "timeline",
    ),
    IDENTITY(
        "identity",
        "Identity",
        "Identity: consented breach checks for declared addresses",
        "person",
    ),
    SETTINGS(
        "settings",
        "Settings",
        "Settings: engine status, recorded-data deletion, and diagnostics",
        "settings",
    ),
}

/** The four guardian panes inside [AppSection.SHIELD], in display order. */
enum class ShieldPane(val route: String, val title: String, val contentDescription: String) {
    LINKS("links", "Links", "Links: check a suspicious link on this device"),
    NETWORK("network", "Network", "Network: opt-in local DNS filter and blocked lookups"),
    APPS("apps", "Apps", "Apps: installed-app inventory with capability findings"),
    PRIVACY("privacy", "Privacy", "Privacy: observable privacy facts and device integrity"),
}
