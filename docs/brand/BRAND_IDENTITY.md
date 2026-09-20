# Selvard Brand Identity

## 1. Name

**Selvard** (SEL-vard) — a coined word from Old Norse roots:

- *selv* — "self" (Scandinavian; Old Norse *sjálfr*)
- *vǫrðr* — "warden, guardian, watchman"

Meaning: **the self-warden** — a guardian that stays on your side of the wall. The name encodes the product's founding thesis: trust is not outsourced. Security must not become surveillance; your guardian never ships your data to someone else's server. The name was adopted in ADR-008 after a 13-candidate conflict screening; every natural-word candidate was already in use in the security/software industry.

Attributes: institutional · calm · precise · protective · privacy-first · no false confidence.

## 2. Name screening record (Phase 0, web-verified)

| Candidate | Found conflict | Verdict |
| --- | --- | --- |
| Cordon | getcordon.com (MCP security gateway), cordon.sh, `cordon-ai` (PyPI) | Rejected — same-industry collisions |
| Redoubt | Redoubt Forge (security posture platform), redoubt.dk (Shopify cybersecurity) | Rejected |
| Limen | Limen (Go auth library), Limen (Fairblock AI data-boundary product) | Rejected |
| Glacis | glacis.io (AI runtime guardrails) | Rejected |
| Cardea | Cardea Secure Browser, cardea.cc, Project Cardea (AI cybersecurity platform) | Rejected |
| Merlon | Merlon Software Corporation (XYPRO, security software) | Rejected |
| Lares | Lares (well-known cybersecurity consulting) | Rejected |
| Vorn | vorn.sh (Mac dictation app), vorn.run (agent command center) | Rejected |
| Vallum | Vallum Software (Halo Manager, infrastructure monitoring) | Rejected |
| Verja | `tflori/verja` (Old Norse "defender" — security validation library) | Rejected |
| Stillward | stillwardlabs.com, stillward.co.za | Rejected |
| QuietKeep | quietkeep.com (Pranix AI Labs), QuietKeep (quietwire.dev — Linux patch/threat-intel dashboard) | Rejected |
| Innerkeep | Adjacent to Inkeep, Inc. (funded AI platform) | Rejected — avoid confusion |
| **Selvard** | No software/company/brand usage found — only an unrelated surname and directory noise | **Adopted** |

Pre-adoption checklist (before first public release): formal trademark search & registration (Nice Class 9/42), domain acquisition (`selvard.com` / `selvard.app`), app-store handle reservation. Documented in ADR-008.

## 3. Logomark

**Concept: the sealed boundary and the guarded gate.** A rounded-square perimeter — the security environment — almost fully sealed. One corner stays open: the controlled entry point, guarded by a standing node (the warden at the gate). At the center, the core: the protected self. Nothing enters except through the warden.

Construction (viewBox 96×96, geometric, no gradients):

- Perimeter: single path, stroke-width 8, round caps; three rounded corners (radius 16); top-right corner intentionally open.
- Gate node: solid circle r 5.5 at the opening's arc midpoint (75.3, 20.7).
- Core: solid rounded square 16×16 (rx 5) centered at (48, 48).

Files: `assets/brand/mark.svg` (ink on light surfaces), `assets/brand/mark-dark.svg` (for dark surfaces), `assets/brand/banner.svg` (README banner).

## 4. Palette

| Token | Hex | Use |
| --- | --- | --- |
| Deep Ink | `#0C1322` | Banner/app dark surfaces |
| Ink | `#101A2E` | Mark on light surfaces, wordmark on light |
| Off-White | `#EDF1F7` | Mark/wordmark on dark surfaces |
| Slate | `#9FB0C6` | Secondary text |
| Stillwater Teal | `#3E8E9E` | Accent (core) on light; `#58B7CC` on dark |

Restrained, cool, institutional. Monochrome rule: the mark must remain recognizable in pure black or pure white — the core and node take the boundary's color when the accent is unavailable.

## 5. Usage rules

- Clear space: at least 25% of mark height on all sides.
- Minimum size: 24 px digital / 8 mm print; below that use the core-and-gate glyph only.
- Wordmark: "SELVARD" (caps, letter-spaced) or "Selvard" (sentence case in prose). Never "SelVard".
- Forbidden: gradients, neon/glow effects, cyberpunk styling, shields/padlocks added to the mark, recoloring outside the palette, placing the mark on noisy imagery.
- The identity communicates containment and calm precision — never aggression.
