# Security Testing Plan

## 1. Unit tests (core, every phase)

URL parsing/normalization/canonicalization (punycode, IDN homoglyphs, shortener expansion, URL-encoded tricks, scheme confusion); malicious-URL classification (labeled corpus: phishingkit URLs, brand-impersonation set, benign set); redirect handling (chain depth, cross-origin chains); domain normalization; TI provider failure injection (timeout/HTTP 500/malformed JSON -> UNKNOWN semantics); network policy evaluation; event correlation (time-window, entity); risk calculation determinism + weight versioning; permission-pattern analysis; identity exposure state mapping; encrypted-storage round-trip; retention enforcement; deletion completeness; offline behavior; posture computation.

## 2. Integration tests

Event bus -> store -> correlation -> posture pipeline; provider fallback chains; VPN engine <-> DNS resolver loopback; biometric gate + keystore binding; export/import redaction.

## 3. Instrumentation tests (Android)

VpnService lifecycle (start, crash, teardown, kill-switch posture); package visibility across API 30-37; Play Integrity verdict handling (mock + real sandbox); boot-receiver behavior; background-execution limits; per-UID attribution on emulator matrix.

## 4. Security-specific tests

- Fuzzing: URL pipeline (AFL/libFuzzer-style corpus on JVM), VPN packet handler (malformed frames), event deserialization (schema version attacks).
- Access control: exported-component probe; intent-validation tests (oversized inputs, malformed URIs).
- Storage: unlocked-screen screenshot protection (FLAG_SECURE), backup exclusion verification, root-detection-of-store tests.
- Permission minimization: CI manifest check — no permission beyond DRD §9 allowlist.
- Static analysis: Detekt + Android Lint + dependency vulnerability scan + secret scan.

## 5. Penetration testing (Phase 10, pre-release)

External review scope: attack surface of app components; VPN/DNS stack handling; crypto key lifecycle; export flows; the "honesty" surface (verify no UI path implies unavailable capability).

## 6. Malware test hygiene

Never execute malware samples on development devices. Static test artifacts: EICAR-standard benign markers, synthetic manifests, hash-only fixtures. Dynamic analysis only in dedicated offline sandbox infra (V2+, never user devices).

## 7. Performance & reliability (measured, not assumed)

Battery: Battery Historian + on-device energy accounting during VPN-active profiles (target: <3%/day overhead above baseline on reference devices, revised by measurement); memory: RSS limits on event store (target: <150 MB total app with 6 months of events); startup: cold start <2 s to posture screen; network overhead of feeds; VPN throughput on reference devices; event-bus latency (target: <200 ms end-to-end for blocking decisions).

## 8. Accuracy targets (Link Guardian)

On a held-out labeled corpus: ≥ 97% precision on MALICIOUS/BLOCK verdicts (false positive is the costliest error); recall target ≥ 90% for feed-indexed URLs; heuristics-only verdicts capped at SUSPICIOUS (never MALICIOUS without matched intel). All accuracy numbers published in release notes with methodology.
