# Dependency Policy

Every dependency is attack surface — especially in a security product, where the dependency list is the O-category supply-chain threat in `THREAT_MODEL.md`.

## Intake rules

1. **Necessity:** no dependency without a written justification in this ledger. Do not import a framework to implement a small function.
2. **Review before adoption:** maintainer, license, activity, release history, transitive dependency surface, known CVEs, security history, alternatives considered.
3. **Pin & lock:** Gradle lockfiles; upgrades are deliberate (not auto-PRs); major upgrades get changelog review + test run.
4. **Audit gates:** CI runs dependency vulnerability scanning (OWASP dependency-check or equivalent) + secret scanning (gitleaks) on every change.
5. **Minimization:** fewer, smaller, boring choices win. First-party Jetpack is preferred where sufficient. No analytics SDKs ever. No SDK with network access that we cannot fully allowlist.
6. **License:** copyleft review before adoption (product is expected to be proprietary; note conflicts early).

## Ledger (planned; finalized per adoption decision)

| Dependency | Purpose | Status | Notes |
| --- | --- | --- | --- |
| Kotlin 2.0.21 + Kotlin Multiplatform | Core domain language | Pinned (Phase 1) | First-party; jvm + android targets |
| Android Gradle Plugin 8.7.2 | Android builds | Pinned (Phase 1) | First-party; Gradle 8.10.2 wrapper |
| Jetpack Compose (BOM 2024.10.01) + Material 3 | UI | Pinned (Phase 1) | First-party |
| androidx.core / activity / lifecycle | App foundation | Pinned (Phase 1) | First-party |
| Detekt 1.23.7 | Static analysis | Pinned (Phase 1) | CI gate; config in `/config/detekt` |
| kotlinx-serialization-json 1.7.3 | Event schema codec | Pinned (Phase 2) | First-party JetBrains; strict mode (unknown fields rejected) |
| kotlinx-coroutines-core/test 1.9.0 | Event bus, store, engines | Pinned (Phase 2) | First-party JetBrains |
| Room 2.6.1 (runtime/ktx/compiler via KSP 2.0.21-1.0.28) | Encrypted event store | Pinned (Phase 2) | First-party; schema exported to `/android/app/schemas`; KSP1 forced (Room not KSP2-ready) |
| (none) | Phase 3 Link Guardian URL pipeline | Zero new dependencies | Pure-Kotlin URL parser, RFC 3492 punycode decoder, Damerau-Levenshtein, brand seed list — no library intake; full public-suffix list deferred as a deliberate future decision |
| (none) | Phase 4 Network Guardian DNS filter | Zero new dependencies | Pure-Kotlin RFC 1035 DNS parser, IPv4/UDP packet codec (checksum verified against an independently computed golden value), filter engine + tier policy — no library intake; VpnService is a platform API |
| SQLCipher (community/android-database-sqlcipher) | DB encryption | **Review required** | Third-party; verify maintenance status vs Room + field-level Keystore alternative; decide in ADR-006 addendum before adoption |
| WorkManager | Background jobs | Approved in principle | First-party |
| kotlinx.serialization | Event schema | Approved in principle | First-party, deterministic |
| OkHttp | TI/feed HTTP | Approved in principle | Boring, well-audited |
| Play Integrity SDK | Integrity verdicts | Approved in principle | First-party; enhanced verdicts licensing verified in phase |
| Biometric / security-crypto | BiometricPrompt; file encryption | Approved in principle | First-party |
| ktlint / Detekt / gitleaks / dep-check | CI tooling | Approved in principle | Dev-only |
| YARA / static-analysis libs (Phase 3+) | APK triage | **Deferred review** | Evaluate libyara on Android (size, NDK surface) before adoption |
| Any LLM SDK | Explanation features (V2) | **Prohibited in decision path** | SECURITY_ARCHITECTURE §4 |

## Supply-chain protections

- No dynamic code loading (Play policy, also our O-mitigation).
- Verify dependency checksums; CI builds from pinned locks; releases tagged and artifact-hashed.
- New *transitive* dependencies visible in lockfile diff review.
- Quarterly dependency review on the calendar; incident-driven immediate review (see INCIDENT_RESPONSE.md).
