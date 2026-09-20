# Privacy Architecture

**Governing principle: the security system must not become the surveillance system.**

## 1. Data inventory (what we collect, why, where)

| Data | Why | Processed | Stored | Leaves device? |
| --- | --- | --- | --- | --- |
| URLs user submits to Link Guardian | Analysis | Local (+ TI lookup of normalized domain) | Derived verdict + evidence, not raw URL | Domain only, to consented TI providers |
| DNS destinations (while VPN active) | Filtering + timeline | Local | Domain + timestamp + UID, retention-limited | Never (blocklists download only) |
| App inventory metadata | Risk signals | Local | Package name + declared permissions + installer + signature state | Never |
| Identities (emails/phones) user declares | Breach exposure checks | Local vault; hashed query to HIBP | Encrypted, biometric-gated | HIBP (subscription; k-anonymity range query where plan permits) |
| Events / incidents | Timeline + posture | Local | Encrypted store | Never |
| Diagnostics | User-initiated export only | Local | n/a | Only if user exports |
| Telemetry | **None in MVP** | — | — | — |

## 2. What we deliberately do NOT collect

Message contents or contacts; browsing history; keystrokes; clipboard background access; notification bodies; precise location (we never request location); other apps' private data; raw traffic payloads. The MVP requests **no** sensitive Android permissions beyond those in DRD §9.

## 3. Data flow rules

1. Derived-minimum egress: e.g., we send a normalized domain, never the full URL path (path segments can contain tokens/PII); never full email unless k-anonymity unavailable and the user has explicitly consented to that disclosure (stated at identity-creation time).
2. Cloud queries are attributed as product-level, not user-level: no user identifiers in TI requests; HIBP queries carry only the k-anonymity prefix or the address itself when consented.
3. All external endpoints enumerated in code; unknown egress is a build failure (CI network allowlist test).
4. No third-party analytics SDKs — they are the classic surveillance-drift vector for security apps.

## 4. Consent architecture

| Feature | Consent gate | Style |
| --- | --- | --- |
| Link Guardian | None (user-initiated) | In-context |
| Network Guardian | Prominent disclosure + explicit activation (Play VpnService policy compliant) | Blocking modal, plain language, before first enable |
| App Guardian | Prominent disclosure at first use (QUERY_ALL_PACKAGES) | In-context screen |
| Identity Exposure | Per-identity consent incl. exact disclosure of what a HIBP query reveals | Blocking, detailed |
| Any loosening of policy defaults | Informed consent screen with consequences | Blocking |

Consent records are themselves events (auditable, user-visible in timeline). Revocation must be immediate, verifiable, and propagate to retention.

## 5. Retention & deletion

Per-class retention in `DATA_CLASSIFICATION.md`, enforced by scheduled job; deletion is complete (store + caches + derived posture) with a verified deletion test (Phase 2). "Delete all my data" performs: store wipe, key destruction, provider unenrollment where applicable, with a completion receipt shown to the user.

## 6. Third parties

Only: threat-intelligence feeds (download-only), reputation lookups (domain-level), HIBP (identity queries, consented). Each provider documented in `research/THREAT_INTELLIGENCE.md` with data-sent/data-kept/fallback. New providers require a privacy-architecture update before code.

## 7. Privacy self-audit checklist

- Can any component read data beyond its need? (No — interfaces sized to need.)
- Does any feature make "silent" egress possible? (No — allowlist + tests.)
- Is every disclosure point honest about NOT MONITORED areas? (Matrix-driven UI.)
- Could our own event store harm the user if seized? (Encrypted, hardware-bound, biometric-gated, retention-limited, deletable.)
