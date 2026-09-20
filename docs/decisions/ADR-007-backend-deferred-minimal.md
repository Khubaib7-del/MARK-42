# ADR-007: Backend deferred and minimal (Kotlin/Ktor if built)

**Status:** Accepted (Phase 0)

## Context
MVP is local-first and needs no backend. Eventual needs: Play Integrity verdict verification (server-side decryption of tokens), feed aggregation/stripping, remote-sandbox orchestration (V2). Choice of language: re-evaluate at build time; Ktor keeps one language across core/android/backend; Go/Rust remain alternatives for performance-critical services.

## Decision
No backend in MVP. When needed, a minimal Kotlin/Ktor service: stateless, least-privilege, feed aggregation + integrity verification only. Backend is untrusted for verdicts (clients treat its responses as inputs, never as commands; a compromised backend cannot cause a false BLOCK or SAFE).

## Consequences
- + Zero server attack surface for MVP; no user data in cloud (strongest possible privacy posture for launch).
- − Play Integrity verdicts are treated as unverified local signals in MVP (disclosed); some V2 features wait.
