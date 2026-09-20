# Threat Intelligence Research

## 1. Requirements

Categories needed: malicious domains, phishing URLs, malware hashes, IP reputation, DNS intelligence, tracker lists, brand impersonation, breach intelligence, campaign/stealer-log intel. Constraints: no scraping in violation of ToS; no single-provider dependency; local caching mandatory (offline operation + data minimization); every provider documented (API, license, rate limits, cost, data sent, data returned, reliability, fallback, cacheability).

## 2. Candidate providers (Phase 0 assessment — verify before adoption)

| Provider | Category | Access & license | Cost | Data sent | Cacheable | Assessment |
| --- | --- | --- | --- | --- | --- | --- |
| abuse.ch URLhaus | Malware URLs | Community API (Auth-Key, fair-use; commercial use may need paid tier) | Free / paid tier for commercial | URL lookup (on demand) | Yes: CSV/JSON dumps, RPZ feed | Strong MVP feed for malware-distribution domains; feeds explicitly *not* for blocking — use API datasets, respect terms |
| abuse.ch ThreatFox, MalwareBazaar | IOCs, malware hashes | Community API (Auth-Key) | Free / paid tier | Hash lookup | Yes (dumps) | Hash reputation MVP |
| OpenPhish | Phishing URLs | Community feed free; commercial licensing for bulk | Free/fee | Feed download | Yes | Candidate phishing tier; verify current licensing terms at adoption |
| PhishTank | Phishing URLs | API + attribution terms | Free | URL lookup + feed | Yes | Historically slow verification; evaluate activity |
| Google Safe Browsing | Phishing/malware | API licensing required, quotas, attribution rules | Free tier w/ limits | URL hash-prefix queries | Limited | Strongest coverage but licensing+attribution terms require legal review; V2 candidate |
| HIBP v3 | Breaches, stealer logs | Subscription API, `hibp-api-key` | Paid (per RPM tier); Pwned Passwords free | Email (or k-anonymity prefix on Pro plan) | No (query-on-demand) | Identity Exposure engine (ADR-010); k-anonymity strongly preferred — it is Pro-only, factor into plan choice |
| Spamhaus / RPZ services | Domains/IPs | Commercial licensing | Fee | Feed download | Yes | V2 candidate for enterprise-grade lists |
| Tracker lists (Disconnect-style) | Trackers | Open lists with license review | Free | List download | Yes | Tracker tier of Network Guardian |
| NVD / CISA vuln feeds | CVE context | Public APIs | Free | Product lookups | Yes | Backend enrichment (optional) |

## 3. Provider abstraction (ADR-004)

```
ThreatIntelProvider (core interface)
├── LocalDatabaseProvider   (bundled/cached feeds - always present, offline truth)
├── RemoteLookupProvider     (URLhaus/ThreatFox/OpenPhish adapters)
├── HIBPProvider             (identity - consented only)
└── FutureProvider           (Safe Browsing post-licensing)
```

Rules: provider adapters are interchangeable; a provider is health-scored (success rate, freshness) and can be disabled at runtime (including remotely *off*, never remotely *on*); verdicts merge with source attribution preserved in evidence. Local feeds are the floor: **no provider availability is ever required for an answer** — absence yields UNKNOWN.

## 4. Privacy-preserving lookup design

- Feed model: download blocklists/hashes to device (bulk, batched, WorkManager) — no per-user queries at all.
- On-demand lookups send only: normalized domain (not full URL path — paths carry tokens/PII), SHA-256 of files, k-anonymity prefix for identity queries (Pro plans).
- No user identifiers in any TI request. Providers see product traffic from egress IPs, not user identities.
- Providers that demand full-email queries must show exact disclosure before identity creation (PRIVACY_ARCHITECTURE §3).

## 5. Open questions for implementation

- OpenPhish/PhishTank current licensing and activity levels (re-verify at Phase 3).
- Safe Browsing licensing feasibility for a privacy-first product (Phase 3 legal review).
- HIBP plan tier decision (k-anonymity requires Pro) — Phase 7.
- Stealer-log exposure monitoring scope (sensitive; needs careful UX framing) — Phase 7+.
