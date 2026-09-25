package app.selvard.core.domain.link

/**
 * Small, bundled brand seed list for impersonation heuristics. Deliberately
 * partial: it is a heuristic seed, not a protection claim (provenance states this).
 */
object BrandList {
    val brands: Set<String> = setOf(
        "google", "youtube", "gmail", "microsoft", "outlook", "office", "live",
        "apple", "icloud", "github", "gitlab", "linkedin", "facebook",
        "instagram", "whatsapp", "telegram", "x", "twitter", "paypal",
        "chase", "bankofamerica", "wellsfargo", "citibank", "hsbc", "barclays",
        "hdfcbank", "amazon", "netflix", "coinbase", "binance", "revolut",
        "steam", "discord", "roblox", "dhl", "fedex", "usps", "irs", "gov",
    )

    val sensitiveWords: Set<String> = setOf(
        "login", "signin", "verify", "verification", "secure", "security",
        "account", "update", "confirm", "wallet", "recovery", "billing",
        "support", "helpdesk", "authenticate", "unlock", "suspended",
    )
}
