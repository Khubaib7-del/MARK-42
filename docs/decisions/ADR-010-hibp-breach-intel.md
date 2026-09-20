# ADR-010: HIBP v3 for breach intelligence

**Status:** Accepted (Phase 0) — plan tier decision at Phase 7

## Context
Identity Exposure needs authorized breach intelligence. HIBP is the de-facto standard (recommended by governments/CERTs), offers a documented v3 subscription API, and uniquely supports **k-anonymity** range queries (Pro tier) that avoid disclosing the full email address. Alternatives (DeHashed etc.) have legal/ethical problems and are excluded; Firefox Monitor has no public API.

## Decision
Integrate HIBP v3 API only. Identity creation requires explicit consent describing exactly what a query discloses. Prefer the Pro plan for k-anonymity prefix queries (data minimization). Store only: breach name/date/data-classes + the declared identity (encrypted vault). Respect terms: clear HIBP attribution in-app; no "ambulance chasing" patterns; key held per Keystore-wrapped client secret.

## Consequences
- + Legitimate, stable, privacy-minimizable breach source; attribution requirement documented.
- − Recurring cost; coverage limited to HIBP's index (UNKNOWN beyond — disclosed, never "your identity is safe").
