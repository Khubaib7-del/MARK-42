# System Architecture

Status: Phase 0 foundation | Governing ADRs: ADR-001 … ADR-010

## 1. Conceptual model

```
                     USER / DEVICE
                          |
                          v
             +---------------------------+
             |   SECURITY CONTROL PLANE  |
             |                           |
             | Risk Engine (deterministic)|
             | Policy Engine             |
             | Privacy Engine            |
             | Event Engine (bus+store)  |
             | Threat Intel (provider API)|
             | Incident Correlation      |
             +-------------+-------------+
                           |
          +----------------+----------------+
          |                |                |
          v                v                v
    LINK GUARDIAN    NETWORK GUARDIAN   APP GUARDIAN
    (URL analysis)   (VpnService,       (inventory,
                     local DNS filter)   permissions,
          |                |              integrity)
          |                |                |
          +----------------+----------------+
                           |
                +----------v----------+
                |    ANDROID CORE     |
                | sandbox · permissions · Keystore |
                | integrity APIs · VpnService      |
                +---------------------+

  Pillars: IDENTITY EXPOSURE · DEVICE INTEGRITY ·
           PRIVACY MONITOR · INCIDENT TIMELINE
```

## 2. Module layout

```
/core      Platform-neutral domain (Kotlin Multiplatform: jvm + android targets; desktop later).
           - event model + event bus interface + event store interface
           - risk engine (pure functions, evidence-based)
           - policy engine (declarative rules, versioned)
           - URL analysis pipeline (normalization -> canonicalization ->
             indicators -> reputation lookup interface -> verdict)
           - threat-intelligence provider interfaces + local feed model
           - correlation engine (time-window, entity-graph correlation)
           - posture computation
           - identity exposure domain model
/android   Compose UI + sensors + Android implementations of core interfaces
           (VpnService engine, Room event store, Keystore keyring, package observer)
/backend   Minimal cloud intelligence (feed aggregation, optional integrity verdict verification).
           Not required for MVP operation; deferred to Phase 12 (ADR-007).
/desktop   Future (V3).
```

**Rule:** every security decision is made in `/core` as a pure, testable function of evidence. Android code only *collects* evidence and *renders* outcomes. This keeps the domain honest, testable, and portable.

## 3. Event pipeline

```
Network Event / App Event / Privacy Event / Identity Event /
Device Event / Link Event / User Event
        |
        v
   EVENT BUS (in-process, Kotlin Flow, buffered)
        |
        v
   CORRELATION ENGINE (session/time-window/entity correlation)
        |
        v
   RISK ENGINE (deterministic scoring + confidence)
        |
        v
   INCIDENT STORE (append-only, encrypted, Room/SQLCipher)
        |
        v
   UI (Posture, Timeline, evidence-first alerts)
```

Event schema is defined in `DRD.md` §5. Every event carries: `event_id`, `timestamp`, `source`, `category`, `severity`, `confidence`, `affected_asset`, `evidence[]`, `action_taken`, `related_events[]`, `privacy_classification`, `retention_policy`.

## 4. Engine responsibilities

| Engine | Responsibility | Key constraint |
| --- | --- | --- |
| Link Guardian | URL -> verdict (OPEN / OPEN WITH WARNING / OPEN ISOLATED / BLOCK / UNKNOWN) with evidence | Input only via explicit user action (share intent, manual, foreground clipboard) — no silent interception |
| Network Guardian | On-device DNS + destination filtering via VpnService; per-UID attribution where feasible | No remote tunnel (local loopback only), no TLS inspection; Play declaration + prominent disclosure |
| App Guardian | Inventory of observable app metadata + risk signals | Respect package visibility policy (QUERY_ALL_PACKAGES declaration); no claims of internal app inspection |
| Privacy Engine | Timeline of observable privacy-relevant events | Complement OS indicators; never Accessibility-based spying; document non-observable events as NOT MONITORED |
| Identity Exposure | User-declared identities vs breach intel | HIBP v3 (paid) with explicit consent; k-anonymity where plan permits |
| Device Integrity | Play Integrity verdicts, boot events, unexpected restart recording | Never attribute causality without evidence |
| Incident Engine | Correlate events into incidents; explain without inventing causality | Correlation is temporal/entity-based, not speculative |

## 5. Data flow principles

1. **Local-first:** device -> local processing -> local decision. Remote services receive only derived, minimized data (e.g., normalized domain for reputation lookup — never message content).
2. **Append-only incident store:** events are immutable; corrections are new events referencing prior ones.
3. **Fail safe:** TI provider unavailable -> verdict UNKNOWN / LIMITED ANALYSIS, never SAFE. Default-deny on decision components failing.
4. **Zero trust between components:** the VPN engine cannot read the incident store; UI reads through a read-only repository interface; engines communicate only via the event bus and typed interfaces.

## 6. Security boundaries

| Boundary | Mechanism |
| --- | --- |
| Process | Android app sandbox (single app, no exported components except explicit share target) |
| Storage | Android Keystore-held keys + SQLCipher/encrypted Room; no secrets in source (see DEPENDENCY_POLICY, ADR-006) |
| Network | Egress only to explicitly enumerated endpoints (TI providers, feed hosts); certificate pinning considered per ADR review |
| UI | Sensitive screens use FLAG_SECURE; biometric gate for identity data (see DRD §9) |
| Cloud (future) | Backend is untrusted for verdicts; core verdicts remain computable locally |

## 7. Disclosure states (system-wide)

Every surfaced capability must use exactly one of: `PROTECTED`, `DETECTED`, `BLOCKED`, `UNKNOWN`, `NOT MONITORED`, `NOT SUPPORTED`, `PERMISSION REQUIRED`, `OS LIMITATION`, `CLOUD ANALYSIS REQUIRED`. UI labels map 1:1 to engine output states.

## 8. Key decisions

Recorded as ADRs in `docs/decisions/`. Summary: Kotlin Multiplatform core (ADR-001); Compose UI (ADR-002); local-only VpnService (ADR-003); TI provider abstraction (ADR-004); append-only event store + Flow bus (ADR-005); encrypted Room storage (ADR-006); minimal Ktor backend deferred (ADR-007); deterministic evidence-based risk engine (ADR-009); HIBP v3 for breach intel (ADR-010).
