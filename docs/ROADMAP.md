# Roadmap

Phased per the governing specification. Each phase: implement -> test -> inspect -> fix -> document -> verify, then continue. Phase 0 is complete with this foundation.

| Phase | Scope | Exit criteria (verified) |
| --- | --- | --- |
| **0 — Foundation** (done) | Research, threat model, capability matrix, architecture, PRD/DRD, security/privacy docs, ADRs, this roadmap | All required documents exist and are internally consistent; capability claims verified against cited sources |
| **1 — Project foundation** (done) | Gradle multi-module scaffold (/core KMP + /android), CI skeleton (lint, detekt, gitleaks, unit tests), minimal Compose shell app with disclosure-first onboarding | Verified 2026-09-21: `detekt` clean, `:core:domain:jvmTest` 10/10, `lintDebug` clean, `assembleDebug` produces APK; manifest declares **zero** permissions (INTERNET deferred to Phase 3 per least privilege) |
| **2 — Security core** (done) | Event model + event bus + encrypted Room event store + risk engine skeleton + policy engine + retention enforcement | Verified 2026-09-21: 35/35 JVM tests (schema round-trip, encryption/tamper/IV-uniqueness, risk determinism, retention deletion); detekt + Android lint clean; APK assembles; Room schema v1 exported. Keystore keyring implemented (StrongBox-aware); **on-device execution pending** (sandbox has no emulator/KVM) — first device run must re-verify |
| **3 — Link Guardian** | URL pipeline (normalization, canonicalization, punycode/IDN, heuristic indicators, brand impersonation heuristics), local feed lookup, share-intent entry, manual check UI, verdict + evidence screens | Labeled corpus accuracy measured vs targets; fuzz corpus no crashes; offline behavior = UNKNOWN; zero claims beyond evidence |
| **4 — Network Guardian** | VpnService local loopback DNS filter, per-UID attribution where feasible, blocklist policy, connection timeline, prominent disclosure + consent flow, Play declaration prepared | Filter verified in emulator + device tests (blocked domain events, per-app attribution test); battery benchmark documented; fail-closed verified |
| **5 — App Guardian** | Package inventory (visibility policy), permission analysis, installer source, signature info, risk signals; QUERY_ALL_PACKAGES declaration | Inventory correct across API 30+; no grant-state claims (documented OS limitation) |
| **6 — Privacy Engine** | Observable privacy events (boot events, our own sensor rationale, Play Integrity app-access-risk surfacing, permission-change snapshots) + timeline correlation | Every NOT-MONITORED area labeled honestly; no accessibility APIs |
| **7 — Identity Exposure** | Identity store (encrypted, biometric-gated), HIBP v3 integration behind explicit consent, exposure states | k-anonymity used where plan permits; breach data minimized (breach names, dates, data classes only); deletion flow tested |
| **8 — Incident Correlation** | Correlation engine (time-window, entity), incident model, timeline UI with "occurred shortly after" language | Correlation tests with synthetic event streams; causal-claim lint on all copy |
| **9 — UI/UX** | Full information architecture (HOME/SECURITY/NETWORK/APPS/IDENTITY/TIMELINE/SETTINGS), liquid-glass-restrained visual system, accessibility pass | Contrast/touch-target/screen-reader verified; reduced-motion honored; color-independent status |
| **10 — Security hardening** | Threat-model-driven review, pen test, flag review (exported components, FLAG_SECURE), dependency audit, R8/obfuscation review | Findings triaged; criticals fixed; report archived in repo |
| **11 — MVP testing** | Full SECURITY_TESTING.md matrix: unit/integration/instrumentation/security, battery/memory benchmarks, false-positive field study | All gates pass; performance numbers recorded, not assumed |
| **12 — Release preparation** | Store listing with honest capability statements, privacy policy, Play declarations, staged rollout plan, incident-response dry run | Release checklist green |

## V2 (post-MVP)

Secure browsing environment (in-app WebView with injected client-side checks), remote sandbox APK analysis (ADR-gated, isolated), deeper identity graph, richer TI integrations, desktop investigation begins.

## V3

Desktop agent (Windows first), cross-device dashboard reusing `/core`.

## Non-goals (all versions)

TLS interception; accessibility-based content inspection; notification body scanning without a full privacy re-review; marketing-driven fake signals.
