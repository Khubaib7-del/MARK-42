# Cordon — Working Project Title

**A privacy-first Android security environment** that detects, explains, contains, and correlates threats across links, applications, networks, identity, privacy activity, and device security — while keeping the user's sensitive information on the device whenever technically possible.

> Codename "Cordon" is a working title only. Brand naming is deliberately deferred; see `docs/decisions/ADR-008-naming-direction.md`.

## Status

**Phase 0 — Foundation (complete, documentation only).**

No application code exists yet. The repository currently contains the engineering foundation: threat model, Android capability matrix, architecture, PRD/DRD, privacy and security documentation, roadmap, and initial ADRs. Implementation begins only after this foundation is reviewed.

## Governing principles

1. The security system must not become the surveillance system.
2. Never fake a capability that Android does not permit.
3. Never present absence of detection as safety ("no known threat" ≠ "safe").
4. Local-first processing; minimum necessary data leaves the device.
5. Deterministic, evidence-backed decisions; AI assists explanation, never sole judgment.

## Documentation index

| Document | Purpose |
| --- | --- |
| `docs/PROJECT_FOUNDATION_REPORT.md` | Phase 0 summary report |
| `docs/PRD.md` | Product requirements |
| `docs/DRD.md` | Developer/design requirements |
| `docs/ARCHITECTURE.md` | System architecture |
| `docs/ROADMAP.md` | Phased roadmap (Phase 0–12) |
| `docs/security/THREAT_MODEL.md` | Threat model (categories A–T) |
| `docs/security/SECURITY_ARCHITECTURE.md` | Security architecture |
| `docs/security/PRIVACY_ARCHITECTURE.md` | Privacy architecture |
| `docs/security/DATA_CLASSIFICATION.md` | Data classes and retention |
| `docs/security/DEPENDENCY_POLICY.md` | Dependency evaluation policy |
| `docs/security/INCIDENT_RESPONSE.md` | Incident response procedures |
| `docs/security/SECURITY_TESTING.md` | Security test plan |
| `docs/research/ANDROID_CAPABILITIES.md` | Capability matrix (mandatory) |
| `docs/research/THREAT_INTELLIGENCE.md` | TI sources and provider design |
| `docs/research/MALWARE_ANALYSIS.md` | Malware analysis approach |
| `docs/research/COMPETITIVE_RESEARCH.md` | Market landscape |
| `docs/product/MVP_SCOPE.md` | MVP scope and security boundary |
| `docs/product/USER_FLOWS.md` | User flows |
| `docs/product/PROTECTED_ASSETS.md` | Protected asset model |
| `docs/decisions/` | Architecture Decision Records |

## Planned repository layout

```
/docs        documentation (this foundation)
/core        platform-neutral security domain (Kotlin Multiplatform)
/android     Android app (Kotlin, Jetpack Compose)
/backend     cloud intelligence services (minimal, Phase 12+)
/desktop     future desktop agent (not MVP)
/scripts     build, CI, tooling
/tests       cross-cutting test assets
```

Directories other than `/docs` are created when their first implementation phase begins.
