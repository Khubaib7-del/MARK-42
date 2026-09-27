# Phase 11 — MVP Test Report

Test matrix per `docs/security/SECURITY_TESTING.md`. JVM-side gates run in CI;
on-device gates stay pending (sandbox has no `/dev/kvm`, no emulator) and are
listed as pending with the exact procedure to run them.

## 1. Unit tests (SECURITY_TESTING §1) — PASS

- `158/158` JVM tests pass (`:core:domain:jvmTest`), `detekt` clean,
  `:android:app:lintDebug` clean, `:android:app:assembleDebug` produces APK.
- Baseline was `138/138` at Phase 10; Phase 11 adds 20 gates:
  `LinkGuardianAccuracyTest` (6), `PipelineIntegrationTest` (10),
  `ManifestPolicyTest` (4).
- Coverage added: provider failure injection (throwing feed → Link `UNKNOWN`,
  DNS fail-closed default vs opt-out), manifest permission/export lockdown,
  bus → store → correlation → posture pipeline, DNS decision loopback,
  event-JSON fuzz (2000 malformed inputs, only `SerializationException` or
  `IllegalArgumentException`), verdict redaction (no raw path/query in
  reasons), store scale (3000 events, bounded 500-page query), retention and
  deletion lifecycle.

## 2. Integration tests (SECURITY_TESTING §2) — PASS (JVM)

- Bus → store → correlation → posture runs end to end: 3 events published,
  stored, correlated into 1 incident (`pipe-1`, `pipe-2`) plus 1 uncorrelated
  single, posture `HIGH_RISK`; reversed input yields the identical assessment.
- VPN DNS loopback: wire query for `secure-login-verify-account.com` parses,
  `decide` BLOCKS, `buildRefused` round-trips (id mirrored, question kept, no
  answers); `wikipedia.org` passes.
- Fallback chains: throwing feed degrades Link to `UNKNOWN` (“limited
  analysis”, never `OPEN`); DNS fails closed by default
  (`selvard-policy / intelligence-unavailable`) and passes only when
  `failClosed = false` is explicitly configured.
- Export redaction: feed-match detail names the listed host only; path/query
  tokens (`secret-path`, `abc123`) appear in neither reasons nor findings.

## 3. Instrumentation tests (SECURITY_TESTING §3) — PENDING (no emulator)

- Not run: sandbox has no KVM or emulator, so `VpnService` lifecycle,
  package visibility across API 30–35, boot-receiver behavior, and per-UID
  attribution stay unverified on-device.
- To run: `connectedCheck` on an API 30–35 emulator matrix plus one physical
  device; start/stop the tunnel, revoke VPN consent, reboot, and confirm the
  OFF state surfaces instantly and no traffic is claimed while OFF.

## 4. Security-specific tests (SECURITY_TESTING §4) — PASS (JVM) + pending device

- Fuzzing (all JVM, zero unexpected exceptions): URL parser 2000 cases,
  verdict mutations 1000 cases, DNS parser 3000 packets (only
  `DnsParseException`), share-text 1000 cases, event-JSON 2000 cases.
- Access control: `ManifestPolicyTest` pins the DRD §9 permission budget
  (8 allowlisted, 13 prohibited absent), `allowBackup=false`, cleartext ban +
  `networkSecurityConfig` wired, exactly 2 exported components
  (`MainActivity`, `CheckLinkActivity`), tunnel keeps its `BIND_VPN_SERVICE`
  guard. H1–H13 fixes from Phase 10 remain covered (bounds tests, tap-jacking
  guard, `FLAG_SECURE`, key masking).
- Pending on-device: `FLAG_SECURE` screenshot check, backup-exclusion
  verification, exported-component probe on the built APK.
- Static analysis: `detekt` clean; Android lint clean; dependency
  vulnerability scan and lockfiles still absent from CI (gitleaks only) —
  carried from Phase 10.

## 5. Penetration testing (SECURITY_TESTING §5) — NOT DONE

- No external pen test commissioned. In-tree hardening review (H1–H13) and
  the Phase 11 gates above are the only review evidence.

## 6. Malware test hygiene (SECURITY_TESTING §6) — OBSERVED

- No live samples. Corpus is synthetic (typosquats, punycode, IP literals,
  deep chains) plus the labeled development mock feed; no dynamic analysis.

## 7. Performance and reliability (SECURITY_TESTING §7) — JVM PROXY PASS, device PENDING

- JVM proxies (gates, not device claims): 200 link analyses complete in
  ~33 ms total (~0.17 ms each, far inside the 200 ms end-to-end budget);
  100 bus publications and a 500-page query over 3000 stored events each
  complete inside low-second gates.
- Pending measurement on reference devices: VPN battery overhead (<3%/day
  target), RSS with 6 months of events (<150 MB target), cold start <2 s to
  the posture screen, VPN throughput, and event-bus latency on-device.

## 8. Accuracy (SECURITY_TESTING §8) — MEASURED ON HELD-OUT CORPUS

- Corpus: 36 phishing-pattern URLs (4 feed-listed, 5 policy violations,
  27 heuristic: typosquats, brand-in-host, IP literals, punycode, deep
  chains, unusual TLDs) and 36 benign URLs (major news, docs, open-source,
  reference sites).
- Feed-indexed recall: `4/4` BLOCK (100%, target ≥ 90%).
- Policy violations: `5/5` BLOCK.
- Heuristic recall: `27/27` flagged (`BLOCK` or `OPEN_WITH_WARNING`), zero
  missed as `OPEN`/`UNKNOWN`.
- BLOCK precision: `0/36` benign BLOCK (no false-positive BLOCK; heuristics
  stay capped at `SUSPICIOUS`/`OPEN_WITH_WARNING`, never `MALICIOUS`).
- Benign warning rate: `0/36` (0%) after the Phase 11 short-label fix — a
  pre-fix probe warned on `bbc.com` (`bbc` is 2 edits from `hsbc`), so
  distance-2 typosquat now requires labels ≥ 4 chars. Every corpus verdict
  carries reasons; every warning names its evidence.
- Limits: sample feed only (real blocklists with TI integration pending);
  corpus-scale validation and the field-study false-positive rate carry to
  release preparation (Phase 12).

## Honest caveats

- No on-device run (no emulator/KVM in sandbox); no release-APK smoke.
- No external pen test; no dependency vulnerability scan or lockfiles in CI.
- IPv6 DNS bypass, single-threaded pump, and battery benchmarks carry over
  from Phase 10. Biometric gate remains deferred; Play Integrity is Phase 12.

## Verdict

Phase 11 JVM gates pass: `158/158` tests, `detekt` + lint clean, APK
assembles. The release decision (Phase 12) still needs the pending on-device
runs above.

