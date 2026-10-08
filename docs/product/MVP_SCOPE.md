# MVP scope and current security boundary

Reviewed against the current source on 9 October 2026. This describes implemented preview behavior. The original initializer, PRD, and architecture include future capabilities; their presence in a design document does not mean they have shipped.

| Area | Current implementation | Boundary |
| --- | --- | --- |
| Links | Shared or manually submitted URL normalization, local heuristics, bundled sample feed, evidence and recommendation | No interception inside other apps, live reputation, page/form inspection, domain-age checks, or redirect tracing |
| Network | User-approved local VpnService DNS filter; matched DNS requests refused; allowed requests forwarded to the configured resolver | Development sample lists; split tunnel; other traffic bypasses the app; no TLS content inspection; IPv6/encrypted DNS coverage and battery benchmarks remain outstanding |
| Apps | Visible package inventory, requested permissions, installer/target-SDK risk signals | On-demand capability analysis, not a malware verdict or granted-permission audit; signature analysis and per-UID traffic attribution deferred |
| Privacy | Install/update/remove facts, session permission-declaration snapshots, boot timestamps, supported security-state facts | Other apps' real-time microphone/camera/location activity is not monitored; no pre-install history; no invented restart causality |
| Identity | Encrypted local declarations; consented HIBP lookups with session-only subscription key and separate full-address consent | External lookup, not entirely on-device; no enumeration of all accounts/data sharing; no biometric gate yet; no match is not proof of safety |
| Posture | Deterministic assessment of a bounded local event history, with reasons | Not a complete device inspection or arbitrary percentage score |
| Timeline | Entity/time proximity groups and single events with inspectable evidence | Correlation does not establish causation; latest-event query bound applies |
| Storage | Keystore-backed encrypted payloads and vault; manual deletion with independent store receipts | Hardware backing depends on device; automatic retention wiring and atomic vault recovery need further work |
| Reliability | Explicit local OS process-exit diagnostics on Android 11+ | Exit categories alone do not identify a crash's root cause; before Android 11 these records are unavailable |

## What this preview can prevent

- DNS requests that enter the active local tunnel and match its bundled lists can be refused.
- Selvard protects its own persisted event payloads and declared identities with platform-backed encryption.
- Link warnings help the user decide not to open a submitted URL. A link verdict alone does not prove a connection was intercepted or blocked.

## What it cannot prevent or inspect

- All malware, zero days, arbitrary account takeover, malicious APK installation, hardware faults, or attacks inside other apps.
- Private message/email contents, other apps' private files or memory, arbitrary encrypted connection contents, or real-time sensor use by other apps.
- Complete traffic filtering across every protocol, Private Space inventories, universal URL interception, or OS-level application sandboxing.

## Deferred targets

Live licensed intelligence; corpus/field accuracy studies; signed release and Play filings; real-device battery/network validation; per-UID attribution; signature analysis; verified Play Integrity integration; biometric gate; retention scheduling and interrupted-write recovery; remote isolated analysis; desktop agent.

No permission is granted and no security engine is enabled by first-run onboarding. Network and identity retain separate feature disclosures and approvals. Simulated/sample capabilities must be visible as such.
