# Product Requirements Document

Project: Cordon (working title) — Privacy-first Android security environment
Phase: 0 (Foundation) | Status: Draft v1.0 | Owner: Core team

## 1. Problem

Ordinary Android users face a dense stream of security threats — phishing links over SMS and messaging apps, malicious APKs from third-party stores, stalkerware, overlay attacks, tracker-laden apps, account breaches, insecure networks — and no product gives them a **coherent, honest, privacy-respecting picture** of their device's security state.

Existing consumer mobile security products cluster into isolated categories (antivirus, VPN, DNS blocker, breach checker) with three systemic failures:

1. **False confidence** — "Your device is safe!" after a signature scan detects nothing. Malware-free ≠ safe.
2. **Surveillance drift** — the security tool itself collects contacts, messages, browsing history, or full traffic, becoming the very risk class it claims to defend against.
3. **Unexplained decisions** — blocking and alerting without evidence, confidence levels, or actionable next steps.

## 2. Users

| Persona | Description | Primary need |
| --- | --- | --- |
| Privacy-conscious general user | Non-expert; uses banking, messaging, social apps | Understand what is happening on the device; act on real threats |
| Security-aware professional | Developer/admin; carries work accounts on personal device | Visibility into network destinations, app risk, identity exposure |
| At-risk user | Journalist, activist, victim of targeted abuse | Detect stalkerware, overlay abuse, phishing; strong data hygiene |

## 3. User scenarios

- Receives an SMS with a "bank verification" link → wants to know whether the link is dangerous *before* opening it.
- App asking for accessibility permissions it doesn't plausibly need → wants the risk explained.
- Notices unusual battery drain or data usage → wants evidence-based explanation, not speculation.
- Sees a green camera dot at an odd hour → wants a historical record of when apps accessed sensors.
- Hears about a breach of a service they use → wants to know if their identity is exposed.
- Wakes to find the device rebooted overnight → wants a factual record of the event, without invented causality.

## 4. Product mission

> A privacy-first Android security environment that detects, explains, contains, and correlates threats across links, applications, networks, identity, privacy activity, and device security — while keeping sensitive information private whenever technically possible.

This is a **security control plane**, not an antivirus, VPN, or "score" app. It may contain capabilities from those categories, but combines them into one coherent, explainable environment.

## 5. Product principles

1. The security system must not become the surveillance system.
2. Local-first processing; data minimization; explicit consent.
3. Never fake a capability Android does not permit.
4. "No known threat" ≠ "safe". Use honest states: SAFE/LOW RISK, NO KNOWN THREAT, UNKNOWN, SUSPICIOUS, HIGH RISK, MALICIOUS, BLOCKED, PROTECTED, NOT MONITORED.
5. Every high-impact decision carries evidence and a confidence level.
6. Deterministic, auditable controls; AI assists explanation only.
7. Correlation without invented causality ("occurred shortly after", not "caused by").
8. Engineering correctness precedes UI polish.

## 6. Threat categories

The full threat model covers 20 categories (A–T): link-based, messaging, email, social-media, browser, download, APK/application, network, identity, privacy, account, device-integrity, physical-access, data-leakage, supply-chain, sensor-abuse, permission-abuse, unexpected-device-events, malware-persistence, and credential-harvesting attacks. See `docs/security/THREAT_MODEL.md` — each threat documents mechanism, delivery, data at risk, Android detection capability, restrictions, preventability, confidence, false-positive/negative risks, messaging, and mitigation.

## 7. Features (concept)

| Engine | Feature | One-line description |
| --- | --- | --- |
| Link Guardian | URL risk analysis | Explainable verdict (OPEN / WARN / BLOCK / UNKNOWN) on demand via share intent, manual entry, clipboard (foreground), and later an in-app isolated browser view |
| Network Guardian | Local DNS filtering | VpnService-based on-device filtering: DNS + destination metadata only; no remote tunnel; no TLS interception |
| App Guardian | App security inventory | Observable app metadata: identity, permissions requested, installer source, signature, risk signals — no claims of inspecting app internals |
| Privacy Monitor | Sensor/permission history | Historical timeline and correlation of observable privacy events; augments, never replaces, OS privacy indicators |
| Identity Exposure | Breach monitoring | User-declared identities checked against authorized breach intelligence (HIBP v3) with explicit privacy disclosure |
| Device Integrity | Integrity signals | Play Integrity verdicts, boot/restart events, verified-boot signals where available |
| Incident Engine | Event correlation | Unified event bus → correlation → risk engine → incident store → timeline UI |
| Posture | Security posture | NORMAL / ATTENTION REQUIRED / ELEVATED / HIGH RISK / CRITICAL with evidence — no vacuous numeric score |

## 8. MVP scope

See `docs/product/MVP_SCOPE.md` for the complete boundary. In summary, the MVP must honestly deliver: Link Guardian (share/manual), Network Guardian (local DNS blocklists), App inventory, Privacy Monitor (observable signals), Identity Exposure foundation, Event Bus, Risk Engine, Incident Timeline, Posture, encrypted local storage. Anything not reliably implementable under normal Android constraints is **documented as a limitation, not faked**.

## 9. Explicitly non-MVP

- TLS/HTTPS traffic interception (never planned).
- Remote sandbox detonation of malware (V2+).
- In-app secure browser environment (V2).
- Desktop agent (V3).
- Accessibility-based content inspection (prohibited by our own principles; detection of *others'* accessibility abuse uses Play Integrity `appAccessRiskVerdict`).
- Notification content scanning (deferred; high privacy risk; requires Play declaration if ever attempted).
- Generic "security score" numeric marketing.

## 10. Success criteria

- Zero false-capability claims shipped (audited against capability matrix).
- Link Guardian verdict accuracy ≥ target defined in `SECURITY_TESTING.md` on a labeled corpus, with measured false-positive rate.
- Network Guardian battery impact measured (not assumed) below stated budget.
- Every block/warning screen renders evidence list + confidence.
- Independent review of threat model and privacy architecture.
- Crash-free sessions > 99.5%.

## 11. Failure conditions

- Any feature presented as protection that is actually a mock or decoration (violates "NO FAKE FEATURES", absolute).
- Any raw message content, contact, or browsing history leaving the device.
- Blocking infrastructure with single-provider dependency and no offline fallback semantics (UNKNOWN must be surfaced, never silently "SAFE").

## 12. Platform limitations (summary)

The capability matrix (`docs/research/ANDROID_CAPABILITIES.md`) is the authoritative reference. Highlights: no URL interception from third-party apps; no granted-permission state of other apps; no visibility into apps inside Private Space (Android 15+); no camera/mic-in-use events for other apps; no TLS plaintext; VpnService requires Play declaration + prominent disclosure; QUERY_ALL_PACKAGES requires Play permission declaration (device security apps are a permitted use).

## 13. Legal & compliance

- Google Play: VpnService policy (declaration + prominent disclosure), Device and Network Abuse policy, QUERY_ALL_PACKAGES declaration, Accessibility service policy.
- Third-party API terms: HIBP v3 (subscription, k-anonymity is Pro feature), abuse.ch fair-use principles, Google Safe Browsing licensing if used.
- Data protection: data minimization, purpose limitation, explicit consent, documented retention (see `PRIVACY_ARCHITECTURE.md`, `DATA_CLASSIFICATION.md`).

## 14. Third-party dependencies (planned)

Dependency intake is governed by `docs/security/DEPENDENCY_POLICY.md`. Planned: Jetpack (Compose, Room, WorkManager, Security-crypto where applicable), Play Integrity, kotlinx.serialization, OkHttp. Each requires a documented security review before adoption.

## 15. Future desktop architecture

V3 target. The platform-neutral core (`/core`, Kotlin Multiplatform) owns the event model, risk engine, policy model, TI interfaces, and URL analysis pipeline, so the desktop agent reuses them; only platform sensors and UI are desktop-specific. See `ADR-001`.
