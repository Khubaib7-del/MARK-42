# Developer / Design Requirements Document

Companion to `docs/ARCHITECTURE.md` (system shape) and `docs/PRD.md` (product intent). This document defines the engineering contract for implementation.

## 1. Modules

| Module | Location | Type | Notes |
| --- | --- | --- | --- |
| `core-domain` | `/core/domain` | KMP library (jvm+android) | Event model, risk/policy engines, URL pipeline, correlation — zero Android dependencies |
| `core-ti` | `/core/ti` | KMP library | Threat-intel provider interfaces + feed normalization |
| `android-app` | `/android` | Android app | Compose UI, engines wiring, Android implementations |
| `backend` | `/backend` | Deferred (ADR-007) | Feed aggregation; verdict verification if introduced |

## 2. Internal APIs (interfaces, implemented by `/android`)

```kotlin
interface EventBus { suspend fun publish(event: SecurityEvent); fun events(): Flow<SecurityEvent> }
interface EventStore { suspend fun append(event: SecurityEvent); fun query(filter: EventQuery): Flow<SecurityEvent> }
interface ThreatIntelProvider {
    val id: ProviderId
    suspend fun lookupDomain(domain: String): DomainVerdict?      // null = provider unavailable
    suspend fun lookupUrl(url: String): UrlVerdict?
    suspend fun lookupHash(sha256: String): HashVerdict?
}
interface LinkGatekeeper { suspend fun analyze(url: String, context: LinkContext): LinkVerdict }
interface DeviceIntegritySource { suspend fun integritySnapshot(): IntegritySnapshot }
interface AppInventorySource { fun observe(): Flow<List<AppRecord>> }
```

Rules: all engine interfaces are suspend-safe and timeout-bounded; every remote call has an explicit timeout, retry budget, and offline fallback documented in the provider adapter.

## 3. Event schema (JSON via kotlinx.serialization)

```json
{
  "event_id": "evt_uuid_v7",
  "timestamp": "2026-09-20T02:41:00Z",
  "source": "network_guardian",
  "category": "NETWORK",
  "severity": "INFO|MEDIUM|HIGH|CRITICAL",
  "confidence": "HIGH|MEDIUM|LOW",
  "affected_asset": { "type": "APP|IDENTITY|DEVICE|URL", "ref": "com.example.bank" },
  "evidence": [ { "kind": "domain_on_blocklist", "value": "example-bad[.]tld", "provenance": "urlhaus" } ],
  "action_taken": "BLOCKED|WARNED|NONE",
  "related_events": ["evt_..."],
  "privacy_classification": "PUBLIC|LOW_SENSITIVITY|SENSITIVE|HIGHLY_SENSITIVE|SECRET",
  "retention_policy": "retention:<id>"
}
```

Schema is append-only and versioned (`schema_version`). Migrations never rewrite stored events.

## 4. Risk engine contract

- Pure function: `risk(eventStream, policy, intelContext) -> RiskAssessment(severity, confidence, evidence, reasons[])`.
- Deterministic: identical inputs -> identical output; no RNG, no LLM calls in the decision path.
- Every signal has a weight registry versioned in code; changes require review + test updates.
- Verdict thresholds are policy-driven (user can tighten, never loosen below safe defaults without explicit informed consent screens).

## 5. Authentication & authorization

- No user accounts in MVP. There is no login; the app is local-first.
- App access protection (optional): Android BiometricPrompt gate for the app and for Identity Exposure data specifically.
- Component authorization: exported components limited to a share-target Activity (Link Guardian entry). Deep links for `cordon://check?url=…` are allowed only with user-confirmation screen (no auto-navigation to sensitive state).
- Backend (if introduced): mTLS or token-based, least privilege; backend can never override a local BLOCK with an unauthenticated remote instruction.

## 6. Encryption & storage

- Key management: Android Keystore (StrongBox where available) generates and holds AES-256 keys; keys never leave hardware-backed storage (ADR-006).
- Database: Room over SQLCipher; master key wrapped by Keystore key. No passcode-derived custom crypto.
- Files: `EncryptedFile` for exports (opt-in, user-triggered only).
- Backups: android:allowBackup=false for sensitive data; no event store in cloud backups.

## 7. Networking

- Egress allowlist: only TI/feed endpoints, on explicit user-triggered or scheduled sync. No analytics SDKs.
- All remote calls: TLS 1.2+ enforced, certificate pinning evaluated per provider in the dependency review.
- Data minimization: send normalized domain / SHA-256 of APK / hashed identity query — never raw message text, page content, or file contents (see PRIVACY_ARCHITECTURE.md).

## 8. Background tasks

- WorkManager for: TI feed sync (periodic, batched, charging/battery-not-low constraints), daily posture recompute, retention enforcement.
- VpnService runs only while enabled; non-dismissable notification as required by platform.
- No persistent foreground services besides the VPN when active. Respect battery: batching, no polling under screen-off where avoidable.

## 9. Permissions (Android manifest budget)

| Permission | Justification | Play requirement |
| --- | --- | --- |
| INTERNET | TI lookups | none |
| FOREGROUND_SERVICE + FOREGROUND_SERVICE_SPECIAL_USE (VPN) | Network Guardian | VpnService declaration form |
| POST_NOTIFICATIONS | Security notifications | runtime permission |
| RECEIVE_BOOT_COMPLETED | Record boot events | normal |
| QUERY_ALL_PACKAGES | App inventory (device security is a permitted use) | Permissions Declaration Form + prominent disclosure |
| USE_BIOMETRIC | Optional app gate | none |
| Never: ACCESSIBILITY_SERVICES, BIND_NOTIFICATION_LISTENER_SERVICE (MVP), READ_SMS, READ_CONTACTS, READ_CALL_LOG, location | — | prohibited / not needed |

## 10. Error handling

| Failure | Required behavior |
| --- | --- |
| TI provider timeout/error | Verdict UNKNOWN with reason "limited analysis", cached data retained, provider marked degraded |
| DNS engine failure in VPN | Fail-open vs fail-closed is configurable with fail-closed default while VPN is active; state surfaced to user |
| DB corruption | Event store rebuilt from write-ahead log; user notified of retention impact; never silently continue with lost data |
| Keystore unavailable (rare hardware fault) | Feature degrades to unavailable state, no plaintext fallback |
| App crash | Next-boot integrity self-check (Play Integrity) + event-store consistency check |

## 11. Logging & telemetry

- Local logs: structured, levels, redacted by construction — log API receives data classes, not raw strings; PII fields are enum/coded. No URLs (only domains+hashes), no message content, no tokens.
- Remote telemetry: **none in MVP**. If ever added: privacy-preserving counters only, explicit opt-in, documented in PRIVACY_ARCHITECTURE.

## 12. Testing

Full matrix in `docs/security/SECURITY_TESTING.md`. Minimum bar per phase: unit tests for every `/core` engine (risk, URL pipeline, correlation, policy), provider-failure injection tests, storage encryption round-trip tests, retention/deletion tests, and instrumentation tests for VpnService lifecycle and package visibility on API 26–37 range.

## 13. CI/CD

- CI stages (planned in Phase 1): format (ktlint), lint (Android Lint), static analysis (Detekt), dependency audit (OWASP dependency-check / Gradle versions plugin + lockfiles), secret scan (gitleaks), unit + integration tests, instrumentation tests on emulator matrix, release build verification (R8, signing in CI with secrets outside repo).
- No secrets in repo; CI credentials via environment. Signed artifacts reproducible.

## 14. Deployment & observability

- Distribution target: Google Play (declared VpnService usage, QUERY_ALL_PACKAGES declaration).
- In-app diagnostics screen: engine health (provider status, feed ages, event counts, storage size), user-exportable diagnostics bundle (redacted).
- Performance budgets and measurement plan defined in PRD §10 and SECURITY_TESTING.md §7; battery measured via Battery Historian / internal energy counters during Network Guardian testing, not assumed.

## 15. Incident handling

- Product security incidents (vulnerabilities in this app): `docs/security/INCIDENT_RESPONSE.md`.
- In-app incident model (user-facing): incident = correlated event cluster with summary, evidence, actions, confidence — never causal claims without evidence.
