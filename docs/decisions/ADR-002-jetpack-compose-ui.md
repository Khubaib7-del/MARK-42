# ADR-002: Jetpack Compose for Android UI

**Status:** Accepted (Phase 0)

## Context
UI arrives in Phase 9; the stack must be chosen now for scaffolding (Phase 1). Options: Compose vs XML Views. Compose is Google's current first-party direction; type-safe state mapping helps enforce the disclosure-state contract (SECURITY_ARCHITECTURE §7).

## Decision
Jetpack Compose, single-activity architecture, state-driven from engine enums. No custom views; Material 3 foundation with the product's restrained visual system (PRD §29 of spec) layered above.

## Consequences
- + Type system prevents rendering disallowed states (e.g., "SAFE" without evidence); modern tooling.
- − Team must honor accessibility requirements (§38 of spec) deliberately — Compose gives semantic control, tests required.
