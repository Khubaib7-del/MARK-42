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

## 3. Primary logo (owner-supplied)

**Source:** generated and adopted by the product owner (2026-09-21) as the primary identity. Files: `assets/brand/logo.png` (primary, 1254×1254, light surfaces) and `assets/brand/logo-transparent.png` (edge flood-fill background removal, for dark or photographic surfaces).

**Composition (sampled programmatically):** near-white field (~79% coverage) with dark ink structure (~15%) and a green accent family (~3%): deep teal-green `#0A6C60`, bright signal green `#54E454`, with light-green `#78F054` highlights.

**Launcher icon:** the logo is the Android adaptive-icon foreground on a `#FDFEFB` background layer, scaled to 58% of the canvas (inside the 66dp safe zone); see `android/app/src/main/res/mipmap-*`.

**Banner:** `assets/brand/banner.png` (1280×360) — the transparent logo on Mist, with the letter-spaced SELVARD wordmark in Ink, tagline in Slate, and the attribute line in Verdant Green.

### 3a. Secondary vector mark

**Concept: the keystone arch.** Engineering's oldest proof of reliability: an arch stands for millennia because every stone locks every other into place — and all of it holds through one stone, the keystone. Selvard is the keystone of your device's security; you stand safe beneath.

Construction (viewBox 96×96, no gradients): two arc segments of radius 28 centered at (48,54), stroke 9, round caps; keystone diamond (48,17)–(56,25.5)–(48,34)–(40,25.5) in accent; piers at the springing points; ground stroke 4.5; core rounded square 12×12 (rx 3.5) centered low in the passage. Files: `assets/brand/mark.svg` (light) and `mark-dark.svg` (dark).

### 3b. Vector reconstruction (future task)

The primary logo is raster-only. Recreating it as SVG (geometry, silhouette, monochrome pass) remains open for Phase 9 branding work.

**Concept: the keystone arch.** Engineering's oldest proof of reliability: an arch stands for millennia because every stone locks every other into place — and all of it holds through one stone, the keystone. The mark draws a raised arch with the keystone crowning it in accent teal, springing from piers set on a ground line, with the protected self standing centered in the passage. The story is the product: Selvard is the keystone of your device's security; you stand safe beneath it. Reliability is not decorated on — it is the structure.

Construction (viewBox 96×96, no gradients):

- Arch: two arc segments of radius 28 centered at (48,54), stroke 9, round caps, springing from (20,54) and (76,54); the crown gap is left open for the keystone.
- Keystone: diamond (48,17)–(56,25.5)–(48,34)–(40,25.5), accent teal fill, seated in the crown gap and rising above the arch ring — the piece that completes the structure.
- Piers: vertical strokes (stroke 9) from the springing points; ground line: stroke 4.5 across the base — standing on solid ground.
- Core: rounded square 12×12 (rx 3.5) centered low in the passage — the protected self.

Files: `assets/brand/mark.svg` (ink on light surfaces), `assets/brand/mark-dark.svg` (for dark surfaces), `assets/brand/banner.svg` (README banner).

## 4. Palette

| Token | Hex | Use |
| --- | --- | --- |
| Deep Ink | `#0C1322` | Banner/app dark surfaces |
| Ink | `#101A2E` | Mark on light surfaces, wordmark on light |
| Off-White | `#EDF1F7` | Mark/wordmark on dark surfaces |
| Slate | `#9FB0C6` | Secondary text |
| Mist | `#F6F8FB` | Banner/app light surfaces |
| Verdant Green | `#0A6C60` | Primary accent (sampled from logo) |
| Signal Green | `#54E454` | Detection/highlight accent (sampled from logo) |
| Stillwater Teal | `#3E8E9E` | Legacy accent, secondary mark only; `#58B7CC` on dark |

Restrained, cool, institutional. Monochrome rule: the mark must remain recognizable in pure black or pure white — the core and node take the boundary's color when the accent is unavailable.

## 5. Usage rules

- Clear space: at least 25% of mark height on all sides.
- Minimum size: 24 px digital / 8 mm print; below that use the arch silhouette without ground line and core detail.
- Wordmark: "SELVARD" (caps, letter-spaced) or "Selvard" (sentence case in prose). Never "SelVard".
- Forbidden: gradients, neon/glow effects, cyberpunk styling, shields/padlocks added to the mark, recoloring outside the palette, placing the mark on noisy imagery.
- The identity communicates containment and calm precision — never aggression.
