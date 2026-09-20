# ADR-001: Kotlin Multiplatform platform-neutral core

**Status:** Accepted (Phase 0)

## Context
The domain logic (event model, risk engine, policy, correlation, URL pipeline, TI interfaces) must be reusable across Android (V1), backend, and desktop (V3), and must be testable without Android. Alternatives: duplicate per-platform (drift risk), pure-JVM module (blocks future desktop reuse), Rust core (mature FFI cost, smaller hiring pool, slower iteration for a Kotlin team).

## Decision
Build `/core` as a Kotlin Multiplatform library (jvm + android targets now; add desktop targets later). Zero Android dependencies in core; Android implements interfaces. All security decisions are pure functions in core.

## Consequences
- + Determinism tests run on JVM without emulators; desktop port is a target addition, not a rewrite; single risk-engine weight registry.
- − KMP adds build complexity; iOS was evaluated and is explicitly out of scope until researched.
