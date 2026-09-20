# Security Architecture

## 1. Trust model

**We do not trust:** other apps, the OS integrity state (we can only attest it), the network, threat-intelligence providers, our future backend, or — deliberately — ourselves. Every component treats inputs from other components as untrusted data (validate, classify, size-limit).

**We do rely on:** Android's sandbox and permission model, Keystore/StrongBox hardware, Play Integrity's hardware-backed attestation, and TLS to enumerated endpoints. We never build custom cryptographic primitives.

## 2. Secure defaults

| Domain | Default | Rationale |
| --- | --- | --- |
| Data at rest | Encrypted Room store, Keystore-held key | Device seizure, backup leakage |
| Backup | `allowBackup=false` for store; no auto-cloud export | Backups escape our control |
| VpnService failure | Fail-closed (block all) while enabled | Open = false confidence for protection claims |
| TI lookup failure | UNKNOWN verdict, never SAFE | False confidence is the enemy |
| Notifications | Only MEDIUM+ actionable events; batched digests | Alert fatigue trains users to ignore |
| Exports | Never automatic; user-triggered, encrypted, redacted | Data egress control |
| Remote control | None — app is not remotely controllable | Attack surface + trust |

## 3. Component security

### 3.1 Android app
- Minimum exported surface: share-target activity + launcher only. No exported services/receivers except boot receiver (no sensitive data in broadcasts) and VPN service (not bindable by third parties — permission-protected).
- All intents into the app are validated (size-limited, URL-strict), never auto-executing decisions without user interaction.
- Sensitive screens: FLAG_SECURE + BiometricPrompt gate for Identity vault and policy loosening screens.
- No dynamic code loading (Play policy + O-supply-chain); no WebView JavaScript bridges (V2 browser will be ADR-gated with strict sandboxing).
- App integrity: Play Integrity appIntegrity self-check at launch (tamper detection for *our* binary); no secrets embedded in client (API keys are per-user/remote-wrapped, see 3.3).

### 3.2 Network Guardian (VPN engine)
- Local loopback only: no remote tunnel endpoint exists (ADR-003). Packet handling code is memory-safe-reviewed; malformed input from the tunnel is untrusted (fuzz target, Phase 11).
- Per-UID attribution data stays in process memory unless an event crosses the bus — then only classified metadata.
- Kill-switch semantics: if the filter crashes, the VPN is torn down (traffic returns to direct — and the UI must immediately show PROTECTION OFF rather than imply continued coverage).

### 3.3 Secrets
- No secrets in source, repo, CI logs. HIBP key + feed keys held in Keystore-wrapped storage, provisioned on first use; backend (if built) wraps client keys so a decompiled APK contains nothing usable. Client keys are revocable per-installation.
- gitleaks in CI; signing keys in environment only.

### 3.4 Event store
- Append-only, encrypted; integrity via chained content hashes (tamper evidence for our own records — non-cryptographic-binding to OS truth, documented).
- Retention enforced by policy engine job; user deletion propagates everywhere including derived posture caches.

## 4. Decision integrity

- All security decisions in `/core` pure functions; decision inputs are logged (evidence), versioned (weights registry), and unit-tested for determinism.
- The risk engine's output includes the *absence* state: "no signal" produces NO KNOWN THREAT / UNKNOWN posture contribution, never SAFE.
- LLM/AI is banned from: verdicts, blocks, integrity claims, risk scores, anything on the decision path. Permitted only in: user-facing explanation summarization (Phase 9+, opt-in, offline-capable defaults preferred) with a hard rule that AI text can never *increase* severity beyond engine output.

## 5. Cryptography

| Use | Primitive | Key location |
| --- | --- | --- |
| DB encryption | AES-256 (via SQLCipher) | Keystore/StrongBox |
| File export | AES-256-GCM via EncryptedFile | Keystore |
| Hashing (intel) | SHA-256 (standard) | n/a |
| K-anonymity breach query | SHA-1 prefix (HIBP protocol) | n/a |
| No custom primitives. No obfuscation-as-security. | | |

## 6. Hardening checklist (Phase 10 gate)

- Threat-model walk-through per category A–T against shipped code.
- Penetration test: intent surface, VPN handling, storage, export flows, notification flows.
- R8 + resource shrink; secrets scan of artifact; manifest review against declared permissions.
- Dependency audit against DEPENDENCY_POLICY ledger; lockfiles pinned.
- Accessibility of *our own* app screens to abuse: `filterTouchesWhenObscured`, `accessibilityDataSensitive` on sensitive views (Android 16+), overlay-blocking posture on our dialogs.

## 7. Disclosure enforcement

UI layer is generated from engine state enums; rendering code has no path to a "SAFE" string on missing data (enforced by type system + UI test).
