# User Flows

Flows define behavior before UI exists (per specification: engineering first, UI later). Every alert answers: what happened, why, what was affected, what we did, what you can do, how certain we are.

## F1 — Check a suspicious link (primary flow)

1. User long-presses link in SMS/chat -> Share -> "Check link" (Selvard share target). *(Manual paste variant: open app -> paste.)*
2. Link Guardian pipeline runs locally; remote reputation lookup (normalized domain only) if enabled.
3. Verdict screen: verdict state (OPEN / OPEN WITH WARNING / BLOCK / UNKNOWN) + evidence list (e.g., "domain on malware-distribution list (URLhaus)", "login form markers + brand mismatch", "redirect chain: 2 hops") + confidence (HIGH/MED/LOW).
4. Actions: Proceed (with warning acknowledgment) / Open in isolated view (V2) / Copy report / Do nothing.
5. Event recorded; if user proceeds and Network Guardian active, DNS block may still apply — shown as "Connection blocked" with evidence.

## F2 — First-run onboarding (disclosure-first)

1. What the product is / is not (explicit "cannot read your messages; cannot see inside encrypted traffic; cannot intercept links you tap" honesty panel).
2. Feature-by-feature consent: App inventory disclosure (prominent), Network Guardian disclosure (Play-compliant, before any VPN enable), Identity (per-identity, later).
3. No data leaves device in onboarding; no account creation.

## F3 — Enable Network Guardian

1. Prominent disclosure: what routes through the tunnel, that filtering is local-only (no traffic to us), what is blocked, battery expectations.
2. User activates -> system VPN consent dialog -> active state with non-dismissable notification.
3. Network screen: live connection list (destination + app attribution where feasible + classification), blocked list, tracker counts, per-tier toggles (malware / phishing / trackers).
4. Deactivation at any time; UI immediately reflects PROTECTION OFF (no lingering implied coverage).

## F4 — Review app inventory

1. APPS screen lists visible packages with risk signals (declared permissions vs category norms, sideload marker, a11y/SAW/device-admin declarations, hidden-icon marker).
2. App detail: enumerated evidence, plain-language explanation, what we cannot know (grant state, behavior), actions: mark as protected asset, allowlist (e.g., legit screen reader), guidance.

## F5 — Identity exposure

1. IDENTITY screen -> Add identity -> explicit disclosure of what a HIBP query reveals and how it is minimized (k-anonymity where plan permits) -> confirm.
2. Vault stored encrypted; biometric gate on re-entry.
3. Exposure results: breach name/date/data-classes per identity, state model (VERIFIED_CONNECTION / POSSIBLE_ACCOUNT / KNOWN_EXPOSURE / HISTORICAL_EXPOSURE / UNKNOWN), remediation guidance.
4. Delete identity -> complete local removal + confirmation receipt.

## F6 — Incident timeline & posture

1. HOME shows posture (NORMAL / ATTENTION REQUIRED / ELEVATED RISK / HIGH RISK / CRITICAL) with per-domain rows (Network / Applications / Identity / Device / Privacy) each with state + why.
2. TIMELINE shows correlated events; incident detail shows the chain (e.g., "02:41 suspicious URL opened -> 02:41 redirect chain -> 02:41 blocked -> 02:45 device restarted -> 02:46 integrity check passed") with correlation language only ("occurred shortly after").
3. Any item drill-down to full evidence + provenance (feed/provider names).

## F7 — Privacy controls & data deletion

1. SETTINGS: per-feature switches, notification thresholds, retention tightener, provider list with data-sent disclosure.
2. "Delete all data": shows exactly what exists -> wipes store + destroys keys -> receipt.

## F8 — Diagnostics

Engine health (providers, feed freshness), event counts, storage size, battery-impact reading, redacted export.
