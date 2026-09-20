# ADR-004: Threat-intelligence provider abstraction with local feeds as floor

**Status:** Accepted (Phase 0)

## Context
Multiple TI sources (URLhaus, ThreatFox, OpenPhish, trackers, HIBP, possibly Safe Browsing) with different terms, reliability, and privacy footprints. Single-provider dependency is a supply-chain and correctness risk (INCIDENT_RESPONSE: provider compromise). Providers must be replaceable and disable-able.

## Decision
All intelligence flows through a `ThreatIntelProvider` interface in core (THREAT_INTELLIGENCE.md §3). Bundled/cached local feeds are the always-present floor; remote lookups are optional accelerators. Provider health scoring; runtime disable (locally or remotely *off* only). Verdict merging preserves per-provider provenance in evidence.

## Consequences
- + Offline operation; provider swaps without engine changes; graceful UNKNOWN semantics when all remote sources fail (never SAFE).
- − Slightly more indirection; feed freshness/size management becomes our responsibility (WorkManager sync).
