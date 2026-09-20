# MVP Scope and Security Boundary

## MVP components

1. **Link Guardian** — share-intent + manual + foreground-paste URL analysis with evidence-backed verdicts (OPEN / OPEN WITH WARNING / BLOCK / UNKNOWN) and redirect-chain + domain intelligence.
2. **Network Guardian** — local-only VpnService DNS filter with tiered blocklists (malware, phishing, trackers as separate opt-in tiers), per-UID attribution where feasible, connection timeline.
3. **App Guardian** — inventory with permission-pattern risk signals, installer source, signature info, new-install events; Play declarations filed.
4. **Privacy Monitor** — observable privacy events (install events, permission-set snapshots, integrity app-access-risk windows, boot events) with honest NOT MONITORED labeling.
5. **Identity Exposure foundation** — encrypted identity vault (biometric-gated), HIBP v3 consented breach checks, exposure states.
6. **Event Bus** — typed, versioned, privacy-classified event schema (DRD §3).
7. **Risk Engine** — deterministic, evidence-weighted, versioned weights (ADR-009).
8. **Incident Timeline** — correlation engine + timeline UI, correlation-not-causation language.
9. **Security Posture** — NORMAL / ATTENTION REQUIRED / ELEVATED RISK / HIGH RISK / CRITICAL with evidence links.
10. **Secure local data architecture** — Keystore-backed encrypted Room store, retention enforcement, deletion flows.

## What the MVP CAN protect (prevent)

- DNS resolution of known malicious/trackers destinations (while Network Guardian is active — only guarantee while active; state surfaced when not).
- Opening a *submitted* link after a high-confidence BLOCK verdict (via user heeding the warning; we block DNS origin too).
- Our own data at rest: encrypted store, hardware-backed keys, biometric gate, FLAG_SECURE.

## What the MVP CAN detect

- User-submitted URLs matching threat intel / phishing heuristics.
- DNS destinations matched against blocklists (with attribution when feasible).
- Newly installed apps with high-risk permission patterns; sideloaded hidden-icon/accessibility-class installs (as risk markers, not verdicts).
- Integrity verdict anomalies (device/app/play-protect states via Play Integrity).
- Boot/restart events; breach exposure of declared identities.

## What the MVP CAN warn about

- Credential-harvesting indicators on submitted URLs (form + brand + domain-age evidence).
- App risk combinations associated with spyware (never as verdicts without matched intel).
- Tracker-heavy app behavior (destination profiling).
- Play-Protect-disabled state; outdated patch level via MEETS_STRONG_INTEGRITY.

## What the MVP CANNOT detect (and must label honestly)

- Real-time sensor use (camera/mic/location) by other apps — NOT MONITORED (OS-owned).
- Granted (vs declared) permission state of other apps.
- Content of any TLS traffic — by design, no MITM.
- Anything inside messaging/email clients unless the user shares it.
- Apps inside Private Space; app internals/memory; zero-day malware with no markers.

## What the MVP CANNOT prevent

- Installation of malicious APKs (OS/Play Protect domain) — we can only warn before/after.
- Overlay attacks in progress on other apps' screens.
- Account takeover happening off-device.
- Physical extraction of unencrypted data from other apps.

## What REQUIRES FUTURE OS SUPPORT

- Install-time blocking (device-owner/enterprise only). Real-time app sensor-event feeds. Third-party access to Play Protect remediation APIs. AVF-based isolation.

## What REQUIRES CLOUD INFRASTRUCTURE

- Full verdict verification for Play Integrity (we do best-effort local treatment in MVP; backend Phase 12).
- Reputation lookup for URLs beyond local feeds (provider abstraction allows local-first operation).
- Remote malware sandbox detonation (V2+, ADR-gated).

## What REQUIRES ENTERPRISE / PRIVILEGED ACCESS

- Enforced per-app network policy at OS level; app isolation/sandboxing; install allowlisting; SIM state access; lock-screen telemetry. Documented; not pursued.

## Honest-state contract

Every MVP surface must render one of the disclosure states (PRD §5.4) and never the word "safe" as a bare verdict. "No known threat" is the strongest no-signal claim permitted.
