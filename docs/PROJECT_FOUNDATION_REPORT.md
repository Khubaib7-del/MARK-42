# PROJECT FOUNDATION REPORT

Phase 0 — Research, reconnaissance, and documentation foundation. No implementation has begun. This report summarizes the required deliverables (spec §57) and ends Phase 0.

## A. Workspace state

Fresh, empty workspace (no prior commits, no existing project, no agent-instruction files). Operating environment: Linux sandbox with git/GH CLI, Node/Python/Bun tooling (no Android SDK installed yet — Phase 1 will pin tooling via Gradle/mise). All Phase 0 output is documentation, on branch `hoplite/taras-0f2fc809`.

## B. Recommended architecture

Security control plane (Risk / Policy / Privacy / Event / TI / Correlation engines) over three guardians (Link, Network, App) on Android core primitives. Platform-neutral `/core` in Kotlin Multiplatform holds all decision logic as pure functions; `/android` only collects evidence and renders outcomes. Full detail: `docs/ARCHITECTURE.md`. Key decision: Network Guardian is a **local-only VpnService filter** — no remote tunnel exists in the design, eliminating the surveillance-drift risk that plagues security VPNs (ADR-003).

## C. Android capability matrix summary

Full matrix: `docs/research/ANDROID_CAPABILITIES.md`. Headlines, verified against Android Developers / AOSP / Play policy docs:

- **Available:** share-intent link analysis, VpnService DNS filtering (Play declaration + prominent disclosure required; device security apps are a permitted use), app inventory via QUERY_ALL_PACKAGES (Play declaration form required; antivirus/device-security is a permitted use), static APK analysis, Play Integrity verdicts (incl. enhanced appAccessRiskVerdict / playProtectVerdict / MEETS_STRONG_INTEGRITY), boot/restart detection, Keystore-backed encrypted storage.
- **Not available (and never faked):** URL interception inside browsers/messengers, granted-permission state of other apps, real-time sensor-use events by other apps (OS indicators only), TLS content (by design — no MITM), visibility into Private Space, app sandboxing/AVF for third-party apps, install blocking.

## D. Threat model summary

20 categories (A–T) documented with all 20 required attributes each: `docs/security/THREAT_MODEL.md`. Priority: credential harvesting (T), link (A), accessibility/overlay abuse (Q), malicious APK (G), stalkerware (J) are the interactive-compromise tier; destination/metadata attacks (E, H, N, S) follow; integrity/identity (L, R, I, K) are signal-gathering. The model explicitly treats **the security app itself as an adversary** (surveillance drift) and supply-chain compromise (O) as product-level threats.

## E. MVP boundary

`docs/product/MVP_SCOPE.md` defines what the MVP can protect (DNS-level blocks, warnings, our own encrypted store), can detect, can warn about, cannot detect (sensor use by others, granted permissions, TLS content, message/email bodies), cannot prevent (installs, in-progress overlay attacks, off-device ATO), and what requires future OS support / cloud / enterprise privilege. Honest-state contract: bare "SAFE" is banned; "no known threat" is the ceiling of absence claims.

## F. Technology recommendations (reasoned)

- **Kotlin + KMP core** (ADR-001): single language across core/Android/backend; pure-function testability without emulators; desktop reuse in V3.
- **Jetpack Compose** (ADR-002): first-party, type-state mapping enforces disclosure contract.
- **Room + Keystore-held key + SQLCipher (pending dependency review)** (ADR-006): no custom crypto; hardware-bound keys; fallback field-level encryption.
- **Backend: deferred, minimal Ktor if built** (ADR-007): zero server attack surface at launch; backend untrusted by design.
- **TI: provider abstraction over local feeds** (ADR-004): URLhaus/ThreatFox/OpenPhish-class feeds; local floor means offline-capable and provider-independent. Safe Browsing deferred pending licensing review.
- **HIBP v3** for breach intel (ADR-010): industry-standard, government-recommended, k-anonymity support.

## G. Security risks

1. **Trust concentration in the VPN path** — all traffic routes through our code; mitigated by local-only design, fail-closed, fuzzing, and the PROTECTION OFF state surfaced instantly.
2. **Heuristic false positives/negatives** — measured, not assumed (accuracy targets in SECURITY_TESTING §8; heuristics capped at SUSPICIOUS).
3. **Play Integrity arms race** (rooted-device cloaking) — verdicts are one signal, never proof of integrity.
4. **Supply chain** — dependency ledger + CI audit gates + no dynamic code (DEPENDENCY_POLICY).
5. **Our own event store as evidence against the user** — encrypted, hardware-bound, retention-limited, deletable (PRIVACY_ARCHITECTURE).

## H. Privacy risks

Destination metadata is inherently sensitive (DNS history reveals behavior); mitigations: on-device processing only, short retention, no raw URL paths, k-anonymity where feasible, no analytics SDKs, egress allowlist enforced in CI. Identity queries (HIBP) are consented per-identity with exact-disclosure screens. Notification-access and SMS are deliberately excluded — the two features where a security app becomes spyware.

## I. External services required

TI feed providers (abuse.ch APIs, OpenPhish-class, tracker lists — download/cached), HIBP v3 (subscription), Google Play Integrity (server-verified queries). All documented with data-sent/fallback/caching in `docs/research/THREAT_INTELLIGENCE.md`. None is required for the app to function — absence yields UNKNOWN, never SAFE.

## J. Open-source components worth evaluating

None adopted yet. Research watch: YARA (libyara-on-Android vs server-side — Phase 3 review), NetGuard-class local-VPN packet handling (reference architecture; license-compatible reimplement, don't copy), SQLCipher (maintenance review pending), CAPE/Cuckoo-class sandboxes (V2 remote detonation), open AV engines (unlikely — surface > value). All intake flows through DEPENDENCY_POLICY.md.

## K. Platform limitations (top)

No link interception (share-intent is the honest path; V2 in-app browser gets full pre-open analysis); no granted-permission visibility; no third-party sensor events; no TLS content ever; QUERY_ALL_PACKAGES + VpnService both require Play declarations; Android 17 cross-profile loopback + ACCESS_LOCAL_NETWORK changes affect VPN edge cases (tracked for re-verification per phase).

## L. Documentation created

All 19 required deliverables: PRD, DRD, ARCHITECTURE, ROADMAP, THREAT_MODEL, SECURITY/PRIVACY ARCHITECTURE, DATA_CLASSIFICATION, DEPENDENCY_POLICY, INCIDENT_RESPONSE, SECURITY_TESTING, ANDROID_CAPABILITIES, THREAT_INTELLIGENCE, MALWARE_ANALYSIS, COMPETITIVE_RESEARCH, MVP_SCOPE, USER_FLOWS, PROTECTED_ASSETS, ADR-001…010 (10 records), plus this report and the README index.

## M. Initial roadmap

Phase 0 (done) through Phase 12 (release) with verified exit criteria per phase: scaffold/CI (1), security core (2), Link Guardian (3), Network Guardian (4), App Guardian (5), Privacy (6), Identity (7), Correlation (8), UI/UX (9), hardening (10), MVP testing (11), release (12). V2: isolated browser, remote sandbox, deeper identity. V3: desktop. `docs/ROADMAP.md`.

## N. Major unresolved questions

1. SQLCipher vs field-level encryption (decided in Phase 2 after dependency review).
2. HIBP plan tier (k-anonymity is Pro-only) — business cost decision at Phase 7.
3. Google Safe Browsing licensing feasibility for this product's model (Phase 3 legal review).
4. Notification-access-based link extraction (V2): currently rejected on privacy grounds; re-evaluate only with full privacy review + Play declaration.
5. Play Integrity enhanced-verdict availability/licensing tiers for this app class (verify Phase 6).
6. Monetization model (subscription vs paid) — deferred deliberately; must never create surveillance incentive (ad-funded telemetry is prohibited by principle).

---

**Phase 0 is complete. Per the specification's initial execution rule, implementation does not begin until the next development instruction.**
