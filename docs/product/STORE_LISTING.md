# Play Store Listing (draft for Phase 12 submission)

Package `app.selvard`. Category: Tools. Content rating target: Everyone
(no content, no ads, no purchases). Free, no in-app purchases.

## Short description (80 chars)

On-device security: link checks, DNS filter, app risks. No cloud, no tracking.

## Full description

Selvard is a privacy-first Android security environment. Everything runs on
your device: nothing you analyze is uploaded, there is no account, and there
is no analytics.

- Link Guardian: share or paste a link and Selvard analyzes it on-device —
  typosquat and brand-impersonation heuristics, punycode/IDN checks, local
  threat-intelligence lookup. Verdicts are evidence-first: OPEN means no
  known threat (never "safe"), warnings name their evidence, BLOCKs cite
  matched intelligence or policy.
- Network Guardian (opt-in): a local-only DNS filter. Only DNS passes
  through Selvard; all other traffic bypasses it entirely. Blocked names are
  refused on-device with zero network egress.
- App Guardian: on-demand inventory of installed apps — declared-permission
  analysis against a transparent catalog, stalkerware capability-combination
  detection, installer provenance, legacy-target-SDK findings. Grant state
  is never read or claimed; no result is a malware verdict.
- Privacy Monitor: install/update/remove facts, permission snapshots, boot
  records, device-integrity facts — with honest NOT MONITORED labels where
  the OS gives no visibility.
- Identity Exposure: consented breach checks (needs your own HIBP API key).
- Incident Timeline: temporal grouping of recorded signals — never causal.

## What Selvard is not (stated on the listing)

Not an antivirus, not a VPN, not a score app. It cannot see inside
browsers or messengers, granted permissions of other apps, sensor use by
other apps, or TLS content — and it never claims otherwise. Full boundary:
`docs/product/MVP_SCOPE.md`.

## Permission justifications (mirror in Play console)

| Permission | Why |
| --- | --- |
| INTERNET | On-device DNS forwarding loop for the local filter |
| FOREGROUND_SERVICE (+ SPECIAL_USE) | User-activated tunnel, non-dismissable notification |
| POST_NOTIFICATIONS | Security notifications only |
| ACCESS_NETWORK_STATE | Read the current resolver so filtering never silently changes who resolves names |
| QUERY_ALL_PACKAGES | App inventory for device-security scanning (permitted use) |
| RECEIVE_BOOT_COMPLETED | Timestamp-only boot facts |

## Data safety (draft answers)

- Data collected: none. Data shared: none. No analytics SDKs.
- Exception, declared honestly: breach checks transmit the declared identity
  (or k-anonymity prefix) to Have I Been Pwned only after per-identity
  consent → declare Personal info / Email address, optional, user-initiated,
  encrypted in transit, deletable.
- Security practices: data encrypted in transit (cleartext banned app-wide)
  and at rest (Keystore-held AES-256); users can request data deletion
  in-app with a receipt.

## Graphics checklist

- [x] Launcher icon (`mipmap-*`, brand mark)
- [x] Brand banner (`assets/brand/banner.webp`)
- [ ] Phone screenshots — OWNER ACTION: capture on a real device in
  Antigravity/native testing (Home, Security verdict, Network consent,
  Timeline, Settings receipt)
- [ ] Feature graphic 1024×500 — OWNER ACTION
