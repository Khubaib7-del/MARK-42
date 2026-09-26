# Phase 10 — Security Hardening Report

Date: 2026-09-26. Scope: the MVP as shipped at v0.5.0-phase9 (commit `06e7896`).
Method: threat-model-driven code review of every trust boundary (intent
surface, tunnel packet path, HIBP egress, vault codec, event store, UI
overlays), plus the Phase 10 gate items in SECURITY_ARCHITECTURE §6.
External penetration testing was **not** performed — no testers were engaged
and nothing below is presented as a third-party assessment.

## Findings fixed in v0.6.0-phase10

| # | Severity | Finding | Fix |
| --- | --- | --- | --- |
| H1 | High | IHL length field from tunnel packets used as a slice index without range-checking (`SelvardVpnService`); absurd IHL values crashed the pump thread, killing all filtering (fail-open-via-crash). | IHL range-validated (20..60) in `IpPacketCodec` and re-validated at the pump before slicing; absurd values fail closed. Tests: `IpPacketCodecBoundsTest`. |
| H2 | High | `DnsFilterEngine.decide` throws on blank input; the pump let it propagate, so a hostile DNS name crashed the pump thread (same fail-open shape as H1). | Pump wraps `decide` in `runCatching`; failure yields no answer (fail closed). |
| H3 | Medium | `IpPacketCodec.readU16`/`ipChecksum` indexed blindly; malformed queries crashed instead of failing closed. | Both validate windows and reject with `IllegalArgumentException`. Tests: `IpPacketCodecBoundsTest`. |
| H4 | Medium | Share-intent extraction took the first whitespace token unbounded (Binder-sized strings) and fed over-limit tokens to the analyzer. | New `SharedUrlParser` (8 KiB input cap, analyzer-limit token cap, never throws) used by `CheckLinkActivity`; old inline extractor removed. Tests: `SharedUrlParserTest` (incl. 1000-case fuzz). |
| H5 | Medium | K-anonymity range match compared one side case-sensitively: lowercase HIBP suffixes silently missed real breaches (false negative). | Case-insensitive suffix comparison in `IdentityExposure.matchRange`. Existing tests already used lowercase entries and now pass for the right reason. |
| H6 | Medium | HIBP bodies read unbounded (`readText`) and parsed unbounded — a hostile proxy could force multi-megabyte JSON parses. | 256 KiB bounded read in `HibpClient`; 512 KiB / 2048-entry / 256-breach caps in `HibpResponses`. |
| H7 | Medium | Vault codec `parseOne`/`num` trusted field shapes: trailing escapes, missing terminators, and non-numeric timestamps threw raw exceptions; no entry-count bound. | Structural validation (truncation, escape, terminator, numeric-shape checks) plus entry/field/number size caps. |
| H8 | Medium | `maskEmail` destructured on `@` and crashed on corrupt stored values, breaking the vault list render. | Index-based masking with `"***"` fallback. Tests added. |
| H9 | Medium | Blocked-host event built `AffectedAsset` from an unbounded wire string: names over 128 chars threw inside the recording coroutine, losing the block record. | Host truncated to `AffectedAsset.MAX_REF_LENGTH` before constructing. |
| H10 | Low | HIBP API key typed in a plain visible field. | Password-masked field (no autocorrect). Key remains session-only (never persisted). |
| H11 | Low | Destructive "delete all" accepted taps through overlay windows (tap-jacking). | Hosting view sets `filterTouchesWhenObscured = true` (platform API; Compose 1.7 has no `filterTouchesWhenObscured` modifier — verified absent from the resolved foundation 1.7.5 jars). |
| H12 | Low | Verdict, posture, and masked-identity screens allowed screenshots/recents thumbnails. | `FLAG_SECURE` on `MainActivity` and `CheckLinkActivity`. |
| H13 | Low | Cleartext posture relied on a default; release kept unshrunk resources. | `usesCleartextTraffic=false` + `network_security_config` (cleartext banned, no exceptions); `isShrinkResources=true` in release. |

## Gate items reviewed

- **Exported components:** launcher + share-target activities only (both `exported=true` by necessity); package/boot receiver not exported; VPN service permission-gated (`BIND_VPN_SERVICE`). Share input re-validated (H4). No change required beyond H4.
- **Permissions vs DRD §9 allowlist:** INTERNET, FOREGROUND_SERVICE(+SPECIAL_USE), POST_NOTIFICATIONS, ACCESS_NETWORK_STATE, QUERY_ALL_PACKAGES (Play-declared), RECEIVE_BOOT_COMPLETED — each matches a documented engine need. No additions in this phase.
- **FLAG_SECURE review:** H12 closes the gap on sensitive screens.
- **Dependency audit:** catalog versions pinned in `gradle/libs.versions.toml`
  (AGP 8.7.2, Kotlin 2.0.21, Compose BOM 2024.10.01, kotlinx-serialization 1.7.3,
  coroutines 1.9.0, Room 2.6.1, Detekt 1.23.7); **no new dependencies added in
  Phase 10** (HIBP stays on platform `HttpsURLConnection`; parsers stay
  hand-written). No lockfiles exist in this repo (no `dependencyLocking`
  configured) — noted, not introduced here to avoid build-system churn at the
  gate. No dependency vulnerability scan runs in CI (DEPENDENCY_POLICY §4
  aspires to one); gitleaks secret scanning does. Owners of the next release
  should add an SCA step (e.g. OWASP dependency-check) and lockfile
  enforcement rather than treat this report as that coverage.
- **R8/obfuscation review:** release enables minification + resource shrinking
  (H13). Rules file carries no custom keeps; verified safe because Room uses
  KSP-generated code (not reflection) and kotlinx-serialization uses the
  compiler plugin — but a release-APK smoke run is still owed (no emulator in
  this environment; carried as a known limit below).
- **Secrets scan:** gitleaks runs in CI on every change; no secrets were
  introduced (HIBP key is user-supplied, session-only).
- **Crypto review:** AES-256-GCM via Keystore-wrapped keys (vault) and
  fresh-IV-per-encryption payload crypto (tested: round-trip, tamper-reject,
  IV-uniqueness, truncation-reject). No custom primitives. SHA-1 use is the
  HIBP k-anonymity protocol, not a security claim.
- **Event-store integrity:** Room payloads are AES-GCM sealed (tamper-evident
  at rest). SECURITY_ARCHITECTURE §3.4's "chained content hashes" phrase
  describes no mechanism present in `RoomEventStore` — no hash chain exists in
  the shipped code. This report does not invent one; the wording is corrected
  in this same change (see below) to "AES-GCM-sealed rows".

## Threat-model walk-through (residual posture)

- **A (links):** share/manual submission only — no silent interception (by
  design). Heuristics capped at SUSPICIOUS; feed hits block with named list.
- **G/Q (apps):** manifest-fact inventory; grant state never read/claimed.
- **H/E (network):** split-tunnel DNS filter only; REFUSED served on-device;
  fail-closed on parse/relay failure (H1–H3 harden this claim).
- **I/K (identity):** k-anonymity default; full-address mode needs separate
  consent; no-match never means safe; H5 closes the case-miss hole.
- **L/M (device/physical):** encrypted store + vault, `allowBackup=false`,
  FLAG_SECURE (H12); biometric gate still deferred (tracked since Phase 7);
  no device-admin or wipe capability (by design).
- **N (leakage):** HIBP egress is https-only to haveibeenpwned.com with
  bounded bodies (H6/H13); deletion wipes store + vault with a receipt.
- **O (supply chain):** zero new dependencies this phase; audit limits stated
  above.
- **R (unexpected events):** install/update/remove + boot facts, timestamp-only.
- **T (credential harvesting):** brand/typosquat heuristics + sample feed;
  real blocklists still pending (TI integration).

## Known limits (not fixed here)

- No external pen test; no on-device/emulator verification in this
  environment (129→138 JVM tests green, detekt + lint clean, debug APK
  assembles; release-APK smoke still owed).
- IPv6 DNS bypass undetected; single-threaded pump; battery benchmark and
  corpus-scale accuracy targets carry to Phase 11.
- Biometric gate for the vault deferred; Play Integrity deferred to Phase 12.
- No dependency vulnerability scan in CI; no Gradle lockfiles.

## Verification

`detekt` clean; `:core:domain:jvmTest` 138/138 (129 baseline + 9 new:
4 `SharedUrlParserTest`, 4 `IpPacketCodecBoundsTest`, 1
`maskingNeverThrowsOnCorruptStoredValues`); `:android:app:lintDebug` clean;
`:android:app:assembleDebug` produces `app-debug.apk`.
