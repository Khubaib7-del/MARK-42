# ADR-009: Deterministic, evidence-based risk engine

**Status:** Accepted (Phase 0)

## Context
The spec (§35, §43) forbids AI as sole authority for blocking/verdicts and fake scores. Numeric "87/100" scores manufacture false precision; opaque verdicts destroy trust; LLM non-determinism is unacceptable on the decision path.

## Decision
Risk engine is a pure, deterministic function over typed evidence. Versioned weight registry (in code, review-gated); every assessment outputs: severity + confidence + enumerated evidence + reasons. Signal absence produces UNKNOWN/NO KNOWN THREAT contributions, never SAFE. LLMs (if ever used, Phase 9+) are restricted to explanation-only, never able to raise severity, and off by default.

## Consequences
- + Deterministic unit tests; auditability; explainability is structural, not cosmetic; posture (categorical, not numeric) inherits honesty.
- − Heuristic quality becomes the product's ceiling — accuracy must be measured and reported (SECURITY_TESTING §8).
