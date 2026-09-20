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

**Concept: the tied keep.** Reliability rendered structurally. Two complete, closed shells — the outer wall and the inner keep — are tied together by braces at opposing corners, so the structure shares load and cannot be pulled apart. The protected core sits at the exact center. The visual vocabulary is dependability itself: closure, symmetry, layered depth, braced rigidity — defense in depth made legible.

Construction (viewBox 96×96, closed geometry, no gradients):

- Outer shell: closed rounded square spanning 14–82, corner radius 18, stroke 7.
- Inner keep: closed rounded square spanning 30–66, corner radius 10, stroke 6.
- Corner braces: two diagonal strokes (width 5, round caps) joining the shells' 45° corner points at top-left and bottom-right (19.3,19.3→32.9,32.9 and its 180° mirror).
- Core: solid rounded square 14×14 (rx 4), centered, accent teal.

Files: `assets/brand/mark.svg` (ink on light surfaces), `assets/brand/mark-dark.svg` (for dark surfaces), `assets/brand/banner.svg` (README banner).

## 4. Palette

| Token | Hex | Use |
| --- | --- | --- |
| Deep Ink | `#0C1322` | Banner/app dark surfaces |
| Ink | `#101A2E` | Mark on light surfaces, wordmark on light |
| Off-White | `#EDF1F7` | Mark/wordmark on dark surfaces |
| Slate | `#9FB0C6` | Secondary text |
| Mist | `#F6F8FB` | Banner/app light surfaces |
| Stillwater Teal | `#3E8E9E` | Accent (core) on light; `#58B7CC` on dark |

Restrained, cool, institutional. Monochrome rule: the mark must remain recognizable in pure black or pure white — the core and node take the boundary's color when the accent is unavailable.

## 5. Usage rules

- Clear space: at least 25% of mark height on all sides.
- Minimum size: 24 px digital / 8 mm print; below that use the core-and-gate glyph only.
- Wordmark: "SELVARD" (caps, letter-spaced) or "Selvard" (sentence case in prose). Never "SelVard".
- Forbidden: gradients, neon/glow effects, cyberpunk styling, shields/padlocks added to the mark, recoloring outside the palette, placing the mark on noisy imagery.
- The identity communicates containment and calm precision — never aggression.
